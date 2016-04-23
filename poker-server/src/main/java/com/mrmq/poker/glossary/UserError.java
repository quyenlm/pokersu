package com.mrmq.poker.glossary;

public enum UserError {
	SUCCESS("00"),
	
	ACCOUNT_NOT_EXIST("10"),
	ACCOUNT_EXIST("11"),
	ACCOUNT_INVALID("12"),
	
	PASS_INVALID("15"),
	PASS_NOT_MATCH("16"),
	
	NAME_INVALID("18"),;
	
	private String code;
	
	UserError(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
}
