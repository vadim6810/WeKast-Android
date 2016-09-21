package com.wekast.wekastandroidclient.activity.slider;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.activity.FragmentSlider;


/**
 * Created by RDL on 03.09.2016.
 */
public class CommentsFragment extends Fragment {
    View view;
    String comments;
    String title;
    TextView commentsTitle;
    TextView commentsBody;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_comments, null);
        commentsTitle = (TextView)view.findViewById(R.id.comments_title);
        commentsBody = (TextView)view.findViewById(R.id.comments_body);
        commentsTitle.setText(title);
        commentsBody.setText(comments);
        return view;
    }
    public void setComments(FragmentSlider.Slide slide){
        if(slide != null){
            comments = slide.getComments();
            title = "Slide "+slide.getSlideNumber()+" comments";
        }
    }
    public String getCurrentComments(){
        return comments;
    }
}
