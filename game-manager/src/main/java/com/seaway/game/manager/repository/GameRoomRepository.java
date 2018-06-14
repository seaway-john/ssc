package com.seaway.game.manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.seaway.game.common.entity.mysql.GameRoom;

@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, Integer> {

	GameRoom findByWxRidAndEnabled(String wxRid, boolean enabled);

	GameRoom findFirstByAgentWxUidAndEnabled(String agentWxUid, boolean enabled);

	List<GameRoom> findByEnabled(boolean enabled);

	List<GameRoom> findByAgentWxUidAndEnabled(String agentWxUid, boolean enabled);

	void deleteByAgentWxUid(String agentWxUid);

}
