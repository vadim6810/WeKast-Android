package com.wekast.wekastandroidclient.commands;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ELAD on 11/12/2016.
 */

public class StopCommand implements ICommand {

    @Override
    public String getJsonString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("command", "stop");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
