package com.seaway.game.web.controller.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.seaway.game.common.entity.ResponseEntity;
import com.seaway.game.common.entity.game.api.AgentConfig;
import com.seaway.game.common.entity.game.api.AgentInfo;
import com.seaway.game.common.entity.game.api.LoginQrcode;
import com.seaway.game.common.entity.manager.UserManage;
import com.seaway.game.common.entity.manager.UserRechargeApply;
import com.seaway.game.manager.api.AgentApiManager;
import com.seaway.game.manager.api.WechatApiManager;

@RestController
@RequestMapping("/api/agent")
public class AgentApiController {

	private final AgentApiManager agentApiManager;

	private final WechatApiManager wechatApiManager;

	@Autowired
	public AgentApiController(AgentApiManager agentApiManager,
			WechatApiManager wechatApiManager) {
		this.agentApiManager = agentApiManager;
		this.wechatApiManager = wechatApiManager;
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public ResponseEntity logout(@RequestParam("uid") String uid,
			@RequestParam("sid") String sid) {
		return agentApiManager.logout(uid, sid);
	}

	@RequestMapping(value = "/refresh", method = RequestMethod.GET)
	public AgentInfo getAgentInfo(@RequestParam("rid") String rid,
			@RequestParam("uid") String uid, @RequestParam("sid") String sid) {

		return agentApiManager.getAgentInfo(rid, uid, sid);
	}

	@RequestMapping(value = "/user/manage/table", method = RequestMethod.GET)
	public Map<String, Object> getUserManageMap(
			@RequestParam("rid") String rid, @RequestParam("uid") String uid,
			@RequestParam("sid") String sid, @RequestParam("page") int page,
			@RequestParam("limit") int limit) {
		return agentApiManager.getUserManageMap(rid, uid, sid, page, limit);
	}

	@RequestMapping(value = "/user/manage/update", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void updateUserRemarkname(@RequestBody UserManage userManage,
			@RequestParam("uid") String uid, @RequestParam("sid") String sid) {
		agentApiManager.updateUserRemarkname(uid, sid, userManage);
	}

	@RequestMapping(value = "/user/manage/blacklist", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void blacklisUsers(@RequestBody List<UserManage> userManages,
			@RequestParam("uid") String uid, @RequestParam("sid") String sid,
			@RequestParam(value = "cancel", required = false) boolean cancel) {
		agentApiManager.blacklisUsers(uid, sid, cancel, userManages);
	}

	@RequestMapping(value = "/journal/table", method = RequestMethod.GET)
	public Map<String, Object> getJournalMap(
			@RequestParam("uid") String uid,
			@RequestParam("sid") String sid,
			@RequestParam(value = "day", required = false, defaultValue = "0") int day) {
		return agentApiManager.getJournalMap(uid, sid, day);
	}

	@RequestMapping(value = "contact/upload", method = RequestMethod.POST)
	public ResponseEntity uploadQrcode(
			@RequestParam("file") MultipartFile file,
			@RequestParam("uid") String uid, @RequestParam("sid") String sid,
			@RequestParam("name") String name) throws IOException {
		return agentApiManager.uploadQrcode(uid, sid, name, file.getBytes());
	}

	@RequestMapping(value = "/config/refresh", method = RequestMethod.GET)
	public AgentConfig getConfig(@RequestParam("uid") String uid,
			@RequestParam("sid") String sid) {
		return agentApiManager.getConfig(uid, sid);
	}

	@RequestMapping(value = "/config/update", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void updateConfig(@RequestBody AgentConfig agentConfig,
			@RequestParam("uid") String uid, @RequestParam("sid") String sid) {
		agentApiManager.updateConfig(uid, sid, agentConfig);
	}

	@RequestMapping(value = "/sync-balance", method = RequestMethod.GET)
	public ResponseEntity syncBalance(@RequestParam("rid") String rid,
			@RequestParam("uid") String uid, @RequestParam("sid") String sid) {
		return agentApiManager.syncBalance(uid, sid);
	}

	@RequestMapping(value = "/room/login-url", method = RequestMethod.GET)
	public LoginQrcode getRoomLoginUrl(@RequestParam("rid") String rid,
			@RequestParam("uid") String uid, @RequestParam("sid") String sid) {
		return wechatApiManager.getRoomLoginUrl(rid);
	}

	@RequestMapping(value = "/room/rule", method = RequestMethod.POST)
	public void updateRoomRule(@RequestBody String rule,
			@RequestParam("rid") String rid, @RequestParam("uid") String uid,
			@RequestParam("sid") String sid) {
		agentApiManager.updateRoomRule(rid, uid, sid, rule);
	}

	@RequestMapping(value = "/user/recharge/table", method = RequestMethod.GET)
	public Map<String, Object> getUserRechargeMap(
			@RequestParam("uid") String uid, @RequestParam("sid") String sid,
			@RequestParam("page") int page, @RequestParam("limit") int limit) {
		return agentApiManager.getUserRechargeMap(uid, sid, page, limit);
	}

	@RequestMapping(value = "/user/recharge/ignore", method = RequestMethod.GET)
	public ResponseEntity ignoreAllUserRecharges(
			@RequestParam("uid") String uid, @RequestParam("sid") String sid) {
		return agentApiManager.ignoreAllUserRecharges(uid, sid);
	}

	@RequestMapping(value = "/user/recharge/agreen", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void agreenUserRecharges(
			@RequestBody List<UserRechargeApply> recharges,
			@RequestParam("uid") String uid, @RequestParam("sid") String sid,
			@RequestParam(value = "cancel", required = false) boolean cancel) {
		agentApiManager.agreenUserRecharges(uid, sid, recharges, cancel);
	}

}
