package com.seaway.game.manager.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.seaway.game.common.entity.mysql.GameUserBalanceReport;

@Repository
public interface GameUserBalanceReportRepository extends
		JpaRepository<GameUserBalanceReport, Integer> {

	Page<GameUserBalanceReport> findByAgentWxUidAndTypeInOrderByCreatedDesc(
			Pageable pageable, String agentWxUid, List<String> types);

	Page<GameUserBalanceReport> findByAgentWxUidAndUserWxUidAndCreatedBetweenAndEncash(
			Pageable pageable, String agentWxUid, String userWxUid,
			Date dateStart, Date dateEnd, boolean encash);

	int countByAgentWxUidAndUserWxUidAndTypeAndEncash(String agentWxUid,
			String userWxUid, String type, boolean encash);

	void deleteByAgentWxUid(String agentWxUid);

	void deleteByAgentWxUidAndTypeInAndEncash(String agentWxUid,
			List<String> types, boolean encash);

	@Modifying(clearAutomatically = true)
	@Query(value = "update game_user_balance_report set remarkName = :remarkName where agentWxUid = :agentWxUid and userWxUid = :userWxUid", nativeQuery = true)
	void updateRemarknameByAgentWxUidAndUserWxUid(
			@Param("agentWxUid") String agentWxUid,
			@Param("userWxUid") String userWxUid,
			@Param("remarkName") String remarkName);

}
