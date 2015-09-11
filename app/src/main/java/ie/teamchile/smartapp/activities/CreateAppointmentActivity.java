package ie.teamchile.smartapp.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ie.teamchile.smartapp.R;
import ie.teamchile.smartapp.api.SmartApiClient;
import ie.teamchile.smartapp.model.Appointment;
import ie.teamchile.smartapp.model.BaseModel;
import ie.teamchile.smartapp.model.PostingData;
import ie.teamchile.smartapp.model.ServiceUser;
import ie.teamchile.smartapp.util.AdapterListResults;
import ie.teamchile.smartapp.util.AdapterSpinner;
import ie.teamchile.smartapp.util.CustomDialogs;
import ie.teamchile.smartapp.util.SharedPrefs;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CreateAppointmentActivity extends BaseActivity {
    private ArrayAdapter<String> visitPriorityAdapter, returnTypeAdapter;
    private String userName, apptDate, time, priority, visitType, clinicName,
            hospitalNumber, email, sms, address;
    private int userID;
    private Calendar c, myCalendar;
    private Date daySelected;
    private List<Integer> idList = new ArrayList<>();
    private AppointmentCalendarActivity passOptions = new AppointmentCalendarActivity();
    private SharedPreferences prefs;
    private AlertDialog.Builder alertDialog;
    private AlertDialog ad;
    private int clinicID, serviceOptionId;
    private EditText etUserName;
    private Button btnConfirmAppointment;
    private ImageButton btnUserSearch;
    private TextView tvTime, tvTimeTitle, tvDate, tvClinic, tvReturnTitle, tvPriorityTitle;
    private Spinner visitReturnTypeSpinner, visitPrioritySpinner;
    private List<ServiceUser> serviceUserList = new ArrayList<>();
    private String returnType;
    private ArrayList<String> listName = new ArrayList<>();
    private ArrayList<String> listDob = new ArrayList<>();
    private ArrayList<String> listHospitalNumber = new ArrayList<>();
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentForNav(R.layout.activity_create_appointment);

        c = Calendar.getInstance();
        myCalendar = Calendar.getInstance();

        etUserName = (EditText) findViewById(R.id.et_service_user);
        btnConfirmAppointment = (Button) findViewById(R.id.btn_confirm_appointment);
        btnUserSearch = (ImageButton) findViewById(R.id.btn_user_search);
        tvTime = (TextView) findViewById(R.id.tv_visit_time);
        tvTimeTitle = (TextView) findViewById(R.id.tv_visit_time_title);
        tvDate = (TextView) findViewById(R.id.tv_visit_date);
        tvClinic = (TextView) findViewById(R.id.tv_visit_clinic);
        tvReturnTitle = (TextView) findViewById(R.id.tv_return_type_title);
        tvPriorityTitle = (TextView) findViewById(R.id.tv_prioirty_title);

        etUserName.setText(null);

        btnConfirmAppointment.setOnClickListener(new ButtonClick());
        btnUserSearch.setOnClickListener(new ButtonClick());

        visitReturnTypeSpinner = (Spinner) findViewById(R.id.spnr_visit_return_type);
        visitReturnTypeSpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());

        visitPrioritySpinner = (Spinner) findViewById(R.id.spnr_visit_priority);
        visitPrioritySpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());

        getSharedPrefs();

        setReturnTypeSpinner();
        setPrioritySpinner();
        checkIfEditEmpty();
        checkDirectionOfIntent();
    }

    private void clinicAppt() {
        clinicID = Integer.parseInt(getIntent().getStringExtra("clinicID"));
        daySelected = AppointmentCalendarActivity.daySelected;
        clinicName = BaseModel.getInstance().getClinicMap().get(clinicID).getName();
        time = getIntent().getStringExtra("time");
        tvTime.setText(time);
        visitPrioritySpinner.setSelection(1);

        myCalendar.setTime(daySelected);
        tvDate.setText(dfDateMonthNameYear.format(daySelected));

        tvClinic.setText(clinicName);
    }

    private void homeVisitAppt() {
        daySelected = HomeVisitAppointmentActivity.daySelected;
        serviceOptionId = Integer.parseInt(getIntent().getStringExtra("serviceOptionId"));
        clinicName = BaseModel.getInstance().getServiceOptionsHomeMap().get(serviceOptionId).getName();
        tvTime.setVisibility(View.GONE);
        tvTimeTitle.setVisibility(View.GONE);
        visitReturnTypeSpinner.setVisibility(View.GONE);
        visitPrioritySpinner.setVisibility(View.GONE);
        tvPriorityTitle.setVisibility(View.GONE);
        tvReturnTitle.setVisibility(View.GONE);

        priority = "home-visit";
        returnType = "returning";

        myCalendar.setTime(daySelected);
        tvDate.setText(dfDateMonthNameYear.format(daySelected));

        tvClinic.setText(clinicName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDirectionOfIntent();
        checkIfEditEmpty();
    }

    private void checkDirectionOfIntent() {
        String intentOrigin = getIntent().getStringExtra("from");
        if (intentOrigin.equals("clinic-appointment")) {
            Log.d("bugs", "intent from clinic");
            clinicAppt();
        } else if (intentOrigin.equals("home-visit")) {
            Log.d("bugs", "intent from home visit");
            homeVisitAppt();
        }
    }

    private Boolean checkIfEditEmpty() {
        if (TextUtils.isEmpty(String.valueOf(userID)) ||
                TextUtils.isEmpty(etUserName.getText())) {
            etUserName.setError("Field Empty");
            return true;
        } else
            return false;
    }

    private Boolean checkIfOkToGo() {
        if (userID != 0 &&
                visitPrioritySpinner.getSelectedItemPosition() != 0 &&
                visitReturnTypeSpinner.getSelectedItemPosition() != 0) {
            return true;
        } else
            return false;
    }

    private void getSharedPrefs() {
        prefs = getSharedPreferences("SMART", MODE_PRIVATE);

        if (prefs != null && prefs.getBoolean("reuse", false)) {
            userName = prefs.getString("name", null);
            userID = Integer.parseInt(prefs.getString("id", ""));
            visitType = prefs.getString("visit_type", null);
            hospitalNumber = prefs.getString("hospitalNumber", "");
            email = prefs.getString("email", "");
            sms = prefs.getString("mobile", "");

            etUserName.setText(userName);
        }
    }

    private void setPrioritySpinner() {
        visitPriorityAdapter = new AdapterSpinner(this,
                R.array.visit_priority_list,
                R.layout.spinner_layout,
                R.id.tv_spinner_item);
        visitPriorityAdapter.setDropDownViewResource(R.layout.spinner_layout);
        visitPrioritySpinner.setAdapter(visitPriorityAdapter);
    }

    private void setReturnTypeSpinner() {
        returnTypeAdapter = new AdapterSpinner(this,
                R.array.return_type_list,
                R.layout.spinner_layout,
                R.id.tv_spinner_item);
        returnTypeAdapter.setDropDownViewResource(R.layout.spinner_layout);
        visitReturnTypeSpinner.setAdapter(returnTypeAdapter);
    }

    private void showEmptyFieldDialog() {
        pd = new CustomDialogs().showProgressDialog(
                CreateAppointmentActivity.this,
                "Cannot proceed, \nSome fields are empty!");
        new CountDownTimer(2000, 1000) {
            @Override
            public void onFinish() {
                pd.dismiss();
            }

            @Override
            public void onTick(long millisUntilFinished) {
            }
        }.start();
    }

    private void searchPatient(String serviceUserName) {
        listName.clear();
        listDob.clear();
        listHospitalNumber.clear();

        SmartApiClient.getAuthorizedApiClient().getServiceUserByName(
                serviceUserName,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        String name, hospitalNumber, dob;
                        List<String> searchResults = new ArrayList<>();
                        int id;
                        if (baseModel.getServiceUsers().size() != 0) {
                            BaseModel.getInstance().setServiceUsers(baseModel.getServiceUsers());
                            BaseModel.getInstance().setPregnancies(baseModel.getPregnancies());
                            BaseModel.getInstance().setBabies(baseModel.getBabies());
                            for (int i = 0; i < baseModel.getServiceUsers().size(); i++) {
                                ServiceUser serviceUserItem = baseModel.getServiceUsers().get(i);
                                serviceUserList.add(serviceUserItem);
                                name = serviceUserItem.getPersonalFields().getName();
                                dob = serviceUserItem.getPersonalFields().getDob();
                                hospitalNumber = serviceUserItem.getHospitalNumber();
                                id = serviceUserItem.getId();

                                listName.add(name);
                                listDob.add(dob);
                                listHospitalNumber.add(hospitalNumber);

                                idList.add(id);
                                searchResults.add(name + "\n" + hospitalNumber + "\n" + dob);
                            }
                            pd.dismiss();
                            userSearchDialog("Search Results");
                        } else {
                            pd.dismiss();
                            new CustomDialogs().showWarningDialog(
                                    CreateAppointmentActivity.this,
                                    "No search results found");
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("Retro", "retro failure error = " + error);
                        if (!error.getKind().equals(RetrofitError.Kind.NETWORK)) {
                            switch (error.getResponse().getStatus()) {
                                case 500:
                                    new CustomDialogs().showWarningDialog(
                                            CreateAppointmentActivity.this,
                                            "No search results found");
                                    break;
                            }
                        }
                        pd.dismiss();
                    }
                }
        );
    }

    private void userSearchDialog(final String title) {
        LayoutInflater inflater = getLayoutInflater();
        alertDialog = new AlertDialog.Builder(CreateAppointmentActivity.this);
        View convertView = inflater.inflate(R.layout.dialog_list_only, null);
        ListView list = (ListView) convertView.findViewById(R.id.lv_dialog);

        list.setOnItemClickListener(new onItemListener());

        TextView tvDialogTitle = (TextView) convertView.findViewById(R.id.tv_dialog_title);
        ImageView ivExit = (ImageView) convertView.findViewById(R.id.iv_exit_dialog);
        ivExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.dismiss();
            }
        });

        alertDialog.setView(convertView);
        tvDialogTitle.setText(title);
        BaseAdapter baseAdapter = new AdapterListResults(
                CreateAppointmentActivity.this,
                listName,
                listDob,
                listHospitalNumber);
        list.setAdapter(baseAdapter);
        ad = alertDialog.show();
    }

    private void postOrAnte() {
        visitType = "post-natal";        //TODO: this need to be changed
    }

    private void makeAlertDialog() {
        String dateWords = dfDateMonthNameYear.format(daySelected);
        String dateDay = dfDayShort.format(daySelected);

        LayoutInflater inflater = getLayoutInflater();
        alertDialog = new AlertDialog.Builder(CreateAppointmentActivity.this);
        View convertView = inflater.inflate(R.layout.dialog_confirm_appointment, null);
        TextView tvConfirmUserName = (TextView) convertView.findViewById(R.id.tv_confirm_name);
        TextView tvConfirmLocation = (TextView) convertView.findViewById(R.id.tv_confirm_location);
        TextView tvConfirmDateTime = (TextView) convertView.findViewById(R.id.tv_confirm_time);
        TextView tvConfirmEmailTo = (TextView) convertView.findViewById(R.id.tv_confirm_email);
        TextView tvConfirmSmsTo = (TextView) convertView.findViewById(R.id.tv_confirm_sms);

        tvConfirmUserName.setText(userName + " (" + hospitalNumber + ")");
        if (priority.equals("home-visit")) {
            tvConfirmLocation.setText(address);
            tvConfirmDateTime.setText(dateDay + ", " + dateWords);
        } else if (priority.equals("scheduled")) {
            tvConfirmLocation.setText(clinicName);
            tvConfirmDateTime.setText(time + " on " + dateWords);
        }

        tvConfirmEmailTo.setText(email);
        tvConfirmSmsTo.setText(sms);

        Button btnYes = (Button) convertView.findViewById(R.id.btn_confirm_yes);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("bugs", "yes 	 button clicked");
                pd = new CustomDialogs().showProgressDialog(
                        CreateAppointmentActivity.this,
                        "Booking Appointment");
                postAppointment();
            }
        });
        Button btnNo = (Button) convertView.findViewById(R.id.btn_confirm_no);
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.cancel();
            }
        });
        ImageView ivExit = (ImageView) convertView.findViewById(R.id.iv_exit_dialog);
        ivExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.cancel();
            }
        });

        alertDialog.setView(convertView);
        ad = alertDialog.show();
    }

    private void postAppointment() {
        final PostingData appointment = new PostingData();
        if (priority.equals("home-visit")) {
            appointment.postAppointment(apptDate, userID, priority, visitType, returnType, serviceOptionId);
        } else if (priority.equals("scheduled")) {
            int clinicID = Integer.parseInt(getIntent().getStringExtra("clinicID"));
            appointment.postAppointment(apptDate, time, userID, clinicID, priority, visitType, returnType);
        }

        SmartApiClient.getAuthorizedApiClient().postAppointment(
                appointment,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        BaseModel.getInstance().addAppointment(baseModel.getAppointment());
                        if (returnType.equals("returning"))
                            addNewApptToMaps();
                        else if (returnType.equals("new"))
                            getAppointmentById(baseModel.getAppointment().getId());
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("Retrofit", "retro failure error = " + error);
                        checkRetroError(error, CreateAppointmentActivity.this);
                        if (error.getKind() != RetrofitError.Kind.NETWORK) {
                            if (error.getResponse().getStatus() == 422) {
                                BaseModel body = (BaseModel) error.getBodyAs(BaseModel.class);
                                Toast.makeText(CreateAppointmentActivity.this,
                                        body.getError().getAppointmentTaken(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            c = Calendar.getInstance();
                            String time = dfTimeWSec.format(c.getTime());
                            String prefsTag = "appointment_post_" + time;
                            SharedPrefs prefsUstil = new SharedPrefs();
                            prefsUstil.setJsonPrefs(CreateAppointmentActivity.this, appointment, prefsTag);
                            Toast.makeText(CreateAppointmentActivity.this,
                                    "No internet, appointment will be booked when available",
                                    Toast.LENGTH_LONG).show();
                        }
                        if (ad.isShowing())
                            ad.cancel();
                        if (pd.isShowing())
                            pd.dismiss();
                    }
                }
        );
    }

    private void getAppointmentById(int apptId) {
        SmartApiClient.getAuthorizedApiClient().getAppointmentById(
                apptId + 1,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Log.d("retro", "getAppointmentById success");
                        BaseModel.getInstance().addAppointment(baseModel.getAppointment());
                        addNewApptToMaps();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("retro", "getAppointmentById failure = " + error);
                        pd.dismiss();
                    }
                }
        );
    }

    private void addNewApptToMaps() {
        List<Integer> clinicApptIdList;
        Map<String, List<Integer>> clinicVisitdateApptIdMap;
        Map<Integer, Map<String, List<Integer>>> clinicVisitClinicDateApptIdMap = new ArrayMap<>();
        Map<Integer, Appointment> clinicVisitIdApptMap = new ArrayMap<>();

        List<Integer> homeApptIdList;
        Map<String, List<Integer>> homeVisitdateApptIdMap;
        Map<Integer, Map<String, List<Integer>>> homeVisitClinicDateApptIdMap = new ArrayMap<>();
        Map<Integer, Appointment> homeVisitIdApptMap = new ArrayMap<>();
        for (int i = 0; i < BaseModel.getInstance().getAppointments().size(); i++) {
            clinicApptIdList = new ArrayList<>();
            homeApptIdList = new ArrayList<>();
            clinicVisitdateApptIdMap = new ArrayMap<>();
            homeVisitdateApptIdMap = new ArrayMap<>();
            Appointment appt = BaseModel.getInstance().getAppointments().get(i);
            String apptDate = appt.getDate();
            int apptId = appt.getId();
            int clinicId = appt.getClinicId();
            int serviceOptionId = 0;
            if (appt.getServiceOptionIds().size() > 0) {
                serviceOptionId = appt.getServiceOptionIds().get(0);
            }

            if (appt.getPriority().equals("home-visit")) {
                Log.d("bugs", " appt ID = " + appt.getId());
                if (homeVisitClinicDateApptIdMap.get(serviceOptionId) != null) {
                    homeVisitdateApptIdMap = homeVisitClinicDateApptIdMap.get(serviceOptionId);
                    if (homeVisitdateApptIdMap.get(apptDate) != null) {
                        homeApptIdList = homeVisitdateApptIdMap.get(apptDate);
                    }
                }
                homeApptIdList.add(apptId);
                homeVisitdateApptIdMap.put(apptDate, homeApptIdList);

                homeVisitClinicDateApptIdMap.put(serviceOptionId, homeVisitdateApptIdMap);
                homeVisitIdApptMap.put(apptId, appt);
            } else {
                if (clinicVisitClinicDateApptIdMap.get(clinicId) != null) {
                    clinicVisitdateApptIdMap = clinicVisitClinicDateApptIdMap.get(clinicId);
                    if (clinicVisitdateApptIdMap.get(apptDate) != null) {
                        clinicApptIdList = clinicVisitdateApptIdMap.get(apptDate);
                    }
                }
                clinicApptIdList.add(apptId);
                clinicVisitdateApptIdMap.put(apptDate, clinicApptIdList);

                clinicVisitClinicDateApptIdMap.put(clinicId, clinicVisitdateApptIdMap);
                clinicVisitIdApptMap.put(apptId, appt);
            }
        }
        BaseModel.getInstance().setClinicVisitClinicDateApptIdMap(clinicVisitClinicDateApptIdMap);
        BaseModel.getInstance().setClinicVisitIdApptMap(clinicVisitIdApptMap);

        BaseModel.getInstance().setHomeVisitOptionDateApptIdMap(homeVisitClinicDateApptIdMap);
        BaseModel.getInstance().setHomeVisitIdApptMap(homeVisitIdApptMap);

        Intent intentClinic = new Intent(CreateAppointmentActivity.this, AppointmentCalendarActivity.class);
        Intent intentHome = new Intent(CreateAppointmentActivity.this, HomeVisitAppointmentActivity.class);

        if (priority.equals("home-visit"))
            startActivity(intentHome);
        else if (priority.equals("scheduled"))
            startActivity(intentClinic);
        else if (priority.equals("drop-in"))
            startActivity(intentClinic);

        if (ad.isShowing())
            ad.cancel();
        if (pd.isShowing())
            pd.dismiss();
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private class ButtonClick implements View.OnClickListener {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_confirm_appointment:
                    apptDate = dfDateOnly.format(myCalendar.getTime());
                    passOptions.setDaySelected(myCalendar.getTime());

                    if (priority.equals("home-visit")) {
                        if (!checkIfEditEmpty())
                            makeAlertDialog();
                        else
                            showEmptyFieldDialog();
                    } else if (priority.equals("scheduled")) {
                        if (checkIfOkToGo())
                            makeAlertDialog();
                        else
                            showEmptyFieldDialog();
                    }
                    break;
                case R.id.btn_user_search:
                    if (!checkIfEditEmpty()) {
                        hideKeyboard();
                        userID = 0;
                        userName = etUserName.getText().toString();
                        checkIfEditEmpty();
                        pd = new CustomDialogs().showProgressDialog(
                                CreateAppointmentActivity.this,
                                "Fetching Information");
                        searchPatient(userName);
                    }
                    break;
            }
        }
    }

    private class MySpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getId()) {
                case R.id.spnr_visit_return_type:
                    switch (position) {
                        case 0:
                            break;
                        case 1:
                            returnType = "returning";
                            break;
                        case 2:
                            returnType = "new";
                            break;
                    }
                    break;
                case R.id.spnr_visit_priority:
                    switch (position) {
                        case 0:
                            //Select Visit Priority
                            break;
                        case 1:
                            priority = "scheduled";
                            //Scheduled
                            break;
                        case 2:
                            priority = "drop-in";
                            //Drop-In
                            break;
                    }
                    break;
                case R.id.list_dialog:
                    Log.d("bugs", "list position is: " + position);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class onItemListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getId()) {
                case R.id.lv_dialog:
                    hideKeyboard();
                    ServiceUser serviceUser = serviceUserList.get(position);
                    BaseModel.getInstance().setServiceUser(serviceUser);
                    userName = serviceUser.getPersonalFields().getName();
                    hospitalNumber = serviceUser.getHospitalNumber();
                    email = serviceUser.getPersonalFields().getEmail();
                    sms = serviceUser.getPersonalFields().getMobilePhone();
                    address = serviceUser.getPersonalFields().getHomeAddress();

                    etUserName.setText(userName);
                    userID = serviceUser.getId();
                    postOrAnte();
                    ad.cancel();
                    break;
            }
        }
    }
}
