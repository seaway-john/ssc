package com.seaway.game.common.entity.mysql;

import java.util.Date;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "game_config")
public class GameConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private String agentWxUid;

	private String roomWxRid;

	private String attribute;

	private String name;

	private String value;

	private Date created;

	private Date lastUpdate;

	private GameConfig() {
		this.agentWxUid = "";
		this.roomWxRid = "";
		this.created = new Date();
	}

	public GameConfig(String attribute, String name, String value) {
		this();

		this.attribute = attribute;
		this.name = name;
		this.value = value;
	}

}
