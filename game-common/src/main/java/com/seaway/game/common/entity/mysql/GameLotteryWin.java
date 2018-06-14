package com.seaway.game.common.entity.mysql;

import java.util.Date;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "game_lottery_win")
public class GameLotteryWin {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private String sequence;

	private String awardNumber;

	private String status;

	private Date publishDate;

	private Date created;

	private Date lastUpdate;

	private GameLotteryWin() {
		this.created = new Date();
	}

	public GameLotteryWin(String sequence, GameLotteryWinStatus status) {
		this();

		this.sequence = sequence;
		this.status = status.toString();
	}
}
