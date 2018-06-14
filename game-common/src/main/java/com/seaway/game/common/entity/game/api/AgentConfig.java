package com.seaway.game.common.entity.game.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentConfig {

	private String wxUsername;

	private String bankerUsername;

	private String bankerPassword;

	private String roomName;

	private int trialPlayBalance;

	private int minEachBetInvest;

	private int maxEachBetInvest;

	private float rebateRate;

	private int wealRate;

	private int limit2;

	private int limit3;

	private int limit4;

}
