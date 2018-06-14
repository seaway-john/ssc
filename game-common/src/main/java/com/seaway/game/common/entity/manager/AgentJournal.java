package com.seaway.game.common.entity.manager;

import com.seaway.game.common.entity.mysql.GameAgents;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentJournal extends GameAgents {

	private float rebateRate;

	private float betRebate;

	public AgentJournal(String wxUid, String wxSid) {
		super(wxUid, wxSid);
	}

}
