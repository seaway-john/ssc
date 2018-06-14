package com.seaway.game.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.seaway.game.common.entity.mysql.GameAgents;

@Repository
public interface GameAgentsRepository extends
		JpaRepository<GameAgents, Integer> {

	GameAgents findByWxUid(String wxUid);

	GameAgents findByWxSid(String wxSid);

	GameAgents findByWxUidAndEnabled(String wxUid, boolean enabled);

	void deleteByEnabled(boolean enabled);

	void deleteByWxUid(String wxUid);

	@Modifying(clearAutomatically = true)
	@Query(value = "update game_agents set description = :description, bankerUsername = :bankerUsername, bankerPassword = :bankerPassword, lastupdate = now()"
			+ " where wxUid = :wxUid", nativeQuery = true)
	void updateByWxUidAsAdmin(@Param("wxUid") String wxUid,
			@Param("description") String description,
			@Param("bankerUsername") String bankerUsername,
			@Param("bankerPassword") String bankerPassword);

	@Modifying(clearAutomatically = true)
	@Query(value = "update game_agents set wxUsername = :wxUsername, bankerUsername = CASE WHEN length(bankerUsername) = 0 THEN :bankerUsername ELSE bankerUsername END, bankerPassword = :bankerPassword, lastupdate = now()"
			+ " where wxUid = :wxUid and enabled = 1", nativeQuery = true)
	void updateByWxUidAsAgent(@Param("wxUid") String wxUid,
			@Param("wxUsername") String wxUsername,
			@Param("bankerUsername") String bankerUsername,
			@Param("bankerPassword") String bankerPassword);

	@Modifying(clearAutomatically = true)
	@Query(value = "update game_agents set balance = :balance, lastupdate = now()"
			+ " where wxUid = :wxUid", nativeQuery = true)
	void updateBalanceByWxUid(@Param("wxUid") String wxUid,
			@Param("balance") int balance);

	@Modifying(clearAutomatically = true)
	@Query(value = "update game_agents set betInvest = betInvest + :betInvest, lastupdate = now()"
			+ " where wxUid = :wxUid", nativeQuery = true)
	void updateBetInvestByWxUid(@Param("wxUid") String wxUid,
			@Param("betInvest") int betInvest);

	@Modifying(clearAutomatically = true)
	@Query(value = "update game_agents set betIncome = betIncome + :betIncome, lastupdate = now()"
			+ " where wxUid = :wxUid", nativeQuery = true)
	void updateBetIncomeByWxUid(@Param("wxUid") String wxUid,
			@Param("betIncome") int betIncome);

}
