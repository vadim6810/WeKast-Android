package com.wekast.wekastandroidclient.commands;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ELAD on 11/14/2016.
 */

public class PingCommand implements ICommand {

    @Override
    public Answer execute() {
        return null;
    }

    @Override
    public void parseArgs(JSONObject jsonObject) throws JSONException {
    }

    @Override
    public String getCommand() {
        return "ping";
    }

    @Override
    public String getJsonString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("command", "ping");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
