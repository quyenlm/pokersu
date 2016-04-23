package com.mrmq.poker.client.utils;

import java.util.Map;

public class Helper {
	public static Integer findNextPosition(Map<Integer, Boolean> mapPosition, int totalPosition) {
		for(int i = 1; i <= totalPosition; i++) {
			if(!mapPosition.containsKey(i)) {
				return i;
			}
		}
		return null;
	}
}