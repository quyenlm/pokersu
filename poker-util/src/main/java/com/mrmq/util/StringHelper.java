package com.mrmq.util;


public class StringHelper {
    public static boolean isEmpty(String values) {
    	return (values == null || values.trim().length() == 0);
    }
    
    public static boolean isEqual(String value1, String value2) {
		if(value1 != null)
			return value1.equals(value2);
		if(value2 != null)
			return value2.equals(value1);
		return true;
	}
}