package com.wekast.wekastandroidclient.commands;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ELAD on 10/28/2016.
 */

public class SlideCommand implements ICommand {

    private String slide;
    private String media;

    public SlideCommand(String slide, String media) {
        this.slide = slide;
        this.media = media;
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
        return "slide";
    }

    @Override
    public String getJsonString() {
        JSONObject jsonObject = new JSONObject();
        JSONObject args = new JSONObject();
        try {
            args.put("slide", slide);
            args.put("media", media);
            jsonObject.put("command", "slide");
            jsonObject.put("args", args);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

}
