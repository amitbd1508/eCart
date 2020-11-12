package com.barisalbazar.shop.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.barisalbazar.shop.R;
import com.barisalbazar.shop.activity.DrawerActivity;
import com.barisalbazar.shop.activity.LoginActivity;
import com.barisalbazar.shop.activity.MainActivity;
import com.barisalbazar.shop.helper.AndroidMultiPartEntity;
import com.barisalbazar.shop.helper.ApiConfig;
import com.barisalbazar.shop.helper.AppController;
import com.barisalbazar.shop.helper.Constant;
import com.barisalbazar.shop.helper.Session;
import com.barisalbazar.shop.helper.VolleyCallback;
import com.barisalbazar.shop.model.City;
import com.barisalbazar.shop.ui.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.barisalbazar.shop.helper.ApiConfig.createJWT;
import static com.barisalbazar.shop.helper.ApiConfig.getAddress;

public class ProfileFragment extends Fragment implements OnMapReadyCallback {

    public static int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static TextView tvCurrent;
    public static SupportMapFragment mapFragment;
    public static OnMapReadyCallback mapReadyCallback;
    public static int SELECT_FILE = 110;
    public CircleImageView imgProfile;
    public FloatingActionButton fabProfile;
    public int reqReadPermission = 1;
    public int reqWritePermission = 2;
    public ProgressBar progressBar;
    View root;
    TextView txtchangepassword, txtdob, tvUpdate;
    EditText edtname, edtemail, edtaddress, edtPinCode, edtMobile;//edtcity;
    Session session;
    String city = "0", area = "0", cityId, areaId;
    ArrayList<City> cityArrayList, areaList;
    AppCompatSpinner cityspinner, areaSpinner;
    Button btnsubmit;
    Activity activity;
    Uri fileUri;
    File sourceFile;
    long totalSize = 0;
    private File output = null;
    private String filePath = null;


