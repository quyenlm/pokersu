package com.mrmq.poker.common.glossary;

public enum ServiceType {
	ADMIN("admin"),
	POKER("poker");
	
	String value;
	private ServiceType(String value) {
		this.value = value;
	}
	
	public String getValue(){
		return value;
	}
}
