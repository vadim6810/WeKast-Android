package com.wekast.wekastandroidclient.activity.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wekast.wekastandroidclient.R;

import java.util.ArrayList;

/**
 * Created by RDL on 28/02/2017.
 */

public class CustomAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Ezs> ezsList;

    CustomAdapter(Context context, ArrayList<Ezs> ezsList) {
        this.context = context;
        this.ezsList = ezsList;
    }

    @Override
    public int getCount() {

        return ezsList.size();
    }

    @Override
    public Object getItem(int position) {

        return ezsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView != null) {
            view = convertView;
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_listitem, parent, false);
        }

        ImageView imgIcon = (ImageView) view.findViewById(R.id.preview);
        TextView txtTitle = (TextView) view.findViewById(R.id.title);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        Ezs ezs = ezsList.get(position);
        // setting the image resource and title
        imgIcon.setImageBitmap(ezs.getLogo());
        imgIcon.setScaleType(ImageView.ScaleType.FIT_XY);
        txtTitle.setText(ezs.getTitle());
        if (ezs.isPreview()) {
            imgIcon.setImageAlpha(100);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            imgIcon.setImageAlpha(255);
            progressBar.setVisibility(View.GONE);
        }
        return view;
    }
}
