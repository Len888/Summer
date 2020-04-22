package com.swingfrog.summer.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClientCluster {

	private int next = -1;
	private List<ClientGroup> clientGroupList;
	private Map<String, ClientGroup> nameToClientGroup;
	
	public ClientCluster() {
		clientGroupList = new ArrayList<>();
		nameToClientGroup = new HashMap<>();
	}
	
	public void addClient(String name, ClientGroup clientGroup) {
		clientGroupList.add(clientGroup);
		nameToClientGroup.put(name, clientGroup);
	}
	
	public Client getClientByName(String name) {
		return nameToClientGroup.get(name).getClientWithNext();
	}
	
	public Client getClientWithNext() {
		int size = clientGroupList.size();
		if (size > 0) {
			if (size == 1) {
				return clientGroupList.get(0).getClientWithNext();
			}
			next ++;
			next = next % size;
			return clientGroupList.get(next % size).getClientWithNext();
		}
		return null;
	}
	
	public List<Client> listClients() {
		List<Client> list = new ArrayList<>();
		Iterator<ClientGroup> ite = clientGroupList.iterator();
		while (ite.hasNext()) {
			list.addAll(ite.next().listClients());
		}
		return list;
	}
	
}
