package com.seaway.game.common.entity.topology;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BaseTopology {

    String icon;

    String color;

    String text;

    List<String> tags;

    String route;

    public BaseTopology(String text) {
        this.text = text;
    }

    public BaseTopology(String text, String route) {
        this.text = text;
        this.route = route;
    }

    public BaseTopology(String text, String route, String icon) {
        this.text = text;
        this.route = route;
        this.icon = icon;
    }

    public BaseTopology(String text, String route, String icon, String color) {
        this.text = text;
        this.route = route;
        this.icon = icon;
        this.color = color;
    }
}
