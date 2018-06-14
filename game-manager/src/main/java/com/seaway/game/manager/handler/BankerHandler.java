package com.seaway.game.manager.handler;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.seaway.game.common.entity.manager.banker.BankerResponse;
import com.seaway.game.common.entity.manager.banker.MemberCredit;
import com.seaway.game.common.entity.manager.banker.BetSendResponse;
import com.seaway.game.common.entity.manager.banker.LastPeriods;
import com.seaway.game.common.entity.manager.banker.LoginSuccessResponse;
import com.seaway.game.common.entity.mysql.GameAgents;
import com.seaway.game.manager.entity.BankerHttpEntity;
import com.seaway.game.manager.repository.impl.GameAgentManager;
import com.seaway.game.manager.schedule.BetterNetworkSchedule;
import com.seaway.game.manager.schedule.BetterNetworkTask;

@Slf4j
@Component
public class BankerHandler {

	private final GameAgentManager gameAgentManager;

	private final BetterNetworkSchedule betterNetworkSchedule;

	private final BetterNetworkTask betterNetworkTask;

	private final String bankerError = "上庄服务器忙";

	private static Map<String, BankerHttpEntity> agentMap = new ConcurrentHashMap<>();

	@Autowired
	public BankerHandler(GameAgentManager gameAgentManager,
			BetterNetworkSchedule betterNetworkSchedule,
			BetterNetworkTask betterNetworkTask) {
		this.gameAgentManager = gameAgentManager;
		this.betterNetworkSchedule = betterNetworkSchedule;
		this.betterNetworkTask = betterNetworkTask;
	}

	public BetSendResponse sendBet(String agentWxUid, Map<String, String> map) {
		BetSendResponse betSendResponse = new BetSendResponse();
		BankerResponse bankerResponse = getLastPeriod(agentWxUid);
		if (!bankerResponse.isStatus()) {
			betSendResponse.setInfo(bankerResponse.getInfo());
			return betSendResponse;
		}

		String url = betterNetworkTask.getUrl("/FastBeat/MemberBet");
		String response = post(agentWxUid, url, map, true);
		if (response == null) {
			betSendResponse.setInfo("上庄服务器超时");
			return betSendResponse;
		}

		Gson gson = new Gson();
		bankerResponse = gson.fromJson(response, BankerResponse.class);
		if (!bankerResponse.isStatus()) {
			betSendResponse.setInfo(bankerResponse.getInfo());
			return betSendResponse;
		}

		betSendResponse = gson.fromJson(response, BetSendResponse.class);
		if (syncBalance(agentWxUid, betSendResponse.getCmdObject(), false)) {
			if (!betSendResponse.isStatus()) {
				log.warn(
						"Failed to send bet {}, but agent {} balance changed and treat as bet success",
						map, agentWxUid);

				betSendResponse.setStatus(true);
			}
		}

		return betSendResponse;
	}

	public BankerResponse cancelBet(String agentWxUid, Map<String, String> map) {
		BankerResponse bankerResponse = getLastPeriod(agentWxUid);
		if (!bankerResponse.isStatus()) {
			return bankerResponse;
		}

		String url = betterNetworkTask.getUrl("/FastBeat/BackBetOperator");
		String response = post(agentWxUid, url, map, true);
		if (response == null) {
			bankerResponse.setStatus(false);
			bankerResponse.setInfo("上庄服务器超时");
			return bankerResponse;
		}

		Gson gson = new Gson();
		bankerResponse = gson.fromJson(response, BankerResponse.class);
		if (syncBalance(agentWxUid, null, false)) {
			if (!bankerResponse.isStatus()) {
				log.warn(
						"Failed to cancel bet {}, but agent {} balance changed and treat as cancel success",
						map, agentWxUid);

				bankerResponse.setStatus(true);
			}
		}

		return bankerResponse;
	}

