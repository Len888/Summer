package com.swingfrog.summer.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swingfrog.summer.client.exception.CreateRemoteFailException;
import com.swingfrog.summer.config.ClientConfig;
import com.swingfrog.summer.config.ConfigMgr;
import com.swingfrog.summer.proxy.ProxyUtil;

import javassist.NotFoundException;

public class ClientMgr {

	private static final Logger log = LoggerFactory.getLogger(ClientMgr.class);
	private Map<String, ClientCluster> nameToCluster;
	private ConcurrentMap<Class<?>, Object> remoteMap;
	private AtomicLong currentId = new AtomicLong(0);
	
	private static class SingleCase {
		public static final ClientMgr INSTANCE = new ClientMgr();
	}
	
	private ClientMgr() {
		nameToCluster = new HashMap<>();
		remoteMap = new ConcurrentHashMap<>();
	}
	
	public static ClientMgr get() {
		return SingleCase.INSTANCE;
	}
	
	public void init() throws NotFoundException, SchedulerException {
		log.info("client init...");
		ClientConfig[] configs = ConfigMgr.get().getClientConfigs();
		if (configs != null) {
			log.info("client count {}", configs.length);
			for (int i = 0; i < configs.length; i++) {
				ClientConfig config = configs[i];
				ClientCluster clientCluster = nameToCluster.get(config.getCluster());
				if (clientCluster == null) {
					log.info("client create cluster {}", config.getCluster());
					clientCluster = new ClientCluster();
					nameToCluster.put(config.getCluster(), clientCluster);
				}
				ClientGroup clientGroup = new ClientGroup();
				for (int j = 0; j < config.getConnectNum(); j ++) {
					clientGroup.addClient(new Client(j, config));
				}
				clientCluster.addClient(config.getServerName(), clientGroup);
			}
			PushDispatchMgr.get().init();
		} else {
			log.warn("no clients");
		}
	}
	
	public void connectAll() {
		if (nameToCluster.size() > 0) {
			log.info("clients connect...");
			Iterator<ClientCluster> iteCluster = nameToCluster.values().iterator();
			while (iteCluster.hasNext()) {
				List<Client> clients = iteCluster.next().listClients();
				for (int i = 0 ;i < clients.size(); i ++) {
					clients.get(i).connect();
				}
			}
		}
	}

	public void shutdown() {
		if (nameToCluster.size() > 0) {
			log.info("clients shutdown...");
			Iterator<ClientCluster> iteCluster = nameToCluster.values().iterator();
			while (iteCluster.hasNext()) {
				List<Client> clients = iteCluster.next().listClients();
				for (int i = 0 ;i < clients.size(); i ++) {
					clients.get(i).shutdown();
				}
			}
		}
	}
	
	public ClientRemote getClientRemote(String cluster, String name) {
		ClientCluster clientCluster = nameToCluster.get(cluster);
		if (clientCluster != null) {
			Client client = clientCluster.getClientByName(name);
			if (client != null) {
				return client.getClientRemote();
			}
		}
		return null;
	}

	public ClientRemote getRandomClientRemote(String cluster) {
		ClientCluster clientCluster = nameToCluster.get(cluster);
		if (clientCluster != null) {
			Client client = clientCluster.getClientWithNext();
			if (client != null) {
				return client.getClientRemote();
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getRemoteInvokeObject(String cluster, String name, Class<?> clazz) {
		Object obj = remoteMap.get(clazz);
		if (obj == null) {
			try {
				obj = ProxyUtil.getProxyClientRemote(clazz.newInstance(), cluster, name);
			} catch (Exception e) {
				throw new CreateRemoteFailException("remote object newInstance fail");
			}
			remoteMap.put(clazz, obj);
		}
		return (T) obj;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getRemoteInvokeObjectWithRetry(String cluster, String name, Class<?> clazz) {
		Object obj = remoteMap.get(clazz);
		if (obj == null) {
			try {
				obj = ProxyUtil.getProxyClientRemoteWithRetry(clazz.newInstance(), cluster, name);
			} catch (Exception e) {
				throw new CreateRemoteFailException("remote object newInstance fail");
			}
			remoteMap.put(clazz, obj);
		}
		return (T) obj;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getRandomRemoteInvokeObject(String cluster, Class<?> clazz) {
		Object obj = remoteMap.get(clazz);
		if (obj == null) {
			try {
				obj = ProxyUtil.getProxyRandomClientRemote(clazz.newInstance(), cluster);
			} catch (Exception e) {
				throw new CreateRemoteFailException("remote object newInstance fail");
			}
			remoteMap.put(clazz, obj);
		}
		return (T) obj;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getRandomRemoteInvokeObjectWithRetry(String cluster, Class<?> clazz) {
		Object obj = remoteMap.get(clazz);
		if (obj == null) {
			try {
				obj = ProxyUtil.getProxyRandomClientRemoteWithRetry(clazz.newInstance(), cluster);
			} catch (Exception e) {
				throw new CreateRemoteFailException("remote object newInstance fail");
			}
			remoteMap.put(clazz, obj);
		}
		return (T) obj;
	}
	
	long incrementCurrentId() {
		return currentId.incrementAndGet();
	}
}
