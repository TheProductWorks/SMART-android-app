package ie.teamchile.smartapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import ie.teamchile.smartapp.model.BaseModel;
import ie.teamchile.smartapp.util.CheckForRoot;

public class SplashScreenActivity extends Activity {
    private String rootMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        Log.d("isDeviceRooted", "isDeviceRooted() = " + new CheckForRoot().isDeviceRooted());

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
}