	public boolean syncBalance(String agentWxUid, MemberCredit memberCredit,
			boolean changeBanker) {
		int bankerBalance = 0;
		if (memberCredit == null) {
			if (changeBanker) {
				agentMap.remove(agentWxUid);
			}
			memberCredit = getMemberCredit(agentWxUid);
		}
		if (memberCredit == null) {
			return true;
		}

		bankerBalance = new Double(Math.floor(memberCredit.getCredit()))
				.intValue();
		GameAgents gameAgent = gameAgentManager.getByWxUid(agentWxUid);
		if (gameAgent == null || gameAgent.getBalance() == bankerBalance) {
			return false;
		}

		log.info("Sync agent {} balance from {} to {}",
				gameAgent.getWxNickName(), gameAgent.getBalance(),
				bankerBalance);
		gameAgentManager.updateBalance(agentWxUid, bankerBalance);

		return true;
	}

	private MemberCredit getMemberCredit(String agentWxUid) {
		String url = betterNetworkTask.getUrl("/Home/GetMemberCredit?_="
				+ System.currentTimeMillis());

		String response = get(agentWxUid, url, true);
		if (response == null || reLogin(response)) {
			return null;
		}

		Gson gson = new Gson();
		return gson.fromJson(response, MemberCredit.class);
	}

	private BankerResponse getLastPeriod(String agentWxUid) {
		BankerResponse bankerResponse = new BankerResponse();

		String url = betterNetworkTask.getUrl("/Home/GetLastPeriods?_="
				+ System.currentTimeMillis());

		String response = get(agentWxUid, url, true);
		if (response == null) {
			bankerResponse.setInfo(bankerError);
			return bankerResponse;
		}

		Gson gson = new Gson();
		LastPeriods lastPeriods = gson.fromJson(response, LastPeriods.class);
		if (reLogin(lastPeriods.getInfo())) {
			log.warn("getLastPeriod need re-login, agentWxUid {}, message {}",
					agentWxUid, lastPeriods.getInfo());

			agentMap.remove(agentWxUid);
			return getLastPeriod(agentWxUid);
		}

		if (lastPeriods.getPeriodsStatus() == 0) {
			bankerResponse.setInfo("现在还不能押粮，请稍后再试");
			return bankerResponse;
		}

		bankerResponse.setStatus(true);
		return bankerResponse;
	}

	private boolean login(String agentWxUid) {
		GameAgents gameAgent = gameAgentManager.getByWxUid(agentWxUid);
		if (gameAgent == null) {
			return false;
		}

		String loginPageHtml = get(agentWxUid, betterNetworkTask.getUrl(""),
				false);
		if (loginPageHtml == null) {
			return false;
		}

		try {
			Gson gson = new Gson();
			String url = betterNetworkTask.getUrl("/home/UserLogin");

			Map<String, String> map = new LinkedHashMap<>();
			map.put("UserName", gameAgent.getBankerUsername());
			map.put("Password", gameAgent.getBankerPassword());

			String response = post(agentWxUid, url, map, false);
			if (response == null || response.contains("错误")
					|| response.contains("失败")) {
				return false;
			}

			try {
				LoginSuccessResponse loginResponse = gson.fromJson(response,
						LoginSuccessResponse.class);

				log.info("Success to login banker, agentWxUid {}, message {}",
						agentWxUid, loginResponse.getMsg());
				return true;
			} catch (JsonSyntaxException e) {
				log.warn(
						"JsonSyntaxException in login, agentWxUid {}, response {}, reason: {}",
						agentWxUid, response, e.getMessage());
				return false;
			}
		} catch (Exception e) {
			log.error("Exception in login, agentWxUid {}, reason {}",
					agentWxUid, e.getMessage());
		}

		return false;
	}

