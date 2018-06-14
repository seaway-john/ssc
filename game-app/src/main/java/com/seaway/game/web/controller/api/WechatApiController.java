package com.seaway.game.web.controller.api;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.seaway.game.manager.api.WechatApiManager;

@Slf4j
@RestController
@RequestMapping("/api/wechat")
public class WechatApiController {

	private final WechatApiManager wechatApiManager;

	@Autowired
	public WechatApiController(WechatApiManager wechatApiManager) {
		this.wechatApiManager = wechatApiManager;
	}

	@RequestMapping(value = "/signature", method = RequestMethod.GET)
	public String signature(@RequestParam("signature") String signature,
			@RequestParam("timestamp") String timestamp,
			@RequestParam("nonce") String nonce,
			@RequestParam("echostr") String echostr) {
		log.info("Get wechat signature {}, timestamp {}, nonce {}, echostr {}",
				signature, timestamp, nonce, echostr);

		return echostr;
	}

	@RequestMapping(value = "/agent/login", method = RequestMethod.GET)
	public void agentLogin(HttpServletResponse response,
			@RequestParam("code") String code,
			@RequestParam("state") String state) {
		log.info("Get wechat agent login, code {}, state {}", code, state);

		String url = wechatApiManager.agentLogin(state, code);
		if (url == null) {
			url = "https://news.sina.cn/";
		}

		response.addHeader("location", url);
		response.setStatus(302);
	}

	@RequestMapping(value = "/user/login", method = RequestMethod.GET)
	public void userLogin(HttpServletResponse response,
			@RequestParam("code") String code,
			@RequestParam("state") String state) {
		log.info("Get wechat user login, code {}, state {}", code, state);

		String url = wechatApiManager.userLogin(state, code);
		if (url == null) {
			url = "https://news.sina.cn/";
		}

		response.addHeader("location", url);
		response.setStatus(302);
	}

}
