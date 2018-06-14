package com.seaway.game.manager.entity;

import com.seaway.game.manager.websocket.WebSocketType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebSocketMessage {

	private WebSocketType type;

	private Object data;

	public WebSocketMessage(WebSocketType type, Object data) {
		this.type = type;
		this.data = data;
	}

}
