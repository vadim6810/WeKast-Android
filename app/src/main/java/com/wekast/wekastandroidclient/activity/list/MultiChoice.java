package com.wekast.wekastandroidclient.activity.list;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;

import com.wekast.wekastandroidclient.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by RDL on 07.10.2016.
 */


public class MultiChoice implements AbsListView.MultiChoiceModeListener {
    private AbsListView listView;
    public static final String TAG = "MultiChoice";

    public MultiChoice(AbsListView listView) {
        this.listView = listView;
    }

    interface Callback{
        void callingBackMultiChoice(List<Integer> selectedEzs);
    }

    Callback callback;

    public void registerCallBack(Callback callback){
        this.callback = callback;
    }

    @Override
    //Метод вызывается при любом изменения состояния выделения рядов
    public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
        Log.d(TAG, "onItemCheckedStateChanged");
        int selectedCount = listView.getCheckedItemCount();
        //Добавим количество выделенных рядов в Context Action Bar
        setSubtitle(actionMode, selectedCount);
    }

    @Override
    //Здесь надуваем наше меню из xml
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        Log.d(TAG, "onCreateActionMode");
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.menu_delete, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        Log.d(TAG, "onPrepareActionMode");
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        callback.callingBackMultiChoice(getSelectedEzs());
        actionMode.finish();
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        Log.d(TAG, "onDestroyActionMode");
    }

    private void setSubtitle(ActionMode mode, int selectedCount) {
        switch (selectedCount) {
            case 0:
                mode.setSubtitle(null);
                break;
            default:
                mode.setTitle(String.valueOf(selectedCount));
                break;
        }
    }

    private List<Integer> getSelectedEzs() {
        List<Integer> selectedEzs = new ArrayList<>();

        SparseBooleanArray sparseBooleanArray = listView.getCheckedItemPositions();
        for (int i = 0; i < sparseBooleanArray.size(); i++) {
            if (sparseBooleanArray.valueAt(i)) {
               selectedEzs.add(sparseBooleanArray.keyAt(i));
            }
        }
        return selectedEzs;
    }
}