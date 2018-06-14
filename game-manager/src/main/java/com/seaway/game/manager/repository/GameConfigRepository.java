package com.seaway.game.manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.seaway.game.common.entity.mysql.GameConfig;

@Repository
public interface GameConfigRepository extends
		JpaRepository<GameConfig, Integer> {

	GameConfig findFirstByAgentWxUidInAndRoomWxRidInAndAttributeOrderByRoomWxRidDescAgentWxUidDesc(
			List<String> agentWxUids, List<String> roomWxRids, String attribute);

	GameConfig findByAgentWxUidAndRoomWxRidAndAttribute(String agentWxUid,
			String roomWxRid, String attribute);

	List<GameConfig> findByAgentWxUidInAndRoomWxRidInAndAttributeInOrderByRoomWxRidDescAgentWxUidDesc(
			List<String> agentWxUids, List<String> roomWxRids,
			List<String> attribute);

	void deleteByAgentWxUid(String agentWxUid);

}
