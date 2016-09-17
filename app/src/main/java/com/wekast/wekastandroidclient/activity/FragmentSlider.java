package com.wekast.wekastandroidclient.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.activity.slider.CommentsFragment;
import com.wekast.wekastandroidclient.activity.slider.InputImage;
import com.wekast.wekastandroidclient.activity.slider.MainImage;
import com.wekast.wekastandroidclient.activity.slider.OutputImage;
import com.wekast.wekastandroidclient.model.core.ApplicationManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import static com.wekast.wekastandroidclient.model.Utils.*;



/**
 * Created by RDL on 03.09.2016.
 */
public class FragmentSlider extends Fragment implements View.OnTouchListener {
    float y;
    float y1;
    View view;
    InputImage inputImage;
    MainImage mainImage;
    OutputImage outputImage;
    FrameLayout inputSlideContainer;
    FrameLayout currentSlideContainer;
    FrameLayout outputSlideContainer;
    FrameLayout commentsContainer;
    FrameLayout commentsFullSize;
    CommentsFragment commentsFragment;
    FragmentTransaction tr;
    int currentSlide = 0;
    ArrayList<Slide> slidesList;
    final Handler handler;
    ApplicationManager applicationManager;
    boolean fullCommentVisible = false;
    TextView commentsFullSizeText;


    public FragmentSlider() {

        slidesList = new ArrayList<>();
        commentsFragment = new CommentsFragment();
        handler = new Handler();
        applicationManager = ApplicationManager.getInstance();
        currentSlide = applicationManager.getCurrentSlide()-1;


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_slider, null);
        inputSlideContainer = (FrameLayout)view.findViewById(R.id.input_slide_container);
        currentSlideContainer = (FrameLayout)view.findViewById(R.id.current_slide_container);
        outputSlideContainer = (FrameLayout)view.findViewById(R.id.output_slide_container);
        commentsContainer = (FrameLayout)view.findViewById(R.id.comments_container);
        commentsFullSize = (FrameLayout)view.findViewById(R.id.comments_full_size);
        commentsFullSizeText = (TextView)view.findViewById(R.id.comments_full_size_text);
        createWorkArray();
        applicationManager.setSlidesSize(slidesList.size());

        inputImage = new InputImage();
        mainImage = new MainImage();
        outputImage = new OutputImage();

        commentsFragment.setComments(slidesList.get(currentSlide));

        tr = getFragmentManager().beginTransaction();
        if(currentSlide > 0)
            tr.add(R.id.output_slide_container, outputImage);
        tr.add(R.id.input_slide_container, inputImage);
        tr.add(R.id.current_slide_container, mainImage);
        tr.add(R.id.comments_container,commentsFragment);
        tr.addToBackStack(null);
        tr.commit();

        view.setOnTouchListener(this);
        commentsContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                commentsFullSizeText.setText(commentsFragment.getCurrentComments());
                commentsFullSize.setVisibility(View.VISIBLE);

