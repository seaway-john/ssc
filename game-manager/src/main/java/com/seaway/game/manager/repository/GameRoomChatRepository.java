package com.seaway.game.manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.seaway.game.common.entity.mysql.GameRoomChat;

@Repository
public interface GameRoomChatRepository extends
		JpaRepository<GameRoomChat, Integer> {

	List<GameRoomChat> findFirst10ByRoomWxRidOrderByIdDesc(String roomWxRid);

	List<GameRoomChat> findFirst10ByRoomWxRidAndIdLessThanOrderByIdDesc(
			String roomWxRid, int id);

	void deleteByRoomWxRid(String roomWxRid);

}
