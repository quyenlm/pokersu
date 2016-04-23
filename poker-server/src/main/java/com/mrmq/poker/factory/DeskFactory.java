package com.mrmq.poker.factory;

import com.mrmq.poker.common.proto.PokerModelProto.Room;
import com.mrmq.poker.common.proto.PokerModelProto.RoundType;
import com.mrmq.poker.common.proto.PokerModelProto.Table;
import com.mrmq.poker.manager.PokerMananger;

public class DeskFactory {
	public static Table createTable(Room room) {
		Table.Builder table = Table.newBuilder();
		
		table.setTableId(PokerMananger.genTableId());
		table.setRoundType(RoundType.WAITTING);
		table.setRoomId(room.getRoomId());
		
		return table.build();
	}
}