package com.barisalbazar.shop.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.barisalbazar.shop.R;
import com.barisalbazar.shop.activity.LoginActivity;
import com.barisalbazar.shop.activity.MainActivity;
import com.barisalbazar.shop.adapter.CartAdapter;
import com.barisalbazar.shop.adapter.OfflineCartAdapter;
import com.barisalbazar.shop.helper.ApiConfig;
import com.barisalbazar.shop.helper.AppController;
import com.barisalbazar.shop.helper.Constant;
import com.barisalbazar.shop.helper.DatabaseHelper;
import com.barisalbazar.shop.helper.Session;
import com.barisalbazar.shop.helper.VolleyCallback;
import com.barisalbazar.shop.model.Cart;
import com.barisalbazar.shop.model.OfflineCart;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.barisalbazar.shop.helper.ApiConfig.AddMultipleProductInCart;
import static com.barisalbazar.shop.helper.ApiConfig.GetSettings;


public class CartFragment extends Fragment {
    public static LinearLayout lytempty;
    public static RelativeLayout lyttotal;
    public static ArrayList<Cart> carts;
    public static ArrayList<OfflineCart> offlineCarts;
    public static HashMap<String, String> values;
    static TextView txtcheckout, txttotal, txtstotal, txtdeliverycharge, txtsubtotal;
    static CartAdapter cartAdapter;
    static OfflineCartAdapter offlineCartAdapter;
    static Activity activity;
    static Session session;
    static JSONObject objectbject;
    View root;
    RecyclerView cartrecycleview;
    NestedScrollView scrollView;
    double total;
    ProgressBar progressBar;
    Button btnShowNow;
    private boolean isLoadMore = false;
    private DatabaseHelper databaseHelper;

    @SuppressLint("SetTextI18n")
    public static void SetData() {
        try {
            if (Constant.FLOAT_TOTAL_AMOUNT <= Constant.SETTING_MINIMUM_AMOUNT_FOR_FREE_DELIVERY) {
                txtstotal.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(Constant.FLOAT_TOTAL_AMOUNT));
                txtdeliverycharge.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(Constant.SETTING_DELIVERY_CHARGE));
                txtsubtotal.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(Constant.SETTING_DELIVERY_CHARGE + Constant.FLOAT_TOTAL_AMOUNT));

