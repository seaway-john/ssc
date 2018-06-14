package com.seaway.game.common.entity.mysql;

import java.util.Date;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "game_user_login_report")
public class GameUserLoginReport {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private String wxUid;

	private String wxSid;

	private String wxNickName;

	private boolean agent;

	private String roomWxRid;

	private String roomName;

	private Date created;

	private Date lastUpdate;

	private GameUserLoginReport() {
		this.agent = false;
		this.created = new Date();
	}

	public GameUserLoginReport(String wxUid, String wxSid, String wxNickName) {
		this();

		this.wxUid = wxUid;
		this.wxSid = wxSid;
		this.wxNickName = wxNickName;
	}

}
