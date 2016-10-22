package com.wekast.wekastandroidclient.commands;

/**
 * Created by Samanta on 17.10.2016.
 */

public class ConfigAnswer extends Answer {
    public ConfigAnswer() {
        setType("config");
        add("message", "ok");
    }
}
