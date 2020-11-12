package com.barisalbazar.shop.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.barisalbazar.shop.R;
import com.barisalbazar.shop.fragment.WebViewFragment;
import com.barisalbazar.shop.helper.ApiConfig;
import com.barisalbazar.shop.helper.AppController;
import com.barisalbazar.shop.helper.Constant;
import com.barisalbazar.shop.helper.DatabaseHelper;
import com.barisalbazar.shop.helper.GPSTracker;
import com.barisalbazar.shop.helper.PinView;
import com.barisalbazar.shop.helper.ProgressDisplay;
import com.barisalbazar.shop.helper.Session;
import com.barisalbazar.shop.helper.Utils;
import com.barisalbazar.shop.helper.VolleyCallback;
import com.barisalbazar.shop.model.City;

public class LoginActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static EditText edtaddress;
    public static SupportMapFragment mapFragment;
    public static OnMapReadyCallback mapReadyCallback;
    ProgressDisplay progress;
    LinearLayout lytchangpsw, lytforgot, lytlogin, signUpLyt, lytotp, lytverify, lytResetPass, lytPrivacy;
    EditText edtResetPass, edtResetCPass, edtnewpsw, edtRefer, edtoldpsw, edtforgotmobile, edtloginpassword, edtLoginMobile, edtname, edtemail, edtmobile, edtPinCode, edtpsw, edtcpsw, edtMobVerify;//edtpincode
    Button btnotpverify, btnEmailVerify, btnsubmit, btnResetPass;
    CountryCodePicker edtCode, edtFCode;
    String from, mobile, fromto, city, area, cityId = "0", areaId = "0";
    PinView edtotp;
    TextView txtmobileno, tvResend, tvSignUp, tvForgotPass, tvPrivacyPolicy, tvResendPass, tvTime;
    ScrollView scrollView;
    AppCompatSpinner cityspinner, areaSpinner;
    ArrayList<City> cityArrayList, areaList;
    GPSTracker gps;
    int selectedCityId = 0;
    Session session;
    Toolbar toolbar;
    CheckBox chPrivacy;
    ////Firebase
    String phoneNumber, firebase_otp, otpFor = "";
    FirebaseAuth auth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    DatabaseHelper databaseHelper;
    Activity activity;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        activity = LoginActivity.this;
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        databaseHelper = new DatabaseHelper(activity);

        from = getIntent().getStringExtra("from");
        fromto = getIntent().getStringExtra("fromto");
        mobile = getIntent().getStringExtra("txtmobile");
        firebase_otp = getIntent().getStringExtra("OTP");

        gps = new GPSTracker(activity);
        session = new Session(getApplicationContext());
        cityArrayList = new ArrayList<>();
        areaList = new ArrayList<>();
        chPrivacy = findViewById(R.id.chPrivacy);
        txtmobileno = findViewById(R.id.txtmobileno);
        cityspinner = findViewById(R.id.cityspinner);
        areaSpinner = findViewById(R.id.areaSpinner);
        edtnewpsw = findViewById(R.id.edtnewpsw);
        edtoldpsw = findViewById(R.id.edtoldpsw);
        edtCode = findViewById(R.id.edtCode);
        edtFCode = findViewById(R.id.edtFCode);
        edtResetPass = findViewById(R.id.edtResetPass);
        edtResetCPass = findViewById(R.id.edtResetCPass);
        edtforgotmobile = findViewById(R.id.edtforgotmobile);
        edtloginpassword = findViewById(R.id.edtloginpassword);
        edtLoginMobile = findViewById(R.id.edtLoginMobile);
        lytchangpsw = findViewById(R.id.lytchangpsw);
        lytforgot = findViewById(R.id.lytforgot);
        lytlogin = findViewById(R.id.lytlogin);
        lytResetPass = findViewById(R.id.lytResetPass);
        lytPrivacy = findViewById(R.id.lytPrivacy);
        scrollView = findViewById(R.id.scrollView);
        edtotp = findViewById(R.id.edtotp);
        btnResetPass = findViewById(R.id.btnResetPass);
        btnsubmit = findViewById(R.id.btnsubmit);
        btnEmailVerify = findViewById(R.id.btnEmailVerify);
        btnotpverify = findViewById(R.id.btnotpverify);
        edtMobVerify = findViewById(R.id.edtMobVerify);
        lytverify = findViewById(R.id.lytverify);
        signUpLyt = findViewById(R.id.signUpLyt);
        lytotp = findViewById(R.id.lytotp);
        edtotp = findViewById(R.id.edtotp);
        edtname = findViewById(R.id.edtname);
        edtemail = findViewById(R.id.edtemail);
        edtmobile = findViewById(R.id.edtmobile);
        edtaddress = findViewById(R.id.edtaddress);
        edtPinCode = findViewById(R.id.edtPinCode);
        edtpsw = findViewById(R.id.edtpsw);
        edtcpsw = findViewById(R.id.edtcpsw);
        edtRefer = findViewById(R.id.edtRefer);
        tvResend = findViewById(R.id.tvResend);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPass = findViewById(R.id.tvForgotPass);
        tvPrivacyPolicy = findViewById(R.id.tvPrivacy);

        tvResendPass = findViewById(R.id.tvResendPass);
        tvTime = findViewById(R.id.tvTime);
        tvSignUp.setText(underlineSpannable(getString(R.string.not_registered)));
        tvForgotPass.setText(underlineSpannable(getString(R.string.forgottext)));
        tvResendPass.setText(underlineSpannable(getString(R.string.resend_cap)));

        mapReadyCallback = new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                final GoogleMap mMap = googleMap;
                double saveLatitude = Double.parseDouble(session.getCoordinates(Session.KEY_LATITUDE));
                double saveLongitude = Double.parseDouble(session.getCoordinates(Session.KEY_LONGITUDE));
                mMap.clear();

                LatLng latLng = new LatLng(saveLatitude, saveLongitude);
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .draggable(true)
                        .title(getString(R.string.current_location)));

                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
            }
        };

        edtLoginMobile.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_phone, 0, 0, 0);

        edtoldpsw.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_password, 0, R.drawable.ic_show, 0);
        edtnewpsw.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_password, 0, R.drawable.ic_show, 0);

        edtloginpassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_password, 0, R.drawable.ic_show, 0);
        edtpsw.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_password, 0, R.drawable.ic_show, 0);
        edtcpsw.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_password, 0, R.drawable.ic_show, 0);
        edtResetPass.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_password, 0, R.drawable.ic_show, 0);
        edtResetCPass.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_password, 0, R.drawable.ic_show, 0);
        Utils.setHideShowPassword(edtpsw);
        Utils.setHideShowPassword(edtcpsw);
        Utils.setHideShowPassword(edtloginpassword);
        Utils.setHideShowPassword(edtoldpsw);
        Utils.setHideShowPassword(edtnewpsw);
        Utils.setHideShowPassword(edtResetPass);
        Utils.setHideShowPassword(edtResetCPass);

        progress = new ProgressDisplay(this);

