package com.seaway.game.common.entity.game.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentInfo {

	private boolean status;

	private String wxUid;

	private String wxNickName;

	private boolean wxSex;

	private int balance;

	private String roomName;

	private String currentLotterySequence;

	private int deadlineSeconds;

}
