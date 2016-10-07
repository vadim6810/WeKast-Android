package com.wekast.wekastandroidclient.activity.slider;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.EquationsBitmap;

/**
 * Created by RDL on 03.09.2016.
 */
public class MainImage extends Fragment {
    View view;
    private String imagePath;
    String mTitle;
    TextView title;
    ImageView image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_main_image, null);
        title = (TextView)view.findViewById(R.id.current_slide_title);
        image = (ImageView)view.findViewById(R.id.current_slide_picture);
        if(imagePath != null){
            Bitmap btm = EquationsBitmap.decodeSampledBitmapFromFile(imagePath, 400, 224);
            Log.d("MainImage = ", String.valueOf(btm.getWidth()) + ":" + btm.getHeight());
            image.setImageBitmap(btm);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            title.setText(mTitle);
        }
        return view;
    }

    public void setImagePath(FragmentSlider.Slide slide, int size){
        if(slide != null){
            imagePath = slide.getFilePath();
            mTitle = "Slide " + slide.getSlideNumber() + "/" + size;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        view = null;
        System.gc();
    }
}
