package com.swingfrog.summer.client;

public interface RemoteCallback {

	public void success(Object obj);
	
	public void failure(long code, String msg);
	
}
