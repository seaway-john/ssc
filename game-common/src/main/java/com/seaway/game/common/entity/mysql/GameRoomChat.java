package com.seaway.game.common.entity.mysql;

import java.util.Date;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "game_room_chat")
public class GameRoomChat {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private String roomWxRid;

	// true: admin || agent || npc
	// false: user
	private boolean server;

	private String headImage;

	private String nickName;

	private String message;

	private Date created;

	private Date lastUpdate;

	private GameRoomChat() {
		this.server = false;
		this.created = new Date();
	}

	public GameRoomChat(String roomWxRid) {
		this();
		
		this.roomWxRid = roomWxRid;
	}

}