    private DatePickerDialog.OnDateSetListener pickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
            String monthString = String.valueOf(selectedMonth + 1);
            String dateString = String.valueOf(selectedDay);
            if (monthString.length() == 1) {
                monthString = "0" + monthString;
            }
            if (dateString.length() == 1) {
                dateString = "0" + dateString;
            }
            txtdob.setText(dateString + "-" + monthString + "-" + selectedYear);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_profile, container, false);
        activity = getActivity();


        cityArrayList = new ArrayList<>();
        areaList = new ArrayList<>();
        cityspinner = root.findViewById(R.id.cityspinner);
        areaSpinner = root.findViewById(R.id.areaSpinner);
        txtdob = root.findViewById(R.id.txtdob);
        tvCurrent = root.findViewById(R.id.tvCurrent);
        edtname = root.findViewById(R.id.edtname);
        edtemail = root.findViewById(R.id.edtemail);
        edtMobile = root.findViewById(R.id.edtMobile);
        edtaddress = root.findViewById(R.id.edtaddress);
        edtPinCode = root.findViewById(R.id.edtPinCode);
        tvUpdate = root.findViewById(R.id.tvUpdate);
        btnsubmit = root.findViewById(R.id.btnsubmit);
        txtchangepassword = root.findViewById(R.id.txtchangepassword);
        fabProfile = root.findViewById(R.id.fabProfile);
        progressBar = root.findViewById(R.id.progressBar);

        setHasOptionsMenu(true);

        session = new Session(getContext());

        final Calendar c = Calendar.getInstance();


        ApiConfig.getLocation(activity);

        imgProfile = root.findViewById(R.id.imgProfile);
        imgProfile.setDefaultImageResId(R.drawable.logo_login);
        imgProfile.setImageUrl(session.getData(Constant.PROFILE), Constant.imageLoader);

        fabProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectProfileImage();
            }
        });

        txtdob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), AlertDialog.THEME_HOLO_LIGHT, pickerListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        txtchangepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(activity, LoginActivity.class).putExtra("from", "changepsw"));
            }
        });

        tvUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ApiConfig.isGPSEnable(activity)) {
                    Fragment fragment = new MapFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("from", "profile");
                    fragment.setArguments(bundle);
                    MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                } else {
                    ApiConfig.displayLocationSettingsRequest(activity);
                }
            }
        });

        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = edtname.getText().toString();
                final String email = edtemail.getText().toString();
                final String address = edtaddress.getText().toString();
                final String dob = txtdob.getText().toString();
                final String mobile = edtMobile.getText().toString();
                /*final String city = edtcity.getText().toString();*/
                final String pincode = edtPinCode.getText().toString();

                if (ApiConfig.CheckValidattion(name, false, false))
                    edtname.setError(getString(R.string.enter_name));
                if (ApiConfig.CheckValidattion(email, false, false))
                    edtemail.setError(getString(R.string.enter_email));
                else if (ApiConfig.CheckValidattion(email, true, false))
                    edtemail.setError(getString(R.string.enter_valid_email));
                else if (cityId.equals("0"))
                    Toast.makeText(getContext(), getResources().getString(R.string.selectcity), Toast.LENGTH_LONG).show();
                else if (areaId.equals("0"))
                    Toast.makeText(getContext(), getResources().getString(R.string.selectarea), Toast.LENGTH_LONG).show();
                else if (ApiConfig.CheckValidattion(address, false, false))
                    edtaddress.setError(getString(R.string.enter_address));
                else if (ApiConfig.CheckValidattion(pincode, false, false))
                    edtPinCode.setError(getString(R.string.enter_pincode));
                else if (AppController.isConnected(activity)) {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Constant.TYPE, Constant.EDIT_PROFILE);
                    params.put(Constant.ID, session.getData(Session.KEY_ID));
                    params.put(Constant.NAME, name);
                    params.put(Constant.EMAIL, email);
                    params.put(Constant.CITY_ID, cityId);
                    params.put(Constant.AREA_ID, areaId);
                    params.put(Constant.MOBILE, mobile);
                    params.put(Constant.STREET, address);
                    params.put(Constant.PINCODE, pincode);
                    params.put(Constant.DOB, dob);
                    params.put(Constant.LONGITUDE, session.getCoordinates(Session.KEY_LONGITUDE));
                    params.put(Constant.LATITUDE, session.getCoordinates(Session.KEY_LATITUDE));
                    params.put(Constant.FCM_ID, AppController.getInstance().getDeviceToken());
                    //System.out.println("====update res " + params.toString());
                    ApiConfig.RequestToVolley(new VolleyCallback() {
                        @Override
                        public void onSuccess(boolean result, String response) {
                            //System.out.println ("=================* " + response);
                            if (result) {
                                try {
                                    JSONObject objectbject = new JSONObject(response);
                                    if (!objectbject.getBoolean(Constant.ERROR)) {
                                        session.setData(Session.KEY_NAME, name);
                                        session.setData(Session.KEY_EMAIL, email);
                                        session.setData(Session.KEY_CITY, city);
                                        session.setData(Session.KEY_AREA, area);
                                        session.setData(Session.KEY_MOBILE, mobile);
                                        session.setData(Session.KEY_CITY_ID, cityId);
                                        session.setData(Session.KEY_AREA_ID, areaId);
                                        session.setData(Session.KEY_ADDRESS, address);
                                        session.setData(Session.KEY_PINCODE, pincode);
                                        session.setData(Session.KEY_DOB, dob);
                                        DrawerActivity.tvName.setText(name);

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
        });

        edtname.setText(session.getData(Session.KEY_NAME));
        edtemail.setText(session.getData(Session.KEY_EMAIL));
        edtaddress.setText(session.getData(Session.KEY_ADDRESS));
        txtdob.setText(session.getData(Session.KEY_DOB));
        /*edtcity.setText(session.getData(Session.KEY_CITY));*/
        edtPinCode.setText(session.getData(Session.KEY_PINCODE));
        edtMobile.setText(session.getData(Session.KEY_MOBILE));
        cityId = session.getData(Session.KEY_CITY_ID);
        areaId = session.getData(Session.KEY_AREA_ID);


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


        SetSpinnerData();

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(mapReadyCallback);

        return root;
    }


    public void SelectProfileImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, reqWritePermission);
            } else {
                selectDialog();
            }
        } else {
            selectDialog();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {
            } else {
                if (ActivityCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(activity.getResources().getString(R.string.alert));
                    builder.setMessage(activity.getResources().getString(R.string.image_permisison_msg));
                    builder.setPositiveButton(activity.getResources().getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        }
    }

    public void selectDialog() {
        final CharSequence[] items = {getString(R.string.from_library), getString(R.string.cancel)};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(getString(R.string.from_library))) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, SELECT_FILE);
                } else if (items[item].equals(getString(R.string.cancel))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void SetSpinnerData() {
        Map<String, String> params = new HashMap<String, String>();
        cityArrayList.clear();
        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        JSONObject objectbject = new JSONObject(response);

                        int pos = 0;
                        if (!objectbject.getBoolean(Constant.ERROR)) {
                            cityArrayList.add(0, new City("0", getString(R.string.select_city)));
                            JSONArray jsonArray = objectbject.getJSONArray(Constant.DATA);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                cityArrayList.add(new City(jsonObject.getString(Constant.ID), jsonObject.getString(Constant.NAME)));
                                if (jsonObject.getString(Constant.ID).equals(cityId))
                                    pos = i;

                            }
                            cityspinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_item, cityArrayList));
                            cityspinner.setSelection(pos + 1);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, activity, Constant.CITY_URL, params, false);

        cityspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cityId = cityArrayList.get(position).getCity_id();
                city = cityspinner.getSelectedItem().toString();
                SetAreaSpinnerData(cityId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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

                        JSONObject objectbject = new JSONObject(response);
                        int pos = 0;
                        if (!objectbject.getBoolean(Constant.ERROR)) {
                            areaList.add(0, new City("0", getString(R.string.select_area)));
                            JSONArray jsonArray = objectbject.getJSONArray(Constant.DATA);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                areaList.add(new City(jsonObject.getString(Constant.ID), jsonObject.getString(Constant.NAME)));
                                if (jsonObject.getString(Constant.ID).equals(areaId))
                                    pos = i;

                            }
                            areaSpinner.setAdapter(new ArrayAdapter<City>(getContext(), R.layout.spinner_item, areaList));
                            areaSpinner.setSelection(pos + 1);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, activity, Constant.GET_AREA_BY_CITY, params, false);

        areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                areaId = areaList.get(position).getCity_id();
                area = areaSpinner.getSelectedItem().toString();
                //incode = cityArrayList.get(position).getPincode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_FILE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setOutputCompressQuality(80)
                    .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setAspectRatio(1, 1)
                    .start(activity);

        } else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.activity(FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", output)).start(activity);

        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                fileUri = result.getUri();
                new UploadFileToServer().execute();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.profile);
        activity.invalidateOptionsMenu();

        tvCurrent.setText(getString(R.string.location_1) + getAddress(Double.parseDouble(session.getData(Session.KEY_LATITUDE)), Double.parseDouble(session.getData(Session.KEY_LONGITUDE)), activity));
        mapFragment.getMapAsync(this);
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_logout).setVisible(true);
        menu.findItem(R.id.toolbar_search).setVisible(false);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_cart).setVisible(false);
    }

    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Constant.RegisterUrl);

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                //publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });
                filePath = fileUri.getPath();
                sourceFile = new File(filePath);

