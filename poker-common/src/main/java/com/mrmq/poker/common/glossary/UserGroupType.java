package com.mrmq.poker.common.glossary;

public enum UserGroupType {
	DEMO("DEMO"),
	PLAYER("PLAYER"),
	ADMIN("ADMIN");
	
	String value;
	private UserGroupType(String value) {
		this.value = value;
	}
	
	public String getValue(){
		return value;
	}
}