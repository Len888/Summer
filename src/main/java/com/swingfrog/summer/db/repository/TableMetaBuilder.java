package com.swingfrog.summer.db.repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.swingfrog.summer.db.repository.annotation.CacheKey;
import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.IndexKey;
import com.swingfrog.summer.db.repository.annotation.PrimaryKey;
import com.swingfrog.summer.db.repository.annotation.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class TableMetaBuilder {

    public static TableMeta getTableMeta(Class<?> clazz) {
        TableMeta tableMeta = new TableMeta();
        Table table = clazz.getAnnotation(Table.class);
        Objects.requireNonNull(table, String.format("entity need use @Table - %s", clazz.getName()));
        if (table.name().length() == 0) {
            tableMeta.setName(clazz.getSimpleName());
        } else {
            tableMeta.setName(table.name());
        }
        if (table.charset().length() > 0) {
            tableMeta.setCharset(table.charset());
            if (table.collate().length() > 0) {
                tableMeta.setCollate(table.collate());
            }
        }
        tableMeta.setComment(table.comment());
        tableMeta.setColumns(Lists.newLinkedList());
        tableMeta.setIndexKeys(Sets.newHashSet());
        tableMeta.setCacheKeys(Sets.newHashSet());
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
                if (primaryKey != null) {
                    if (tableMeta.getPrimaryColumn() != null) {
                        throw new RuntimeException(String.format("only one primary key - %s", clazz.getName()));
                    }
                    tableMeta.setPrimaryColumn(getColumnMate(field));
                    if (TableSupport.isJavaBean(tableMeta.getPrimaryColumn())) {
                        throw new RuntimeException("primary key type not be java bean");
                    }
                } else {
                    TableMeta.ColumnMeta columnMeta = getColumnMate(field);
                    tableMeta.getColumns().add(columnMeta);
                    IndexKey indexKey = field.getAnnotation(IndexKey.class);
                    if (indexKey != null) {
                        tableMeta.getIndexKeys().add(columnMeta);
                    }
                    CacheKey cacheKey = field.getAnnotation(CacheKey.class);
                    if (cacheKey != null) {
                        tableMeta.getCacheKeys().add(columnMeta);
                    }
                }
            }
        }
        Map<String, TableMeta.ColumnMeta> columnMetaMap = Maps.newHashMap();
        columnMetaMap.put(tableMeta.getPrimaryColumn().getName(), tableMeta.getPrimaryColumn());
        tableMeta.getColumns().forEach(columnMeta -> columnMetaMap.put(columnMeta.getName(), columnMeta));
        tableMeta.setColumnMetaMap(columnMetaMap);
        return tableMeta;
    }

    private static TableMeta.ColumnMeta getColumnMate(Field field) {
        TableMeta.ColumnMeta columnMeta = new TableMeta.ColumnMeta();
        Column column = field.getAnnotation(Column.class);
        columnMeta.setName(field.getName());
        if (column.type().length() == 0) {
            String type = getTableType(field.getGenericType());
            if (field.getType() == String.class) {
                type = String.format("%s(%s)", type, column.length());
            }
            columnMeta.setType(type);
        } else {
            columnMeta.setType(column.type());
        }
        if (columnMeta.getType().endsWith("int")) {
            columnMeta.setIntNumber(true);
        }
        columnMeta.setReadOnly(column.readOnly());
        columnMeta.setField(field);
        columnMeta.setNonNull(column.nonNull());
        columnMeta.setUnsigned(column.unsigned());
        PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
        if (primaryKey != null) {
            if (columnMeta.isIntNumber()) {
                columnMeta.setAuto(primaryKey.auto());
            }
            columnMeta.setNonNull(true);
        }
        IndexKey indexKey = field.getAnnotation(IndexKey.class);
        if (indexKey != null) {
            columnMeta.setIndex(indexKey.index());
        }
        columnMeta.setComment(column.comment());
        return columnMeta;
    }

    private static String getTableType(Type type) {
        if (type == boolean.class) {
            return "bit";
        } else if (type == byte.class) {
            return "tinyint";
        } else if (type == short.class) {
            return "smallint";
        } else if (type == int.class) {
            return "int";
        } else if (type == long.class) {
            return "bigint";
        } else if (type == float.class) {
            return "float";
        } else if (type == double.class) {
            return "double";
        } else if (type == Boolean.class) {
            return "bit";
        } else if (type == Byte.class) {
            return "tinyint";
        } else if (type == Short.class) {
            return "smallint";
        } else if (type == Integer.class) {
            return "int";
        } else if (type == Long.class) {
            return "bigint";
        } else if (type == Float.class) {
            return "float";
        } else if (type == Double.class) {
            return "double";
        } else if (type == String.class) {
            return "varchar";
        } else if (type == Date.class) {
            return "datetime";
        }
        return "longtext";
    }
}
