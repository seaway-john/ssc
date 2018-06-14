package com.seaway.game.manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.seaway.game.common.entity.mysql.GameLotteryWin;

@Repository
public interface GameLotteryWinRepository extends
		JpaRepository<GameLotteryWin, Integer> {

	GameLotteryWin findTopByOrderBySequenceDesc();

	GameLotteryWin findFirstByStatusOrderBySequenceDesc(String status);

	GameLotteryWin findBySequence(String sequence);

	List<GameLotteryWin> findByStatusAndSequenceLike(String status,
			String sequenceLike);

}
