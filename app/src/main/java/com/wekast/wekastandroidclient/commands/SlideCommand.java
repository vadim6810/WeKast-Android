package com.wekast.wekastandroidclient.commands;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ELAD on 10/28/2016.
 */

public class SlideCommand implements ICommand {

    private String slide;
    private String animation;
    private String video;
    private String audio;

    public SlideCommand(String slide, String animation, String video, String audio) {
        this.slide = slide;
        this.animation = animation;
        this.video = video;
        this.audio = audio;
    }

    @Override
    public String getJsonString() {
        JSONObject jsonObject = new JSONObject();
        JSONObject args = new JSONObject();
        try {
            args.put("slide", slide);
            args.put("animation", animation);
            args.put("video", video);
            args.put("audio", audio);
            jsonObject.put("command", "slide");
            jsonObject.put("args", args);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

}
