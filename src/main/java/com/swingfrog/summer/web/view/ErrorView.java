package com.swingfrog.summer.web.view;

public class ErrorView extends TextView {
	
	private int status;
	
	public ErrorView(int status, long code, String msg) {
		super(String.format("status: %s, code: %s, msg: %s", status, code, msg));
		this.status = status;
	}
	
	public ErrorView(int status, String msg) {
		super(String.format("status: %s, msg: %s", status, msg));
		this.status = status;
	}
	
	@Override
	public int getStatus() {
		return status;
	}
	
	@Override
	public String toString() {
		return "ErrorView";
	}
}
