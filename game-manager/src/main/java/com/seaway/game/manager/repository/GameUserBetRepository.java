package com.seaway.game.manager.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.seaway.game.common.entity.mysql.GameUserBet;

@Repository
public interface GameUserBetRepository extends
		JpaRepository<GameUserBet, Integer> {

	List<GameUserBet> findByLotteryWinSequenceAndBetStatus(
			String lotteryWinSequence, String betStatus);

	List<GameUserBet> findByBetStatus(String betStatus);

	List<GameUserBet> findByLotteryWinSequenceAndRoomWxRid(
			String lotteryWinSequence, String roomWxRid);

	List<GameUserBet> findByAgentWxUidAndLotteryWinSequenceBetweenAndBetStatusInAndTrialPlayAndNpc(
			String agentwxuid, String lotteryWinSequenceStart,
			String lotteryWinSequenceEnd, List<String> betStatusIn,
			boolean trialPlay, boolean npc);

	List<GameUserBet> findByAgentWxUidAndCreatedBetweenAndBetStatusInAndTrialPlayAndNpc(
			String agentwxuid, Date createdStart, Date createdEnd,
			List<String> betStatusIn, boolean trialPlay, boolean npc);

	List<GameUserBet> findByAgentWxUidAndCreatedBetweenAndBetStatusInAndTrialPlayAndNpcOrderByIdDesc(
			String agentWxUid, Date createdStart, Date createdEnd,
			List<String> betStatusIn, boolean trialPlay, boolean npc);

	void deleteByAgentWxUid(String agentWxUid);

	@Query(value = "select agentwxuid, betstatus, trialplay, npc, sum(betinvest) as betinvest, sum(betincome) as betincome,"
			+ " 0 as id, '' as lotterywinsequence, '' as roomwxrid, '' as userwxuid, '' as remarkname, '' as betstring, '' as betformat,"
			+ " '' as betdecode, 0 as cancelbetid, 0 as cancelbetcode, now() as created, null as lastupdate"
			+ " from game_user_bet"
			+ " where created between :createdStart and :createdEnd and betstatus = 'BET_ENCASH' and trialplay = 0 and npc = 0"
			+ " group by agentwxuid", nativeQuery = true)
	List<GameUserBet> sumAllAgent(@Param("createdStart") Date createdStart,
			@Param("createdEnd") Date createdEnd);

	@Modifying(clearAutomatically = true)
	@Query(value = "update game_user_bet set remarkName = :remarkName where agentWxUid = :agentWxUid and userWxUid = :userWxUid", nativeQuery = true)
	void updateRemarknameByAgentWxUidAndUserWxUid(
			@Param("agentWxUid") String agentWxUid,
			@Param("userWxUid") String userWxUid,
			@Param("remarkName") String remarkName);

}
