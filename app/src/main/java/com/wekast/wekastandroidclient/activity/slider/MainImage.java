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
 * Created by RDL on 03.09.2016.
 */
public class MainImage extends Fragment {
    View view;
    private String imagePath;
    String mTitle;
    TextView title;
    ImageView image;

    public MainImage() {
        imagePath = DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY + CASH_DIRECTORY + ApplicationManager.getInstance().getCurrentSlide() + ".jpg";
        mTitle = "Slide " + ApplicationManager.getInstance().getCurrentSlide();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_main_image, null);
        title = (TextView)view.findViewById(R.id.current_slide_title);
        image = (ImageView)view.findViewById(R.id.current_slide_picture);
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
