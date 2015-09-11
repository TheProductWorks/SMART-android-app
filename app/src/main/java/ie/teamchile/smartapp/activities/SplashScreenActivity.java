package ie.teamchile.smartapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.util.Calendar;

import ie.teamchile.smartapp.R;
import ie.teamchile.smartapp.model.BaseModel;
import ie.teamchile.smartapp.util.CheckForRoot;
import ie.teamchile.smartapp.util.Constants;
import ie.teamchile.smartapp.util.SharedPrefs;

public class SplashScreenActivity extends Activity implements View.OnClickListener {
    private String rootMsg;
    private Button btnYes;
    private Button btnNo;
    private SharedPrefs sharedPrefs = new SharedPrefs();
    private Calendar c = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_splash_screen);

        btnYes = (Button) findViewById(R.id.btn_yes);
        btnYes.setOnClickListener(this);
        btnNo = (Button) findViewById(R.id.btn_no);
        btnNo.setOnClickListener(this);

        long time = sharedPrefs.getLongPrefs(this, Constants.SHARED_PREFS_SPLASH_LOG);

        if(time != 0) {
            if (DateUtils.isToday(time))
                checkIfLoggedIn();
            else
                sharedPrefs.setLongPrefs(this, c.getTimeInMillis(), Constants.SHARED_PREFS_SPLASH_LOG);
        }

        Log.d("isDeviceRooted", "isDeviceRooted() = " + new CheckForRoot().isDeviceRooted());
    }

    private void checkIfLoggedIn() {
        if (BaseModel.getInstance().getLoginStatus()) {
            Log.d("bugs", "logged in = " + BaseModel.getInstance().getLoginStatus());
            Intent intent = new Intent(SplashScreenActivity.this, QuickMenuActivity.class);
            startActivity(intent);
        } else {
            Log.d("bugs", "logged in = " + BaseModel.getInstance().getLoginStatus());
            Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                checkIfLoggedIn();
                break;
            case R.id.btn_no:
                finish();
                break;
        }
    }
}
