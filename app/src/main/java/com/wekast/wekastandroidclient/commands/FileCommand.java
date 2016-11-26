package com.wekast.wekastandroidclient.commands;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ELAD on 10/28/2016.
 */

public class FileCommand implements ICommand  {

    private String fileSize;

    public FileCommand(String fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public Answer execute() {
        return null;
    }

    @Override
    public void parseArgs(JSONObject jsonObject) throws JSONException {

    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public String getJsonString() {
        JSONObject jsonObject = new JSONObject();
        JSONObject args = new JSONObject();
        try {
            args.put("fileSize", fileSize);
            jsonObject.put("command", "file");
            jsonObject.put("args", args);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
