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
public class OutputImage extends Fragment {
    View view;
    private String imagePath;
    TextView title;
    TextView comments;
    ImageView image;
    private String mTitle;
    private String mComments;

    public OutputImage() {
        if(ApplicationManager.getInstance().getCurrentSlide()> 1) {

            imagePath = DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY + CASH_DIRECTORY + (ApplicationManager.getInstance().getCurrentSlide() -1) + ".jpg";
            mTitle = "Slide "+(ApplicationManager.getInstance().getCurrentSlide() - 1) ;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_output_image, null);
        title = (TextView)view.findViewById(R.id.output_slide_title);
        image = (ImageView)view.findViewById(R.id.output_slide_picture);
        if(imagePath != null){
            Bitmap btm = EquationsBitmap.decodeSampledBitmapFromFile(imagePath, 400, 224);
            image.setImageBitmap(btm);
            title.setText(mTitle);
        }

        return view;
    }

    public void setImagePath(FragmentSlider.Slide slide){
        if(slide == null){


        }else{
            imagePath = slide.getFilePath();
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
