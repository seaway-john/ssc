package com.seaway.game.web.controller.api;

import java.util.List;

import com.seaway.game.common.entity.ResponseEntity;
import com.seaway.game.common.entity.game.api.AwardInfo;
import com.seaway.game.common.entity.mysql.GameRoomChat;
import com.seaway.game.manager.api.LobbyApiManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lobby")
public class LobbyApiController {

	private final LobbyApiManager lobbyApiManager;

	@Autowired
	public LobbyApiController(LobbyApiManager lobbyApiManager) {
		this.lobbyApiManager = lobbyApiManager;
	}

	@RequestMapping(value = "/award/latest", method = RequestMethod.GET)
	public AwardInfo awardLatest() {
		return lobbyApiManager.awardLatest();
	}

	@RequestMapping(value = "/award/history", method = RequestMethod.GET)
	public List<AwardInfo> agentAwardHistory(@RequestParam("rid") String rid) {
		return lobbyApiManager.awardHistory(rid);
	}

	@RequestMapping(value = "/rule", method = RequestMethod.GET)
	public List<String> getRule(@RequestParam("rid") String rid) {
		return lobbyApiManager.getRules(rid);
	}

	@RequestMapping(value = "/message/last", method = RequestMethod.GET)
	public List<GameRoomChat> lastMessage(@RequestParam("rid") String rid,
			@RequestParam("uid") String uid, @RequestParam("sid") String sid) {
		return lobbyApiManager.lastMessage(rid);
	}

	@RequestMapping(value = "/message/previous", method = RequestMethod.GET)
	public List<GameRoomChat> previousMessage(@RequestParam("rid") String rid,
			@RequestParam("uid") String uid, @RequestParam("sid") String sid,
			@RequestParam("startId") int startId) {
		return lobbyApiManager.previousMessage(rid, startId);
	}

	@RequestMapping(value = "/agent/message/send", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void sendAgentMessage(@RequestBody String message,
			@RequestParam("rid") String rid, @RequestParam("uid") String uid,
			@RequestParam("sid") String sid) {
		if (StringUtils.isEmpty(message)) {
			return;
		}

		lobbyApiManager.sendAgentMessage(rid, uid, sid, message.trim());
	}

	@RequestMapping(value = "/user/message/send", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void sendUserMessage(@RequestBody String message,
			@RequestParam("rid") String rid, @RequestParam("uid") String uid,
			@RequestParam("sid") String sid) {
		if (StringUtils.isEmpty(message)) {
			return;
		}

		lobbyApiManager.sendUserMessage(rid, uid, sid, message.trim(), false);
	}

	@RequestMapping(value = "/user/message/keyboard-send", method = RequestMethod.GET)
	public ResponseEntity sendUseKeyboardrMessage(
			@RequestParam("rid") String rid, @RequestParam("uid") String uid,
			@RequestParam("sid") String sid,
			@RequestParam("message") String message) {
		ResponseEntity response = new ResponseEntity();
		if (StringUtils.isEmpty(message)) {
			return response;
		}

		return lobbyApiManager.sendUserMessage(rid, uid, sid, message.trim(),
				true);
	}

	@RequestMapping(value = "/cancel-bet", method = RequestMethod.GET)
	public ResponseEntity cancelBet(@RequestParam("rid") String rid,
			@RequestParam("uid") String uid, @RequestParam("sid") String sid,
			@RequestParam("betId") int betId) {
		return lobbyApiManager.cancelBet(rid, uid, sid, betId);
	}

}
