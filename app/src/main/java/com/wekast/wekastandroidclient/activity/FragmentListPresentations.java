package com.wekast.wekastandroidclient.activity;

import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wekast.wekastandroidclient.R;
import com.wekast.wekastandroidclient.model.AccessServiceAPI;
import com.wekast.wekastandroidclient.model.Utils;

import org.json.JSONObject;

import java.util.HashMap;
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        login = getFieldSP(getActivity(), "login");
        password = getFieldSP(getActivity(), "password");

        initListPresentations();
        new TaskDownload().execute(login, password);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listpresentations, null);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        return view;
    }

    private void initListPresentations() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, getAllFilesLocal());
        setListAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        toastShow(getActivity(), "fragment: " + ((TextView) v).getText().toString());
    }

    @Override
    public void onRefresh() {
//        initListPresentations();
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
}
