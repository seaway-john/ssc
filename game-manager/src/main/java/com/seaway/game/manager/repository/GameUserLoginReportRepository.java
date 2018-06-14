package com.seaway.game.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.seaway.game.common.entity.mysql.GameUserLoginReport;

@Repository
public interface GameUserLoginReportRepository extends JpaRepository<GameUserLoginReport, Integer> {

}
