package com.seaway.game.common.entity.game.api;

import lombok.Getter;
import lombok.Setter;

import com.seaway.game.common.entity.mysql.GameUserBalanceReportType;

@Getter
@Setter
public class UserRecharge {

	private GameUserBalanceReportType type;

	private int number;

}
