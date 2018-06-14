package com.seaway.game.manager.repository.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.seaway.game.common.entity.mysql.GameAgents;
import com.seaway.game.common.entity.mysql.GameRoom;
import com.seaway.game.common.entity.mysql.GameRoomChat;
import com.seaway.game.common.entity.mysql.GameUsers;
import com.seaway.game.manager.entity.WebSocketMessage;
import com.seaway.game.manager.repository.GameRoomChatRepository;
import com.seaway.game.manager.websocket.WebSocketEndPoint;
import com.seaway.game.manager.websocket.WebSocketType;

@Component
public class GameRoomChatManager {

	private final GameRoomChatRepository gameRoomChatRepository;

	private final WebSocketEndPoint webSocketEndPoint;

	private static final String robotHeadImage = "npc.png";

	private static final String robotNickName = "【NPC】小爱";

	@Autowired
	public GameRoomChatManager(GameRoomChatRepository gameRoomChatRepository,
			WebSocketEndPoint webSocketEndPoint) {
		this.gameRoomChatRepository = gameRoomChatRepository;
		this.webSocketEndPoint = webSocketEndPoint;
	}

	public List<GameRoomChat> getLastMessage(String roomWxRid) {
		List<GameRoomChat> gameRoomChats = gameRoomChatRepository
				.findFirst10ByRoomWxRidOrderByIdDesc(roomWxRid);
		if (gameRoomChats != null && !gameRoomChats.isEmpty()) {
			Collections.reverse(gameRoomChats);
		} else {
			gameRoomChats = new ArrayList<>();
		}

		GameRoomChat addGameRoomChat = new GameRoomChat(roomWxRid);
		addGameRoomChat.setServer(true);
		addGameRoomChat.setNickName(robotNickName);
		addGameRoomChat.setHeadImage(robotHeadImage);

		String message = "<i class=\"fa fa-check-circle\" style=\"color: #5bd454;\"></i><span style=\"color: #30b549;\"> 成功连接服务器</span>";
		addGameRoomChat.setMessage(message);

		gameRoomChats.add(addGameRoomChat);

		return gameRoomChats;
	}

	public List<GameRoomChat> getPreviousMessage(String wxRid, int startId) {
		List<GameRoomChat> gameRoomChats = gameRoomChatRepository
				.findFirst10ByRoomWxRidAndIdLessThanOrderByIdDesc(wxRid,
						startId);
		if (gameRoomChats == null) {
			gameRoomChats = new ArrayList<>();
		}

		return gameRoomChats;
	}

	public void insertAgent(GameAgents gameAgent, GameRoom gameRoom,
			String message) {
		GameRoomChat gameRoomChat = new GameRoomChat(gameRoom.getWxRid());
		gameRoomChat.setServer(true);
		gameRoomChat.setHeadImage(gameAgent.getWxUid() + ".jpg");
		gameRoomChat.setNickName("【房管】" + gameAgent.getWxNickName());
		gameRoomChat.setMessage(message);

		insert(gameRoomChat, gameRoom.getWxRid());
	}

	public void insertUser(GameUsers gameUser, GameRoom gameRoom, String message) {
		GameRoomChat gameRoomChat = new GameRoomChat(gameRoom.getWxRid());
		gameRoomChat.setServer(false);
		gameRoomChat.setHeadImage(gameUser.getWxUid() + ".jpg");
		gameRoomChat.setNickName(gameUser.getWxNickName());
		gameRoomChat.setMessage(message);

		insert(gameRoomChat, gameRoom.getWxRid());
	}

	public void insertRobot(GameRoom gameRoom, String message) {
		GameRoomChat gameRoomChat = new GameRoomChat(gameRoom.getWxRid());
		gameRoomChat.setServer(true);
		gameRoomChat.setHeadImage(robotHeadImage);
		gameRoomChat.setNickName(robotNickName);
		gameRoomChat.setMessage(message);

		insert(gameRoomChat, gameRoom.getWxRid());
	}

	public void insertRobot(GameRoom gameRoom, List<String> messages,
			String wxNickName, boolean trialPlay) {
		StringBuilder sb = new StringBuilder();

		sb.append("<span style=\"color: #0066cc;\">@ ");
		sb.append(wxNickName);
		sb.append("</span>");

		String split = "<br/>";
		if (trialPlay) {
			split += "【试玩】";
		}
		sb.append(split + String.join(split, messages));

		insertRobot(gameRoom, sb.toString());
	}

	private void insert(GameRoomChat gameRoomChat, String roomWxRid) {
		gameRoomChatRepository.save(gameRoomChat);

		WebSocketMessage wsMessage = new WebSocketMessage(WebSocketType.CHAT,
				gameRoomChat);
		webSocketEndPoint.broadcastRid(roomWxRid, wsMessage);
	}

	@Transactional
	public void deleteByRoomWxRid(String roomWxRid) {
		gameRoomChatRepository.deleteByRoomWxRid(roomWxRid);
	}

}
