package com.swingfrog.summer.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.swingfrog.summer.client.ClientMgr;
import com.swingfrog.summer.client.ClientRemote;
import com.swingfrog.summer.client.exception.CreateRemoteFailException;
import com.swingfrog.summer.concurrent.SynchronizedMgr;
import com.swingfrog.summer.db.DataBaseMgr;
import com.swingfrog.summer.ioc.ContainerMgr;
import com.swingfrog.summer.ioc.MethodParameterName;
import com.swingfrog.summer.redis.RedisMgr;

public class ProxyUtil {
	
	@SuppressWarnings("unchecked")
	public static <T> T getProxyService(T service) {
		Object obj = ProxyFactory.getProxyInstance(service, new ProxyMethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args) throws Throwable {
				String synchronizedName = ContainerMgr.get().getSynchronizedName(method);
				String value = null;
				if (synchronizedName != null) {
					synchronizedName = String.join("-", "synchronized", synchronizedName);
					value = UUID.randomUUID().toString();
					SynchronizedMgr.get().lock(synchronizedName, value);
				}
				try {
					DataBaseMgr.get().setDiscardConnectionLevelForService();
					RedisMgr.get().setDiscardConnectionLevelForService();
					return method.invoke(obj, args);
				} catch (InvocationTargetException e) {
					throw e.getTargetException();
				} finally {
					DataBaseMgr.get().discardConnectionFromService();
					RedisMgr.get().discardConnectionFromService();
					if (synchronizedName != null) {
						SynchronizedMgr.get().unlock(synchronizedName, value);
					}
				}
			}
		});
		return (T)obj;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getProxyRemote(T remote) {
		Object obj = ProxyFactory.getProxyInstance(remote, new ProxyMethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args) throws Throwable {
				String synchronizedName = ContainerMgr.get().getSynchronizedName(method);
				String value = null;
				if (synchronizedName != null) {
					synchronizedName = String.join("-", "synchronized", synchronizedName);
					value = UUID.randomUUID().toString();
					SynchronizedMgr.get().lock(synchronizedName, value);
				}
				boolean transaction = false;
				if (ContainerMgr.get().isTransaction(method)) {
					DataBaseMgr.get().openTransaction();
					transaction = true;
				}
				try {
					DataBaseMgr.get().setDiscardConnectionLevelForRemote();
					RedisMgr.get().setDiscardConnectionLevelForRemote();
					Object res = method.invoke(obj, args);
					if (transaction) {
						DataBaseMgr.get().getConnection().commit();
					}
					return res;
				} catch (InvocationTargetException e) {
					if (transaction) {
						DataBaseMgr.get().getConnection().rollback();
					}	
					throw e.getTargetException();
				} catch (Exception e) {
					if (transaction) {
						DataBaseMgr.get().getConnection().rollback();
					}					
					throw e;
				} finally {
					DataBaseMgr.get().discardConnectionFromRemote();
					RedisMgr.get().discardConnectionFromRemote();
					if (synchronizedName != null) {
						SynchronizedMgr.get().unlock(synchronizedName, value);
					}
				}
			}
		});
		return (T)obj;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getProxyClientRemote(T remote, String cluster, String name) {
		Object obj = ProxyFactory.getProxyInstance(remote, new ProxyMethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args) throws Exception {
				Map<String, Object> map = new HashMap<>();
				MethodParameterName mpn = new MethodParameterName(obj.getClass());
				String[] params = mpn.getParameterNameByMethod(method);
				for (int i = 0; i < params.length; i ++) {
					map.put(params[i], args[i]);
				}
				ClientRemote clientRemote = ClientMgr.get().getClientRemote(cluster, name);
				if (clientRemote == null) {
					throw new CreateRemoteFailException("clientRemote is null");
				}
				return clientRemote.syncRemote(obj.getClass().getSimpleName(), method.getName(), map, method.getGenericReturnType());
			}
		});
		return (T)obj;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getProxyClientRemoteWithRetry(T remote, String cluster, String name) {
		Object obj = ProxyFactory.getProxyInstance(remote, new ProxyMethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args) throws Exception {
				Map<String, Object> map = new HashMap<>();
				MethodParameterName mpn = new MethodParameterName(obj.getClass());
				String[] params = mpn.getParameterNameByMethod(method);
				for (int i = 0; i < params.length; i ++) {
					map.put(params[i], args[i]);
				}
				ClientRemote clientRemote = ClientMgr.get().getClientRemote(cluster, name);
				if (clientRemote == null) {
					throw new CreateRemoteFailException("clientRemote is null");
				}
				return clientRemote.rsyncRemote(obj.getClass().getSimpleName(), method.getName(), map, method.getGenericReturnType());
			}
		});
		return (T)obj;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getProxyRandomClientRemote(T remote, String cluster) {
		Object obj = ProxyFactory.getProxyInstance(remote, new ProxyMethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args) throws Exception {
				Map<String, Object> map = new HashMap<>();
				MethodParameterName mpn = new MethodParameterName(obj.getClass());
				String[] params = mpn.getParameterNameByMethod(method);
				for (int i = 0; i < params.length; i ++) {
					map.put(params[i], args[i]);
				}
				ClientRemote clientRemote = ClientMgr.get().getRandomClientRemote(cluster);
				if (clientRemote == null) {
					throw new CreateRemoteFailException("clientRemote is null");
				}
				return clientRemote.syncRemote(obj.getClass().getSimpleName(), method.getName(), map, method.getGenericReturnType());
			}
		});
		return (T)obj;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getProxyRandomClientRemoteWithRetry(T remote, String cluster) {
		Object obj = ProxyFactory.getProxyInstance(remote, new ProxyMethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args) throws Exception {
				Map<String, Object> map = new HashMap<>();
				MethodParameterName mpn = new MethodParameterName(obj.getClass());
				String[] params = mpn.getParameterNameByMethod(method);
				for (int i = 0; i < params.length; i ++) {
					map.put(params[i], args[i]);
				}
				ClientRemote clientRemote = ClientMgr.get().getRandomClientRemote(cluster);
				if (clientRemote == null) {
					throw new CreateRemoteFailException("clientRemote is null");
				}
				return clientRemote.rsyncRemote(obj.getClass().getSimpleName(), method.getName(), map, method.getGenericReturnType());
			}
		});
		return (T)obj;
	}
	
}
