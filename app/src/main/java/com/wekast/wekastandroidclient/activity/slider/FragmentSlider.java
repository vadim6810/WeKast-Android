package com.wekast.wekastandroidclient.activity.slider;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.services.DongleService;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import static com.wekast.wekastandroidclient.model.Utils.*;
import static java.lang.Math.abs;

/**
 * Created by RDL on 03.09.2016.
 */
public class FragmentSlider extends Fragment implements View.OnTouchListener {
    float startY;
    float stopY;
    float startX;
    float stopX;
    float resY;
    float resX;
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
    int currentChID = 1;
    ArrayList<Slide> slidesList;
    ArrayList<Integer> chID = new ArrayList<>();
    private int slideNumber;
    private String comments;
    private String filePath;
    boolean fullCommentVisible = false;
    TextView commentsFullSizeText;

    public FragmentSlider() {
        slidesList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_slider, container, false);
        inputSlideContainer = (FrameLayout) view.findViewById(R.id.input_slide_container);
        currentSlideContainer = (FrameLayout) view.findViewById(R.id.current_slide_container);
        outputSlideContainer = (FrameLayout) view.findViewById(R.id.output_slide_container);
        commentsContainer = (FrameLayout) view.findViewById(R.id.comments_container);
        commentsFullSize = (FrameLayout) view.findViewById(R.id.comments_full_size);
        commentsFullSizeText = (TextView) view.findViewById(R.id.comments_full_size_text);
        createWorkArray();

        inputImage = new InputImage();
        mainImage = new MainImage();
        outputImage = new OutputImage();
        commentsFragment = new CommentsFragment();

        inputImage.setImagePath(slidesList.get(currentSlide + 1), false);
        mainImage.setImagePath(slidesList.get(currentSlide), slidesList.size(), currentChID, false);
        commentsFragment.setComments(slidesList.get(currentSlide), false);
        changeSlideToDongle(currentSlide + 1, currentChID);

        tr = getFragmentManager().beginTransaction();
        tr.add(R.id.input_slide_container, inputImage);
        tr.add(R.id.current_slide_container, mainImage);
        tr.add(R.id.output_slide_container, outputImage);
        tr.add(R.id.comments_container, commentsFragment);
        tr.commit();

