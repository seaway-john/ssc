package com.seaway.game.common.entity.mysql;

import java.util.Date;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "game_user_balance")
public class GameUserBalance {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private String agentWxUid;

	private String userWxUid;

	private String remarkName;

	private int balance;

	private int betInvest;

	private int betIncome;

	// true: is trial play
	// false: official
	private boolean trialPlay;

	private int trialPlayBalance;

	private int availableBetInvest;

	private boolean enabled;

	private Date created;

	private Date lastUpdate;

	private GameUserBalance() {
		this.remarkName = "";
		this.enabled = true;
		this.trialPlay = false;
		this.created = new Date();
	}

	public GameUserBalance(String agentWxUid, GameUsers dbGameUser) {
		this();

		this.agentWxUid = agentWxUid;
		this.userWxUid = dbGameUser.getWxUid();
		this.remarkName = dbGameUser.getWxNickName();
	}

}