//                // Adding file data to http body
                entity.addPart(Constant.AccessKey, new StringBody(Constant.AccessKeyVal));
                entity.addPart(Constant.PROFILE, new FileBody(sourceFile));
                entity.addPart(Constant.USER_ID, new StringBody(session.getData(Constant.ID)));
                entity.addPart(Constant.TYPE, new StringBody(Constant.UPLOAD_PROFILE));


                totalSize = entity.getContentLength();
                httppost.addHeader(Constant.AUTHORIZATION, "Bearer " + createJWT("eKart", "eKart Authentication"));
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: " + statusCode;
                }

            } catch (IOException e) {
                responseString = e.toString();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;

            try {
                JSONObject jsonObject = new JSONObject(result);
                boolean error = jsonObject.getBoolean("error");
                if (!error) {
                    session.setData(Constant.PROFILE, jsonObject.getString(Constant.PROFILE));
                    imgProfile.setImageUrl(session.getData(Constant.PROFILE), Constant.imageLoader);
                    DrawerActivity.imgProfile.setImageUrl(session.getData(Constant.PROFILE), Constant.imageLoader);
                }
                Toast.makeText(activity, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

            } catch (JSONException e) {
                e.printStackTrace();
            }

            progressBar.setVisibility(View.GONE);
            super.onPostExecute(result);
        }

    }


}