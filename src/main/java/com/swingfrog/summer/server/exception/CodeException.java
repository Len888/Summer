package com.swingfrog.summer.server.exception;

public class CodeException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private long code;
	private String msg;
	
	public CodeException(long code, String msg) {
		super(String.format("code:%s msg:%s", code, msg));
		this.code = code;
		this.msg = msg;
	}
	
	public CodeException(CodeMsg msg, Object ...args) {
		this(msg.getCode(), String.format(msg.getMsg(), args));
	}
	
	public long getCode() {
		return code;
	}
	
	public String getMsg() {
		return msg;
	}
}
