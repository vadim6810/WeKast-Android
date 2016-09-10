package com.wekast.wekastandroidclient.activity;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.AccessServiceAPI;
import com.wekast.wekastandroidclient.model.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wekast.wekastandroidclient.model.Utils.*;

/**
 * Created by RDL on 03.09.2016.
 */
public class FragmentListPresentations  extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private AccessServiceAPI m_AccessServiceAPI = new AccessServiceAPI();
    private HashMap<String, String> mapDownload = new HashMap<>();
    private String login;
    private String password;

    private List<RowItem> rowItems;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        login = getFieldSP(getActivity(), "login");
        password = getFieldSP(getActivity(), "password");

        rowItems = new ArrayList<>();
        ArrayList<String> local = getAllFilesLocal();
        for (int i = 0; i < local.size(); i++) {
            RowItem items = new RowItem(local.get(i), R.drawable.wekastbrand);
            rowItems.add(items);
        }

        initListPresentations();
//        new TaskDownload().execute(login, password);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listpresentations, null);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        return view;
    }

//    private void initListPresentations() {
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, getAllFilesLocal());
//        setListAdapter(adapter);
//        adapter.notifyDataSetChanged();
//    }
    private void initListPresentations() {
        CustomAdapter adapter = new CustomAdapter(getActivity(), rowItems);
        setListAdapter(adapter);
//        adapter.notifyDataSetChanged();
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        toastShow(getActivity(), "fragment: " + ((TextView) v).getText().toString());
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        new TaskDownload().execute(login, password);
    }

    public class TaskDownload extends AsyncTask<String, Void, Integer> {
        String LOG_TAG = "FragmentListPresentations = ";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            initListPresentations();
        }

        @Override
        protected Integer doInBackground(String... params) {
            HashMap<String, String> param = new HashMap<>();
            param.put("login", params[0]);
            param.put("password", params[1]);
            //getListOnServer
            try {
                String response = m_AccessServiceAPI.getJSONStringWithParam_POST(Utils.SERVICE_API_URL_LIST, param);
                JSONObject jsonObject = m_AccessServiceAPI.convertJSONString2Obj(response);
                if (jsonObject.getInt("status") == 0) {
                    response = jsonObject.getString("answer");
                    mapDownload = mappingPresentations(parseJSONArrayMap(response), getAllFilesLocal());
                } else {
                    return RESULT_ERROR;
                }
            } catch (Exception e) {
                return RESULT_ERROR;
            }

            //downloadOfServer
            for (Map.Entry<String, String> item : mapDownload.entrySet()) {
                try {
                    byte[] content = m_AccessServiceAPI.getDownloadWithParam_POST(SERVICE_API_URL_DOWNLOAD + item.getKey(), param);
                    writeFile(content, item.getValue(), LOG_TAG);
                    publishProgress();
                } catch (Exception e) {
                    return RESULT_ERROR;
                }
            }
            return RESULT_SUCCESS;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            initListPresentations();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == RESULT_SUCCESS) {
                toastShow(getActivity(), "Download completed.");
            } else {
                toastShow(getActivity(), "Download fail!!!");
            }
            initListPresentations();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public class RowItem {

        private String title;
        private int icon;

        public RowItem(String title, int icon) {
            this.title = title;
            this.icon = icon;

        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

    }

    public class CustomAdapter extends BaseAdapter {

        Context context;
        List<RowItem> rowItem;

        CustomAdapter(Context context, List<RowItem> rowItem) {
            this.context = context;
            this.rowItem = rowItem;

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
            imgIcon.setImageResource(row_pos.getIcon());
            txtTitle.setText(row_pos.getTitle());

            return convertView;

        }

    }
}
