package com.wekast.wekastandroidclient.activity.slider;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.services.DongleService;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import static com.wekast.wekastandroidclient.model.Utils.CASH_ABSOLUTE_PATH;
import static com.wekast.wekastandroidclient.model.Utils.SLIDE;
import static com.wekast.wekastandroidclient.model.Utils.infoXML;
import static com.wekast.wekastandroidclient.model.Utils.toastShow;
import static java.lang.Math.abs;

/**
 * Created by RDL on 03.09.2016.
 */
public class FragmentSlider extends Fragment implements View.OnTouchListener {

    private float startY, stopY, startX, stopX, resY, resX;
    private View view;
    private InputImage inputImage;
    private MainImage mainImage;
    private OutputImage outputImage;
    private FrameLayout inputSlideContainer, currentSlideContainer, outputSlideContainer, commentsContainer, commentsFullSize;
    private CommentsFragment commentsFragment;
    private FragmentTransaction tr;
    private int currentSlide = 0;
    private int currentChID = 1;
    private ArrayList<Slide> slidesList = new ArrayList<>();
    private ArrayList<Integer> chID = new ArrayList<>();
    private int slideNumber;
    private String comments;
    private String filePath;
    private boolean fullCommentVisible = false;
    private TextView commentsFullSizeText;
    private Vibrator vibrator;
    private long millsVib = 30L;
    private ProgressBar progressBarSlider;
    private int progress = 1;
    private ProgressBar progressBarTimer;
    private CountDownTimer timerPrBar;
    private int progressTimer = 0;
    private MenuItem itemSetting;
    private TimerPopUp timerPopUp;
    private TextView timerClock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_slider, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inputSlideContainer = (FrameLayout) view.findViewById(R.id.input_slide_container);
        currentSlideContainer = (FrameLayout) view.findViewById(R.id.current_slide_container);
        outputSlideContainer = (FrameLayout) view.findViewById(R.id.output_slide_container);
        commentsContainer = (FrameLayout) view.findViewById(R.id.comments_container);
        commentsFullSize = (FrameLayout) view.findViewById(R.id.comments_full_size);
        commentsFullSizeText = (TextView) view.findViewById(R.id.comments_full_size_text);
        vibrator = (Vibrator) getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);
        timerClock = (TextView) view.findViewById(R.id.timer_clock);

        createWorkArray();

        progressBarSlider = (ProgressBar) view.findViewById(R.id.progressBarSlider);
        progressBarSlider.setMax(slidesList.size());
        progressBarSlider.setProgress(progress);
        progressBarTimer = (ProgressBar) view.findViewById(R.id.progressBarTimer);

        inputImage = new InputImage();
        mainImage = new MainImage();
        outputImage = new OutputImage();
        commentsFragment = new CommentsFragment();

        if (slidesList.size() > 0) {
            inputImage.setImagePath(slidesList.get(currentSlide + 1), false);
            mainImage.setImagePath(slidesList.get(currentSlide), slidesList.size(), currentChID, false);
            commentsFragment.setComments(slidesList.get(currentSlide), false);
            changeSlideToDongle(currentSlide + 1, currentChID);
        }

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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_timer, menu);
        itemSetting = menu.findItem(R.id.action_settings);
        itemSetting.setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_timer:
                timerPopUp = new TimerPopUp(getActivity(), new TimerPopUp.CallbackPopUpListener() {
                    @Override
                    public void resultOk(int time) {
                        startTimer(time);
                    }
                });
//                timerPopUp.setAnimationStyle(R.style.shake_and_shake);
                timerPopUp.showAtLocation(getView(), Gravity.CENTER, 0, 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startTimer(final int seconds) {
        Log.d("startTimer: ", ": " + seconds);
        if (timerPrBar != null) {
            timerPrBar.cancel();
        }
        if (seconds < 10) {
            timerClock.setText("00:0" + seconds);
        } else {
            timerClock.setText("00:" + seconds);
        }
        progressTimer = 0;
        progressBarTimer.setMax(seconds);
        timerPrBar = new CountDownTimer((seconds + 1) * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                progressBarTimer.setSecondaryProgress(progressTimer++);
                final long countTimer = millisUntilFinished / 1000;
                if (countTimer < 10) {
                    timerClock.setText("00:0" + countTimer);
                } else {
                    timerClock.setText("00:" + countTimer);
                }
            }

            @Override
            public void onFinish() {
                progressBarTimer.setSecondaryProgress(progressBarTimer.getMax());
                timerClock.setText("00:00");
                vibrator.vibrate(millsVib * 4);
            }
        }.start();
    }

    private void createWorkArray() {
        try {
            XmlPullParser parser = prepareXpp();
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (parser.getEventType()) {
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("slide")) {
                            slideNumber = Integer.parseInt(parser.getAttributeValue(0));
                            filePath = CASH_ABSOLUTE_PATH + parser.getAttributeValue(1);
                            if (parser.getAttributeCount() > 2)
                                comments = parser.getAttributeValue(2);
                        }
                        if (parser.getName().equals("animation")) {
                            chID.add(Integer.parseInt(parser.getAttributeValue(0)));
                        }

                        if (parser.getName().equals("media")) {
                            chID.add(Integer.parseInt(parser.getAttributeValue(0)));
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
            Log.e("Error XML parser: ", t.toString());
            toastShow(getActivity(), "Broken XML from EZS.");
        }
    }

    private XmlPullParser prepareXpp() throws XmlPullParserException {
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
            vibrator.vibrate(millsVib);
        }
        mainImage.setTitleChid(currentChID, slidesList.get(currentSlide).getChID().size());
    }

    private void rightSlide() {
        if (currentChID < slidesList.get(currentSlide).getChID().size()) {
            currentChID++;
            changeSlideToDongle(currentSlide + 1, currentChID);
            vibrator.vibrate(millsVib);
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
        String chid = getChid(currentSlide, currentChID);
        Log.d("changeSlideToDongle", currentSlide + "|" + chid);
        Intent i = new Intent(getActivity(), DongleService.class);
        i.putExtra("command", SLIDE);
        i.putExtra("SLIDE", Integer.toString(currentSlide));
        i.putExtra("MEDIA", chid);
        getActivity().startService(i);
    }

    private String getChid(int currentSlide, int currentChID) {
        String res = "";
        ArrayList<Integer> chidList = slidesList.get(currentSlide - 1).getChID();
        if (chidList.size() > 0) {
            res = String.valueOf(chidList.get(currentChID - 1));
        }
        return res;
    }

    public void prevSlide() {
        if (currentSlide > 0) {
            progressBarSlider.setProgress(--progress);
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
            vibrator.vibrate(millsVib);
        }
    }

    public void nextSlide() {
        if (currentSlide < slidesList.size() - 1) {
            progressBarSlider.setProgress(++progress);
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
            vibrator.vibrate(millsVib);
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
        if (timerPrBar != null) {
            timerPrBar.cancel();
        }
    }
}
