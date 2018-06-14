package com.seaway.game.admin.menu;

import com.seaway.game.admin.repository.impl.UserManager;
import com.seaway.game.common.entity.topology.BaseNodesTopology;
import com.seaway.game.common.entity.topology.BaseTopology;
import com.seaway.game.common.utils.Constants;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MenuManager {

	private final UserManager userManager;

	public MenuManager(UserManager userManager) {
		this.userManager = userManager;
	}

	public List<BaseTopology> getSidebarLeft(String username) {
		List<BaseTopology> menuList = new ArrayList<>();
		BaseNodesTopology menu;
		List<BaseTopology> nodes;

		boolean hasAdminRole = userManager.hasRole(username,
				Constants.ROLE_ADMIN);

		boolean hasRo = userManager.hasRole(username, Constants.ROLE_RO);

		// Administration
		if (hasAdminRole) {
			menu = new BaseNodesTopology("管理员", "admin",
					"zmdi zmdi-puzzle-piece");
			nodes = new ArrayList<>();

			nodes.add(new BaseTopology("管理用户", "admin.users"));
			nodes.add(new BaseTopology("数据库", "admin.database"));
			if (hasAdminRole) {
				nodes.add(new BaseTopology("系统", "admin.system"));
			}

			menu.setNodes(nodes);
			menuList.add(menu);
		}

		// Configuration
		if (hasRo) {
			menu = new BaseNodesTopology("游戏配置", "config", "zmdi zmdi-wrench");
			nodes = new ArrayList<>();

			nodes.add(new BaseTopology("配置", "config.setting"));
			nodes.add(new BaseTopology("线路", "config.network"));
			nodes.add(new BaseTopology("邀请二维码", "config.qrcode"));
			nodes.add(new BaseTopology("开奖", "config.lottery-win"));
			nodes.add(new BaseTopology("代理", "config.agents"));

			menu.setNodes(nodes);
			menuList.add(menu);
		}

		// Log
		if (hasRo) {
			menu = new BaseNodesTopology("统计", "statistics",
					"zmdi zmdi-collection-text");
			nodes = new ArrayList<>();

			nodes.add(new BaseTopology("代理流水", "statistics.agent-journal-account"));
			nodes.add(new BaseTopology("游戏登录记录", "statistics.game-login-report"));

			menu.setNodes(nodes);
			menuList.add(menu);
		}
		return menuList;
	}
}
