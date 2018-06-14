package com.seaway.game.common.entity.mysql;

import java.util.Date;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "game_user_bet")
public class GameUserBet {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private String lotteryWinSequence;

	private String agentWxUid;

	private String roomWxRid;

	private String userWxUid;

	private String remarkName;

	private String betString;

	private String betFormat;

	private String betDecode;

	private int betInvest;

	private int betWinTicket;

	private int betIncome;

	private String betStatus;

	private int cancelBetCode;

	// true: is trial play
	// false: official
	private boolean trialPlay;

	// true: npc user
	// false: real user
	private boolean npc;

	private Date created;

	private Date lastUpdate;

	private GameUserBet() {
		this.remarkName = "";
		this.betString = "";
		this.betFormat = "";
		this.betDecode = "";
		this.trialPlay = false;
		this.npc = false;
		this.created = new Date();
	}

	public GameUserBet(String lotteryWinSequence, String agentWxUid,
			String roomWxRid, String userWxUid, GameUserBetStatus betStatus) {
		this();

		this.lotteryWinSequence = lotteryWinSequence;
		this.agentWxUid = agentWxUid;
		this.roomWxRid = roomWxRid;
		this.userWxUid = userWxUid;
		this.betStatus = betStatus.toString();
	}

}
