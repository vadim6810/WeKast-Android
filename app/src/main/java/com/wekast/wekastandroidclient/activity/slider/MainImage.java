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
    String mTitleChid;
    TextView title;
    TextView titleChid;
    ImageView image;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main_image, null);
        title = (TextView) view.findViewById(R.id.current_slide_title);
        titleChid = (TextView) view.findViewById(R.id.current_chid_title);
        image = (ImageView) view.findViewById(R.id.current_slide_picture);

        viewSlide();

        return view;
    }

    private void viewSlide() {
        if (imagePath != null) {
            Bitmap btm = EquationsBitmap.decodeSampledBitmapFromFile(imagePath, 400, 224);
            Log.d("MainImage = ", String.valueOf(btm.getWidth()) + ":" + btm.getHeight());
            image.setImageBitmap(btm);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            title.setText(mTitle);
            titleChid.setText(mTitleChid);
        }
    }

    public void setImagePath(FragmentSlider.Slide slide, int size, int currentChID, boolean isshow) {
        if (slide != null) {
            imagePath = slide.getFilePath();
            mTitle = "Slide " + slide.getSlideNumber() + "/" + size;
            mTitleChid = "animation " + currentChID + "/" + slide.getChID().size();
            if (isshow)
                viewSlide();
        }
    }

    public void setTitleChid(int currentChID, int countChid){
        mTitleChid = "animation " + currentChID + "/" + countChid;
        titleChid.setText(mTitleChid);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        view = null;
        System.gc();
    }
}
