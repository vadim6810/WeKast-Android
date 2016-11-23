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
    private View view;
    private String imagePath;
    private boolean mContentLoaded;
    private int mShortAnimationDuration;
    private ImageView mImage1;
    private ImageView mImage2;
    private String mTitle;
    private String mTitleChid;
    private TextView title;
    private TextView titleChid;

    public MainImage() {
        mShortAnimationDuration = 500;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main_image, container, false);
        title = (TextView) view.findViewById(R.id.current_slide_title);
        titleChid = (TextView) view.findViewById(R.id.current_chid_title);
        mImage1 = (ImageView) view.findViewById(R.id.current_slide_picture);
        mImage2 = (ImageView) view.findViewById(R.id.current_slide_picture2);
        viewSlide();
        return view;
    }

    private void viewSlide() {
        if (imagePath != null) {
            Bitmap btm = EquationsBitmap.decodeSampledBitmapFromFile(imagePath, 400, 224);
            Log.d("MainImage = ", String.valueOf(btm.getWidth()) + ":" + btm.getHeight());
            if (!mContentLoaded) {
                mImage1.setImageBitmap(btm);
            }
            else {
                mImage2.setImageBitmap(btm);
            }
            mContentLoaded = !mContentLoaded;
            crossfader(mContentLoaded);

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

    public void setTitleChid(int currentChID, int countChid) {
        mTitleChid = "animation " + currentChID + "/" + countChid;
        titleChid.setText(mTitleChid);
    }

    private void crossfader(boolean contentLoaded) {
        final ImageView showView = contentLoaded ? mImage1 : mImage2;
        final ImageView hideView = contentLoaded ? mImage2 : mImage1;

        showView.setAlpha(0f);

        showView.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration);

        hideView.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        view = null;
        System.gc();
    }
}
