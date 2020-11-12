package wrteam.ekart.shop.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import wrteam.ekart.shop.R;
import wrteam.ekart.shop.activity.MainActivity;
import wrteam.ekart.shop.helper.ApiConfig;
import wrteam.ekart.shop.helper.AppController;
import wrteam.ekart.shop.helper.Constant;
import wrteam.ekart.shop.helper.Session;
import wrteam.ekart.shop.helper.VolleyCallback;
import wrteam.ekart.shop.model.Cart;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class CheckoutFragment extends Fragment {
    public static String pCode = "", appliedCode = "", deliveryCharge = "0";
    public static LinearLayout deliveryLyt, paymentLyt;
    public double pCodeDiscount = 0.0, total, subtotal, taxAmt = 0.0, dCharge = 0.0;
    public TextView tvConfirmOrder, tvPayment, tvDelivery;
    public ArrayList<String> variantIdList, qtyList;
    public TextView tvTaxPercent, tvTaxAmt, tvAlert, tvWltBalance, tvName, tvTotal, tvDeliveryCharge, tvSubTotal, tvCurrent,
            tvWallet, tvPromoCode, tvPCAmount, tvPreTotal;
    public LinearLayout lytTax, lytOrderList, lytCLocation, processLyt;
    View root;
    RelativeLayout confirmLyt, mainLayout;
    boolean isApplied;
    ImageView imgRefresh;
    Button btnApply;
    ProgressBar progressBar;
    EditText edtPromoCode;
    Session session;
    Activity activity;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_checkout, container, false);

        activity = getActivity();
        session = new Session(activity);
        progressBar = root.findViewById(R.id.progressBar);
        lytTax = root.findViewById(R.id.lytTax);
        tvTaxAmt = root.findViewById(R.id.tvTaxAmt);
        tvTaxPercent = root.findViewById(R.id.tvTaxPercent);
        tvDelivery = root.findViewById(R.id.tvDelivery);
        tvPayment = root.findViewById(R.id.tvPayment);
        tvPCAmount = root.findViewById(R.id.tvPCAmount);
        tvPromoCode = root.findViewById(R.id.tvPromoCode);
        tvAlert = root.findViewById(R.id.tvAlert);
        edtPromoCode = root.findViewById(R.id.edtPromoCode);
        tvSubTotal = root.findViewById(R.id.tvSubTotal);
        tvDeliveryCharge = root.findViewById(R.id.tvDeliveryCharge);
        tvTotal = root.findViewById(R.id.tvTotal);
        tvName = root.findViewById(R.id.tvName);
        tvCurrent = root.findViewById(R.id.tvCurrent);
        lytOrderList = root.findViewById(R.id.lytOrderList);
        lytCLocation = root.findViewById(R.id.lytCLocation);
        paymentLyt = root.findViewById(R.id.paymentLyt);
        deliveryLyt = root.findViewById(R.id.deliveryLyt);
        confirmLyt = root.findViewById(R.id.confirmLyt);
        tvWallet = root.findViewById(R.id.tvWallet);
        tvConfirmOrder = root.findViewById(R.id.tvConfirmOrder);
        processLyt = root.findViewById(R.id.processLyt);
        imgRefresh = root.findViewById(R.id.imgRefresh);

        setHasOptionsMenu(true);


        tvWltBalance = root.findViewById(R.id.tvWltBalance);
        tvPreTotal = root.findViewById(R.id.tvPreTotal);
        btnApply = root.findViewById(R.id.btnApply);

        tvConfirmOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new AddressListFragment();
                Bundle bundle = new Bundle();
                bundle.putDouble("subtotal", Double.parseDouble(Constant.formater.format(subtotal)));
                bundle.putDouble("total", Double.parseDouble(Constant.formater.format(total)));
                bundle.putDouble("taxAmt", Double.parseDouble(Constant.formater.format(taxAmt)));
                bundle.putDouble("pCodeDiscount", Double.parseDouble(Constant.formater.format(pCodeDiscount)));
                bundle.putDouble("dCharge", dCharge);
                //System.out.println("DCHARGE : " + dCharge);

                bundle.putStringArrayList("variantIdList", variantIdList);
                bundle.putStringArrayList("qtyList", qtyList);
                bundle.putString("from", "process");

                AddressListFragment.selectedAddress = "";
                fragment.setArguments(bundle);
                MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
            }
        });

        imgRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isApplied) {
                    btnApply.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                    btnApply.setText("Apply");
                    edtPromoCode.setText("");
                    tvPromoCode.setVisibility(View.GONE);
                    tvPCAmount.setVisibility(View.GONE);
                    isApplied = false;
                    appliedCode = "";
                    pCode = "";
                    SetDataTotal();
                }
            }
        });


        if (AppController.isConnected(activity)) {
            ApiConfig.getWalletBalance(activity, session);
            getCartData();
            PromoCodeCheck();
        }

        return root;
    }


    private void getCartData() {

        ApiConfig.getCartItemCount(activity, session);

        progressBar.setVisibility(View.VISIBLE);
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_USER_CART, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.LIMIT, "" + Constant.TOTAL_CART_ITEM);

        //System.out.println("PARAMS : " + params);

        ApiConfig.RequestToVolley(new VolleyCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                        Gson gson = new Gson();
                        variantIdList = new ArrayList<>();
                        qtyList = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {


                            Cart cart = gson.fromJson(String.valueOf(jsonArray.getJSONObject(i)), Cart.class);
                            variantIdList.add(cart.getProduct_variant_id());
                            qtyList.add(cart.getQty());

                            LinearLayout linearLayout = new LinearLayout(getContext());
                            linearLayout.setWeightSum(4f);

                            TextView tv1 = new TextView(getContext());
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
                            lp.weight = 1.5f;
                            tv1.setLayoutParams(lp);
                            tv1.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                            tv1.setText(cart.getItems().get(0).getName() + "(" + cart.getItems().get(0).getMeasurement() + " " + cart.getItems().get(0).getUnit() + ")");
                            linearLayout.addView(tv1);

                            TextView tv2 = new TextView(getContext());
                            tv2.setText(cart.getQty());
                            LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
                            lp1.weight = 0.7f;
                            tv2.setLayoutParams(lp1);
                            tv2.setGravity(Gravity.CENTER);
                            linearLayout.addView(tv2);

                            TextView tv3 = new TextView(getContext());
                            float price;
                            if (cart.getItems().get(0).getDiscounted_price().equals("0")) {
                                price = Float.parseFloat(cart.getItems().get(0).getPrice());
                            } else {
                                price = Float.parseFloat(cart.getItems().get(0).getDiscounted_price());
                            }

                            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
                            lp2.weight = 0.8f;
                            tv3.setLayoutParams(lp2);
                            tv3.setGravity(Gravity.CENTER);
                            tv3.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(price));
                            linearLayout.addView(tv3);

                            TextView tv4 = new TextView(getContext());
                            LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
                            lp3.weight = 1f;
                            tv4.setLayoutParams(lp3);
                            tv4.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                            linearLayout.addView(tv4);
                            tv4.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(price * (Integer.parseInt(cart.getQty()))));
                            lytOrderList.addView(linearLayout);

                            total += price * (Integer.parseInt(cart.getQty()));
                        }
                        SetDataTotal();

                        confirmLyt.setVisibility(View.VISIBLE);
                        deliveryLyt.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, activity, Constant.CART_URL, params, false);
    }

    @SuppressLint("SetTextI18n")
    public void SetDataTotal() {
        tvTotal.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(Double.parseDouble("" + total)));
        subtotal = total;
        if (total <= Constant.SETTING_MINIMUM_AMOUNT_FOR_FREE_DELIVERY) {
            tvDeliveryCharge.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(Constant.SETTING_DELIVERY_CHARGE));
            subtotal = Double.parseDouble(Constant.formater.format(subtotal + Constant.SETTING_DELIVERY_CHARGE));
            deliveryCharge = Constant.formater.format(Constant.SETTING_DELIVERY_CHARGE);
        } else {
            tvDeliveryCharge.setText(getResources().getString(R.string.free));
            deliveryCharge = "0";
        }
        dCharge = tvDeliveryCharge.getText().toString().equals("Free") ? 0.0 : Constant.SETTING_DELIVERY_CHARGE;
        taxAmt = ((Constant.SETTING_TAX * total) / 100);
        if (pCode.isEmpty()) {
            subtotal = (subtotal + taxAmt);
        } else
            subtotal = (subtotal + taxAmt - pCodeDiscount);
        tvTaxPercent.setText("Tax(" + Constant.SETTING_TAX + "%)");
        tvTaxAmt.setText("+ " + Constant.SETTING_CURRENCY_SYMBOL + "" + Constant.formater.format(Double.parseDouble("" + taxAmt)));
        tvSubTotal.setText(Constant.SETTING_CURRENCY_SYMBOL + "" + Constant.formater.format(Double.parseDouble("" + subtotal)));
    }

    public void PromoCodeCheck() {
        btnApply.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                final String promoCode = edtPromoCode.getText().toString().trim();
                if (promoCode.isEmpty()) {
                    tvAlert.setVisibility(View.VISIBLE);
                    tvAlert.setText("Enter Promo Code");
                } else if (isApplied && promoCode.equals(appliedCode)) {
                    Toast.makeText(getContext(), "promo code already applied", Toast.LENGTH_SHORT).show();
                } else {
                    if (isApplied) {
                        SetDataTotal();
                    }
                    tvAlert.setVisibility(View.GONE);
                    btnApply.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    Map<String, String> params = new HashMap<>();
                    params.put(Constant.VALIDATE_PROMO_CODE, Constant.GetVal);
                    params.put(Constant.USER_ID, session.getData(Session.KEY_ID));
                    params.put(Constant.PROMO_CODE, promoCode);
                    params.put(Constant.TOTAL, String.valueOf(total));

                    ApiConfig.RequestToVolley(new VolleyCallback() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onSuccess(boolean result, String response) {
                            if (result) {
                                try {
                                    JSONObject object = new JSONObject(response);
                                    //   System.out.println("===res " + response);
                                    if (!object.getBoolean(Constant.ERROR)) {
                                        pCode = object.getString(Constant.PROMO_CODE);
                                        tvPromoCode.setText(getString(R.string.promo_code) + "(" + pCode + ")");
                                        btnApply.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_green));
                                        btnApply.setText("Applied");
                                        isApplied = true;
                                        appliedCode = edtPromoCode.getText().toString();
                                        tvPCAmount.setVisibility(View.VISIBLE);
                                        tvPromoCode.setVisibility(View.VISIBLE);
                                        dCharge = tvDeliveryCharge.getText().toString().equals("Free") ? 0.0 : Constant.SETTING_DELIVERY_CHARGE;
                                        subtotal = (object.getDouble(Constant.DISCOUNTED_AMOUNT) + taxAmt + dCharge);
                                        pCodeDiscount = Double.parseDouble(object.getString(Constant.DISCOUNT));
                                        tvPCAmount.setText("- " + Constant.SETTING_CURRENCY_SYMBOL + pCodeDiscount);
                                        tvSubTotal.setText(Constant.SETTING_CURRENCY_SYMBOL + subtotal);
                                    } else {
                                        btnApply.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                                        btnApply.setText("Apply");
                                        tvAlert.setVisibility(View.VISIBLE);
                                        tvAlert.setText(object.getString("message"));
                                    }
                                    progressBar.setVisibility(View.GONE);
                                    btnApply.setVisibility(View.VISIBLE);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, activity, Constant.PROMO_CODE_CHECK_URL, params, false);

                }
                try {
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert imm != null;
                    imm.hideSoftInputFromWindow(mainLayout.getWindowToken(), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.checkout);
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
        activity.invalidateOptionsMenu();
    }

}