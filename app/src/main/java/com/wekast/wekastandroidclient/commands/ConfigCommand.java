package com.wekast.wekastandroidclient.commands;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ELAD on 10/15/2016.
 */

public class ConfigCommand implements ICommand {

    private String ssid;
    private String password;

    public ConfigCommand(String ssid, String password) {
        this.ssid = ssid;
        this.password = password;
    }

    @Override
    public String getJsonString() {
        JSONObject jsonObject = new JSONObject();
        JSONObject args = new JSONObject();
        try {
            args.put("password", password);
            args.put("ssid", ssid);
            jsonObject.put("command", "config");
            jsonObject.put("args", args);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

}
