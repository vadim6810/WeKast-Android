package com.wekast.wekastandroidclient.activity.list;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.activity.PhoneConfirmActivity;
import com.wekast.wekastandroidclient.model.EquationsBitmap;
import com.wekast.wekastandroidclient.model.Utils;
import com.wekast.wekastandroidclient.services.DownloadService;

import java.util.ArrayList;
import java.util.List;

import static com.wekast.wekastandroidclient.model.Utils.DELETE;
import static com.wekast.wekastandroidclient.model.Utils.DOWNLOAD;
import static com.wekast.wekastandroidclient.model.Utils.ERROR_CONFIRM;
import static com.wekast.wekastandroidclient.model.Utils.ERROR_DOWNLOAD;
import static com.wekast.wekastandroidclient.model.Utils.PREVIEW_ABSOLUTE_PATH;
import static com.wekast.wekastandroidclient.model.Utils.STATUS_FINISH_ALL;
import static com.wekast.wekastandroidclient.model.Utils.STATUS_FINISH_ONE;
import static com.wekast.wekastandroidclient.model.Utils.STATUS_FINISH_PREVIEW;
import static com.wekast.wekastandroidclient.model.Utils.STATUS_START;
import static com.wekast.wekastandroidclient.model.Utils.clearWorkDirectory;
import static com.wekast.wekastandroidclient.model.Utils.getAllFilesList;
import static com.wekast.wekastandroidclient.model.Utils.getAllPreviewList;
import static com.wekast.wekastandroidclient.model.Utils.toastShow;
import static com.wekast.wekastandroidclient.model.Utils.unZipPresentation2;
import static com.wekast.wekastandroidclient.model.Utils.unZipPreview;

/**
 * Created by RDL on 03.09.2016.
 */
public class FragmentListPresentations extends Fragment implements SwipeRefreshLayout.OnRefreshListener, MultiChoice.Callback, AdapterView.OnItemClickListener {
    private static final String TAG = "FragListPres";
    private SwipeRefreshLayout swipeRefreshLayout;
    private onSomeEventListener someEventListener;
    private ArrayList<String[]> localEzs;
    private ArrayList<String[]> localPrev;
    private ArrayList<String> serverEzsDel;
    private ArrayList<Ezs> ezsItems;
    private CustomAdapter adapter;
    private UnzipAsyncTask unzipAsyncTask;
    public final static String BROADCAST_ACTION = "com.wekast.wekastandroidclient.activity.list";
    BroadcastReceiver broadcastReceiver;

    @Override
    public void callingBackMultiChoice(List<Integer> selectedEzs) {
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!ezsItems.get(position).isSelected()) {
//            ezsItems.get(position).setSelected(true);
            if (unzipAsyncTask != null) {
                unzipAsyncTask.cancel(true);
            }
            clearWorkDirectory(PREVIEW_ABSOLUTE_PATH);
            unzipAsyncTask = new UnzipAsyncTask(position);
            unzipAsyncTask.execute();
        } else someEventListener.someEvent(ezsItems.get(position).getPath());
    }

    public interface onSomeEventListener {
        void someEvent(String presPath);
    }

    /**
     * onAttach(Context) не вызовется до API 23 версии вместо этого будет вызван onAttach(Activity),
     * который устарел с 23 API
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
        View view = inflater.inflate(R.layout.fragment_listpresentations, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
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
                if (DOWNLOAD == intent.getIntExtra("command", 0)) {
                    int status = intent.getIntExtra("status", 0);
                    if (status == STATUS_START) {
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
                        clearWorkDirectory(PREVIEW_ABSOLUTE_PATH);
                        updateListPresentations();
                    }
                    if (status == ERROR_DOWNLOAD) {
                        Log.d(TAG, "onReceive: ERROR_DOWNLOAD");
                        toastShow(getActivity(), "Error download EZS!");
                    }
                    if (status == ERROR_CONFIRM) {
                        Log.d(TAG, "onReceive: ERROR_CONFIRM");
                        toastShow(getActivity(), "ERROR_CONFIRM!");
                        Intent i = new Intent(context, PhoneConfirmActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        context.startActivity(i);
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
        ezsItems = new ArrayList<>();
        createListPresentations();

        ListView listView = (ListView) getView().findViewById(R.id.list_view);
        adapter = new CustomAdapter(getActivity(), ezsItems);
        listView.setAdapter(adapter);
        //Указываем ListView хотим режим с мультивыделеним
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        //Указываем callback и обработчик такого режима
        MultiChoice multiChoice = new MultiChoice(listView);
        multiChoice.registerCallBack(this);
        listView.setMultiChoiceModeListener(multiChoice);
        listView.setOnItemClickListener(this);

        startDownloadService();
        startBroadcastReceiver();
    }

    private void createListPresentations() {
        localEzs = getAllFilesList();
        localPrev = getAllPreviewList();
        for (int i = 0; i < localEzs.size(); i++) {
            Ezs items = new Ezs(localEzs.get(i)[0], localEzs.get(i)[1], false);
            ezsItems.add(items);
        }

        for (int i = 0; i < localPrev.size(); i++) {
            Ezs items = new Ezs("download... " + localPrev.get(i)[0], localPrev.get(i)[1], true);
            ezsItems.add(items);
        }
    }

    private void updateListPresentations() {
        if (!ezsItems.isEmpty())
            ezsItems.clear();
        createListPresentations();
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        updateListPresentations();
        startDownloadService();
    }

    private class UnzipAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private int position;
        ProgressDialog progressDialog;

        private UnzipAsyncTask(int position) {
            this.position = position;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(),
                    "Please wait",
                    "Unzipping EZS...",
                    true);
//            unzipProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return unZipPresentation2(ezsItems.get(position).getPath());
        }

        @Override
        protected void onPostExecute(Boolean unzipResult) {
            progressDialog.dismiss();
//            unzipProgress.setVisibility(View.GONE);
            if (unzipResult) {
                toastShow(getActivity(), "Cash unziped!");
                someEventListener.someEvent(ezsItems.get(position).getPath());
            } else {
                ezsItems.get(position).setSelected(false);
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
