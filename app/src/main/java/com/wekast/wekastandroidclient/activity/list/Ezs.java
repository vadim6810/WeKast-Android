package com.wekast.wekastandroidclient.activity.list;

import android.graphics.Bitmap;

import com.wekast.wekastandroidclient.model.EquationsBitmap;

import static com.wekast.wekastandroidclient.model.Utils.unZipPreview;

/**
 * Created by RDL on 28/02/2017.
 */

public class Ezs {

    private String title;
    private String path;
    private boolean isSelected;
    private boolean isPreview;
    private Bitmap logo;


    public Ezs(String title, String path, boolean isPreview) {
        this.title = title;
        this.path = path;
        this.isPreview = isPreview;
        this.isSelected = false;
        if (!isPreview) {
            byte[] image = unZipPreview(path);
            this.logo = EquationsBitmap.decodeSampledBitmapFromByte(image, 100, 80);
        } else {
            this.logo = EquationsBitmap.decodeSampledBitmapFromFile(path, 100, 80);
        }

    }

    public boolean isPreview() {
        return isPreview;
    }

    public void setPreview(boolean preview) {
        isPreview = preview;
    }

    public Bitmap getLogo() {
        return logo;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Ezs{" +
                "title='" + title + '\'' +
                ", path='" + path + '\'' +
                ", isSelected=" + isSelected +
                ", isPreview=" + isPreview +
                ", logo=" + logo +
                '}';
    }
}