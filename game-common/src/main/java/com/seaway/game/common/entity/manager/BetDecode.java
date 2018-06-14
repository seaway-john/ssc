package com.seaway.game.common.entity.manager;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BetDecode {

	private boolean status;

	private String message;

	private String format;

	private int money;

	private int size;

	private Map<String, List<String>> data;

}
