package com.wekast.wekastandroidclient.activity.slider;

import java.util.ArrayList;

/**
 * Created by RDL on 28/02/2017.
 */

public class Slide {
    private String title;
    private int slideNumber;
    private String comments;
    private String filePath;
    private ArrayList<Integer> chID = new ArrayList<>();

    public Slide(String title, int slideNumber, String comments, String filePath, ArrayList<Integer> chID) {
        this.title = title;
        this.slideNumber = slideNumber;
        this.comments = comments;
        this.filePath = filePath;
        this.chID = chID;
    }

    public ArrayList<Integer> getChID() {
        return chID;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getTitle() {
        return title;
    }

    public int getSlideNumber() {
        return slideNumber;
    }

    public String getComments() {
        return comments;
    }

    @Override
    public String toString() {
        return "Slide{" +
                "title='" + title + '\'' +
                ", slideNumber=" + slideNumber +
                ", comments='" + comments + '\'' +
                ", filePath='" + filePath + '\'' +
                ", chID=" + chID +
                '}';
    }
}
