package com.barisalbazar.shop.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.barisalbazar.shop.R;
import com.barisalbazar.shop.adapter.TransactionAdapter;
import com.barisalbazar.shop.helper.ApiConfig;
import com.barisalbazar.shop.helper.AppController;
import com.barisalbazar.shop.helper.Constant;
import com.barisalbazar.shop.helper.Session;
import com.barisalbazar.shop.helper.VolleyCallback;
import com.barisalbazar.shop.model.Transaction;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class TransactionFragment extends Fragment {
    View root;
    RecyclerView recyclerView;
    ArrayList<Transaction> transactions;
    RelativeLayout tvAlert;
    SwipeRefreshLayout swipeLayout;
    NestedScrollView scrollView;
    TransactionAdapter transactionAdapter;
    int total = 0;
    Activity activity;
    private Session session;
    private boolean isLoadMore = false;
    TextView tvAlertTitle, tvAlertSubTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_transection, container, false);

        activity = getActivity();
        session = new Session(activity);
        setHasOptionsMenu(true);


        scrollView = root.findViewById(R.id.scrollView);
        recyclerView = root.findViewById(R.id.recyclerView);
        swipeLayout = root.findViewById(R.id.swipeLayout);
        tvAlert = root.findViewById(R.id.tvAlert);
        tvAlertTitle = root.findViewById(R.id.tvAlertTitle);
        tvAlertSubTitle = root.findViewById(R.id.tvAlertSubTitle);

        tvAlertTitle.setText(getString(R.string.no_transaction_history_found));
        tvAlertSubTitle.setText(getString(R.string.you_have_not_any_transactional_history_yet));

        if (AppController.isConnected(activity)) {
            getTransactionData(0);
        }

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setColorSchemeResources(R.color.colorPrimary);
                Constant.OFFSET_TRANSACTION = 0;
                getTransactionData(0);
            }
        });


        return root;
    }


    private void getTransactionData(final int startoffset) {
        transactions = new ArrayList<>();
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.GET_USER_TRANSACTION, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.TYPE, Constant.TYPE_TRANSACTION);
        params.put(Constant.OFFSET, "" + Constant.OFFSET_TRANSACTION);
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);

        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
//                        System.out.println("====transection " + response);
                        JSONObject objectbject = new JSONObject(response);
                        if (!objectbject.getBoolean(Constant.ERROR)) {
                            total = Integer.parseInt(objectbject.getString(Constant.TOTAL));
                            session.setData(Constant.TOTAL, String.valueOf(total));

                            JSONObject object = new JSONObject(response);
                            JSONArray jsonArray = object.getJSONArray(Constant.DATA);

                            Gson g = new Gson();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                                if (jsonObject1 != null) {
                                    Transaction Transaction = g.fromJson(jsonObject1.toString(), Transaction.class);
                                    transactions.add(Transaction);
                                } else {
                                    break;
                                }

                            }
                            if (startoffset == 0) {
                                transactionAdapter = new TransactionAdapter(activity, transactions);
                                transactionAdapter.setHasStableIds(true);
                                recyclerView.setAdapter(transactionAdapter);
                                scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                                    @Override
                                    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                                        // if (diff == 0) {
                                        if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                                            if (transactions.size() < total) {
                                                if (!isLoadMore) {
                                                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == transactions.size() - 1) {
                                                        //bottom of list!
                                                        transactions.add(null);
                                                        transactionAdapter.notifyItemInserted(transactions.size() - 1);
                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                Constant.OFFSET_TRANSACTION += Constant.LOAD_ITEM_LIMIT;
                                                                Map<String, String> params = new HashMap<>();
                                                                params.put(Constant.GET_USER_TRANSACTION, Constant.GetVal);
                                                                params.put(Constant.USER_ID, session.getData(Constant.ID));
                                                                params.put(Constant.TYPE, Constant.TYPE_TRANSACTION);
                                                                params.put(Constant.OFFSET, "" + Constant.OFFSET_TRANSACTION);
                                                                params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);

                                                                ApiConfig.RequestToVolley(new VolleyCallback() {
                                                                    @Override
                                                                    public void onSuccess(boolean result, String response) {

                                                                        if (result) {
                                                                            try {
                                                                                // System.out.println("====product  " + response);
                                                                                JSONObject objectbject1 = new JSONObject(response);
                                                                                if (!objectbject1.getBoolean(Constant.ERROR)) {

                                                                                    session.setData(Constant.TOTAL, objectbject1.getString(Constant.TOTAL));

                                                                                    transactions.remove(transactions.size() - 1);
                                                                                    transactionAdapter.notifyItemRemoved(transactions.size());

                                                                                    JSONObject object = new JSONObject(response);
                                                                                    JSONArray jsonArray = object.getJSONArray(Constant.DATA);

                                                                                    Gson g = new Gson();


                                                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                                                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                                                                                        if (jsonObject1 != null) {
                                                                                            Transaction Transaction = g.fromJson(jsonObject1.toString(), Transaction.class);
                                                                                            transactions.add(Transaction);
                                                                                        } else {
                                                                                            break;
                                                                                        }

                                                                                    }
                                                                                    transactionAdapter.notifyDataSetChanged();
                                                                                    transactionAdapter.setLoaded();
                                                                                    isLoadMore = false;
                                                                                }
                                                                            } catch (JSONException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                        }
                                                                    }
                                                                }, activity, Constant.TRANSACTION_URL, params, false);

                                                            }
                                                        }, 0);
                                                        isLoadMore = true;
                                                    }

                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            tvAlert.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, activity, Constant.TRANSACTION_URL, params, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.transaction_history);
        activity.invalidateOptionsMenu();
        hideKeyboard();
    }

    public void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_cart).setVisible(false);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(false);
    }

}