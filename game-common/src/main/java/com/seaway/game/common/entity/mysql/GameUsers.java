package com.seaway.game.common.entity.mysql;

import java.util.Date;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "game_users")
public class GameUsers {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private String wxUid;

	private String wxSid;

	private String wxNickName;

	private boolean wxSex;

	private String description;

	private boolean enabled;

	// true: npc user
	// false: real user
	private boolean npc;

	// true: enable tone
	// false: close tone
	private boolean tone;

	private Date created;

	private Date lastUpdate;

	private GameUsers() {
		this.wxNickName = "";
		this.enabled = true;
		this.npc = false;
		this.tone = true;
		this.created = new Date();
	}

	public GameUsers(String wxUid, String wxSid) {
		this();

		this.wxUid = wxUid;
		this.wxSid = wxSid;
	}

}
