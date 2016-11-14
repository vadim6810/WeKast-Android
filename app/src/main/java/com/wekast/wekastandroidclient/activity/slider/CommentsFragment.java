package com.wekast.wekastandroidclient.activity.slider;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wekast.wekastandroidclient.R;


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
        view = inflater.inflate(R.layout.fragment_comments, container, false);
        commentsTitle = (TextView) view.findViewById(R.id.comments_title);
        commentsBody = (TextView) view.findViewById(R.id.comments_body);
        viewSlide();

        return view;
    }

    private void viewSlide() {
        commentsTitle.setText(title);
        commentsBody.setText(comments);
    }

    public void setComments(FragmentSlider.Slide slide, boolean isshow) {
        if (slide != null) {
            comments = slide.getComments();
            title = "Slide " + slide.getSlideNumber() + " comments";
            if (isshow)
                viewSlide();
        }
    }

    public String getCurrentComments() {
        return comments;
    }
}
