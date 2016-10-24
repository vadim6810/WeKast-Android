package com.wekast.wekastandroidclient.activity.list;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.EquationsBitmap;
import com.wekast.wekastandroidclient.model.Utils;
import com.wekast.wekastandroidclient.services.DownloadService;

import java.util.ArrayList;
import java.util.List;


import static com.wekast.wekastandroidclient.model.Utils.*;

/**
 * Created by RDL on 03.09.2016.
 */
public class FragmentListPresentations  extends ListFragment implements SwipeRefreshLayout.OnRefreshListener, MultiChoice.Callback {
    private static final String TAG = "FragListPres";
    private SwipeRefreshLayout swipeRefreshLayout;
    private onSomeEventListener someEventListener;
    private ArrayList<String[]> localEzs;
    private ArrayList<String> serverEzsDel;
    private ArrayList<RowItem> rowItems;
    private CustomAdapter adapter;
    private UnzipAsyncTask unzipAsyncTask;
    public final static String BROADCAST_ACTION = "com.wekast.wekastandroidclient.activity.list";
    BroadcastReceiver broadcastReceiver;


    @Override
    public void callingBackMultiChoice(List<Integer> selectedEzs) {
        toastShow(getActivity(), "Selected items: " + selectedEzs);
        serverEzsDel = new ArrayList<>();
        for (Integer id : selectedEzs) {
            serverEzsDel.add(localEzs.get(id)[0]);
            Utils.deleteEzsLocal(localEzs.get(id)[1]);
        }
        updateListPresentations();
        Intent i = new Intent(getActivity(), DownloadService.class);
        i.putExtra("command", DELETE);
        i.putStringArrayListExtra("serverEzsDel", serverEzsDel);
        getActivity().startService(i);
    }

    public interface onSomeEventListener {
        void someEvent(String presPath);
    }

    /**
     * onAttach(Context) не вызовется до API 23 версии вместо этого будет вызван onAttach(Activity), который устарел с 23 API
     * Так что вызовем onAttachToContext
     */
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
    }

    /**
     * устарел с 23 API
     * Так что вызовем onAttachToContext
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    /**
     * Вызовется в момент присоединения фрагмента к активити
     */
    protected void onAttachToContext(Context context) {
        try {
            someEventListener = (onSomeEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listpresentations, null);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        startDownloadService();
        startBroadcastReceiver();
        return view;
    }

    private void startDownloadService() {
        Intent i = new Intent(getActivity(), DownloadService.class);
        i.putExtra("command", DOWNLOAD);
//        i.putExtra("UPLOAD", presPath);
        getActivity().startService(i);
    }

    private void startBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {
                if (DOWNLOAD == intent.getIntExtra("command", 0)){
                    int status = intent.getIntExtra("status", 0);
                    if (status  == STATUS_START) {
                        Log.d(TAG, "onReceive: STATUS_START");
                    }
                    if (status == STATUS_FINISH_PREVIEW) {
                        Log.d(TAG, "onReceive: STATUS_FINISH_PREVIEW");
                        updateListPresentations();
                    }
                    if (status == STATUS_FINISH_ONE) {
                        Log.d(TAG, "onReceive: STATUS_FINISH_ONE");
                        updateListPresentations();
                    }
                    if (status == STATUS_FINISH_ALL) {
                        Log.d(TAG, "onReceive: STATUS_FINISH_ALL");
                        swipeRefreshLayout.setRefreshing(false);
                        updateListPresentations();
                    }
                }
            }
        };
        // создаем фильтр для BroadcastReceiver
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        // регистрируем BroadcastReceiver
        getActivity().registerReceiver(broadcastReceiver, intFilt);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rowItems = new ArrayList<>();
        createListPresentations();

        ListView listView = getListView();
        adapter = new CustomAdapter(getActivity(), rowItems, listView);
        listView.setAdapter(adapter);
        //Указываем ListView хотим режим с мультивыделеним
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        //Указываем callback и обработчик такого режима
        MultiChoice multiChoice = new MultiChoice(listView);
        multiChoice.registerCallBack(this);
        listView.setMultiChoiceModeListener(multiChoice);

    }
    private void createListPresentations() {
        localEzs = getAllFilesList();
        for (int i = 0; i < localEzs.size(); i++) {
            RowItem items = new RowItem(localEzs.get(i)[0], localEzs.get(i)[1]);
            rowItems.add(items);
        }
    }

    private void updateListPresentations() {
        if(!rowItems.isEmpty())
            rowItems.clear();
        createListPresentations();
        adapter.notifyDataSetChanged();
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
//        toastShow(getActivity(), "fragment: " + rowItems.get(position).getTitle());
        if(!rowItems.get(position).isSelected()){
//            rowItems.get(position).setSelected(true);
            if(unzipAsyncTask != null){
                unzipAsyncTask.cancel(true);
            }
            clearWorkDirectory();
            unzipAsyncTask = new UnzipAsyncTask(position);
            unzipAsyncTask.execute();
        } else someEventListener.someEvent(rowItems.get(position).getPath());
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        updateListPresentations();
        startDownloadService();
    }


    public class RowItem {

        private String title;
        private String path;
        private boolean isSelected;
        private Bitmap logo;


        public RowItem(String title, String path) {
            this.title = title;
            this.path = path;
            this.isSelected = false;
            byte[] image = unZipPreview(path);
            this.logo = EquationsBitmap.decodeSampledBitmapFromFile(image, 80, 60);
        }

        public Bitmap getLogo() {
            return logo;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

    }

    public class CustomAdapter extends BaseAdapter {
        private ListView listView;
        Context context;
        ArrayList<RowItem> rowItem;

        CustomAdapter(Context context, ArrayList<RowItem> rowItem, ListView listView) {
            this.context = context;
            this.rowItem = rowItem;
            this.listView = listView;

        }

        @Override
        public int getCount() {

            return rowItem.size();
        }

        @Override
        public Object getItem(int position) {

            return rowItem.get(position);
        }

        @Override
        public long getItemId(int position) {

            return rowItem.indexOf(getItem(position));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.fragment_listitem, null);
            }

            ImageView imgIcon = (ImageView) convertView.findViewById(R.id.preview);
            TextView txtTitle = (TextView) convertView.findViewById(R.id.title);

            RowItem row_pos = rowItem.get(position);
            // setting the image resource and title
            imgIcon.setImageBitmap(row_pos.getLogo());
            imgIcon.setScaleType(ImageView.ScaleType.FIT_XY);
            txtTitle.setText(row_pos.getTitle());

            return convertView;
        }
    }

    private class UnzipAsyncTask extends AsyncTask<Void, Void, Boolean>{
        private int position;

        private UnzipAsyncTask(int position) {
            this.position = position;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            toastShow(getActivity(), "Cash unzip run!");
//            unzipProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return unZipPresentation2(rowItems.get(position).getPath());
        }

        @Override
        protected void onPostExecute(Boolean unzipResult) {
//            unzipProgress.setVisibility(View.GONE);
            if (unzipResult) {
                toastShow(getActivity(), "Cash unziped!");
                someEventListener.someEvent(rowItems.get(position).getPath());
            } else {
                rowItems.get(position).setSelected(false);
                toastShow(getActivity(), "Unzip error!");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: broadcastReceiver");
        getActivity().unregisterReceiver(broadcastReceiver);
    }
}
