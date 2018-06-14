package com.seaway.game.web.controller.api;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/debug")
public class DebugApiController {

	@RequestMapping(value = "/302", method = RequestMethod.GET)
	public void get302(HttpServletRequest request, HttpServletResponse response) {
		log.info("get302");

		response.addHeader("location", "http://www.shengyuncai.cn/debug.html");

		response.addCookie(new Cookie("name", "Seaway.John"));
		response.addCookie(new Cookie("uid", "o_ZWp0oA6h1awkTUhxc1teDiIOn0"));
		response.addCookie(new Cookie("sid", "081v4qHl0a6SJl1vVvEl0P7FHl0v4qHc"));

		response.setStatus(302);
	}

	@RequestMapping(value = "/check", method = RequestMethod.GET)
	public boolean checkCookie(HttpServletRequest request,
			HttpServletResponse response) {
		log.info("checkCookie");

		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				log.info("Cookie name: " + cookie.getName() + ", value: "
						+ cookie.getValue());
			}
		}

		String browserAgent = request.getHeader("User-Agent");
		if (StringUtils.isEmpty(browserAgent)
				|| !browserAgent.contains("MicroMessenger")) {
			return false;
		}

		return true;
	}

}
