package ie.teamchile.smartapp.activities;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.hockeyapp.android.Tracking;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLHandshakeException;

import ie.teamchile.smartapp.R;
import ie.teamchile.smartapp.api.SmartApiClient;
import ie.teamchile.smartapp.model.Baby;
import ie.teamchile.smartapp.model.BaseModel;
import ie.teamchile.smartapp.model.Pregnancy;
import ie.teamchile.smartapp.util.AppointmentHelper;
import ie.teamchile.smartapp.util.ClearData;
import ie.teamchile.smartapp.util.CustomDialogs;
import ie.teamchile.smartapp.util.ToastAlert;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BaseActivity extends AppCompatActivity {
    protected static CountDownTimer timer;
    protected DateFormat dfDateTimeWZone = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
    protected DateFormat dfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    protected DateFormat dfDateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    protected DateFormat dfTimeOnly = new SimpleDateFormat("HH:mm", Locale.getDefault());
    protected DateFormat dfTimeWSec = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    protected DateFormat dfDateWMonthName = new SimpleDateFormat("dd MMM", Locale.getDefault());
    protected DateFormat dfDayShort = new SimpleDateFormat("E", Locale.getDefault());
    protected DateFormat dfDayLong = new SimpleDateFormat("EEEE", Locale.getDefault());
    protected DateFormat dfDowMonthDay = new SimpleDateFormat("EEE, d MMM", Locale.getDefault());
    protected DateFormat dfDateMonthNameYear = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    protected DateFormat dfMonthFullName = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
    protected DateFormat dfAMPM = new SimpleDateFormat("HH:mm a", Locale.getDefault());
    protected DateFormat dfDateTimeWMillisZone = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ", Locale.getDefault());
    protected DateFormat dfHumanReadableTimeDate = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());
    protected DateFormat dfHumanReadableDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    protected Calendar c = Calendar.getInstance();
    protected DrawerLayout drawerLayout;
    protected ListView drawerList;
    protected ActionBarDrawerToggle drawerToggle;
    protected int p = 0;
    protected int b = 0;
    protected int bId = 0;
    protected int spinnerWarning;
    protected int done = 0;
    protected boolean showDialog;
    protected int thingALing = 0;
    protected LogoutService logServ;
    protected NotificationManager notificationManager;
    protected static int apptDone;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.navigation_drawer_layout);

        spinnerWarning = getResources().getColor(R.color.teal);

        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_custom);

        createNavDrawer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BaseModel.getInstance().getLogin() == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }

        while (thingALing != 0) {
            thingALing--;
            logServ.startTimer(false);
        }
        if (notificationManager != null)
            notificationManager.cancelAll();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isMyServiceRunning()) {
            new ToastAlert(getBaseContext(), "View is now hidden", false);
            thingALing++;
            showNotification("SMART", "You will be logged out of SMART soon", QuickMenuActivity.class);
            logServ = new LogoutService();
            logServ.startTimer(true);
        }
    }

    private boolean isMyServiceRunning() {
        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(this.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    protected void showNotification(String title, String message, Class activity) {
        notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(BaseActivity.this, activity);
        PendingIntent pIntent = PendingIntent.getActivity(BaseActivity.this, 0, intent, 0);

        Notification n = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true).build();

        notificationManager.notify(0, n);
    }

    protected void checkRetroError(RetrofitError error, Context context) {
        c = Calendar.getInstance();
        String time = dfTimeWSec.format(c.getTime());
        try {
            throw (error.getCause());
        } catch (UnknownHostException e) {
            Log.d("retro", "UnknownHostException");
            // unknown host
        } catch (SSLHandshakeException e) {
            Log.d("retro", "SSLHandshakeException");
        } catch (ConnectException e) {
            Log.d("retro", "ConnectException");
            /*if(!checkIfConnected(context)){
                new SharedPrefs().setJsonPrefs(data, time);
            }*/
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            Log.d("retro", "Throwable");
        }
        Log.d("retro_error", error.toString());
    }

    protected boolean checkIfConnected(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null) {
            Toast.makeText(getApplicationContext(), "No Internet connection!", Toast.LENGTH_LONG).show();
            Log.d("retro", "no internet");
            return false;
        } else if (!netInfo.isConnected() || !netInfo.isAvailable()) {
            Toast.makeText(getApplicationContext(), "No Internet connection!", Toast.LENGTH_LONG).show();
            Log.d("retro", "no internet");
            return false;
        }
        return true;
    }

    protected void setContentForNav(int layout) {
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(layout, null, false);
        drawerLayout.addView(contentView, 0);
    }

    protected void setActionBarTitle(String title) {
        View v = getSupportActionBar().getCustomView();
        TextView titleTxtView = (TextView) v.findViewById(R.id.tv_action_bar);
        titleTxtView.setText(title);
    }

    protected void createNavDrawer() {
        String[] drawerItems = getResources().getStringArray(R.array.nav_drawer_items);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.lv_nav_drawer);
        drawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_item_layout, drawerItems));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectItem(int position) {
        Intent intent;
        switch (position) {
            case 0:         //Home
                intent = new Intent(getApplicationContext(), QuickMenuActivity.class);
                startActivity(intent);
                break;
            case 1:         //Patient Search
                intent = new Intent(getApplicationContext(), ServiceUserSearchActivity.class);
                startActivity(intent);
                break;
            case 2:         //Book Appointment
                intent = new Intent(getApplicationContext(), AppointmentTypeSpinnerActivity.class);
                startActivity(intent);
                break;
            case 3:         //TimeRecords
                intent = new Intent(getApplicationContext(), ClinicTimeRecordActivity.class);
                startActivity(intent);
                break;
            case 4:         //Todays Appointments
                //intent = new Intent(getApplicationContext(), TodayAppointmentActivity.class);
                //startActivity(intent);
                break;
            case 5:         //Sync
                getAllAppointments(this);
                pd = new CustomDialogs().showProgressDialog(this, "Updating Appointments");
                break;
            case 6:         //Logout
                showLogoutDialog();
                break;
            default:
        }
    }

    protected void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_dialog_message)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        Log.d("MYLOG", "Logout button pressed");
                        final Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (BaseModel.getInstance().getLoginStatus() == false) {
                            startActivity(intent);
                        } else {
                            doLogout(intent);
                            pd = new CustomDialogs().showProgressDialog(BaseActivity.this, "Logging Out");
                        }
                    }
                }).show();
    }

    private void doLogout(final Intent intent) {
        SmartApiClient.getAuthorizedApiClient().postLogout(
                "",
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Log.d("Retro", "logout success");
                        Tracking.stopUsage(BaseActivity.this);
                        Log.d("HockeyApp", "timeUsage = " + Tracking.getUsageTime(BaseActivity.this));
                        switch (response.getStatus()) {
                            case 200:
                                Log.d("Retro", "in logout success 200");
                                doLogout(intent);
                                break;
                            default:
                                Log.d("Retro", "in logout success response = " + response.getStatus());
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("Retro", "in logout failure error = " + error);
                        if (error.getResponse().getStatus() == 401) {
                            BaseModel.getInstance().setLoginStatus(false);
                            Toast.makeText(getApplicationContext(),
                                    "You are now logged out",
                                    Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "There was a problem with the logout, " +
                                            "\nPlease try again.",
                                    Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }
                        new ClearData(BaseActivity.this);
                    }
                }
        );
    }

    private void doLogoutWithoutIntent() {
        Log.d("logout", "doLogoutWithoutIntent called");
        SmartApiClient.getAuthorizedApiClient().postLogout(
                "",
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Log.d("logout", "in logout success");
                        Tracking.stopUsage(BaseActivity.this);
                        Log.d("HockeyApp", "timeUsage QuickMenu = " + Tracking.getUsageTime(BaseActivity.this));
                        new ClearData(BaseActivity.this);
                        finish();
                        if (notificationManager != null) {
                            notificationManager.cancelAll();
                            showNotification("SMART", "You have been logged out of SMART",
                                    SplashScreenActivity.class);
                        } else
                            showNotification("SMART", "You have been logged out of SMART",
                                    SplashScreenActivity.class);
                        new ClearData(BaseActivity.this);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("logout", "in logout failure error = " + error);
                        finish();
                        new ClearData(BaseActivity.this);
                    }
                }
        );
    }

    protected String putArrayToString(List<String> badList) {
        String listAsString = "";
        int listSize = badList.size();
        for (int i = 0; i < listSize; i++) {
            if (i == (listSize - 1))
                listAsString += badList.get(i);
            else
                listAsString += badList.get(i) + ", ";
        }
        return listAsString;
    }

    protected void getRecentPregnancy() {
        List<Pregnancy> pregnancyList = BaseModel.getInstance().getPregnancies();
        List<Date> asDate = new ArrayList<>();
        String edd;
        Log.d("Retro", "pregnancyList size = " + pregnancyList.size());
        if (pregnancyList.size() > 0) {
            try {
                for (int i = 0; i < pregnancyList.size(); i++) {
                    edd = pregnancyList.get(i).getEstimatedDeliveryDate();
                    asDate.add(dfDateOnly.parse(edd));
                }
                p = asDate.indexOf(Collections.max(asDate));
            } catch (ParseException | NullPointerException e) {
                e.printStackTrace();
            }
        } else
            p = 0;
    }

    protected void getRecentBabyPosition() {
        List<Baby> babyList = BaseModel.getInstance().getBabies();
        List<Date> asDate = new ArrayList<>();
        if (babyList.size() != 1) {
            try {
                for (int i = 0; i < babyList.size(); i++) {
                    String deliveryDateTime = babyList.get(i).getDeliveryDateTime();
                    asDate.add(dfDateTimeWZone.parse(deliveryDateTime));
                }
                b = asDate.indexOf(Collections.max(asDate));
            } catch (ParseException | NullPointerException e) {
                e.printStackTrace();
            }
        } else
            b = 0;
    }

    protected void getRecentBabyId() {
        List<Baby> babyList = BaseModel.getInstance().getBabies();
        List<Integer> idList = new ArrayList<>();
        List<Date> asDate = new ArrayList<>();
        if (babyList.size() != 1) {
            try {
                for (int i = 0; i < babyList.size(); i++) {
                    String deliveryDateTime = babyList.get(i).getDeliveryDateTime();
                    idList.add(babyList.get(i).getId());
                    asDate.add(dfDateTimeWZone.parse(deliveryDateTime));
                }
                bId = idList.get(asDate.indexOf(Collections.max(asDate)));
            } catch (ParseException | NullPointerException e) {
                e.printStackTrace();
            }
        } else
            bId = babyList.get(0).getId();
    }

    protected void getAllAppointments(final Context context) {
        SmartApiClient.getAuthorizedApiClient().getAllAppointments(
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        BaseModel.getInstance().setAppointments(baseModel.getAppointments());
                        new AppointmentHelper().addApptsToMaps(baseModel.getAppointments());
                        Log.d("Retrofit", "appointments finished");
                        done++;
                        Log.d("Retrofit", "done = " + done);
                        pd.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("Retrofit", "appointments retro failure " + error);
                        Toast.makeText(context,
                                "Error downloading appointments\n" +
                                        "please try again",
                                Toast.LENGTH_LONG).show();
                        pd.dismiss();
                    }
                }
        );
    }

    public class LogoutService extends Service {
        @Override
        public void onCreate() {
            super.onCreate();
            Log.d("logout", "LogoutService onCreate()");
        }

        public void startTimer(Boolean startTimer) {
            if (startTimer) {
                Log.d("logout", "timer started");
                timerThing();
                timer.start();
            } else {
                if (timer != null) {
                    Log.d("logout", "timer stopped");
                    timer.cancel();
                }
            }
        }

        public void timerThing() {
            timer = new CountDownTimer(300 * 1000, 1000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    Log.d("logout", "Call Logout by Service");
                    stopSelf();
                    doLogoutWithoutIntent();
                }
            };
        }

        @Override
        public IBinder onBind(Intent intent) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, final int position, long id) {
            drawerLayout.closeDrawers();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    selectItem(position);
                }
            }, 210);
        }
    }
}