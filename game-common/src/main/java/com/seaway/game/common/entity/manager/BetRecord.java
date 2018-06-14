package com.seaway.game.common.entity.manager;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BetRecord {

	private String type;
	
	private String betFormat;

	private int balanceGain;

	private int balanceRemain;

	private String date;

}
