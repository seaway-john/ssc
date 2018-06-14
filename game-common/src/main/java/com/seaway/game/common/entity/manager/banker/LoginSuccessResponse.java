package com.seaway.game.common.entity.manager.banker;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginSuccessResponse {

	private String msg;

	private String url;

	private int type;

	private Map<String, Object> info;

}
