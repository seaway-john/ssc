package com.seaway.game.manager.entity;

import javax.websocket.Session;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebSocketSessionInfo {

	private String id;

	private String rid;

	private String uid;

	private String sid;

	private String wxNickName;

	private Session session;

	public WebSocketSessionInfo(Session session, String rid, String uid,
			String sid, String wxNickName) {
		this.id = session.getId();
		this.rid = rid;
		this.uid = uid;
		this.sid = sid;
		this.wxNickName = wxNickName;
		this.session = session;
	}
}
