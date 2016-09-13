package com.wekast.wekastandroidclient.model.core;


import com.wekast.wekastandroidclient.model.Utils;

import java.util.Observable;


/**
 * Created by Gregory on 14.06.2014.
 */
public class ApplicationManager extends Observable {
    public static ApplicationManager instance = null;
//    private  int connectionState = SERVER_DISCONNECTED;
//    private  int uploadPresentationState = PRESENTATION_UNSELECTED;
    private  int currentSlide = 1;
    private  int slidesSize;
    private String currentPresentationName = null;
//    private  int updateServerState = CHECK_SERVER_VERSION;
//    private  int uiState = PRESENTATION_LIST;
    private String uploadStatus;

    private ApplicationManager(){}

    public static ApplicationManager getInstance() {
        if (instance == null) {
            instance = new ApplicationManager();
        }
        return instance;
    }

//    public int getConnectionState() {
//        return connectionState;
//    }

//    public void setConnectionState(int connectionState) {
//        this.connectionState = connectionState;
//        super.setChanged();
//        super.notifyObservers(CONNECTION_STATE_CHANGED);
//    }

//    public int getUploadPresentationState() {
//        return uploadPresentationState;
//    }

//    public void setUploadPresentationState(int uploadPresentationState) {
//        this.uploadPresentationState = uploadPresentationState;
//        super.setChanged();
//        super.notifyObservers(UPLOAD_PRESENTATION_STATE_CHANGED);
//    }

    public int getCurrentSlide() {
        return currentSlide;
    }

    public void setCurrentSlide(int currentSlide) {
        this.currentSlide = currentSlide;
        super.setChanged();
        super.notifyObservers(Utils.CURRENT_SLIDE_STATE_CHANGED);
    }

    public String getCurrentPresentationName() {
        return currentPresentationName;
    }

    public void setCurrentPresentationName(String currentPresentationName) {
        this.currentPresentationName = currentPresentationName;
        currentSlide = 1;
        super.setChanged();
        super.notifyObservers(Utils.CURRENT_PRESENTATION_STATE_CHANGED);
    }

//    public int getUpdateServerState() {
//        return updateServerState;
//    }
//
//    public void setUpdateServerState(int updateServerState) {
//        this.updateServerState = updateServerState;
//        super.setChanged();
//        super.notifyObservers(UPDATE_SERVER_STATE_CHANGED);
//    }

//    public int getUiState() {
//        return uiState;
//    }
//
//    public void setUiState(int uiState) {
//        this.uiState = uiState;
//        super.setChanged();
//        super.notifyObservers(UI_STATE_CHANGED);
//    }

    public int getSlidesSize() {
        return slidesSize;
    }

    public void setSlidesSize(int slidesSize) {
        this.slidesSize = slidesSize;
    }

    public String getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(String uploadStatus) {
        this.uploadStatus = uploadStatus;
        super.setChanged();
        super.notifyObservers(Utils.UPLOAD_PRESENTATION_STATUS);
    }
}
