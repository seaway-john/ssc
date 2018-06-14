package com.seaway.game.common.entity.topology;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BaseNodesTopology extends BaseTopology {

    private List<BaseTopology> nodes;

    public BaseNodesTopology(String text) {
        super(text);
    }

    public BaseNodesTopology(String text, String route) {
        super(text, route);
    }

    public BaseNodesTopology(String text, List<BaseTopology> nodes) {
        super(text);
        this.nodes = nodes;
    }

    public BaseNodesTopology(String text, String route, String icon) {
        super(text, route, icon);
    }

    public BaseNodesTopology(String text, String route, List<BaseTopology> nodes) {
        super(text, route);
        this.nodes = nodes;
    }

    public BaseNodesTopology(String text, String route, String icon,
                             String color) {
        super(text, route, icon, color);
    }
}
