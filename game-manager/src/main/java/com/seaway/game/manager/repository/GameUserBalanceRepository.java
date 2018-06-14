package com.seaway.game.manager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.seaway.game.common.entity.mysql.GameUserBalance;

@Repository
public interface GameUserBalanceRepository extends
		JpaRepository<GameUserBalance, Integer> {

	GameUserBalance findByAgentWxUidAndUserWxUid(String agentWxUid,
			String userWxUid);

	GameUserBalance findByAgentWxUidAndUserWxUidAndEnabled(String agentWxUid,
			String userWxUid, boolean enabled);

	Page<GameUserBalance> findByAgentWxUidOrderByBetInvestDesc(Pageable pageable,
			String agentWxUid);

	void deleteByAgentWxUid(String agentWxUid);

	@Modifying(clearAutomatically = true)
	@Query(value = "update game_user_balance set balance = balance + :gain, lastupdate = now()"
			+ " where agentWxUid = :agentWxUid and userWxUid = :userWxUid", nativeQuery = true)
	void updateBalanceByAgentWxUidAndUserWxUid(
			@Param("agentWxUid") String agentWxUid,
			@Param("userWxUid") String userWxUid, @Param("gain") int gain);

	@Modifying(clearAutomatically = true)
	@Query(value = "update game_user_balance set trialPlayBalance = trialPlayBalance + :gain, lastupdate = now()"
			+ " where agentWxUid = :agentWxUid and userWxUid = :userWxUid", nativeQuery = true)
	void updateTrialPlayBalanceByAgentWxUidAndUserWxUid(
			@Param("agentWxUid") String agentWxUid,
			@Param("userWxUid") String userWxUid, @Param("gain") int gain);

	@Modifying(clearAutomatically = true)
	@Query(value = "update game_user_balance set balance = balance - :betInvest, betInvest = betInvest + :betInvest, availableBetInvest = availableBetInvest + :betInvest, lastupdate = now()"
			+ " where agentWxUid = :agentWxUid and userWxUid = :userWxUid", nativeQuery = true)
	void updateBetInvestByAgentWxUidAndUserWxUid(
			@Param("agentWxUid") String agentWxUid,
			@Param("userWxUid") String userWxUid,
			@Param("betInvest") int betInvest);

	@Modifying(clearAutomatically = true)
	@Query(value = "update game_user_balance set balance = balance + :betIncome, betIncome = betIncome + :betIncome, lastupdate = now()"
			+ " where agentWxUid = :agentWxUid and userWxUid = :userWxUid", nativeQuery = true)
	void updateBetIncomeByAgentWxUidAndUserWxUid(
			@Param("agentWxUid") String agentWxUid,
			@Param("userWxUid") String userWxUid,
			@Param("betIncome") int betIncome);

	@Modifying(clearAutomatically = true)
	@Query(value = "update game_user_balance set balance = balance + :weal, availableBetInvest = availableBetInvest - :exchangeBetInvest, lastupdate = now()"
			+ " where id = :id", nativeQuery = true)
	void exchangeWealById(@Param("id") int id, @Param("weal") int weal,
			@Param("exchangeBetInvest") int exchangeBetInvest);

	@Modifying(clearAutomatically = true)
	@Query(value = "update game_user_balance set remarkName = :remarkName, lastupdate = now()"
			+ " where id= :id", nativeQuery = true)
	void updateRemarknameById(@Param("id") int id,
			@Param("remarkName") String remarkName);

	@Modifying(clearAutomatically = true)
	@Query(value = "update game_user_balance set enabled = :enabled, lastupdate = now()"
			+ " where id= :id", nativeQuery = true)
	void updateEnableById(@Param("id") int id, @Param("enabled") boolean enabled);

}
