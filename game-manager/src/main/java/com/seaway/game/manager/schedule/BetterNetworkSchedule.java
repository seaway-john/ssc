package com.seaway.game.manager.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.seaway.game.common.entity.mysql.GameConfig;
import com.seaway.game.common.utils.TimeDuration;
import com.seaway.game.manager.repository.impl.GameConfigManager;

@Slf4j
@Component
public class BetterNetworkSchedule implements
		ApplicationListener<ContextRefreshedEvent> {

	private final GameConfigManager gameConfigManager;

	private final BetterNetworkTask betterNetworkTask;

	private static long pollTs = 0;

	@Autowired
	public BetterNetworkSchedule(GameConfigManager gameConfigManager,
			BetterNetworkTask betterNetworkTask) {
		this.gameConfigManager = gameConfigManager;
		this.betterNetworkTask = betterNetworkTask;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		pollNetworks();
	}

	public void pollNetworks() {
		long currentTs = System.currentTimeMillis();
		if (currentTs - pollTs < 60 * 1000) {
			return;
		}
		pollTs = currentTs;

		TimeDuration td = new TimeDuration();

		List<String> attributes = new ArrayList<>();
		for (int i = 1; i <= 9; i++) {
			attributes.add("network" + i);
		}

		Map<String, GameConfig> networkMap = gameConfigManager
				.getMap(attributes);
		if (networkMap.isEmpty()) {
			return;
		}

		CountDownLatch latch = new CountDownLatch(networkMap.size());
		CloseableHttpClient httpClient = generateClient();
		RequestConfig config = generateConfig();
		betterNetworkTask.clearMap();

		networkMap.forEach((key, value) -> {
			betterNetworkTask.getNetworkTask(httpClient, config, value, latch);
		});

		try {
			if (latch.await(10, TimeUnit.SECONDS)) {
			} else {
			}
		} catch (InterruptedException e) {
			log.error("Exception in pollNetworks, cost {} ms, reason {}",
					td.stop(), e.getMessage());
		}

		betterNetworkTask.generateBetter();
	}

	private CloseableHttpClient generateClient() {
		return HttpClientBuilder.create()
				.setDefaultRequestConfig(generateConfig()).build();
	}

	private RequestConfig generateConfig() {
		return RequestConfig.custom().setConnectTimeout(3 * 1000)
				.setSocketTimeout(3 * 1000)
				.setConnectionRequestTimeout(3 * 1000)
				.setRedirectsEnabled(true).build();
	}

}
