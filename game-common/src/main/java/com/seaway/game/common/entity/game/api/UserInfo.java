package com.seaway.game.common.entity.game.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfo {

	private boolean status;

	private String wxUid;

	private String wxNickName;

	private boolean wxSex;

	private String agentWxUid;

	private String roomName;

	private boolean tone;

	private boolean trialPlay;

	private int balance;

	private boolean enabelWeal;

	private int weal;

	private String currentLotterySequence;

	private int deadlineSeconds;

}
