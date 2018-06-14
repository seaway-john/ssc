package com.seaway.game.manager.websocket;

import java.io.EOFException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.seaway.game.manager.entity.WebSocketMessage;
import com.seaway.game.manager.entity.WebSocketSessionInfo;

@Slf4j
@Component
@ServerEndpoint(value = "/api/websocket/{rid}/{uid}/{sid}/{wxNickName}")
public class WebSocketEndPoint {

	private static Map<String, WebSocketSessionInfo> map = new ConcurrentHashMap<>();

	@OnOpen
	public void onOpen(Session session, @PathParam(value = "rid") String rid,
			@PathParam(value = "uid") String uid,
			@PathParam(value = "sid") String sid,
			@PathParam(value = "wxNickName") String wxNickName) {
		if (map.containsKey(uid)) {
			log.warn("Duplicate user {} connect, close old", wxNickName);
			closeByUid(uid);
		}

		WebSocketSessionInfo webSocketSessionInfo = new WebSocketSessionInfo(
				session, rid, uid, sid, wxNickName);
		map.put(uid, webSocketSessionInfo);

		WebSocketMessage wsMessage = new WebSocketMessage(WebSocketType.INFO,
				"Connect web socket success!");
		broadcastUid(uid, wsMessage);

		log.info("New user {} connect, current connect {}", wxNickName,
				map.size());
	}

	@OnError
	public void onError(Session session,
			@PathParam(value = "wxNickName") String wxNickName, Throwable error) {
		if (error instanceof EOFException) {
			log.warn("User {} closed broswer", wxNickName);
		} else {
			log.warn("Error in user {} connect, reason {}", wxNickName,
					error.getMessage());
			// throw error;
		}
	}

	@OnClose
	public void onClose(Session session, @PathParam(value = "uid") String uid) {
		WebSocketSessionInfo webSocketSessionInfo = map.remove(uid);
		if (webSocketSessionInfo == null) {
			log.warn("Invalid web socket {} in onClose", uid);
			return;
		}

		log.info("Close user {} connect, current connect {}",
				webSocketSessionInfo.getWxNickName(), map.size());
	}

	@OnMessage
	public void onMessage(Session session,
			@PathParam(value = "uid") String uid, String message) {
		WebSocketSessionInfo webSocketSessionInfo = map.get(uid);
		if (webSocketSessionInfo == null) {
			log.warn("Invalid web socket {} in onMessage, receive {}", uid,
					message);
			return;
		}

		log.info("Receive user {} connect, message {}",
				webSocketSessionInfo.getWxNickName(), message);
	}

	public void broadcast(WebSocketMessage wsMessage) {
		String message = convertMessage(wsMessage);

		for (WebSocketSessionInfo webSocketSessionInfo : map.values()) {
			sendMessage(webSocketSessionInfo.getSession(), message);
		}
	}

	public void broadcastRid(String rid, WebSocketMessage wsMessage) {
		String message = convertMessage(wsMessage);

		for (WebSocketSessionInfo webSocketSessionInfo : map.values()) {
			if (webSocketSessionInfo.getRid().equals(rid)) {
				sendMessage(webSocketSessionInfo.getSession(), message);
			}
		}
	}

	public void broadcastUid(String uid, WebSocketMessage wsMessage) {
		if (!map.containsKey(uid)) {
			return;
		}

		String message = convertMessage(wsMessage);
		sendMessage(map.get(uid).getSession(), message);
	}

	public boolean onlineRoom(String rid, String uid) {
		if (!map.containsKey(uid)) {
			return false;
		}

		WebSocketSessionInfo webSocketSessionInfo = map.get(uid);
		if (webSocketSessionInfo == null) {
			return false;
		}

		return webSocketSessionInfo.getRid().equals(rid);
	}

	public void closeByUid(String uid) {
		if (!map.containsKey(uid)) {
			return;
		}

		try {
			map.get(uid).getSession().close();
		} catch (IOException e) {
			log.warn("IOException in closeByUid {}, reason {}", uid,
					e.getMessage());
		} finally {
			map.remove(uid);
		}
	}

	private void sendMessage(Session session, String message) {
		try {
			if (session.isOpen()) {
				session.getBasicRemote().sendText(message);
			}
		} catch (IOException e) {
			log.warn("IOException in sendMessage, message (), reason {}",
					message, e.getMessage());
		}
	}

	private String convertMessage(WebSocketMessage wsMessage) {
		Gson gson = new Gson();

		return gson.toJson(wsMessage);
	}

}
