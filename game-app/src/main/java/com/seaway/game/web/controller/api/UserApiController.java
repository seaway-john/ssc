package com.seaway.game.web.controller.api;

import java.util.Map;

import com.seaway.game.common.entity.ResponseEntity;
import com.seaway.game.common.entity.game.api.AgentConfig;
import com.seaway.game.common.entity.game.api.UserInfo;
import com.seaway.game.common.entity.game.api.UserRecharge;
import com.seaway.game.manager.api.UserApiManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserApiController {

	private final UserApiManager userApiManager;

	@Autowired
	public UserApiController(UserApiManager userApiManager) {
		this.userApiManager = userApiManager;
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public ResponseEntity logout(@RequestParam("rid") String rid,
			@RequestParam("uid") String uid, @RequestParam("sid") String sid) {
		return userApiManager.logout(rid, uid, sid);
	}

	@RequestMapping(value = "/refresh", method = RequestMethod.GET)
	public UserInfo getUserInfo(@RequestParam("rid") String rid,
			@RequestParam("uid") String uid, @RequestParam("sid") String sid) {

		return userApiManager.getUserInfo(rid, uid, sid);
	}

	@RequestMapping(value = "/toggle-tone", method = RequestMethod.GET)
	public ResponseEntity toggleTone(@RequestParam("rid") String rid,
			@RequestParam("uid") String uid, @RequestParam("sid") String sid) {
		return userApiManager.toggleTone(rid, uid, sid);
	}

	@RequestMapping(value = "/toggle-trial-play", method = RequestMethod.GET)
	public ResponseEntity toggleTrialPlay(@RequestParam("rid") String rid,
			@RequestParam("uid") String uid, @RequestParam("sid") String sid) {
		return userApiManager.toggleTrialPlay(rid, uid, sid);
	}

	@RequestMapping(value = "/exchange-weal", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity exchangeWeal(@RequestBody UserRecharge exchangeWeal,
			@RequestParam("rid") String rid, @RequestParam("uid") String uid,
			@RequestParam("sid") String sid) {
		return userApiManager.exchangeWeal(rid, uid, sid, exchangeWeal);
	}

	@RequestMapping(value = "/recharge", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity recharge(@RequestBody UserRecharge userRecharge,
			@RequestParam("rid") String rid, @RequestParam("uid") String uid,
			@RequestParam("sid") String sid) {
		return userApiManager.recharge(rid, uid, sid, userRecharge);
	}

	@RequestMapping(value = "/record/table", method = RequestMethod.GET)
	public Map<String, Object> getRecords(
			@RequestParam("rid") String rid,
			@RequestParam("uid") String uid,
			@RequestParam("sid") String sid,
			@RequestParam("page") int page,
			@RequestParam("limit") int limit,
			@RequestParam(name = "day", required = false, defaultValue = "0") int day) {
		return userApiManager.getRecordMap(rid, uid, sid, page, limit, day);
	}

	@RequestMapping(value = "/supply", method = RequestMethod.GET)
	public AgentConfig supply(@RequestParam("rid") String rid,
			@RequestParam("uid") String uid, @RequestParam("sid") String sid) {
		return userApiManager.supply(rid, uid, sid);
	}

}
