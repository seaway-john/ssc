package com.seaway.game.common.entity.mysql;

import java.util.Date;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "game_user_balance_report")
public class GameUserBalanceReport {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private String agentWxUid;

	private String userWxUid;

	private String remarkName;

	private String type;

	private String lotteryWinSequence;

	private String betFormat;

	// true: bet || cash up/down confirm
	// false: cash up/down not confirm yet
	private boolean encash;

	private int gain;

	private int balance;

	private Date created;

	private Date lastUpdate;

	private GameUserBalanceReport() {
		this.remarkName = "";
		this.encash = false;
		this.created = new Date();
	}

	public GameUserBalanceReport(String agentWxUid, String userWxUid,
			GameUserBalanceReportType type) {
		this();

		this.agentWxUid = agentWxUid;
		this.userWxUid = userWxUid;
		this.type = type.toString();
	}

}
