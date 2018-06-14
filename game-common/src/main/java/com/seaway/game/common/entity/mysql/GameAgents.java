package com.seaway.game.common.entity.mysql;

import java.util.Date;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "game_agents")
public class GameAgents {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private String wxUid;

	private String wxSid;

	private String wxUsername;

	private String wxNickName;

	private boolean wxSex;

	private String description;

	private int roomLimit;

	private int balance;

	private int betInvest;

	private int betIncome;

	private String bankerUsername;

	private String bankerPassword;

	private boolean enabled;

	private Date created;

	private Date lastUpdate;

	private GameAgents() {
		this.wxUsername = "";
		this.wxNickName = "";
		this.bankerUsername = "";
		this.bankerPassword = "";
		this.roomLimit = 1;
		this.created = new Date();
	}

	public GameAgents(String wxUid, String wxSid) {
		this();

		this.wxUid = wxUid;
		this.wxSid = wxSid;
	}

}