                txttotal.setText("Total " + objectbject.getDouble(Constant.TOTAL) + " Items " + Constant.formater.format(Constant.FLOAT_TOTAL_AMOUNT));

            } else {
                txtstotal.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(Constant.FLOAT_TOTAL_AMOUNT));
                txtdeliverycharge.setText(activity.getString(R.string.free));
                txtsubtotal.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(Constant.FLOAT_TOTAL_AMOUNT));
                txttotal.setText("Total " + objectbject.getInt(Constant.TOTAL) + " Items " + Constant.formater.format(Constant.FLOAT_TOTAL_AMOUNT));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    public static void SetOfflineData(double total) {
        if (total <= Constant.SETTING_MINIMUM_AMOUNT_FOR_FREE_DELIVERY) {
            txtstotal.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(total));
            txtdeliverycharge.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(Constant.SETTING_DELIVERY_CHARGE));
            txtsubtotal.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(Constant.SETTING_DELIVERY_CHARGE + total));
            txttotal.setText("Total " + Constant.TOTAL_CART_ITEM + " Items " + total);
        } else {
            txtstotal.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(total));
            txtdeliverycharge.setText(activity.getString(R.string.free));
            txtsubtotal.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(total));
            txttotal.setText("Total " + Constant.TOTAL_CART_ITEM + " Items " + Constant.formater.format(total));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_cart, container, false);

        values = new HashMap<>();
        activity = getActivity();
        session = new Session(getActivity());
        progressBar = root.findViewById(R.id.progressBar);
        lyttotal = root.findViewById(R.id.lyttotal);
        lytempty = root.findViewById(R.id.lytempty);
        btnShowNow = root.findViewById(R.id.btnShowNow);
        txttotal = root.findViewById(R.id.txttotal);
        txtsubtotal = root.findViewById(R.id.txtsubtotal);
        txtdeliverycharge = root.findViewById(R.id.txtdeliverycharge);
        txtstotal = root.findViewById(R.id.txtstotal);
        txtcheckout = root.findViewById(R.id.txtcheckout);
        scrollView = root.findViewById(R.id.scrollView);
        cartrecycleview = root.findViewById(R.id.cartrecycleview);
        databaseHelper = new DatabaseHelper(activity);

        ApiConfig.GetSettings(activity);

        setHasOptionsMenu(true);

        Constant.FLOAT_TOTAL_AMOUNT = 0.00;

        carts = new ArrayList<>();
        cartrecycleview.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (AppController.isConnected(getActivity())) {
            if (session.isUserLoggedIn()) {
                getCartData(0);
                ApiConfig.GetPaymentConfig(getActivity());
                GetSettings(getActivity());
            } else {
                GetOfflineCart();
            }
        }

        txtcheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppController.isConnected(getActivity())) {
                    if (Constant.SETTINGS_MINIMUM_ORDER_AMOUNT <= Constant.FLOAT_TOTAL_AMOUNT) {
                        if (session.isUserLoggedIn()) {
                            if (values.size() > 0) {
                                AddMultipleProductInCart(session, getActivity(), values);
                            }
                            MainActivity.fm.beginTransaction().add(R.id.container, new CheckoutFragment()).addToBackStack(null).commit();
                        } else {
                            startActivity(new Intent(getActivity(), LoginActivity.class).putExtra("fromto", "checkout").putExtra("from", "checkout"));
                        }
                    } else {
                        Toast.makeText(activity, getString(R.string.msg_minimum_order_amount) + Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(Constant.SETTINGS_MINIMUM_ORDER_AMOUNT), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        btnShowNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        return root;
    }

    private void GetOfflineCart() {
        progressBar.setVisibility(View.VISIBLE);
        if (databaseHelper.getTotalItemOfCart(activity) >= 1) {
            offlineCarts = new ArrayList<OfflineCart>();
            offlineCartAdapter = null;
            Map<String, String> params = new HashMap<>();
            params.put(Constant.GET_CART_OFFLINE, Constant.GetVal);
            params.put(Constant.VARIANT_IDs, databaseHelper.getCartList().toString().replace("[", "").replace("]", "").replace("\"", ""));

            ApiConfig.RequestToVolley(new VolleyCallback() {
                @Override
                public void onSuccess(boolean result, String response) {

                    if (result) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean(Constant.ERROR)) {
                                session.setData(Constant.TOTAL, jsonObject.getString(Constant.TOTAL));

                                JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);

                                Gson g = new Gson();

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                                    if (jsonObject1 != null) {
                                        OfflineCart cart = g.fromJson(jsonObject1.toString(), OfflineCart.class);
                                        offlineCarts.add(cart);
                                    } else {
                                        break;
                                    }
                                }
                                offlineCartAdapter = new OfflineCartAdapter(getActivity(), offlineCarts);
                                offlineCartAdapter.setHasStableIds(true);
                                cartrecycleview.setAdapter(offlineCartAdapter);
                                lyttotal.setVisibility(View.VISIBLE);

                                progressBar.setVisibility(View.GONE);
                            } else {
                                cartrecycleview.setVisibility(View.GONE);
                                lytempty.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            progressBar.setVisibility(View.GONE);
                            e.printStackTrace();
                        }
                    }
                }
            }, getActivity(), Constant.GET_OFFLINE_CART_URL, params, false);
        } else {
            progressBar.setVisibility(View.GONE);
            cartrecycleview.setVisibility(View.GONE);
            lytempty.setVisibility(View.VISIBLE);
        }
    }

    private void getCartData(final int startoffset) {
        progressBar.setVisibility(View.VISIBLE);
        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.GET_USER_CART, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.OFFSET, "" + startoffset);
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);

        ApiConfig.RequestToVolley(new VolleyCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        objectbject = new JSONObject(response);
                        if (!objectbject.getBoolean(Constant.ERROR)) {
                            JSONObject object = new JSONObject(response);
                            JSONArray jsonArray = object.getJSONArray(Constant.DATA);

                            Gson g = new Gson();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                                if (jsonObject1 != null) {
                                    Cart cart = g.fromJson(jsonObject1.toString(), Cart.class);
                                    carts.add(cart);
                                } else {
                                    break;
                                }
                            }

                            if (startoffset == 0) {
                                cartAdapter = new CartAdapter(getActivity(), carts);
                                cartAdapter.setHasStableIds(true);
                                cartrecycleview.setAdapter(cartAdapter);
                                lyttotal.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                total = Double.parseDouble(objectbject.getString(Constant.TOTAL));
                                session.setData(Constant.TOTAL, String.valueOf(total));
                                Constant.FLOAT_TOTAL_AMOUNT = objectbject.getDouble(Constant.TOTAL_AMOUNT);
                                Constant.TOTAL_CART_ITEM = Integer.parseInt(objectbject.getString(Constant.TOTAL));
                                SetData();

                                progressBar.setVisibility(View.GONE);

                                scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                                    @Override
                                    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                                        if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) cartrecycleview.getLayoutManager();
                                            if (carts.size() < total) {
                                                if (!isLoadMore) {
                                                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == carts.size() - 1) {
                                                        //bottom of list!
                                                        carts.add(null);
                                                        cartAdapter.notifyItemInserted(carts.size() - 1);
                                                        Constant.OFFSET_CART += Constant.LOAD_ITEM_LIMIT;
                                                        Map<String, String> params = new HashMap<>();
                                                        params.put(Constant.GET_USER_CART, Constant.GetVal);
                                                        params.put(Constant.USER_ID, session.getData(Constant.ID));
                                                        params.put(Constant.OFFSET, "" + Constant.OFFSET_CART);
                                                        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);

                                                        ApiConfig.RequestToVolley(new VolleyCallback() {
                                                            @Override
                                                            public void onSuccess(boolean result, String response) {

                                                                if (result) {
                                                                    try {

                                                                        JSONObject objectbject1 = new JSONObject(response);
                                                                        if (!objectbject1.getBoolean(Constant.ERROR)) {
                                                                            session.setData(Constant.TOTAL, objectbject1.getString(Constant.TOTAL));

                                                                            carts.remove(carts.size() - 1);
                                                                            cartAdapter.notifyItemRemoved(carts.size());

                                                                            JSONObject object = new JSONObject(response);
                                                                            JSONArray jsonArray = object.getJSONArray(Constant.DATA);

                                                                            Gson g = new Gson();


                                                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                                                                                if (jsonObject1 != null) {
                                                                                    Cart cart = g.fromJson(jsonObject1.toString(), Cart.class);
                                                                                    carts.add(cart);
                                                                                } else {
                                                                                    break;
                                                                                }
                                                                            }
                                                                            cartAdapter.notifyDataSetChanged();
                                                                            cartAdapter.setLoaded();
                                                                            isLoadMore = false;
                                                                        }
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            }
                                                        }, getActivity(), Constant.CART_URL, params, false);
                                                        isLoadMore = true;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            lytempty.setVisibility(View.VISIBLE);
                            lyttotal.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        progressBar.setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                }
            }
        }, getActivity(), Constant.CART_URL, params, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (values.size() > 0) {
            AddMultipleProductInCart(session, getActivity(), values);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.cart);
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
        menu.findItem(R.id.toolbar_cart).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(false);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

}