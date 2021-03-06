package com.barisalbazar.shop.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.razorpay.Checkout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.barisalbazar.shop.R;
import com.barisalbazar.shop.activity.MainActivity;
import com.barisalbazar.shop.activity.PayPalWebActivity;
import com.barisalbazar.shop.adapter.DateAdapter;
import com.barisalbazar.shop.adapter.SlotAdapter;
import com.barisalbazar.shop.helper.ApiConfig;
import com.barisalbazar.shop.helper.AppController;
import com.barisalbazar.shop.helper.Constant;
import com.barisalbazar.shop.helper.PaymentModelClass;
import com.barisalbazar.shop.helper.Session;
import com.barisalbazar.shop.helper.VolleyCallback;
import com.barisalbazar.shop.model.BookingDate;
import com.barisalbazar.shop.model.Slot;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.barisalbazar.shop.helper.ApiConfig.GetTimeSlotConfig;

public class PaymentFragment extends Fragment {
    public static String razorPayId, paymentMethod = "", deliveryTime = "", deliveryDay = "", pCode = "", deliveryCharge = "0", TAG = CheckoutFragment.class.getSimpleName();
    public static Map<String, String> razorParams;
    public static RecyclerView recyclerView;
    public static SlotAdapter adapter;
    public TextView tvSelectDeliveryDate, tvPreTotal, tvWltBalance;
    public LinearLayout deliveryTimeLyt, lytPayOption, lytTax, lytOrderList, lytCLocation, CODLinearLyt, lytPayU, lytPayPal, lytRazorPay, processLyt;
    public TextView tvProceedOrder, tvConfirmOrder, tvPayment, tvDelivery;
    public ArrayList<String> variantIdList, qtyList, dateList;
    public LinearLayout deliveryLyt, paymentLyt;
    RelativeLayout confirmLyt;
    View root;
    double total;
    RelativeLayout lytWallet;
    RadioButton rbCod, rbPayU, rbPayPal, rbRazorPay;
    PaymentModelClass paymentModelClass;
    CheckBox chWallet;
    ImageView imgRefresh;
    ProgressBar pBar;
    Calendar StartDate, EndDate;
    ArrayList<Slot> slotList;
    int mYear, mMonth, mDay;
    Button btnApply;
    Session session;
    String address = null;
    ScrollView scrollPaymentLyt;
    Activity activity;
    DateAdapter dateAdapter;
    ArrayList<BookingDate> bookingDates;
    RecyclerView recyclerViewDates;
    private TextView tvSubTotal;
    private double finalSubtotal = 0.0, subtotal = 0.0, usedBalance = 0.0, totalAfterTax = 0.0, taxAmt = 0.0, pCodeDiscount = 0.0, dCharge = 0.0;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_payment, container, false);
        activity = getActivity();
        paymentModelClass = new PaymentModelClass(activity);


        Constant.selectedDatePosition = 0;

        session = new Session(getActivity());
        recyclerView = root.findViewById(R.id.recyclerView);
        pBar = root.findViewById(R.id.pBar);
        lytTax = root.findViewById(R.id.lytTax);
        rbCod = root.findViewById(R.id.rbcod);
        rbPayU = root.findViewById(R.id.rbPayU);
        rbPayPal = root.findViewById(R.id.rbPayPal);
        rbRazorPay = root.findViewById(R.id.rbRazorPay);
        tvDelivery = root.findViewById(R.id.tvDelivery);
        tvPayment = root.findViewById(R.id.tvPayment);
        lytPayPal = root.findViewById(R.id.lytPayPal);
        lytRazorPay = root.findViewById(R.id.lytRazorPay);
        lytPayU = root.findViewById(R.id.lytPayU);
        chWallet = root.findViewById(R.id.chWallet);
        lytPayOption = root.findViewById(R.id.lytPayOption);
        lytOrderList = root.findViewById(R.id.lytOrderList);
        lytCLocation = root.findViewById(R.id.lytCLocation);
        lytWallet = root.findViewById(R.id.lytWallet);
        paymentLyt = root.findViewById(R.id.paymentLyt);
        deliveryLyt = root.findViewById(R.id.deliveryLyt);
        tvProceedOrder = root.findViewById(R.id.tvProceedOrder);
        tvConfirmOrder = root.findViewById(R.id.tvConfirmOrder);
        processLyt = root.findViewById(R.id.processLyt);
        tvSelectDeliveryDate = root.findViewById(R.id.tvSelectDeliveryDate);
        deliveryTimeLyt = root.findViewById(R.id.deliveryTimeLyt);
        imgRefresh = root.findViewById(R.id.imgRefresh);
        recyclerViewDates = root.findViewById(R.id.recyclerViewDates);
        tvSubTotal = root.findViewById(R.id.tvSubTotal);
        confirmLyt = root.findViewById(R.id.confirmLyt);
        scrollPaymentLyt = root.findViewById(R.id.scrollPaymentLyt);
        CODLinearLyt = root.findViewById(R.id.CODLinearLyt);
        setHasOptionsMenu(true);

        tvWltBalance = root.findViewById(R.id.tvWltBalance);
        tvPreTotal = root.findViewById(R.id.tvPreTotal);
        btnApply = root.findViewById(R.id.btnApply);

        total = getArguments().getDouble("total");
        subtotal = getArguments().getDouble("subtotal");
        taxAmt = getArguments().getDouble("taxAmt");
        pCodeDiscount = getArguments().getDouble("pCodeDiscount");
        dCharge = getArguments().getDouble("dCharge");

        address = getArguments().getString("address");

        variantIdList = getArguments().getStringArrayList("variantIdList");
        qtyList = getArguments().getStringArrayList("qtyList");

        tvSubTotal.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(subtotal));

        if (AppController.isConnected(getActivity())) {
            ApiConfig.getWalletBalance(getActivity(), session);

            getTimeSlots();
            setPaymentMethod();

            chWallet.setTag("false");

            tvWltBalance.setText("Total Balance: " + Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(Constant.WALLET_BALANCE));

            if (Constant.WALLET_BALANCE == 0) {
                lytWallet.setVisibility(View.GONE);
            } else {
                lytWallet.setVisibility(View.VISIBLE);
            }

            tvProceedOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlaceOrderProcess();
                }
            });

            chWallet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (chWallet.getTag().equals("false")) {
                        chWallet.setChecked(true);
                        lytWallet.setVisibility(View.VISIBLE);

                        if (Constant.WALLET_BALANCE >= subtotal) {
                            usedBalance = subtotal;
                            tvWltBalance.setText(getString(R.string.remaining_wallet_balance) + Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format((Constant.WALLET_BALANCE - usedBalance)));
                            paymentMethod = "wallet";
                            lytPayOption.setVisibility(View.GONE);
                        } else {
                            usedBalance = Constant.WALLET_BALANCE;
                            tvWltBalance.setText(getString(R.string.remaining_wallet_balance) + Constant.SETTING_CURRENCY_SYMBOL + "0.00");
                            lytPayOption.setVisibility(View.VISIBLE);
                        }
                        finalSubtotal = (subtotal - usedBalance);
                        tvSubTotal.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(finalSubtotal));
                        chWallet.setTag("true");

                    } else {
                        walletUncheck();
                    }

                }
            });

        }
        confirmLyt.setVisibility(View.VISIBLE);
        scrollPaymentLyt.setVisibility(View.VISIBLE);


        return root;
    }

    @SuppressLint("SetTextI18n")
    public void walletUncheck() {
        paymentMethod = "";
        rbCod.setChecked(false);
        rbRazorPay.setChecked(false);
        rbPayPal.setChecked(false);
        rbPayU.setChecked(false);
        lytPayOption.setVisibility(View.VISIBLE);
        tvWltBalance.setText(getString(R.string.total) + Constant.SETTING_CURRENCY_SYMBOL + Constant.WALLET_BALANCE);
        usedBalance = 0.00;
        tvSubTotal.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(subtotal));
        chWallet.setChecked(false);
        chWallet.setTag("false");
    }

    public void setPaymentMethod() {
        if (Constant.PAYPAL.equals("1"))
            lytPayPal.setVisibility(View.VISIBLE);
        else
            lytPayPal.setVisibility(View.GONE);

        if (Constant.PAYUMONEY.equals("1"))
            lytPayU.setVisibility(View.VISIBLE);
        else
            lytPayU.setVisibility(View.GONE);

        if (Constant.RAZORPAY.equals("1"))
            lytRazorPay.setVisibility(View.VISIBLE);
        else
            lytRazorPay.setVisibility(View.GONE);

        if (Constant.COD.equals("1"))
            CODLinearLyt.setVisibility(View.VISIBLE);
        else
            CODLinearLyt.setVisibility(View.GONE);

        if (Constant.PAYPAL.equals("0") && Constant.PAYUMONEY.equals("0") && Constant.COD.equals("0") && Constant.RAZORPAY.equals("0")) {
            lytPayOption.setVisibility(View.GONE);
        }

        rbCod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                rbCod.setChecked(true);
                rbPayU.setChecked(false);
                rbPayPal.setChecked(false);
                rbRazorPay.setChecked(false);
                paymentMethod = rbCod.getTag().toString();

            }
        });
        rbPayU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbPayU.setChecked(true);
                rbCod.setChecked(false);
                rbPayPal.setChecked(false);
                rbRazorPay.setChecked(false);
                paymentMethod = rbPayU.getTag().toString();

            }
        });

        rbPayPal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbPayPal.setChecked(true);
                rbCod.setChecked(false);
                rbPayU.setChecked(false);
                rbRazorPay.setChecked(false);
                paymentMethod = rbPayPal.getTag().toString();

            }
        });

        rbRazorPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbRazorPay.setChecked(true);
                rbPayPal.setChecked(false);
                rbCod.setChecked(false);
                rbPayU.setChecked(false);
                paymentMethod = rbRazorPay.getTag().toString();
                Checkout.preload(getContext());
            }
        });
    }

    public void getTimeSlots() {
        GetTimeSlotConfig(session, getActivity());

        GetTimeSlots();

        if (session.getData(Constant.IS_TIME_SLOTS_ENABLE).equals(Constant.GetVal)) {


            StartDate = Calendar.getInstance();
            EndDate = Calendar.getInstance();
            mYear = StartDate.get(Calendar.YEAR);
            mMonth = StartDate.get(Calendar.MONTH);
            mDay = StartDate.get(Calendar.DAY_OF_MONTH);

            int DeliveryStartFrom = Integer.parseInt(session.getData(Constant.DELIVERY_STARTS_FROM));
            int DeliveryAllowFrom = Integer.parseInt(session.getData(Constant.ALLOWED_DAYS));
            if (DeliveryStartFrom > 1) {
                StartDate.add(Calendar.DATE, Integer.parseInt(session.getData(Constant.DELIVERY_STARTS_FROM)));
            }

            EndDate.add(Calendar.DATE, ((DeliveryStartFrom > 1 ? DeliveryStartFrom : 0) + (DeliveryAllowFrom > 1 ? DeliveryAllowFrom - 1 : 0)));

            dateList = ApiConfig.getDates(StartDate.get(Calendar.DATE) + "-" + (StartDate.get(Calendar.MONTH) + 1) + "-" + StartDate.get(Calendar.YEAR), EndDate.get(Calendar.DATE) + "-" + (EndDate.get(Calendar.MONTH) + 1) + "-" + EndDate.get(Calendar.YEAR));
            setDateList(dateList);

        } else {
            deliveryTimeLyt.setVisibility(View.GONE);
            deliveryDay = "Date : N/A";
            deliveryTime = "Time : N/A";

        }
    }

    public void setDateList(ArrayList<String> datesList) {
        bookingDates = new ArrayList<>();
        for (int i = 0; i < datesList.size(); i++) {
            String[] date = datesList.get(i).split("-");

            BookingDate bookingDate1 = new BookingDate();
            bookingDate1.setDate(date[0]);
            bookingDate1.setMonth(date[1]);
            bookingDate1.setYear(date[2]);
            bookingDate1.setDay(date[3]);

            bookingDates.add(bookingDate1);
        }
        dateAdapter = new DateAdapter(getActivity(), bookingDates);

        recyclerViewDates.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewDates.setAdapter(dateAdapter);

    }

    @SuppressLint("SetTextI18n")
    public void PlaceOrderProcess() {
        if (deliveryDay.length() == 0) {
            Toast.makeText(getContext(), getString(R.string.select_delivery_day), Toast.LENGTH_SHORT).show();
            return;
        } else if (deliveryTime.length() == 0) {
            Toast.makeText(getContext(), getString(R.string.select_delivery_time), Toast.LENGTH_SHORT).show();
            return;
        } else if (paymentMethod.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.select_payment_method), Toast.LENGTH_SHORT).show();
            return;
        }
        final Map<String, String> sendparams = new HashMap<String, String>();
        sendparams.put(Constant.PLACE_ORDER, Constant.GetVal);
        sendparams.put(Constant.USER_ID, session.getData(Session.KEY_ID));
        sendparams.put(Constant.TAX_PERCENT, String.valueOf(Constant.SETTING_TAX));
        sendparams.put(Constant.TAX_AMOUNT, "" + taxAmt);
        sendparams.put(Constant.TOTAL, "" + total);
        sendparams.put(Constant.FINAL_TOTAL, "" + subtotal);
        sendparams.put(Constant.PRODUCT_VARIANT_ID, String.valueOf(variantIdList));
        sendparams.put(Constant.QUANTITY, String.valueOf(qtyList));
        sendparams.put(Constant.MOBILE, session.getData(Session.KEY_MOBILE));
        sendparams.put(Constant.DELIVERY_CHARGE, deliveryCharge);
        sendparams.put(Constant.DELIVERY_TIME, (deliveryDay + " - " + deliveryTime));
        sendparams.put(Constant.KEY_WALLET_USED, chWallet.getTag().toString());
        sendparams.put(Constant.KEY_WALLET_BALANCE, String.valueOf(usedBalance));
        sendparams.put(Constant.PAYMENT_METHOD, paymentMethod);
        if (!pCode.isEmpty()) {
            sendparams.put(Constant.PROMO_CODE, pCode);
            sendparams.put(Constant.PROMO_DISCOUNT, Constant.formater.format(pCodeDiscount));
        }
        sendparams.put(Constant.ADDRESS, address);
        sendparams.put(Constant.LONGITUDE, session.getCoordinates(Session.KEY_LONGITUDE));
        sendparams.put(Constant.LATITUDE, session.getCoordinates(Session.KEY_LATITUDE));
        sendparams.put(Constant.EMAIL, session.getData(Session.KEY_EMAIL));

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.dialog_order_confirm, null);
        alertDialog.setView(dialogView);
        alertDialog.setCancelable(true);
        final AlertDialog dialog = alertDialog.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        TextView tvDialogCancel, tvDialogConfirm, tvDialogItemTotal, tvDialogTaxPercent, tvDialogTaxAmt, tvDialogDeliveryCharge, tvDialogTotal, tvDialogPromoCode, tvDialogPCAmount, tvDialogWallet, tvDialogFinalTotal;
        LinearLayout lytDialogPromo, lytDialogWallet;

        lytDialogPromo = dialogView.findViewById(R.id.lytDialogPromo);
        lytDialogWallet = dialogView.findViewById(R.id.lytDialogWallet);
        tvDialogItemTotal = dialogView.findViewById(R.id.tvDialogItemTotal);
        tvDialogTaxPercent = dialogView.findViewById(R.id.tvDialogTaxPercent);
        tvDialogTaxAmt = dialogView.findViewById(R.id.tvDialogTaxAmt);
        tvDialogDeliveryCharge = dialogView.findViewById(R.id.tvDialogDeliveryCharge);
        tvDialogTotal = dialogView.findViewById(R.id.tvDialogTotal);
        tvDialogPCAmount = dialogView.findViewById(R.id.tvDialogPCAmount);
        tvDialogWallet = dialogView.findViewById(R.id.tvDialogWallet);
        tvDialogFinalTotal = dialogView.findViewById(R.id.tvDialogFinalTotal);
        tvDialogCancel = dialogView.findViewById(R.id.tvDialogCancel);
        tvDialogConfirm = dialogView.findViewById(R.id.tvDialogConfirm);
        if (pCodeDiscount > 0) {
            lytDialogPromo.setVisibility(View.VISIBLE);
            tvDialogPCAmount.setText("- " + Constant.SETTING_CURRENCY_SYMBOL + pCodeDiscount);
        } else {
            lytDialogPromo.setVisibility(View.GONE);
        }

        if (chWallet.getTag().toString().equals("true")) {
            lytDialogWallet.setVisibility(View.VISIBLE);
            tvDialogWallet.setText("- " + Constant.SETTING_CURRENCY_SYMBOL + usedBalance);
        } else {
            lytDialogWallet.setVisibility(View.GONE);
        }

        totalAfterTax = (total + dCharge + taxAmt);
        tvDialogItemTotal.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(total));
        tvDialogDeliveryCharge.setText(dCharge > 0 ? Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(Constant.SETTING_DELIVERY_CHARGE) : "Free");
        tvDialogTaxPercent.setText(getString(R.string.tax) + "(" + Constant.SETTING_TAX + "%) :");
        tvDialogTaxAmt.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(taxAmt));
        tvDialogTotal.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(totalAfterTax));
        tvDialogFinalTotal.setText(Constant.SETTING_CURRENCY_SYMBOL + Constant.formater.format(subtotal - usedBalance));

        tvDialogConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paymentMethod.equals(getResources().getString(R.string.codpaytype)) || paymentMethod.equals("wallet")) {
                    ApiConfig.RequestToVolley(new VolleyCallback() {
                        @Override
                        public void onSuccess(boolean result, String response) {
                            if (result) {
                                try {
                                    JSONObject object = new JSONObject(response);
                                    if (!object.getBoolean(Constant.ERROR)) {
                                        if (chWallet.getTag().toString().equals("true"))
                                            ApiConfig.getWalletBalance(getActivity(), session);
                                        dialog.dismiss();
                                        MainActivity.fm.beginTransaction().add(R.id.container, new OrderPlacedFragment()).commit();
                                    } else {
                                        Toast.makeText(getActivity(), object.getString("message"), Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, getActivity(), Constant.ORDERPROCESS_URL, sendparams, true);
                    dialog.dismiss();
                } else {
                    sendparams.put(Constant.USER_NAME, session.getData(Session.KEY_NAME));
                    if (paymentMethod.equals(getString(R.string.pay_u))) {
                        dialog.dismiss();
                        paymentModelClass.OnPayClick(getActivity(), sendparams);
                    } else if (paymentMethod.equals(getString(R.string.paypal))) {
                        dialog.dismiss();
                        StartPayPalPayment(sendparams);
                    } else if (paymentMethod.equals(getString(R.string.razor_pay))) {
                        dialog.dismiss();
                        razorParams = sendparams;
                        CreateOrderId();

                    }
                }
            }
        });

        tvDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void CreateOrderId() {

        String[] amount = String.valueOf(totalAfterTax * 100).split("\\.");
        Map<String, String> params = new HashMap<>();
        params.put("amount", amount[0]);
        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean(Constant.ERROR)) {
                            startPayment(object.getString("id"), object.getString("amount"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, getActivity(), Constant.Get_RazorPay_OrderId, params, true);

    }

    public void startPayment(String orderId, String payAmount) {
        Checkout checkout = new Checkout();
        checkout.setKeyID(Constant.RAZOR_PAY_KEY_VALUE);
        checkout.setImage(R.drawable.ic_launcher);

        try {
            JSONObject options = new JSONObject();
            options.put(Constant.NAME, session.getData(Session.KEY_NAME));
            options.put(Constant.ORDER_ID, orderId);
            options.put(Constant.CURRENCY, "INR");
            options.put(Constant.AMOUNT, payAmount);

            JSONObject preFill = new JSONObject();
            preFill.put(Constant.EMAIL, session.getData(Session.KEY_EMAIL));
            preFill.put(Constant.CONTACT, session.getData(Session.KEY_MOBILE));
            options.put("prefill", preFill);

            checkout.open(getActivity(), options);
        } catch (Exception e) {
            Log.d(TAG, "Error in starting Razorpay Checkout", e);
        }
    }

    public void PlaceOrder(final Activity activity, final String paymentType, final String txnid, boolean issuccess, final Map<String, String> sendparams, final String status) {
        if (issuccess) {
            ApiConfig.RequestToVolley(new VolleyCallback() {
                @Override
                public void onSuccess(boolean result, String response) {

                    if (result) {

                        //System.out.println("PAYMENT ================= " + txnid);
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean(Constant.ERROR)) {
                                AddTransaction(activity, object.getString(Constant.ORDER_ID), paymentType, txnid, status, activity.getString(R.string.order_success), sendparams);
                                MainActivity.fm.beginTransaction().add(R.id.container, new OrderPlacedFragment()).commit();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, activity, Constant.ORDERPROCESS_URL, sendparams, false);
        } else {

            AddTransaction(activity, "", getString(R.string.razor_pay), txnid, status, getString(R.string.order_failed), sendparams);
        }
    }

    public void AddTransaction(Activity activity, String orderId, String paymentType, String txnid, final String status, String message, Map<String, String> sendparams) {
        Map<String, String> transparams = new HashMap<>();
        transparams.put(Constant.Add_TRANSACTION, Constant.GetVal);
        transparams.put(Constant.USER_ID, sendparams.get(Constant.USER_ID));
        transparams.put(Constant.ORDER_ID, orderId);
        transparams.put(Constant.TYPE, paymentType);
        transparams.put(Constant.TRANS_ID, txnid);
        transparams.put(Constant.AMOUNT, sendparams.get(Constant.FINAL_TOTAL));
        transparams.put(Constant.STATUS, status);
        transparams.put(Constant.MESSAGE, message);
        Date c = Calendar.getInstance().getTime();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        transparams.put("transaction_date", df.format(c));

        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {
                    try {
                        JSONObject objectbject = new JSONObject(response);
                        if (!objectbject.getBoolean(Constant.ERROR)) {
                            if (status.equals("Failed")) {

                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, activity, Constant.ORDERPROCESS_URL, transparams, false);
    }

    public void StartPayPalPayment(final Map<String, String> sendParams) {

        final Map<String, String> params = new HashMap<>();
        params.put(Constant.FIRST_NAME, sendParams.get(Constant.USER_NAME));
        params.put(Constant.LAST_NAME, sendParams.get(Constant.USER_NAME));
        params.put(Constant.PAYER_EMAIL, sendParams.get(Constant.EMAIL));
        params.put(Constant.ITEM_NAME, "Card Order");
        params.put(Constant.ITEM_NUMBER, System.currentTimeMillis() + Constant.randomNumeric(3));
        params.put(Constant.AMOUNT, sendParams.get(Constant.FINAL_TOTAL));
        // System.out.println("======params paypal "+params.toString());
        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                //System.out.println("=====url paypal == "+response );
                Intent intent = new Intent(getContext(), PayPalWebActivity.class);
                intent.putExtra("url", response);
                intent.putExtra("item_no", params.get(Constant.ITEM_NUMBER));
                intent.putExtra("params", (Serializable) sendParams);
                startActivity(intent);
            }
        }, getActivity(), Constant.PAPAL_URL, params, true);
    }


    public void GetTimeSlots() {
        slotList = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        params.put("get_time_slots", Constant.GetVal);

        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        JSONObject object = new JSONObject(response);

                        if (!object.getBoolean(Constant.ERROR)) {
                            JSONArray jsonArray = object.getJSONArray("time_slots");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object1 = jsonArray.getJSONObject(i);
                                slotList.add(new Slot(object1.getString("id"), object1.getString("title"), object1.getString("last_order_time")));
                            }

                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                            adapter = new SlotAdapter(deliveryTime, getActivity(), slotList);
                            recyclerView.setAdapter(adapter);


                            deliveryTimeLyt.setVisibility(View.VISIBLE);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, getActivity(), Constant.SETTING_URL, params, true);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null)
            paymentModelClass.TrasactionMethod(data, getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.payment);
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