	private String get(String agentWxUid, String url, boolean needLogin) {
		if (needLogin) {
			if (!agentMap.containsKey(agentWxUid) && !login(agentWxUid)) {
				return null;
			}
		} else {
			agentMap.remove(agentWxUid);

			BankerHttpEntity bankerHttpEntity = new BankerHttpEntity();
			agentMap.put(agentWxUid, bankerHttpEntity);
		}

		try {
			CloseableHttpClient httpClinet = agentMap.get(agentWxUid)
					.getHttpClient();
			HttpClientContext context = agentMap.get(agentWxUid).getContext();

			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse response = httpClinet.execute(httpGet,
					context);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.OK.value()
					&& statusCode != HttpStatus.ACCEPTED.value()) {
				log.warn(
						"Failed to get banker, agentWxUid {}, url {}, status {}",
						agentWxUid, url, statusCode);

				response.close();
				return null;
			}

			String responseEntity = EntityUtils.toString(response.getEntity(),
					"UTF-8");
			response.close();

			if (needLogin) {
				log.info(
						"Success to get banker, agentWxUid {}, url {}, responseEntity {}",
						agentWxUid, url, responseEntity);
			}

			return responseEntity;
		} catch (Exception e) {
			agentMap.remove(agentWxUid);
			log.error(
					"Exception in get banker, agentWxUid {}, url {}, reason {}",
					agentWxUid, url, e.getMessage());
		}

		return null;
	}

	private String post(String agentWxUid, String url, Map<String, String> map,
			boolean needLogin) {
		if (needLogin) {
			if (!agentMap.containsKey(agentWxUid) && !login(agentWxUid)) {
				return null;
			}
		}

		try {
			CloseableHttpClient httpClinet = agentMap.get(agentWxUid)
					.getHttpClient();
			HttpClientContext context = agentMap.get(agentWxUid).getContext();

			HttpPost httpPost = new HttpPost(url);
			httpPost.setEntity(getParam(map));

			CloseableHttpResponse response = httpClinet.execute(httpPost,
					context);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.OK.value()
					&& statusCode != HttpStatus.ACCEPTED.value()) {
				log.warn(
						"Failed to post banker, agentWxUid {}, url {}, map {}, status {}",
						agentWxUid, url, map, statusCode);
				response.close();

				return null;
			}

			String responseEntity = EntityUtils.toString(response.getEntity(),
					"UTF-8");
			response.close();

			if (needLogin && reLogin(responseEntity)) {
				log.warn(
						"Post banker need login, agentWxUid {}, url {}, map {}, responseEntity {}",
						agentWxUid, url, map, responseEntity);

				agentMap.remove(agentWxUid);
				return post(agentWxUid, url, map, needLogin);
			}

			log.info(
					"Success to post banker, agentWxUid {}, url {}, map {}, responseEntity {}",
					agentWxUid, url, map, responseEntity);

			return responseEntity;
		} catch (Exception e) {
			log.error(
					"Exception in post banker, agentWxUid {}, url {}, map {}, reason {}",
					agentWxUid, url, map, e.getMessage());

			if ("Read timed out".equals(e.getMessage())) {
				betterNetworkSchedule.pollNetworks();
			}
		}

		return null;
	}

	private UrlEncodedFormEntity getParam(Map<String, String> paramMap)
			throws UnsupportedEncodingException {
		List<NameValuePair> param = new ArrayList<>();

		paramMap.forEach((key, value) -> {
			param.add(new BasicNameValuePair(key, value));
		});

		return new UrlEncodedFormEntity(param, "UTF-8");
	}

	private boolean reLogin(String message) {
		boolean reLogin = false;
		if (StringUtils.isEmpty(message)) {
			return reLogin;
		}

		List<String> matchs = new ArrayList<>();
		matchs.add("重新登录");
		matchs.add("重新登陆");
		matchs.add("登录超时");
		matchs.add("登陆超时");
		matchs.add("账户名错误");

		for (String match : matchs) {
			if (message.contains(match)) {
				reLogin = true;
				break;
			}
		}

		return reLogin;
	}

}