                fullCommentVisible = true;
                return true;
            }
        });
        commentsContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // We're only interested in anything if our speak button is currently pressed.
                    if(fullCommentVisible){
                        commentsFullSize.setVisibility(View.GONE);
                        fullCommentVisible = false;
                    }
                }
                return false;
            }
        });
        return view;
    }
    public void createWorkArray(){
        File targetDirectory = new File(DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY + CASH_DIRECTORY);
        FilenameFilter fileFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {

                return filename.contains(".jpg");
            }
        };
        for(int i = 0;i<targetDirectory.listFiles(fileFilter).length;i++){
            slidesList.add(new Slide("",i+1,getComments(i+1),DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY + CASH_DIRECTORY+(i+1)+".jpg"));
        }
    }

    private String getComments(int commentsNumber){
        String comments = "";
        File file = new File(DEFAULT_PATH_DIRECTORY + WORK_DIRECTORY + CASH_DIRECTORY,"s"+commentsNumber+".txt");

        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            comments = text.toString();
        }catch (IOException ex){
            ex.printStackTrace();
        }
        return comments;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: // нажатие
                y = motionEvent.getY();
                break;
            case MotionEvent.ACTION_MOVE: // движение

                break;
            case MotionEvent.ACTION_UP: // отпускание

                y1 = motionEvent.getY();
                if((y-y1) > 0 && (y-y1)>100){
                    nextSlide();
                }else{
                    if(((y-y1)*-1)>100)
                        prevSlide();
                }
                break;
            case MotionEvent.ACTION_CANCEL:

                break;
        }
        return true;
    }

    public void prevSlide() {

        if(currentSlide>0){
            currentSlide  = currentSlide -1;
            applicationManager.setCurrentSlide(currentSlide+1);
            if(currentSlide == 0){
                inputImage.setImagePath(slidesList.get(currentSlide+1));
                mainImage.setImagePath(slidesList.get(currentSlide));
                commentsFragment.setComments(slidesList.get(currentSlide));
                FragmentTransaction tr = getFragmentManager().beginTransaction();
                tr.remove(inputImage);
                tr.remove(mainImage);
                tr.remove(commentsFragment);
                tr.remove(outputImage);
                tr.addToBackStack(null);
                tr.commit();
                tr = getFragmentManager().beginTransaction();
                tr.replace(R.id.input_slide_container, inputImage);
                tr.replace(R.id.current_slide_container, mainImage);
                tr.replace(R.id.comments_container,commentsFragment);
                tr.addToBackStack(null);
                tr.commit();

            }else{
                inputImage.setImagePath(slidesList.get(currentSlide+1));
                mainImage.setImagePath(slidesList.get(currentSlide));
                outputImage.setImagePath(slidesList.get(currentSlide-1));
                commentsFragment.setComments(slidesList.get(currentSlide));
                FragmentTransaction tr = getFragmentManager().beginTransaction();
                tr.remove(inputImage);
                tr.remove(mainImage);
                tr.remove(commentsFragment);
                tr.remove(outputImage);
                tr.addToBackStack(null);
                tr.commit();
                tr = getFragmentManager().beginTransaction();
                tr.add(R.id.input_slide_container, inputImage);
                tr.add(R.id.current_slide_container, mainImage);
                tr.add(R.id.comments_container,commentsFragment);
                tr.add(R.id.output_slide_container,outputImage);
                tr.addToBackStack(null);
                tr.commit();
            }

        }

    }

    public void nextSlide() {

        if(currentSlide<slidesList.size()-1){
            currentSlide = currentSlide + 1;
            applicationManager.setCurrentSlide(currentSlide+1);
            if(currentSlide == slidesList.size()-1){
                outputImage.setImagePath(slidesList.get(currentSlide-1));
                mainImage.setImagePath(slidesList.get(currentSlide));
                commentsFragment.setComments(slidesList.get(currentSlide));
                FragmentTransaction tr = getFragmentManager().beginTransaction();
                tr.remove(outputImage);
                tr.remove(mainImage);
                tr.remove(commentsFragment);
                tr.remove(inputImage);
                tr.addToBackStack(null);
                tr.commit();
                tr = getFragmentManager().beginTransaction();
                tr.add(R.id.output_slide_container, outputImage);
                tr.add(R.id.current_slide_container, mainImage);
                tr.add(R.id.comments_container,commentsFragment);
                tr.addToBackStack(null);
                tr.commit();


            }else{
                inputImage.setImagePath(slidesList.get(currentSlide+1));
                mainImage.setImagePath(slidesList.get(currentSlide));
                outputImage.setImagePath(slidesList.get(currentSlide-1));
                commentsFragment.setComments(slidesList.get(currentSlide));
                FragmentTransaction tr = getFragmentManager().beginTransaction();
                tr.remove(inputImage);
                tr.remove(mainImage);
                tr.remove(commentsFragment);
                tr.remove(outputImage);
                tr.addToBackStack(null);
                tr.commit();
                tr = getFragmentManager().beginTransaction();
                tr.add(R.id.input_slide_container, inputImage);
                tr.add(R.id.current_slide_container, mainImage);
                tr.add(R.id.comments_container,commentsFragment);
                tr.add(R.id.output_slide_container,outputImage);
                tr.addToBackStack(null);
                tr.commit();
            }
        }
    }


    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("Slider", "onDestroyView");
        view = null;
        System.gc();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }
    public class Slide {
        private String title;
        private int slideNumber;
        private String comments;
        private String filePath;

        public Slide(String title, int slideNumber, String comments, String filePath) {
            this.title = title;
            this.slideNumber = slideNumber;
            this.comments = comments;
            this.filePath = filePath;
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

    }
}
