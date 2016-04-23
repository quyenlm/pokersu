package com.mrmq.poker.factory;

import com.mrmq.poker.common.proto.PokerModelProto.Room;
import com.mrmq.poker.common.proto.PokerModelProto.Room.RoomState;
import com.mrmq.poker.manager.PokerMananger;

public class RoomFactory {
	public static Room createRoom() {
		Room.Builder room = Room.newBuilder();
		room.setRoomId(PokerMananger.genRoomId());
		room.setState(RoomState.AVAILABLE);
		return room.build();
	}
}