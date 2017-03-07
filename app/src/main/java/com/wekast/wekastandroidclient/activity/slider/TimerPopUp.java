package com.wekast.wekastandroidclient.activity.slider;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.wekast.wekastandroidclient.R;

/**
 * Created by RDL on 08/03/2017.
 */

public class TimerPopUp extends PopupWindow implements View.OnClickListener, NumberPicker.OnValueChangeListener {

    private Context context;
    private CallbackPopUpListener listener;
    private TextView timerTxt;
    private NumberPicker npHH, npMM, npSS;
    private Button startBtn, resetBtn;
    private ImageButton cancelBtn;
    private int mmDef = 5;

    public TimerPopUp(Context context, CallbackPopUpListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;

        setContentView(LayoutInflater.from(context).inflate(R.layout.popup_timer, null));
        setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        View popupView = getContentView();
        setFocusable(true);

        timerTxt = (TextView) popupView.findViewById(R.id.timer_txt);
        startBtn = (Button) popupView.findViewById(R.id.btn_start);
        resetBtn = (Button) popupView.findViewById(R.id.btn_reset);
        cancelBtn = (ImageButton) popupView.findViewById(R.id.btn_cancel);

        startBtn.setOnClickListener(this);
        resetBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        npHH = (NumberPicker) popupView.findViewById(R.id.hh);
        npMM = (NumberPicker) popupView.findViewById(R.id.mm);
        npSS = (NumberPicker) popupView.findViewById(R.id.ss);
        npHH.setMinValue(0);
        npHH.setMaxValue(23);
        npMM.setMinValue(0);
        npMM.setMaxValue(59);
        npSS.setMinValue(0);
        npSS.setMaxValue(59);

        npMM.setValue(mmDef);
        timerTxt.setText("00:0" + mmDef + ":00");

        npHH.setOnValueChangedListener(this);
        npMM.setOnValueChangedListener(this);
        npSS.setOnValueChangedListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_start:
                Log.d("onClick: ", "START");
                listener.resultOk(getTimeSec());
                dismiss();
                break;
            case R.id.btn_reset:
                Log.d("onClick: ", "RESET");
                timerTxt.setText("00:00:00");
                npHH.setValue(0);
                npMM.setValue(0);
                npSS.setValue(0);
                break;
            case R.id.btn_cancel:
                Log.d("onClick: ", "CANCEL");
                dismiss();
                break;
        }

    }

    public int getTimeSec() {
        return (npHH.getValue() * 3600) + (npMM.getValue() * 60) + npSS.getValue();
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        int hh = npHH.getValue();
        int mm = npMM.getValue();
        int ss = npSS.getValue();
        String hhTxt = (hh < 10) ? ("0" + hh) : String.valueOf(hh);
        String mmTxt = (mm < 10) ? ("0" + mm) : String.valueOf(mm);
        String ssTxt = (ss < 10) ? ("0" + ss) : String.valueOf(ss);
        timerTxt.setText(hhTxt + ":" + mmTxt + ":" + ssTxt);
    }

    interface CallbackPopUpListener {
        void resultOk(int time);
    }
}
