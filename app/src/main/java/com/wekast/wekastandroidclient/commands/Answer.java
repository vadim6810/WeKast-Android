package com.wekast.wekastandroidclient.commands;

import org.json.JSONException;
import org.json.JSONObject;


public abstract class Answer extends JSONObject {
    Answer() {
        add("device", "android");
    }

    void add(String name, Object value) {
        try {
            put(name, value);
        } catch (JSONException ignored) {}
    }

    void setType(String type) {
        add("type", type);
    }
}
