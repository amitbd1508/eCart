package com.barisalbazar.shop.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.barisalbazar.shop.R;
import com.barisalbazar.shop.helper.Constant;
import com.barisalbazar.shop.helper.Session;

public class SplashActivity extends AppCompatActivity {

    Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Uri data = this.getIntent().getData();
        if (data != null && data.isHierarchical()) {

            switch (data.getPath().split("/")[1]) {
                case "itemdetail": // Handle the item detail deep link
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("id", data.getPath().split("/")[2]);
                    intent.putExtra("from", "share");
                    intent.putExtra("vpos", 0);
                    startActivity(intent);
                    finish();
                    break;

                case "refer": // Handle the item detail deep link
                    Constant.FRND_CODE = data.getPath().split("/")[2];
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", Constant.FRND_CODE);
                    assert clipboard != null;
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SplashActivity.this, R.string.refer_code_copied, Toast.LENGTH_LONG).show();

                    Intent referIntent = new Intent(this, LoginActivity.class);
                    referIntent.putExtra("from", "register");
                    startActivity(referIntent);
                    finish();
                    break;
            }
        } else {

            setContentView(R.layout.activity_splash);
            session = new Session(SplashActivity.this);

            int SPLASH_TIME_OUT = 2000;
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("from", "");
                    startActivity(intent);
                    finish();
                }
            }, SPLASH_TIME_OUT);

        }
    }


}
