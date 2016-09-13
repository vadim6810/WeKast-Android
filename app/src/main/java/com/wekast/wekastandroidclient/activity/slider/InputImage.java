package com.wekast.wekastandroidclient.activity.slider;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.activity.FragmentSlider;
import com.wekast.wekastandroidclient.model.EquationsBitmap;
import com.wekast.wekastandroidclient.model.core.ApplicationManager;

import static com.wekast.wekastandroidclient.model.Utils.*;


/**
 * Created by Gregory on 04.05.2014.
 */
public class InputImage extends Fragment {
    View view;
    private String imagePath;
    String mTitle;
    TextView title;
    ImageView image;
    public InputImage() {
        if(ApplicationManager.getInstance().getCurrentSlide() < ApplicationManager.getInstance().getSlidesSize()) {

            imagePath = DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY + CASH_DIRECTORY + (ApplicationManager.getInstance().getCurrentSlide()+1) + ".jpg";
            mTitle = "Slide "+(ApplicationManager.getInstance().getCurrentSlide()+1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_input_image, null);
        title = (TextView)view.findViewById(R.id.input_slide_title);
        image = (ImageView)view.findViewById(R.id.input_slide_picture);
        if(imagePath != null){
            Bitmap btm = EquationsBitmap.decodeSampledBitmapFromFile(imagePath, 400, 224);
            image.setImageBitmap(btm);
            title.setText(mTitle);
        }
        return view;
    }

    public void setImagePath(FragmentSlider.Slide slide){
        if(slide == null){
            imagePath = null;
        }else{
            imagePath = DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY + CASH_DIRECTORY + slide.getSlideNumber() + ".jpg";
            mTitle = "Slide" + slide.getSlideNumber();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        view = null;
        System.gc();
    }
}