//        Constant.country_code = "92"; // your country code
//        session.setData(Constant.COUNTRY_CODE, Constant.country_code);
//        edtCode.setCountryForNameCode("" + Constant.country_code);
//        edtFCode.setCountryForNameCode("" + Constant.country_code);

        if (from != null) {
            switch (from) {
                case "forgot":
                    lytforgot.setVisibility(View.VISIBLE);
                    break;
                case "changepsw":
                    lytchangpsw.setVisibility(View.VISIBLE);
                    break;
                case "reset_pass":
                    lytResetPass.setVisibility(View.VISIBLE);
                    break;
                case "register":
                    ApiConfig.getLocation(activity);

                    lytverify.setVisibility(View.VISIBLE);
                    break;
                case "otp_verify":
                case "otp_forgot":
                    lytotp.setVisibility(View.VISIBLE);
                    txtmobileno.setText(getResources().getString(R.string.please_type_verification_code_sent_to) + "  +" + Constant.country_code + " - " + mobile);
                    break;
                case "drawer":
                case "checkout":
                    lytlogin.setVisibility(View.VISIBLE);
                    break;
                default:
                    signUpLyt.setVisibility(View.VISIBLE);
                    edtmobile.setText(mobile);
                    edtRefer.setText(Constant.FRND_CODE);
                    SetCitySpinnerData();
                    break;
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            lytlogin.setVisibility(View.VISIBLE);

        }

        cityspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                city = cityspinner.getSelectedItem().toString();
                cityId = cityArrayList.get(position).getCity_id();
                SetAreaSpinnerData(cityId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                area = areaSpinner.getSelectedItem().toString();
                areaId = areaList.get(position).getCity_id();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(mapReadyCallback);
        StartFirebaseLogin();
        PrivacyPolicy();
    }


    public void generateOTP() {
        final String mobile = edtMobVerify.getText().toString().trim();
        final String code = edtCode.getSelectedCountryCode();
        if (ApiConfig.CheckValidattion(mobile, false, false)) {
            edtMobVerify.setError(getString(R.string.enter_mobile_no));
        } else if (ApiConfig.CheckValidattion(mobile, false, true)) {
            edtMobVerify.setError(getString(R.string.enter_valid_mobile_no));
        } else if (AppController.isConnected(activity)) {
            session.setData(Constant.COUNTRY_CODE, code);
            Constant.country_code = code;
            Map<String, String> params = new HashMap<String, String>();
            params.put(Constant.TYPE, Constant.VERIFY_USER);
            params.put(Constant.MOBILE, mobile);
            ApiConfig.RequestToVolley(new VolleyCallback() {
                @Override
                public void onSuccess(boolean result, String response) {
                    if (result) {
                        try {
                            //System.out.println ("=================*verify  " + response);
                            JSONObject object = new JSONObject(response);
                            otpFor = "otp_verify";
                            phoneNumber = "+" + Constant.country_code + mobile;
                            if (!object.getBoolean(Constant.ERROR)) {
                                sentRequest(phoneNumber);
                            } else {
                                setSnackBar(getString(R.string.verify_alert_1) + getString(R.string.app_name) + getString(R.string.verify_alert_2), getString(R.string.btn_ok), from);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, activity, Constant.RegisterUrl, params, true);
        }
    }


    public void sentRequest(String phoneNumber) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,                     // Phone number to verify
                60,                           // Timeout duration
                TimeUnit.SECONDS,                // Unit of timeout
                activity,        // Activity (for callback binding)
                mCallback);
    }

    private void StartFirebaseLogin() {
        auth = FirebaseAuth.getInstance();
        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NotNull PhoneAuthCredential phoneAuthCredential) {
                //System.out.println ("====verification complete call  " + phoneAuthCredential.getSmsCode ());
            }

            @Override
            public void onVerificationFailed(@NotNull FirebaseException e) {
                setSnackBar(e.getLocalizedMessage(), getString(R.string.btn_ok), "failed");
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Constant.verificationCode = s;
                String mobileno = "";

                if (otpFor.equals("resend")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.otp_resend_alert), Toast.LENGTH_SHORT).show();
                } else {
                    if (otpFor.equals("otp_forgot")) {
                        mobileno = edtforgotmobile.getText().toString();
                    } else {
                        mobileno = edtMobVerify.getText().toString();
                    }
                    startActivity(new Intent(activity, LoginActivity.class)
                            .putExtra("from", otpFor)
                            .putExtra("txtmobile", mobileno)
                            .putExtra("OTP", s));
                }
            }
        };
    }

    public void ChangePassword() {
        final Session sessionpsw = new Session(activity);
        String oldpsw = edtoldpsw.getText().toString();
        String password = edtnewpsw.getText().toString();

        if (ApiConfig.CheckValidattion(oldpsw, false, false)) {
            edtoldpsw.setError(getString(R.string.enter_old_pass));
        } else if (ApiConfig.CheckValidattion(password, false, false)) {
            edtnewpsw.setError(getString(R.string.enter_new_pass));
        } else if (!oldpsw.equals(sessionpsw.getData(Session.KEY_Password))) {
            edtoldpsw.setError(getString(R.string.no_match_old_pass));
        } else if (AppController.isConnected(activity)) {
            final Map<String, String> params = new HashMap<String, String>();
            params.put(Constant.TYPE, Constant.CHANGE_PASSWORD);
            params.put(Constant.PASSWORD, password);
            params.put(Constant.ID, sessionpsw.getData(Session.KEY_ID));

            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
            // Setting Dialog Message
            alertDialog.setTitle(getString(R.string.change_pass));
            alertDialog.setMessage(getString(R.string.reset_alert_msg));
            alertDialog.setCancelable(false);
            final AlertDialog alertDialog1 = alertDialog.create();

            // Setting OK Button
            alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ApiConfig.RequestToVolley(new VolleyCallback() {
                        @Override
                        public void onSuccess(boolean result, String response) {
                            //  System.out.println("=================*changepsw " + response);
                            if (result) {
                                try {
                                    JSONObject object = new JSONObject(response);
                                    if (!object.getBoolean(Constant.ERROR)) {
                                        sessionpsw.logoutUser(activity);
                                    }
                                    Toast.makeText(activity, object.getString("message"), Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, activity, Constant.RegisterUrl, params, true);

                }
            });
            alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog1.dismiss();
                }
            });
            // Showing Alert Message
            alertDialog.show();
        }
    }

    public void ResetPassword() {

        String reset_psw = edtResetPass.getText().toString();
        String reset_c_psw = edtResetCPass.getText().toString();

        if (ApiConfig.CheckValidattion(reset_psw, false, false)) {
            edtResetPass.setError(getString(R.string.enter_new_pass));
        } else if (ApiConfig.CheckValidattion(reset_c_psw, false, false)) {
            edtResetCPass.setError(getString(R.string.enter_confirm_pass));
        } else if (!reset_psw.equals(reset_c_psw)) {
            edtResetPass.setError(getString(R.string.pass_not_match));
        } else if (AppController.isConnected(activity)) {
            final Map<String, String> params = new HashMap<String, String>();
            params.put(Constant.TYPE, Constant.CHANGE_PASSWORD);
            params.put(Constant.PASSWORD, reset_c_psw);
            //params.put(Constant.ID, session.getData(Session.KEY_ID));
            params.put(Constant.ID, Constant.U_ID);

            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
            // Setting Dialog Message
            alertDialog.setTitle(getString(R.string.reset_pass));
            alertDialog.setMessage(getString(R.string.reset_alert_msg));
            alertDialog.setCancelable(false);
            final AlertDialog alertDialog1 = alertDialog.create();
            // Setting OK Button
            alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ApiConfig.RequestToVolley(new VolleyCallback() {
                        @Override
                        public void onSuccess(boolean result, String response) {

                            if (result) {
                                try {
                                    JSONObject object = new JSONObject(response);
                                    if (!object.getBoolean(Constant.ERROR)) {
                                        setSnackBar(getString(R.string.msg_reset_pass_success), getString(R.string.btn_ok), from);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, activity, Constant.RegisterUrl, params, true);

                }
            });
            alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog1.dismiss();
                }
            });
            // Showing Alert Message
            alertDialog.show();
        }
    }

    public void RecoverPassword() {
        final String mobile = edtforgotmobile.getText().toString().trim();
        String code = edtFCode.getSelectedCountryCode();
        if (ApiConfig.CheckValidattion(mobile, false, false)) {
            edtforgotmobile.setError(getString(R.string.enter_mobile_no));
        } else if (mobile.length() != 0 && ApiConfig.CheckValidattion(mobile, false, true)) {
            edtforgotmobile.setError(getString(R.string.enter_valid_mobile_no));

        } else {
            session.setData(Constant.COUNTRY_CODE, code);
            Constant.country_code = code;
            Map<String, String> params = new HashMap<String, String>();
            params.put(Constant.TYPE, Constant.VERIFY_USER);
            params.put(Constant.MOBILE, mobile);
            ApiConfig.RequestToVolley(new VolleyCallback() {
                @Override
                public void onSuccess(boolean result, String response) {
                    if (result) {
                        try {
                            //System.out.println("=================*verify  " + response);
                            JSONObject object = new JSONObject(response);
                            otpFor = "otp_forgot";
                            phoneNumber = ("+" + Constant.country_code + mobile);
                            if (object.getBoolean(Constant.ERROR)) {
                                Constant.U_ID = object.getString(Constant.ID);
                                sentRequest(phoneNumber);
                            } else {
                                setSnackBar(getString(R.string.alert_register_num1) + getString(R.string.app_name) + getString(R.string.alert_register_num2), getString(R.string.btn_ok), from);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, activity, Constant.RegisterUrl, params, true);
        }
    }

    public void UserLogin() {
        String email = edtLoginMobile.getText().toString();
        final String password = edtloginpassword.getText().toString();

        if (ApiConfig.CheckValidattion(email, false, false)) {
            edtLoginMobile.setError(getString(R.string.enter_mobile_no));
        } else if (ApiConfig.CheckValidattion(email, false, true)) {
            edtLoginMobile.setError(getString(R.string.enter_valid_mobile_no));
        } else if (ApiConfig.CheckValidattion(password, false, false)) {
            edtloginpassword.setError(getString(R.string.enter_pass));
        } else if (AppController.isConnected(activity)) {
            Map<String, String> params = new HashMap<String, String>();
            params.put(Constant.MOBILE, email);
            params.put(Constant.PASSWORD, password);
            params.put(Constant.FCM_ID, "" + AppController.getInstance().getDeviceToken());
            ApiConfig.RequestToVolley(new VolleyCallback() {
                @Override
                public void onSuccess(boolean result, String response) {
                    //System.out.println ("============login res " + response);
                    if (result) {
                        try {
                            JSONObject objectbject = new JSONObject(response);
                            if (!objectbject.getBoolean(Constant.ERROR)) {
                                StartMainActivity(objectbject, password);
                            }
                            Toast.makeText(activity, objectbject.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, activity, Constant.LoginUrl, params, true);
        }
    }


    public void setSnackBar(String message, String action, final String type) {
        final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(action, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type.equals("reset_pass") || type.equals("forgot") || type.equals("register")) {
                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                snackbar.dismiss();
            }
        });
        snackbar.setActionTextColor(Color.RED);
        View snackbarView = snackbar.getView();
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.show();
    }

    public void OTP_Varification() {
        String otptext = edtotp.getText().toString().trim();

        if (ApiConfig.CheckValidattion(otptext, false, false)) {
            edtotp.setError(getString(R.string.enter_otp));
        } else {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(firebase_otp, otptext);
            signInWithPhoneAuthCredential(credential, otptext);

        }

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, final String otptext) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //verification successful we will start the profile activity
                            if (from.equalsIgnoreCase("otp_verify")) {
                                startActivity(new Intent(activity, LoginActivity.class).putExtra("from", "info").putExtra("txtmobile", mobile).putExtra("OTP", otptext));
                            } else {
                                startActivity(new Intent(activity, LoginActivity.class).putExtra("from", "reset_pass"));
                            }
                            edtotp.setError(null);
                        } else {

                            //verification unsuccessful.. display an error message
                            String message = "Something is wrong, we will fix it soon...";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }
                            edtotp.setError(message);

                        }
                    }
                });
    }

    public void UserSignUpSubmit(String latitude, String longitude) {

        String name = edtname.getText().toString().trim();
        String email = "" + edtemail.getText().toString().trim();
        String mobile = edtmobile.getText().toString().trim();
        String address = edtaddress.getText().toString().trim();
        String pincode = edtPinCode.getText().toString().trim();
        final String password = edtpsw.getText().toString().trim();
        String cpassword = edtcpsw.getText().toString().trim();


        if (ApiConfig.CheckValidattion(name, false, false)) {
            edtname.setError(getString(R.string.enter_name));
            scrollView.scrollTo(0, edtname.getBottom());
        } else if (ApiConfig.CheckValidattion(email, false, false))
            edtemail.setError(getString(R.string.enter_email));
        else if (ApiConfig.CheckValidattion(email, true, false))
            edtemail.setError(getString(R.string.enter_valid_email));
        else if (cityId.equals("0")) {
            Toast.makeText(activity, getResources().getString(R.string.selectcity), Toast.LENGTH_LONG).show();
            scrollView.scrollTo(0, cityspinner.getBottom());
        } else if (areaId.equals("0")) {
            Toast.makeText(activity, getResources().getString(R.string.selectarea), Toast.LENGTH_LONG).show();
            scrollView.scrollTo(0, areaSpinner.getBottom());
        } else if (ApiConfig.CheckValidattion(pincode, false, false)) {
            edtPinCode.setError(getString(R.string.enter_pincode));
            scrollView.scrollTo(0, edtPinCode.getBottom());
        } else if (ApiConfig.CheckValidattion(address, false, false)) {
            edtaddress.setError(getString(R.string.enter_address));
            scrollView.scrollTo(0, edtaddress.getBottom());
        } else if (ApiConfig.CheckValidattion(password, false, false)) {
            edtpsw.setError(getString(R.string.enter_pass));
            scrollView.scrollTo(0, edtpsw.getBottom());
        } else if (ApiConfig.CheckValidattion(cpassword, false, false)) {
            edtcpsw.setError(getString(R.string.enter_confirm_pass));
            scrollView.scrollTo(0, edtcpsw.getBottom());
        } else if (!password.equals(cpassword)) {
            edtcpsw.setError(getString(R.string.pass_not_match));
            scrollView.scrollTo(0, edtcpsw.getBottom());
        } else if (latitude.equals("0") || longitude.equals("0"))
            Toast.makeText(activity, getString(R.string.alert_select_location), Toast.LENGTH_LONG).show();
        else if (!chPrivacy.isChecked()) {
            Toast.makeText(activity, getString(R.string.alert_privacy_msg), Toast.LENGTH_LONG).show();
        } else if (AppController.isConnected(activity)) {
            Map<String, String> params = new HashMap<String, String>();
            params.put(Constant.TYPE, Constant.REGISTER);
            params.put(Constant.NAME, name);
            params.put(Constant.EMAIL, email);
            params.put(Constant.MOBILE, mobile);
            params.put(Constant.PASSWORD, password);
            params.put(Constant.PINCODE, pincode);
            params.put(Constant.CITY_ID, cityId);
            params.put(Constant.AREA_ID, areaId);
            params.put(Constant.STREET, address);
            params.put(Constant.LONGITUDE, longitude);
            params.put(Constant.LATITUDE, latitude);
            params.put(Constant.COUNTRY_CODE, Constant.country_code);
            params.put(Constant.REFERRAL_CODE, Constant.randomAlphaNumeric(8));
            params.put(Constant.FRIEND_CODE, edtRefer.getText().toString().trim());

            params.put(Constant.FCM_ID, "" + AppController.getInstance().getDeviceToken());
            //System.out.println("==========params " + params);
            ApiConfig.RequestToVolley(new VolleyCallback() {
                @Override
                public void onSuccess(boolean result, String response) {
                    //System.out.println("=================*register " + response);
                    if (result) {
                        try {
                            JSONObject objectbject = new JSONObject(response);
                            if (!objectbject.getBoolean(Constant.ERROR)) {

                                StartMainActivity(objectbject, password);

                            }
                            Toast.makeText(activity, objectbject.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, activity, Constant.RegisterUrl, params, true);
        }
    }

    public void OnBtnClick(View view) {
        int id = view.getId();

        if (id == R.id.tvSignUp) {
            startActivity(new Intent(activity, LoginActivity.class).putExtra("from", "register").putExtra("from", "register"));
        } else if (id == R.id.tvForgotPass) {
            startActivity(new Intent(activity, LoginActivity.class).putExtra("from", "forgot"));
        } else if (id == R.id.btnchangepsw) {

            ChangePassword();

        } else if (id == R.id.btnResetPass) {
            hideKeyboard(activity, view);
            ResetPassword();

        } else if (id == R.id.btnrecover) {
            hideKeyboard(activity, view);
            RecoverPassword();

        } else if (id == R.id.tvResendPass) {
            hideKeyboard(activity, view);
            RecoverPassword();

        } else if (id == R.id.btnlogin) {
            hideKeyboard(activity, view);
            UserLogin();

        } else if (id == R.id.btnEmailVerify) {
            hideKeyboard(activity, view);
            generateOTP();

        } else if (id == R.id.tvResend) {
            otpFor = "resend";
            sentRequest("+" + Constant.country_code + mobile);

        } else if (id == R.id.btnotpverify) {
            hideKeyboard(activity, view);
            OTP_Varification();

        } else if (id == R.id.btnsubmit) {
            double saveLatitude = Double.parseDouble(new Session(getApplicationContext()).getCoordinates(Session.KEY_LATITUDE));
            double saveLongitude = Double.parseDouble(new Session(getApplicationContext()).getCoordinates(Session.KEY_LONGITUDE));

            if (saveLatitude == 0 || saveLongitude == 0) {
                UserSignUpSubmit(String.valueOf(gps.latitude), String.valueOf(gps.longitude));
            } else {
                UserSignUpSubmit(new Session(getApplicationContext()).getCoordinates(Session.KEY_LATITUDE), new Session(getApplicationContext()).getCoordinates(Session.KEY_LONGITUDE));
            }


        } else if (id == R.id.tvUpdate) {
            if (ApiConfig.isGPSEnable(LoginActivity.this))
                startActivity(new Intent(LoginActivity.this, MapActivity.class));
            else
                ApiConfig.displayLocationSettingsRequest(LoginActivity.this);
        }

    }

    public void StartMainActivity(JSONObject objectbject, String password) {
        try {
            new Session(activity).createUserLoginSession(objectbject.getString(Constant.PROFILE)
                    , AppController.getInstance().getDeviceToken(),
                    objectbject.getString(Constant.USER_ID),
                    objectbject.getString(Constant.NAME),
                    objectbject.getString(Constant.EMAIL),
                    objectbject.getString(Constant.MOBILE),
                    objectbject.getString(Constant.DOB),
                    objectbject.getString(Constant.CITY_NAME),
                    objectbject.getString(Constant.AREA_NAME),
                    objectbject.getString(Constant.CITY_ID),
                    objectbject.getString(Constant.AREA_ID),
                    objectbject.getString(Constant.STREET),
                    objectbject.getString(Constant.PINCODE),
                    objectbject.getString(Constant.STATUS),
                    objectbject.getString(Constant.CREATEDATE),
                    objectbject.getString(Constant.APIKEY),
                    password,
                    objectbject.getString(Constant.REFERRAL_CODE),
                    objectbject.getString(Constant.LATITUDE),
                    objectbject.getString(Constant.LONGITUDE));


            DrawerActivity.imgProfile.setImageUrl(session.getData(Constant.PROFILE), Constant.imageLoader);

            ApiConfig.AddMultipleProductInCart(session, activity, databaseHelper.getDataCartList());
            ApiConfig.getCartItemCount(activity, session);

            ArrayList<String> favorites = databaseHelper.getFavourite();
            for (int i = 0; i < favorites.size(); i++) {
                ApiConfig.AddOrRemoveFavorite(activity, session, favorites.get(i), true);
            }

            databaseHelper.DeleteAllFavoriteData();
            databaseHelper.DeleteAllOrderData();


            session.setData(Constant.COUNTRY_CODE, objectbject.getString("country_code"));
            Constant.country_code = objectbject.getString("country_code");

            Intent intent = new Intent(activity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("from", "");
            if (fromto != null && fromto.equals("checkout")) {
                intent.putExtra("from", "checkout");
            }
            startActivity(intent);

            finish();
        } catch (
                JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        final GoogleMap mMap = googleMap;
        mMap.clear();
        LatLng latLng;

        double saveLatitude = Double.parseDouble(new Session(getApplicationContext()).getCoordinates(Session.KEY_LATITUDE));
        double saveLongitude = Double.parseDouble(new Session(getApplicationContext()).getCoordinates(Session.KEY_LONGITUDE));

        if (saveLatitude == 0 || saveLongitude == 0) {
            latLng = new LatLng(gps.latitude, gps.longitude);
        } else {
            latLng = new LatLng(saveLatitude, saveLongitude);
        }

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title(getString(R.string.current_location)));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

    }

    public SpannableString underlineSpannable(String text) {
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new UnderlineSpan(), 0, text.length(), 0);
        return spannableString;
    }

    public void PrivacyPolicy() {
        tvPrivacyPolicy.setClickable(true);
        tvPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());

        String message = getString(R.string.msg_privacy_terms);
        String s2 = getString(R.string.terms_conditions);
        String s1 = getString(R.string.privacy_policy);
        final Spannable wordtoSpan = new SpannableString(message);

        wordtoSpan.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new WebViewFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", "privacy");
                fragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                ds.isUnderlineText();
            }
        }, message.indexOf(s1), message.indexOf(s1) + s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        wordtoSpan.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new WebViewFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", "terms");
                fragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                ds.isUnderlineText();
            }
        }, message.indexOf(s2), message.indexOf(s2) + s2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPrivacyPolicy.setText(wordtoSpan);
    }

    private void SetCitySpinnerData() {
        Map<String, String> params = new HashMap<String, String>();
        cityArrayList.clear();
        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        // System.out.println("====res city " + response);
                        JSONObject objectbject = new JSONObject(response);
                        if (!objectbject.getBoolean(Constant.ERROR)) {
                            cityArrayList.add(0, new City("0", getString(R.string.select_city)));
                            JSONArray jsonArray = objectbject.getJSONArray(Constant.DATA);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                if (session.getData(Session.KEY_CITY_ID).equals(jsonObject.getString(Constant.ID))) {
                                    selectedCityId = i;
                                }
                                cityArrayList.add(new City(jsonObject.getString(Constant.ID), jsonObject.getString(Constant.NAME)));
                            }
                            cityspinner.setAdapter(new ArrayAdapter<City>(activity, R.layout.spinner_item, cityArrayList));
                            cityspinner.setSelection(selectedCityId);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, activity, Constant.CITY_URL, params, false);
    }

    private void SetAreaSpinnerData(String cityId) {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.CITY_ID, cityId);
        areaList.clear();
        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        // System.out.println("====res area " + response);
                        JSONObject objectbject = new JSONObject(response);
                        if (!objectbject.getBoolean(Constant.ERROR)) {
                            JSONArray jsonArray = objectbject.getJSONArray(Constant.DATA);
                            areaList.add(0, new City("0", getString(R.string.select_area)));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                areaList.add(new City(jsonObject.getString(Constant.ID), jsonObject.getString(Constant.NAME)));
                            }
                            areaSpinner.setAdapter(new ArrayAdapter<City>(activity, R.layout.spinner_item, areaList));


                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, activity, Constant.GET_AREA_BY_CITY, params, false);
    }

    @Override
    public void onResume() {
        mapFragment.getMapAsync(mapReadyCallback);
        super.onResume();
    }

    public void hideKeyboard(Activity activity, View root) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}