        outputSlideContainer.setVisibility(View.INVISIBLE);


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
                    if (fullCommentVisible) {
                        commentsFullSize.setVisibility(View.GONE);
                        fullCommentVisible = false;
                    }
                }
                return false;
            }
        });
        return view;
    }

    public void createWorkArray() {
        try {
            XmlPullParser parser = prepareXpp();
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (parser.getEventType()) {
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("slide")) {
                            slideNumber = Integer.parseInt(parser.getAttributeValue(0));
                            comments = parser.getAttributeValue(2);
                            filePath = CASH_ABSOLUTE_PATH + parser.getAttributeValue(1);
                        }
                        if (parser.getName().equals("animation")) {
                            chID.add(Integer.parseInt(parser.getAttributeValue(0)));
                        }

                        if (parser.getName().equals("media")) {
                            chID.add(Integer.parseInt(parser.getAttributeValue(2)));
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("slide")) {
                            Slide slide = new Slide("", slideNumber, comments, filePath, chID);
                            Log.d("XML parser: ", slide.toString());
                            slidesList.add(slide);
                            chID = new ArrayList<>();
                        }
                        break;
                }
                parser.next();
            }
        } catch (Throwable t) {
            Log.d("Error XML parser: ", t.toString());
        }
    }

    XmlPullParser prepareXpp() throws XmlPullParserException {
        // получаем фабрику
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        // включаем поддержку namespace (по умолчанию выключена)
        factory.setNamespaceAware(true);
        // создаем парсер
        XmlPullParser xpp = factory.newPullParser();
        // даем парсеру на вход Reader
        try {
            xpp.setInput(new FileInputStream(infoXML), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return xpp;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: // нажатие
                startY = motionEvent.getY();
                startX = motionEvent.getX();
                break;
            case MotionEvent.ACTION_MOVE: // движение
                break;
            case MotionEvent.ACTION_UP: // отпускание
                stopY = motionEvent.getY();
                stopX = motionEvent.getX();
                resY = (startY - stopY);
                resX = (startX - stopX);
                if (abs(resY) > abs(resX))
                    actionY(resY);
                else actionX(resX);
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    private void actionX(float resX) {
        if (resX > 100) {
            rightSlide();
        } else {
            if (resX < -100)
                leftSlide();
        }
    }

    private void leftSlide() {
        if (currentChID > 1) {
            currentChID--;
            changeSlideToDongle(currentSlide + 1, currentChID);
        }
        mainImage.setTitleChid(currentChID, slidesList.get(currentSlide).getChID().size());
    }

    private void rightSlide() {
        if (currentChID < slidesList.get(currentSlide).getChID().size()) {
            currentChID++;
            changeSlideToDongle(currentSlide + 1, currentChID);
        }
        mainImage.setTitleChid(currentChID, slidesList.get(currentSlide).getChID().size());
    }

    private void actionY(float resY) {
        if (resY > 100) {
            nextSlide();
        } else {
            if (resY < -100)
                prevSlide();
        }
    }

    private void changeSlideToDongle(int currentSlide, int currentChID) {
        Log.d("changeSlideToDongle", currentSlide + "|" + currentChID);
        Intent i = new Intent(getActivity(), DongleService.class);
        i.putExtra("command", SLIDE);
        i.putExtra("SLIDE", Integer.toString(currentSlide));
        i.putExtra("ANIMATION", Integer.toString(currentChID));
        // TODO: pass here strings animation, video and audio
        i.putExtra("VIDEO", "");
        i.putExtra("AUDIO", "");
        getActivity().startService(i);
    }

    public void prevSlide() {
        if (currentSlide > 0) {
            currentSlide = currentSlide - 1;
            currentChID = 1;
            changeSlideToDongle(currentSlide + 1, currentChID);
            if (currentSlide == 0) {
                outputSlideContainer.setVisibility(View.INVISIBLE);
                inputImage.setImagePath(slidesList.get(currentSlide + 1), true);
                mainImage.setImagePath(slidesList.get(currentSlide), slidesList.size(), currentChID, true);
                commentsFragment.setComments(slidesList.get(currentSlide), true);
            } else {
                if (inputSlideContainer.getVisibility() == View.INVISIBLE)
                    inputSlideContainer.setVisibility(View.VISIBLE);
                inputImage.setImagePath(slidesList.get(currentSlide + 1), true);
                mainImage.setImagePath(slidesList.get(currentSlide), slidesList.size(), currentChID, true);
                outputImage.setImagePath(slidesList.get(currentSlide - 1), true);
                commentsFragment.setComments(slidesList.get(currentSlide), true);
            }
        }
    }

    public void nextSlide() {
        if (currentSlide < slidesList.size() - 1) {
            currentSlide = currentSlide + 1;
            currentChID = 1;
            changeSlideToDongle(currentSlide + 1, currentChID);
            if (currentSlide == slidesList.size() - 1) {
                inputSlideContainer.setVisibility(View.INVISIBLE);
                outputImage.setImagePath(slidesList.get(currentSlide - 1), true);
                mainImage.setImagePath(slidesList.get(currentSlide), slidesList.size(), currentChID, true);
                commentsFragment.setComments(slidesList.get(currentSlide), true);

            } else {
                if (outputSlideContainer.getVisibility() == View.INVISIBLE)
                    outputSlideContainer.setVisibility(View.VISIBLE);
                inputImage.setImagePath(slidesList.get(currentSlide + 1), true);
                mainImage.setImagePath(slidesList.get(currentSlide), slidesList.size(), currentChID, true);
                outputImage.setImagePath(slidesList.get(currentSlide - 1), true);
                commentsFragment.setComments(slidesList.get(currentSlide), true);
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

    public static class Slide {
        private String title;
        private int slideNumber;
        private String comments;
        private String filePath;
        private ArrayList<Integer> chID = new ArrayList<>();

        public Slide(String title, int slideNumber, String comments, String filePath, ArrayList<Integer> chID) {
            this.title = title;
            this.slideNumber = slideNumber;
            this.comments = comments;
            this.filePath = filePath;
            this.chID = chID;
        }

        public ArrayList<Integer> getChID() {
            return chID;
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

        @Override
        public String toString() {
            return "Slide{" +
                    "title='" + title + '\'' +
                    ", slideNumber=" + slideNumber +
                    ", comments='" + comments + '\'' +
                    ", filePath='" + filePath + '\'' +
                    ", chID=" + chID +
                    '}';
        }
    }

}
