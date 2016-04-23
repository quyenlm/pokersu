package com.mrmq.poker.common.glossary;

public enum MsgCode {
	SUCCESS("0"),
	FAIL("1"),
	INTERNAL("500"),
	BAD_GATEWAY("502"),	
	GATEWAY_TIMEOUT("504"),
	UNKNOWN("999"),
	
	TABLE_NOT_EXIST("11"),
	TABLE_FULL("12"),
	DEPOSIT_INVALID_CARD("101");
	
	
	private String code;
	private String msg;
	
	MsgCode(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMsg() {
		return msg;
	}
	
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	@Override
	public String toString() {
		return "Result [code=" + code + ", msg=" + msg + "]";
	}
}