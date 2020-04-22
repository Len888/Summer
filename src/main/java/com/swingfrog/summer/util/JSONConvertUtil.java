package com.swingfrog.summer.util;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiFunction;

public class JSONConvertUtil {

    private static final Map<Type, BiFunction<JSONObject, String, Object>> map = Maps.newHashMap();
    static {
        map.put(boolean.class, (jsonObject, key) -> jsonObject.getBooleanValue(key));
        map.put(byte.class, (jsonObject, key) -> jsonObject.getByteValue(key));
        map.put(short.class, (jsonObject, key) -> jsonObject.getShortValue(key));
        map.put(int.class, (jsonObject, key) -> jsonObject.getIntValue(key));
        map.put(long.class, (jsonObject, key) -> jsonObject.getLongValue(key));
        map.put(Boolean.class, (jsonObject, key) -> jsonObject.getBoolean(key));
        map.put(Byte.class, (jsonObject, key) -> jsonObject.getByte(key));
        map.put(Short.class, (jsonObject, key) -> jsonObject.getShort(key));
        map.put(Integer.class, (jsonObject, key) -> jsonObject.getInteger(key));
        map.put(Long.class, (jsonObject, key) -> jsonObject.getLong(key));
        map.put(String.class, (jsonObject, key) -> jsonObject.getString(key));
    }

    public static boolean containsType(Type type) {
        return map.containsKey(type);
    }

    public static Object convert(Type type, JSONObject jsonObject, String key) {
        return map.get(type).apply(jsonObject, key);
    }

}
