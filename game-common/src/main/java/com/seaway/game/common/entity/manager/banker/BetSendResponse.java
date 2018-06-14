package com.seaway.game.common.entity.manager.banker;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BetSendResponse {

	private boolean status;

	private String info;

	@SerializedName("CmdObject")
	private MemberCredit cmdObject;

	private int isStopNum;

}
