package ie.teamchile.smartapp.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ie.teamchile.smartapp.R;
import ie.teamchile.smartapp.api.SmartApiClient;
import ie.teamchile.smartapp.model.BaseModel;
import ie.teamchile.smartapp.model.PostingData;
import ie.teamchile.smartapp.model.ServiceUserAction;
import ie.teamchile.smartapp.util.CustomDialogs;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ServiceUserActivity extends BaseActivity {
    private final CharSequence[] userContactList = {"Call Mobile", "Send SMS", "Send Email"};
    private final CharSequence[] kinContactList = {"Call Mobile", "Send SMS"};
    private ImageView ivAntiDHistory, ivMidwiferyNotes, ivVitKHistory, ivHearingHistory,
            ivNbstHistory, ivFeeding;
    private TextView tvAnteAge, tvAnteGestation, tvAnteParity, tvAnteDeliveryTime,
            tvAnteBloodGroup, tvAnteRhesus, tvAnteLastPeriod;
    private TextView tvUsrHospitalNumber, tvUsrEmail, tvUsrMobileNumber, tvUsrRoad,
            tvUsrCounty, tvUsrPostCode, tvUsrNextOfKinName, tvUsrAge,
            tvUsrKinContact, tvUsrGestation, tvUsrGestationTitle, tvUsrParity;
    private TextView tvPostBirthMode, tvPostPerineum, tvPostAntiD, tvPostDeliveryDate, tvPostDeliveryTIme,
            tvPostDaysSinceBirth, tvPostBabyGender, tvPostBirthWeight, tvPostVitK, tvPostHearing,
            tvPostFeeding, tvPostNBST;
    private LinearLayout llUserContact, llUserAddress, llUserKinContact;
    private String dob = "", age = "", hospitalNumber, email, mobile, userName, kinName,
            kinMobile, road, county, postCode, gestation, parity, estimtedDelivery,
            perineum, birthMode, babyGender, babyWeightKg = "",
            vitK, hearing, antiD, feeding, nbst, deliveryDateTime, daysSinceBirth,
            userCall, userSMS, userEmail, lastPeriodDate;
    private Boolean rhesus;
    private int babyWeightGrams = 0;
    private List<Integer> babyID;
    private double grams = 0.0;
    private String sex_male = "ale";
    private String sex_female = "emale";
    private Dialog dialog;
    private Button bookAppointmentButton, btnBookAction;
    private TableRow trParity, trAntiD, trMidwifeNotes, trVitK, trHearing, trNbst, trFeeding;
    private Date dobAsDate = null;
    private Intent userCallIntent, userSmsIntent, userEmailIntent;
    private Calendar cal = Calendar.getInstance();
    private int userId;
    private AlertDialog.Builder alertDialog;
    private AlertDialog ad;
    private List<String> historyList = new ArrayList<>();
    private List<String> dateTimeList = new ArrayList<>();
    private List<String> providerNameList = new ArrayList<>();
    private BaseAdapter adapter;
    private int optionPosition;
    private int pregnancyId;
    private List<String> historyOptions;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentForNav(R.layout.activity_service_user_tabhost);
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabSpec tab1 = tabHost.newTabSpec("Ante");
        tab1.setContent(R.id.tab_ante);
        tab1.setIndicator("Ante Natal");
        tabHost.addTab(tab1);

        TabSpec tab2 = tabHost.newTabSpec("Contact");
        tab2.setContent(R.id.tab_contact);
        tab2.setIndicator("Contact");
        tabHost.addTab(tab2);

        TabSpec tab3 = tabHost.newTabSpec("Post");
        tab3.setContent(R.id.tab_post);
        tab3.setIndicator("Post Natal");
        tabHost.addTab(tab3);

        tabHost.setCurrentTab(1);

        setAntiD();

        bookAppointmentButton = (Button) findViewById(R.id.btn_usr_book_appointment);
        bookAppointmentButton.setOnClickListener(new ButtonClick());
        btnBookAction = (Button) findViewById(R.id.btn_usr_actions);
        btnBookAction.setOnClickListener(new ButtonClick());

        llUserContact = (LinearLayout) findViewById(R.id.ll_usr_contact);
        llUserContact.setOnClickListener(new ButtonClick());
        llUserAddress = (LinearLayout) findViewById(R.id.ll_usr_address);
        llUserAddress.setOnClickListener(new ButtonClick());
        llUserKinContact = (LinearLayout) findViewById(R.id.ll_usr_kin);
        llUserKinContact.setOnClickListener(new ButtonClick());

        tvAnteAge = (TextView) findViewById(R.id.tv_ante_age);
        tvAnteGestation = (TextView) findViewById(R.id.tv_ante_gestation);
        tvAnteParity = (TextView) findViewById(R.id.tv_ante_parity);
        tvAnteDeliveryTime = (TextView) findViewById(R.id.tv_ante_edd);
        tvAnteBloodGroup = (TextView) findViewById(R.id.tv_ante_blood);
        tvAnteRhesus = (TextView) findViewById(R.id.tv_ante_rhesus);
        tvAnteLastPeriod = (TextView) findViewById(R.id.tv_ante_last_period);

        tvUsrHospitalNumber = (TextView) findViewById(R.id.tv_usr_hospital_number);
        tvUsrAge = (TextView) findViewById(R.id.tv_usr_age);
        tvUsrEmail = (TextView) findViewById(R.id.tv_usr_email);
        tvUsrMobileNumber = (TextView) findViewById(R.id.tv_usr_mobile);
        tvUsrRoad = (TextView) findViewById(R.id.tv_usr_road);
        tvUsrCounty = (TextView) findViewById(R.id.tv_usr_county);
        tvUsrPostCode = (TextView) findViewById(R.id.tv_usr_post_code);
        tvUsrNextOfKinName = (TextView) findViewById(R.id.tv_usr_kin_name);
        tvUsrKinContact = (TextView) findViewById(R.id.tv_usr_kin_contact);
        tvUsrGestation = (TextView) findViewById(R.id.tv_usr_gestation);
        tvUsrGestationTitle = (TextView) findViewById(R.id.tv_usr_gestation_title);
        tvUsrParity = (TextView) findViewById(R.id.tv_usr_partiy);

        tvPostBirthMode = (TextView) findViewById(R.id.tv_post_mode);
        tvPostPerineum = (TextView) findViewById(R.id.tv_post_perineum);
        tvPostAntiD = (TextView) findViewById(R.id.tv_post_anti_d);
        tvPostDeliveryDate = (TextView) findViewById(R.id.tv_post_delivery_date);
        tvPostDeliveryTIme = (TextView) findViewById(R.id.tv_post_delivery_time);
        tvPostDaysSinceBirth = (TextView) findViewById(R.id.tv_post_days_since_birth);
        tvPostBabyGender = (TextView) findViewById(R.id.tv_post_gender);
        tvPostBirthWeight = (TextView) findViewById(R.id.tv_post_weight);
        tvPostVitK = (TextView) findViewById(R.id.tv_post_vit_k);
        tvPostHearing = (TextView) findViewById(R.id.tv_post_hearing);
        tvPostFeeding = (TextView) findViewById(R.id.tv_post_feeding);
        tvPostNBST = (TextView) findViewById(R.id.tv_post_nbst);

        trParity = (TableRow) findViewById(R.id.tr_ante_parity);
        trParity.setOnClickListener(new ButtonClick());
        ivAntiDHistory = (ImageView) findViewById(R.id.iv_anti_d_history_icon);
        ivAntiDHistory.setOnClickListener(new ButtonClick());
        ivMidwiferyNotes = (ImageView) findViewById(R.id.iv_post_midwives_notes);
        ivMidwiferyNotes.setOnClickListener(new ButtonClick());
        ivVitKHistory = (ImageView) findViewById(R.id.iv_vit_k_history_icon);
        ivVitKHistory.setOnClickListener(new ButtonClick());
        ivHearingHistory = (ImageView) findViewById(R.id.iv_hearing_history_icon);
        ivHearingHistory.setOnClickListener(new ButtonClick());
        ivNbstHistory = (ImageView) findViewById(R.id.iv_nbst_history_icon);
        ivNbstHistory.setOnClickListener(new ButtonClick());
        ivFeeding = (ImageView) findViewById(R.id.iv_feeding_history_icon);
        ivFeeding.setOnClickListener(new ButtonClick());

        trAntiD = (TableRow) findViewById(R.id.tr_post_anti_d);
        trAntiD.setOnClickListener(new ButtonClick());
        trMidwifeNotes = (TableRow) findViewById(R.id.tr_midwife_notes);
        trMidwifeNotes.setOnClickListener(new ButtonClick());
        trVitK = (TableRow) findViewById(R.id.tr_vit_k);
        trVitK.setOnClickListener(new ButtonClick());
        trHearing = (TableRow) findViewById(R.id.tr_hearing);
        trHearing.setOnClickListener(new ButtonClick());
        trNbst = (TableRow) findViewById(R.id.tr_nbst);
        trNbst.setOnClickListener(new ButtonClick());
        trFeeding = (TableRow) findViewById(R.id.tr_feeding);
        trFeeding.setOnClickListener(new ButtonClick());

        try {
            dob = BaseModel.getInstance().getServiceUsers().get(0).getPersonalFields().getDob();
            if (dob != null)
                age = getAge(dob);
            getRecentPregnancy();
            getRecentBabyPosition();

            pregnancyId = BaseModel.getInstance().getPregnancies().get(p).getId();
            userId = BaseModel.getInstance().getServiceUsers().get(0).getId();
            hospitalNumber = BaseModel.getInstance().getServiceUsers().get(0).getHospitalNumber();
            email = BaseModel.getInstance().getServiceUsers().get(0).getPersonalFields().getEmail();
            mobile = BaseModel.getInstance().getServiceUsers().get(0).getPersonalFields().getMobilePhone();
            userName = BaseModel.getInstance().getServiceUsers().get(0).getPersonalFields().getName();
            kinName = BaseModel.getInstance().getServiceUsers().get(0).getPersonalFields().getNextOfKinName();
            kinMobile = BaseModel.getInstance().getServiceUsers().get(0).getPersonalFields().getNextOfKinPhone();
            road = BaseModel.getInstance().getServiceUsers().get(0).getPersonalFields().getHomeAddress();
            county = BaseModel.getInstance().getServiceUsers().get(0).getPersonalFields().getHomeCounty();
            postCode = BaseModel.getInstance().getServiceUsers().get(0).getPersonalFields().getHomePostCode();
            perineum = BaseModel.getInstance().getPregnancies().get(p).getPerineum();
            List<String> birthModeList = BaseModel.getInstance().getPregnancies().get(p).getBirthMode();
            if (birthModeList != null)
                birthMode = putArrayToString(BaseModel.getInstance().getPregnancies().get(p).getBirthMode());
            else
                birthMode = "";
            babyGender = BaseModel.getInstance().getBabies().get(b).getGender();
            if (BaseModel.getInstance().getBabies().get(b).getWeight() != null)
                babyWeightKg = getGramsToKg(BaseModel.getInstance().getBabies().get(b).getWeight());
            else
                babyWeightKg = "";
            babyID = BaseModel.getInstance().getServiceUsers().get(0).getBabyIds();
            gestation = BaseModel.getInstance().getPregnancies().get(p).getGestation();
            parity = BaseModel.getInstance().getServiceUsers().get(0).getClinicalFields().getParity();
            estimtedDelivery = BaseModel.getInstance().getPregnancies().get(p).getEstimatedDeliveryDate();
            lastPeriodDate = BaseModel.getInstance().getPregnancies().get(p).getLastMenstrualPeriod();
            vitK = BaseModel.getInstance().getBabies().get(b).getVitK();
            hearing = BaseModel.getInstance().getBabies().get(b).getHearing();
            antiD = BaseModel.getInstance().getPregnancies().get(p).getAntiD();
            feeding = BaseModel.getInstance().getPregnancies().get(p).getFeeding();
            rhesus = BaseModel.getInstance().getServiceUsers().get(0).getClinicalFields().getRhesus();
            if (rhesus)
                tvAnteRhesus.setText("Yes");
            else if (!rhesus)
                tvAnteRhesus.setText("No");
            if (feeding == null)
                feeding = "";
            nbst = BaseModel.getInstance().getBabies().get(b).getNbst();
            deliveryDateTime = BaseModel.getInstance().getBabies().get(b).getDeliveryDateTime();
            if (deliveryDateTime != null)
                daysSinceBirth = getNoOfDays(deliveryDateTime);
            setActionBarTitle(userName);

            if (parity.equals("0 + 0"))
                trParity.setEnabled(false);
            tvAnteParity.setText(parity);
            tvAnteGestation.setText(gestation);
            tvAnteBloodGroup.setText(BaseModel.getInstance().getServiceUsers().get(0).getClinicalFields().getBloodGroup());
            if (estimtedDelivery != null)
                tvAnteDeliveryTime.setText(getEstimateDeliveryDate(estimtedDelivery));
            else
                tvAnteDeliveryTime.setText("");
            tvAnteAge.setText(age);

            tvUsrAge.setText(age);
            tvUsrHospitalNumber.setText(hospitalNumber);
            tvUsrEmail.setText(email);
            tvUsrMobileNumber.setText(mobile);
            tvUsrRoad.setText(road);
            tvUsrCounty.setText(county);
            tvUsrPostCode.setText(postCode);
            tvUsrNextOfKinName.setText(kinName);
            tvUsrKinContact.setText(kinMobile);
            if (gestation == null)
                tvUsrGestationTitle.setVisibility(View.GONE);
            else
                tvUsrGestation.setText(gestation);
            tvUsrParity.setText(parity);
            tvPostVitK.setText(vitK);
            tvPostHearing.setText(hearing);
            tvPostAntiD.setText(antiD);
            tvPostFeeding.setText(feeding);
            tvPostNBST.setText(nbst);
            tvPostDeliveryDate.setText(getDeliveryDate(deliveryDateTime));
            tvPostDeliveryTIme.setText(getDeliveryTime(deliveryDateTime));
            tvPostPerineum.setText(perineum);
            tvPostBirthMode.setText(birthMode);
            tvPostBirthWeight.setText(babyWeightKg);
            if (lastPeriodDate != null)
                tvAnteLastPeriod.setText(getLastPeriodDate(lastPeriodDate));
            if (babyGender.equalsIgnoreCase("M"))
                tvPostBabyGender.setText(babyGender + sex_male);
            else if (babyGender.equalsIgnoreCase("F"))
                tvPostBabyGender.setText(babyGender + sex_female);
            tvPostDaysSinceBirth.setText(daysSinceBirth);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        Log.d("bugs", "baby ids fro user = " + BaseModel.getInstance().getServiceUsers().get(0).getBabyIds().get(b));
        getRecentBabyId();
        getRecentBabyPosition();
        Log.d("bugs", "recent baby id before = " + bId);
        Log.d("bugs", "recent baby position before = " + b);
        pregnancyId = BaseModel.getInstance().getBabies().get(b).getPregnancyId();
        Log.d("bugs", "recent baby id after = " + bId);
        Log.d("bugs", "recent baby position after = " + b);

        BaseModel.getInstance().setPregnancyNotes(
                BaseModel.getInstance().getPregnancies().get(p).getPregnancyNotes());
    }

    private void setAntiD() {
        historyList = new ArrayList<>();
        dateTimeList = new ArrayList<>();
        providerNameList = new ArrayList<>();

        String[] arrayFromXml = getResources().getStringArray(R.array.anti_d_list);
        historyOptions = Arrays.asList(arrayFromXml);

        for (int i = 0; i < BaseModel.getInstance().getAntiDHistories().size(); i++) {
            Date parsed;
            String formatted = "";
            try {
                parsed = dfDateTimeWMillisZone.parse(BaseModel.getInstance().getAntiDHistories().get(i).getCreatedAt());
                formatted = dfHumanReadableTimeDate.format(parsed);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            historyList.add(BaseModel.getInstance().getAntiDHistories().get(i).getAntiD());
            dateTimeList.add(formatted);
            providerNameList.add(BaseModel.getInstance().getAntiDHistories().get(i).getServiceProviderName());
        }
    }

    private void setFeeding() {
        historyList = new ArrayList<>();
        dateTimeList = new ArrayList<>();
        providerNameList = new ArrayList<>();

        String[] arrayFromXml = getResources().getStringArray(R.array.feeding_list);
        historyOptions = Arrays.asList(arrayFromXml);

        for (int i = 0; i < BaseModel.getInstance().getFeedingHistories().size(); i++) {
            Date parsed;
            String formatted = "";
            try {
                parsed = dfDateTimeWMillisZone.parse(BaseModel.getInstance().getFeedingHistories().get(i).getCreatedAt());
                formatted = dfHumanReadableTimeDate.format(parsed);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            historyList.add(BaseModel.getInstance().getFeedingHistories().get(i).getFeeding());
            dateTimeList.add(formatted);
            providerNameList.add(BaseModel.getInstance().getFeedingHistories().get(i).getServiceProviderName());
        }
    }

    private void setVitK() {
        historyList = new ArrayList<>();
        dateTimeList = new ArrayList<>();
        providerNameList = new ArrayList<>();

        String[] arrayFromXml = getResources().getStringArray(R.array.vit_k_list);
        historyOptions = Arrays.asList(arrayFromXml);

        for (int i = 0; i < BaseModel.getInstance().getVitKHistories().size(); i++) {
            Date parsed;
            String formatted = "";
            try {
                parsed = dfDateTimeWMillisZone.parse(BaseModel.getInstance().getVitKHistories().get(i).getCreatedAt());
                formatted = dfHumanReadableTimeDate.format(parsed);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            historyList.add(BaseModel.getInstance().getVitKHistories().get(i).getVitK());
            dateTimeList.add(formatted);
            providerNameList.add(BaseModel.getInstance().getVitKHistories().get(i).getServiceProviderName());
        }
    }

    private void setHearing() {
        historyList = new ArrayList<>();
        dateTimeList = new ArrayList<>();
        providerNameList = new ArrayList<>();

        String[] arrayFromXml = getResources().getStringArray(R.array.hearing_list);
        historyOptions = Arrays.asList(arrayFromXml);

        for (int i = 0; i < BaseModel.getInstance().getHearingHistories().size(); i++) {
            Date parsed;
            String formatted = "";
            try {
                parsed = dfDateTimeWMillisZone.parse(BaseModel.getInstance().getHearingHistories().get(i).getCreatedAt());
                formatted = dfHumanReadableTimeDate.format(parsed);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            historyList.add(BaseModel.getInstance().getHearingHistories().get(i).getHearing());
            dateTimeList.add(formatted);
            providerNameList.add(BaseModel.getInstance().getHearingHistories().get(i).getServiceProviderName());
        }
    }

    private void setNBST() {
        historyList = new ArrayList<>();
        dateTimeList = new ArrayList<>();
        providerNameList = new ArrayList<>();

        String[] arrayFromXml = getResources().getStringArray(R.array.nbst_list);
        historyOptions = Arrays.asList(arrayFromXml);

        for (int i = 0; i < BaseModel.getInstance().getNbstHistories().size(); i++) {
            Date parsed;
            String formatted = "";
            try {
                parsed = dfDateTimeWMillisZone.parse(BaseModel.getInstance().getNbstHistories().get(i).getCreatedAt());
                formatted = dfHumanReadableTimeDate.format(parsed);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            historyList.add(BaseModel.getInstance().getNbstHistories().get(i).getNbst());
            dateTimeList.add(formatted);
            providerNameList.add(BaseModel.getInstance().getNbstHistories().get(i).getServiceProviderName());
        }
    }

    private void dialogContact(final CharSequence[] items) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.contact_title);

        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Call Mobile"))
                    makeCall();
                if (items[item].equals("Send SMS"))
                    sendSms();
                if (items[item].equals("Send Email"))
                    sendEmail();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        alert.setCanceledOnTouchOutside(true);
    }

    private void makeCall() {
        userCall = "tel:" + tvUsrMobileNumber.getText().toString();
        userCallIntent = new Intent(Intent.ACTION_DIAL,
                Uri.parse(userCall));
        try {
            startActivity(userCallIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(ServiceUserActivity.this,
                    "Call failed, please try again later.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void sendSms() {
        userSMS = tvUsrMobileNumber.getText().toString();
        userSmsIntent = new Intent(Intent.ACTION_VIEW);
        userSmsIntent.setType("vnd.android-dir/mms-sms");
        userSmsIntent.putExtra("address", userSMS);
        try {
            startActivity(userSmsIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(ServiceUserActivity.this,
                    "SMS failed, please try again later.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void sendEmail() {
        userEmail = tvUsrEmail.getText().toString();
        userEmailIntent = new Intent(Intent.ACTION_SEND);
        userEmailIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
        userEmailIntent.setType("message/rfc822");
        userEmailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{userEmail});
        userEmailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        userEmailIntent.putExtra(Intent.EXTRA_TEXT, "");
        userEmailIntent.setType("plain/text");
        try {
            startActivity(userEmailIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(ServiceUserActivity.this,
                    "Email failed, please try again later.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void gotoMaps() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.user_address_title)
                .setMessage(R.string.user_address_message)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        String addr = tvUsrRoad.getText().toString() + ", "
                                + tvUsrCounty.getText().toString() + ", "
                                + tvUsrPostCode.getText().toString();
                        Log.d("bugs", "geoCode: " + getGeoCoodinates(addr + "?z=12"));
                        Uri uri = Uri.parse(getGeoCoodinates(addr)); //"geo:47.6,-122.3?z=18"
                        Log.d("bugs", "addr: " + addr);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                        startActivity(intent);
                    }
                }).show();
    }

    private String getAge(String dob) {
        try {
            dobAsDate = dfDateOnly.parse(dob);
            cal.setTime(dobAsDate);
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
        }
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        Date now = new Date();
        int nowMonth = now.getMonth() + 1;
        int nowYear = now.getYear() + 1900;
        int result = nowYear - year;

        if (month > nowMonth) {
            result--;
        } else if (month == nowMonth) {
            int nowDay = now.getDate();

            if (day > nowDay) {
                result--;
            }
        }
        return String.valueOf(result);
    }

    private String getEstimateDeliveryDate(String edd) {
        Date date;
        String ed = null;
        try {
            date = dfDateOnly.parse(edd);
            ed = dfMonthFullName.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ed;
    }

    public String getLastPeriodDate(String edd) {
        Date date;
        String ed = null;
        try {
            date = dfDateOnly.parse(edd);
            ed = dfMonthFullName.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ed;
    }

    protected String getDeliveryDate(String edd) {
        Date date;
        String dateOfDelivery = null;
        try {
            date = dfDateOnly.parse(edd);
            dateOfDelivery = dfMonthFullName.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateOfDelivery;
    }

    private String getDeliveryTime(String edd) {
        String deliveryTime = null;
        Date date;
        try {
            date = dfDateOnly.parse(edd);

            deliveryTime = dfAMPM.format(date);
            date = dfDateTimeWZone.parse(edd);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return deliveryTime;
    }

    private String getNoOfDays(String dateOfDelivery) {
        int numOfDays = 0;
        try {
            Date dodAsDate = dfDateTimeWZone.parse(deliveryDateTime);
            cal = Calendar.getInstance();
            Date now = cal.getTime();
            numOfDays = (int) ((now.getTime() - dodAsDate.getTime()) / (1000 * 60 * 60 * 24)) + 1;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return String.valueOf(numOfDays);
    }

    private String getGramsToKg(int grams) {
        double kg = 0.0;
        kg = grams / 1000.0;
        return String.valueOf(kg);
    }

    private String formatArrayString(String toBeFormatted) {
        String formatedString = toBeFormatted
                .replace(",", ", ")  //remove the commas
                .replace("[", "")  //remove the right bracket
                .replace("]", "")  //remove the left bracket
                .replace("\"", "")
                .trim();
        return formatedString;
    }

    private void setSharedPrefs() {
        Log.d("bugs", "baby ids: " + babyID);
        SharedPreferences.Editor prefs = getSharedPreferences("SMART", MODE_PRIVATE).edit();
        if (babyID.get(p).equals("[]")) {
            prefs.putString("visit_type_str", "Antenatal");
            prefs.putString("visit_type", "ante-natal");
        } else {
            prefs.putString("visit_type_str", "Postnatal");
            prefs.putString("visit_type", "post-natal");
        }
        prefs.putString("name", userName);
        prefs.putString("hospitalNumber", hospitalNumber);
        prefs.putString("email", email);
        prefs.putString("mobile", mobile);
        prefs.putString("id", String.valueOf(userId));
        prefs.putBoolean("reuse", true);

        prefs.commit();
    }

    private String getGeoCoodinates(String address) {
        Geocoder geocoder = new Geocoder(ServiceUserActivity.this);
        Log.d("The Address", address);
        List<Address> addresses = null;
        try {
            if (!address.isEmpty() && address != null)
                addresses = geocoder.getFromLocationName(address, 1);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if (addresses != null && addresses.size() > 0) {
            double latitude = addresses.get(0).getLatitude();
            double longitude = addresses.get(0).getLongitude();
            Log.d("Coordinates Found", String.valueOf(latitude));
            Log.d("Coordinates Found", String.valueOf(longitude));
            //"geo:47.6,-122.3?z=18"
            return "geo:" + String.valueOf(latitude) + "," + String.valueOf(longitude) + "?z=12" +
                    "&q=" + String.valueOf(latitude) + "," + String.valueOf(longitude) +
                    "(" + userName + ")";
        }
        return "Not Found";
    }

    private void historyAlertDialog(final String title) {
        LayoutInflater inflater = getLayoutInflater();
        alertDialog = new AlertDialog.Builder(ServiceUserActivity.this);
        View convertView = (View) inflater.inflate(R.layout.dialog_history, null);
        ListView list = (ListView) convertView.findViewById(R.id.lv_history);
        Button btnAntiD = (Button) convertView.findViewById(R.id.btn_add_history);
        TextView tvDialogTitle = (TextView) convertView.findViewById(R.id.tv_history_dialog_title);
        ImageView ivExit = (ImageView) convertView.findViewById(R.id.iv_exit_dialog);
        ivExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.dismiss();
            }
        });

        btnAntiD.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ad.dismiss();
                        updateAntiDAlertDialog(title);
                    }
                });

        alertDialog.setView(convertView);
        tvDialogTitle.setText(title + " History");
        adapter = new MyListAdapter(
                ServiceUserActivity.this,
                historyList,
                dateTimeList,
                providerNameList);
        list.setAdapter(adapter);
        ad = alertDialog.show();
    }

    private void updateAntiDAlertDialog(final String title) {
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.dialog_update_history, null);
        final ListView list = (ListView) convertView.findViewById(R.id.lv_history_options);
        Button btnAntiD = (Button) convertView.findViewById(R.id.btn_add_history);
        TextView tvDialogTitle = (TextView) convertView.findViewById(R.id.tv_history_dialog_title);
        ImageView ivExit = (ImageView) convertView.findViewById(R.id.iv_exit_dialog);
        ivExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.dismiss();
            }
        });

        btnAntiD.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (list.isSelected()) {
                            String optionSelected = historyOptions.get(optionPosition);
                            if (title.equals("Anti-D"))
                                putAntiD(optionSelected);
                            else if (title.equals("Vit-K"))
                                putVitK(optionSelected);
                            else if (title.equals("Hearing"))
                                putHearing(optionSelected);
                            else if (title.equals("NBST"))
                                putNBST(optionSelected);
                            else if (title.equals("Feeding"))
                                putFeeding(optionSelected);

                            Log.d("bugs", "anti d position button = " + optionPosition);
                        } else
                            Toast.makeText(ServiceUserActivity.this, "Please Make A Selection First", Toast.LENGTH_LONG).show();
                    }
                });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                list.setSelected(true);
                optionPosition = position;
                Log.d("bugs", "anti d position list = " + position);
            }
        });

        alertDialog.setView(convertView);
        tvDialogTitle.setText("Select " + title + " option");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                ServiceUserActivity.this,
                android.R.layout.simple_list_item_1,
                historyOptions);
        list.setAdapter(adapter);
        ad = alertDialog.show();
    }

    private void putAntiD(String putAntiD) {
        Log.d("bugs", "putting antiD");
        Log.d("bugs", "antiD = " + putAntiD);
        c = Calendar.getInstance();
        PostingData puttingAntiD = new PostingData();

        puttingAntiD.putAntiD(putAntiD, userId);

        pd = new CustomDialogs().showProgressDialog(
                this,
                "Updating Anti-D");

        SmartApiClient.getAuthorizedApiClient().putAnitD(
                puttingAntiD,
                BaseModel.getInstance().getPregnancies().get(p).getId(),
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        String antiD = baseModel.getPregnancy().getAntiD();
                        ad.dismiss();
                        tvPostAntiD.setText(antiD);
                        historyList.add(0, antiD);
                        dateTimeList.add(0, dfHumanReadableTimeDate.format(c.getTime()));
                        providerNameList.add(0, BaseModel.getInstance().getServiceProvider().getName());
                        BaseModel.getInstance().updatePregnancies(p, baseModel.getPregnancy());
                        Log.d("retro", "put anti-d retro success");
                        pd.dismiss();
                        ad.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("retro", "put anti-d retro failure = " + error);
                        pd.dismiss();
                    }
                }
        );
    }

    private void putFeeding(String putFeeding) {
        Log.d("bugs", "putting antiD");
        Log.d("bugs", "feeding = " + putFeeding);
        c = Calendar.getInstance();
        PostingData puttingFeeding = new PostingData();

        puttingFeeding.putFeeding(putFeeding, userId);

        pd = new CustomDialogs().showProgressDialog(
                this,
                "Updating Feeding");

        SmartApiClient.getAuthorizedApiClient().putAnitD(
                puttingFeeding,
                BaseModel.getInstance().getPregnancies().get(p).getId(),
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        String feeding = baseModel.getPregnancy().getFeeding();
                        ad.dismiss();
                        tvPostFeeding.setText(feeding);
                        historyList.add(0, feeding);
                        dateTimeList.add(0, dfHumanReadableTimeDate.format(c.getTime()));
                        providerNameList.add(0, BaseModel.getInstance().getServiceProvider().getName());
                        BaseModel.getInstance().updatePregnancies(p, baseModel.getPregnancy());
                        Log.d("retro", "put feeding retro success");
                        pd.dismiss();
                        ad.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("retro", "put feeding retro failure = " + error);
                        pd.dismiss();
                    }
                }
        );
    }

    private void putVitK(String putVitK) {
        Log.d("bugs", "putting vitK");
        Log.d("bugs", "vitK = " + putVitK);
        c = Calendar.getInstance();
        getRecentBabyId();
        Log.d("bugs", "preg id = " + BaseModel.getInstance().getPregnancies().get(0).getId());
        PostingData puttingVitK = new PostingData();

        puttingVitK.putVitK(putVitK, userId, BaseModel.getInstance().getPregnancies().get(0).getId());

        pd = new CustomDialogs().showProgressDialog(
                this,
                "Updating Vit-K");

        SmartApiClient.getAuthorizedApiClient().putVitK(
                puttingVitK,
                bId,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        String vitK = baseModel.getBaby().getVitK();
                        ad.dismiss();
                        tvPostVitK.setText(vitK);
                        historyList.add(0, vitK);
                        dateTimeList.add(0, dfHumanReadableTimeDate.format(c.getTime()));
                        providerNameList.add(0, BaseModel.getInstance().getServiceProvider().getName());
                        //BaseModel.getInstance().updatePregnancies(p, baseModel.getPregnancy());
                        Log.d("retro", "put vit k retro success");
                        pd.dismiss();
                        ad.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("retro", "put vit k retro failure = " + error);
                        pd.dismiss();
                    }
                }
        );
    }

    private void putHearing(String putHearing) {
        Log.d("bugs", "putting hearing");
        Log.d("bugs", "hearing = " + putHearing);
        c = Calendar.getInstance();
        getRecentBabyId();
        Log.d("bugs", "preg id = " + BaseModel.getInstance().getPregnancies().get(0).getId());
        PostingData puttingHearing = new PostingData();

        puttingHearing.putHearing(putHearing, userId, BaseModel.getInstance().getPregnancies().get(0).getId());

        pd = new CustomDialogs().showProgressDialog(
                this,
                "Updating Hearing");

        SmartApiClient.getAuthorizedApiClient().putHearing(
                puttingHearing,
                bId,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        String hearing = baseModel.getBaby().getHearing();
                        ad.dismiss();
                        tvPostHearing.setText(hearing);
                        historyList.add(0, hearing);
                        dateTimeList.add(0, dfHumanReadableTimeDate.format(c.getTime()));
                        providerNameList.add(0, BaseModel.getInstance().getServiceProvider().getName());
                        //BaseModel.getInstance().updatePregnancies(p, baseModel.getPregnancy());
                        Log.d("retro", "put hearing retro success");
                        pd.dismiss();
                        ad.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("retro", "put hearing retro failure = " + error);
                        pd.dismiss();
                    }
                }
        );
    }

    private void putNBST(String putNbst) {
        Log.d("bugs", "putting hearing");
        Log.d("bugs", "nbst = " + putNbst);
        c = Calendar.getInstance();
        getRecentBabyId();
        Log.d("bugs", "preg id = " + BaseModel.getInstance().getBabies().get(b).getPregnancyId());
        PostingData puttingNbst = new PostingData();

        puttingNbst.putNBST(nbst, userId, BaseModel.getInstance().getPregnancies().get(0).getId());

        pd = new CustomDialogs().showProgressDialog(
                this,
                "Updating NBST");

        SmartApiClient.getAuthorizedApiClient().putNBST(
                puttingNbst,
                bId,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        String nbst = baseModel.getBaby().getNbst();
                        ad.dismiss();
                        tvPostNBST.setText(nbst);
                        historyList.add(0, nbst);
                        dateTimeList.add(0, dfHumanReadableTimeDate.format(c.getTime()));
                        providerNameList.add(0, BaseModel.getInstance().getServiceProvider().getName());
                        Log.d("retro", "put nbst retro success");
                        pd.dismiss();
                        ad.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("retro", "put nbst retro failure = " + error);
                        pd.dismiss();
                    }
                }
        );
    }

    private void getMidwiferyNotes(){
        SmartApiClient.getAuthorizedApiClient().getPregnancyNotes(
                BaseModel.getInstance().getServiceUsers().get(0).getPregnancyIds().get(p),
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Log.d("retro", "put getMidwiferyNotes retro success");
                        BaseModel.getInstance().setPregnancyNotes(baseModel.getPregnancyNotes());
                        pd.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("retro", "put getMidwiferyNotes retro failure = " + error);
                        pd.dismiss();
                    }
                }
        );
    }

    private void postPregnancyActions(String action){
        PostingData postAction = new PostingData();
        postAction.postPregnancyAction(action);

        SmartApiClient.getAuthorizedApiClient().postPregnancyAction(
                postAction,
                BaseModel.getInstance().getServiceUsers().get(0).getPregnancyIds().get(p),
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Log.d("retro", "post pregnancy action retro success");
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("retro", "post pregnancy action retro failure = " + error);
                    }
                }
        );
    }

    private void showActionsDialog() {
        Log.d("bugs", "in show dialog method");
        List<String> listServiceActions = new ArrayList<>();
        for(int i = 0; i < BaseModel.getInstance().getServiceUserActions().size(); i++){
            String shortCode = BaseModel.getInstance().getServiceUserActions().get(i).getShortCode();
            listServiceActions.add(shortCode);
            Log.d("bugs", "shortCode = " + shortCode);
        }

        LayoutInflater inflater = getLayoutInflater();
        alertDialog = new AlertDialog.Builder(ServiceUserActivity.this);
        View convertView = (View) inflater.inflate(R.layout.dialog_list_button, null);
        ListView list = (ListView) convertView.findViewById(R.id.lv_dialog);
        TextView tvDialogTitle = (TextView) convertView.findViewById(R.id.tv_dialog_title);
        ImageView ivDialogExit = (ImageView) convertView.findViewById(R.id.iv_dialog_exit);
        Button btnDialog = (Button) convertView.findViewById(R.id.btn_dialog);
        ivDialogExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.dismiss();
            }
        });

        btnDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = new CustomDialogs().showProgressDialog(
                        ServiceUserActivity.this,
                        "Adding Actions");
                getMidwiferyNotes();
                ad.dismiss();
            }
        });

        alertDialog.setView(convertView);
        //tvDialogTitle.setText(title + " History");
        ActionsBaseAdapter adapter = new ActionsBaseAdapter(
                ServiceUserActivity.this,
                listServiceActions);
        list.setAdapter(adapter);
        ad = alertDialog.show();
    }

    private class ActionsBaseAdapter extends BaseAdapter {
        Context context;
        List<String> listServiceActions = new ArrayList<>();
        List<ServiceUserAction> serviceUserActionList =
                BaseModel.getInstance().getServiceUserActions();
        List<Integer> actionIdList = new ArrayList<>();

        public ActionsBaseAdapter(Context context, List<String> listServiceActions) {
            this.context = context;
            this.listServiceActions = listServiceActions;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return listServiceActions.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            convertView = inflater.inflate(R.layout.list_layout_actions, null);
            TextView tvAction = (TextView) convertView.findViewById(R.id.tv_action);
            final Button btnBookAction = (Button) convertView.findViewById(R.id.btn_book_action);
            final ImageView ivStatus = (ImageView) convertView.findViewById(R.id.img_status);
            final SwipeLayout swipeLayout = (SwipeLayout) convertView.findViewById(R.id.swipe_action_list);
            RelativeLayout llActionListItem = (RelativeLayout) convertView.findViewById(R.id.rl_action_list_item);

            btnBookAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ivStatus.setBackgroundResource(R.color.attended);
                    swipeLayout.close();
                    //actionIdList.add(serviceUserActionList.get(position).getId());
                    postPregnancyActions(listServiceActions.get(position));
                }
            });

            tvAction.setText(listServiceActions.get(position));

            return convertView;
        }
    }

    private class ButtonClick implements View.OnClickListener, DialogInterface {
        Intent intent;

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_usr_book_appointment:
                    setSharedPrefs();
                    intent = new Intent(ServiceUserActivity.this, AppointmentTypeSpinnerActivity.class);
                    intent.putExtra("userId", String.valueOf(userId));
                    startActivity(intent);
                    break;
                case R.id.btn_usr_actions:
                    Log.d("bugs", "action btn pressed");
                    showActionsDialog();
                    break;
                case R.id.ll_usr_contact:
                    dialogContact(userContactList);
                    break;
                case R.id.ll_usr_kin:
                    dialogContact(kinContactList);
                    break;
                case R.id.ll_usr_address:
                    gotoMaps();
                    break;
                case R.id.tr_ante_parity:
                    intent = new Intent(ServiceUserActivity.this, ParityDetailsActivity.class);
                    startActivity(intent);
                    break;
                case R.id.tr_post_anti_d:
                case R.id.iv_anti_d_history_icon:
                    setAntiD();
                    historyAlertDialog("Anti-D");
                    break;
                case R.id.tr_midwife_notes:
                case R.id.iv_post_midwives_notes:
                    intent = new Intent(ServiceUserActivity.this, MidwiferyLogActivity.class);
                    intent.putExtra("pregnancyId", String.valueOf(pregnancyId));
                    startActivity(intent);
                    break;
                case R.id.tr_vit_k:
                case R.id.iv_vit_k_history_icon:
                    setVitK();
                    historyAlertDialog("Vit-K");
                    break;
                case R.id.tr_hearing:
                case R.id.iv_hearing_history_icon:
                    setHearing();
                    historyAlertDialog("Hearing");
                    break;
                case R.id.tr_nbst:
                case R.id.iv_nbst_history_icon:
                    setNBST();
                    historyAlertDialog("NBST");
                    break;
                case R.id.tr_feeding:
                case R.id.iv_feeding_history_icon:
                    setFeeding();
                    historyAlertDialog("Feeding");
                    break;
            }
        }

        @Override
        public void cancel() {
        }

        @Override
        public void dismiss() {
        }
    }

    private class MyListAdapter extends BaseAdapter {
        Context context;
        List<String> historyList = new ArrayList<>();
        List<String> dateTimeList = new ArrayList<>();
        List<String> providerNameList = new ArrayList<>();

        public MyListAdapter(Context context,
                             List<String> historyList,
                             List<String> dateTimeList,
                             List<String> providerNameList) {
            this.context = context;
            this.historyList = historyList;
            this.dateTimeList = dateTimeList;
            this.providerNameList = providerNameList;
        }

        @Override
        public int getCount() {
            return dateTimeList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            convertView = (View) inflater.inflate(R.layout.list_layout_history, null);
            TextView tvHistory = (TextView) convertView.findViewById(R.id.tv_anti_d_option);
            TextView tvDateTime = (TextView) convertView.findViewById(R.id.tv_anti_d_date_time);
            TextView tvName = (TextView) convertView.findViewById(R.id.tv_anti_d_provider_name);

            tvHistory.setText(historyList.get(position));
            tvDateTime.setText(dateTimeList.get(position));
            tvName.setText(providerNameList.get(position));
            return convertView;
        }
    }
}
