package com.seaway.game.common.entity.mysql;

import java.util.Date;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "game_room")
public class GameRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private String agentWxUid;

	private String wxRid;

	private String name;

	private String description;

	private boolean enabled;

	private int minEachBetInvest;

	private int maxEachBetInvest;

	private Date created;

	private Date lastUpdate;

	private GameRoom() {
		this.name = "";
		this.enabled = true;
		this.created = new Date();
	}

	public GameRoom(GameAgents gameAgent, String wxRid) {
		this();

		this.agentWxUid = gameAgent.getWxUid();
		this.wxRid = wxRid;
	}

}
