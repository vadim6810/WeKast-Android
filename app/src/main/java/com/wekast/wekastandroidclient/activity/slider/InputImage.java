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
import com.wekast.wekastandroidclient.model.EquationsBitmap;

/**
 * Created by RDL on 03.09.2016.
 */
public class InputImage extends Fragment {
    View view;
    private String imagePath;
    String mTitle;
    TextView title;
    ImageView image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_input_image, container, false);
        title = (TextView) view.findViewById(R.id.input_slide_title);
        image = (ImageView) view.findViewById(R.id.input_slide_picture);

        viewSlide();

        return view;
    }

    private void viewSlide() {
        if (imagePath != null) {
            Bitmap btm = EquationsBitmap.decodeSampledBitmapFromFile(imagePath, 400, 224);
            image.setImageBitmap(btm);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            title.setText(mTitle);
        }
    }

    public void setImagePath(FragmentSlider.Slide slide, boolean isshow) {
        if (slide != null) {
            imagePath = slide.getFilePath();
            mTitle = "Slide " + slide.getSlideNumber();
            if (isshow)
                viewSlide();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        view = null;
        System.gc();
    }
}
