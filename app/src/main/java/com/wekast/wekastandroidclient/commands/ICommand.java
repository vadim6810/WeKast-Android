package com.wekast.wekastandroidclient.commands;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ELAD on 10/15/2016.
 */

public interface ICommand {
    Answer execute();
    void parseArgs(JSONObject args) throws JSONException;
}
