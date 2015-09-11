package ie.teamchile.smartapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.Tracking;
import net.hockeyapp.android.UpdateManager;

import java.util.Map;

import ie.teamchile.smartapp.R;
import ie.teamchile.smartapp.api.SmartApiClient;
import ie.teamchile.smartapp.model.BaseModel;
import ie.teamchile.smartapp.model.PostingData;
import ie.teamchile.smartapp.util.CustomDialogs;
import ie.teamchile.smartapp.util.NotKeys;
import ie.teamchile.smartapp.util.SharedPrefs;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private SharedPrefs prefsUtil = new SharedPrefs();
    private String username, password;
    private Button btnLogin;
    private TextView tvUsername, tvPassword, tvAbout;
    private Intent intent;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_login);

        checkForUpdates();

        btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new ButtonClick());
        tvUsername = (TextView) findViewById(R.id.et_username);
        tvPassword = (TextView) findViewById(R.id.et_password);
        tvAbout = (TextView) findViewById(R.id.tv_about);
        tvAbout.setOnClickListener(new ButtonClick());

        tvUsername.setText(NotKeys.USERNAME);
        tvPassword.setText(NotKeys.PASSWORD);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UpdateManager.unregister();
    }

    private void checkForCrashes() {
        CrashManager.register(this, NotKeys.APP_ID);
    }

    private void checkForUpdates() {
        // Remove this for store / production builds!
        UpdateManager.register(this, NotKeys.APP_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForCrashes();
        BaseModel.getInstance().deleteInstance();
        System.gc();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        BaseModel.getInstance().deleteInstance();
        BaseModel.getInstance().setLoginStatus(false);
        System.gc();
        finish();
    }

    private void getSharedPrefs() {
        Timber.d("getSharedPrefs called");
        prefs = this.getSharedPreferences("SMART", MODE_PRIVATE);
        Map<String, ?> prefsMap = prefs.getAll();
        for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
            Timber.d("key = " + entry.getKey());
            if (entry.getKey().contains("appointment_post")) {
                Timber.d("get key = " + prefs.getString(entry.getKey(), ""));
                prefsUtil.postAppointment(
                        prefsUtil.getObjectFromString(
                                prefs.getString(entry.getKey(), "")),
                        this, entry.getKey());
            }
        }
    }

    private void postLogin() {
        PostingData login = new PostingData();
        login.postLogin(username, password);

        SmartApiClient.getUnAuthorizedApiClient().postLogin(login, new Callback<BaseModel>() {
            @Override
            public void success(BaseModel baseModel, Response response) {
                Timber.d("postLogin success");
                        Tracking.startUsage(LoginActivity.this);
                prefsUtil.deletePrefs(LoginActivity.this, "appts_got");
                BaseModel.getInstance().setLogin(baseModel.getLogin());
                BaseModel.getInstance().setLoginStatus(true);
                pd.dismiss();
                getSharedPrefs();
                intent = new Intent(LoginActivity.this, QuickMenuActivity.class);
                startActivity(intent);
            }

            @Override
            public void failure(RetrofitError error) {
                if (error.getResponse() != null) {
                    BaseModel body = (BaseModel) error.getBodyAs(BaseModel.class);
                    Timber.d("retro error = " + body.getError().getError());
                    Toast.makeText(LoginActivity.this, body.getError().getError(), Toast.LENGTH_LONG).show();
                }
                pd.dismiss();
            }
        });
    }

    private void validateInput() {
        username = tvUsername.getText().toString();
        password = tvPassword.getText().toString();

        if (!TextUtils.isEmpty(username) &&
                !TextUtils.isEmpty(password)) {
            postLogin();
            pd = new CustomDialogs().showProgressDialog(
                    LoginActivity.this,
                    "Logging In");
        } else {
            new CustomDialogs().showErrorDialog(
                    LoginActivity.this,
                    "Error fields empty");
        }
    }

    private class ButtonClick implements View.OnClickListener {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_login:
                    Tracking.startUsage(LoginActivity.this);
                    Timber.d("hockeyApp Login pressed");
                    Timber.d("hockeyApp timeUsage = " + Tracking.getUsageTime(LoginActivity.this));
                    validateInput();
                    break;
                case R.id.tv_about:
                    Tracking.stopUsage(LoginActivity.this);
                    Intent intent = new Intent(LoginActivity.this, AboutActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    break;
            }
        }
    }
}
