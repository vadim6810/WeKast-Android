package com.wekast.wekastandroidclient.commands;



import com.wekast.wekastandroidclient.controllers.CommandController;
import com.wekast.wekastandroidclient.controllers.WifiController;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ELAD on 10/15/2016.
 */

public class ConfigCommand implements ICommand {

    private CommandController controller;

    public ConfigCommand(CommandController controller) {
        this.controller = controller;
    }

    private String ssid;
    private String password;

    @Override
    public Answer execute() {
        WifiController wifiController = controller.getService().getWifiController();
        wifiController.saveWifiConfig(ssid, password);
        wifiController.startConnection();
        wifiController.changeState(WifiController.WifiState.WIFI_STATE_CONNECT);
        return new ConfigAnswer();
    }

    @Override
    public void parseArgs(JSONObject jsonObject) throws JSONException {
        ssid = jsonObject.getString("ssid");
        password = jsonObject.getString("password");
    }

}
