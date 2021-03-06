package com.barisalbazar.shop.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.barisalbazar.shop.activity.MainActivity;
import com.barisalbazar.shop.adapter.AddressAdapter;
import com.barisalbazar.shop.helper.ApiConfig;
import com.barisalbazar.shop.helper.AppController;
import com.barisalbazar.shop.helper.Constant;
import com.barisalbazar.shop.helper.Session;
import com.barisalbazar.shop.helper.VolleyCallback;
import com.barisalbazar.shop.model.Address;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class AddressListFragment extends Fragment {
    public static RecyclerView recyclerView;
    public static ArrayList<Address> addresses;
    public static AddressAdapter addressAdapter;
    public static TextView tvAlert;
    public static String selectedAddress = "";
    public static Activity activity;
    public NestedScrollView nestedScrollView;
    public int total = 0;
    View root;
    SwipeRefreshLayout swipeLayout;
    LinearLayoutManager linearLayoutManager;
    Button btnAddNewAddress, btnAddNewAddress1;
    TextView tvSubTotal, tvConfirmOrder, tvUpdate, tvCurrent;
    LinearLayout lytCLocation, processLyt, processLytBottom;
    private Session session;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_address_list, container, false);
        activity = getActivity();
        session = new Session(activity);

        recyclerView = root.findViewById(R.id.recyclerView);
        swipeLayout = root.findViewById(R.id.swipeLayout);
        nestedScrollView = root.findViewById(R.id.nestedScrollView);
        tvConfirmOrder = root.findViewById(R.id.tvConfirmOrder);
        tvAlert = root.findViewById(R.id.tvAlert);
        lytCLocation = root.findViewById(R.id.lytCLocation);
        btnAddNewAddress = root.findViewById(R.id.btnAddNewAddress);
        btnAddNewAddress1 = root.findViewById(R.id.btnAddNewAddress1);
        processLyt = root.findViewById(R.id.processLyt);
        tvUpdate = root.findViewById(R.id.tvUpdate);
        tvCurrent = root.findViewById(R.id.tvCurrent);
        tvSubTotal = root.findViewById(R.id.tvSubTotal);
        processLytBottom = root.findViewById(R.id.processLytBottom);
        linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        recyclerView.getItemAnimator().setChangeDuration(0);

        if (AppController.isConnected(activity)) {
            Constant.OFFSET_ADDRESS = 0;
            getAddresses(0);
        }

        if (getArguments().getString("from").equalsIgnoreCase("process")) {
            tvSubTotal.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(getArguments().getDouble("subtotal")));

            tvConfirmOrder.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View view) {
                    if (!selectedAddress.isEmpty()) {
                        Fragment fragment = new PaymentFragment();
                        Bundle bundle = new Bundle();
                        bundle.putDouble("subtotal", getArguments().getDouble("subtotal"));
                        bundle.putDouble("total", getArguments().getDouble("total"));
                        bundle.putDouble("taxAmt", getArguments().getDouble("taxAmt"));
                        bundle.putDouble("pCodeDiscount", getArguments().getDouble("pCodeDiscount"));
                        bundle.putDouble("dCharge", getArguments().getDouble("dCharge"));
                        bundle.putString("address", selectedAddress);
                        bundle.putStringArrayList("variantIdList", getArguments().getStringArrayList("variantIdList"));
                        bundle.putStringArrayList("qtyList", getArguments().getStringArrayList("qtyList"));

                        PaymentFragment.paymentMethod = "";
                        PaymentFragment.deliveryTime = "";
                        PaymentFragment.deliveryDay = "";

                        fragment.setArguments(bundle);
                        MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                    } else {
                        Toast.makeText(activity, R.string.select_delivery_address, Toast.LENGTH_SHORT).show();
                    }
                }
            });


            processLyt.setVisibility(View.VISIBLE);
            processLytBottom.setVisibility(View.VISIBLE);
            btnAddNewAddress1.setVisibility(View.GONE);

        } else {

            swipeLayout.setPadding(0, 0, 0, (int) getResources().getDimension(R.dimen.dimen_50dp));
            processLyt.setVisibility(View.GONE);
            processLytBottom.setVisibility(View.GONE);
            btnAddNewAddress1.setVisibility(View.VISIBLE);
        }

        setHasOptionsMenu(true);

        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                addresses.clear();
                addressAdapter = null;
                Constant.OFFSET_ADDRESS = 0;
                getAddresses(0);
                swipeLayout.setRefreshing(false);
            }
        });

        btnAddNewAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewAddress();
            }
        });

        btnAddNewAddress1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewAddress();
            }
        });

        return root;
    }


    public void addNewAddress() {
        Fragment fragment = new AddressAddUpdateFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("model", "");
        bundle.putString("for", "add");
        bundle.putInt("position", 0);

        fragment.setArguments(bundle);
        MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
    }

    public void getAddresses(final int startoffset) {
        addresses = new ArrayList<>();
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.GET_ADDRESSES, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        Constant.selectedAddressId = "";
                        JSONObject jsonObject = new JSONObject(response);
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            total = Integer.parseInt(jsonObject.getString(Constant.TOTAL));
                            session.setData(Constant.TOTAL, String.valueOf(total));
                            JSONObject object = new JSONObject(response);
                            JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                            //System.out.println("=====res addresss   " + response);
                            Gson g = new Gson();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                                if (jsonObject1 != null) {
                                    Address address = g.fromJson(jsonObject1.toString(), Address.class);
                                    if (address.getIs_default().equals("1")) {
                                        Constant.selectedAddressId = address.getId();
                                    }
                                   /* if(jsonObject1.getString("is_default").equals("1")){
                                        Constant.selectedAddressId=jsonObject1.getString("id");
                                    }*/
                                    addresses.add(address);
                                } else {
                                    break;
                                }

                            }
                            addressAdapter = new AddressAdapter(activity, addresses);
                            recyclerView.setAdapter(addressAdapter);
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            tvAlert.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, activity, Constant.GET_ADDRESS_URL, params, true);
    }


    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.addresses);
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