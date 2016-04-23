package com.mrmq.poker.gateway;

import com.mrmq.poker.gateway.impl.BaokimSCard;

/**
 * @author quyen.le.manh
 *
 */
public class GatewayFactory {
	public enum GateWayType {
		BAO_KIM ("BAO_KIM");
		
		public static final String BAO_KIM_VALUE = "BAO_KIM";
		
		private String code;
		
		GateWayType(String code) {
			this.code = code;
		}
		
		public String getCode() {
			return code;
		}
	}
	
	public static Gateway createGateway(String gwCode) {
		if(GateWayType.BAO_KIM_VALUE.equals(gwCode))
			return new BaokimSCard();
		return null;
	}
}