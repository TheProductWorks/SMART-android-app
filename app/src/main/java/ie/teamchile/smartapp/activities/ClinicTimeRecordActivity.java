package ie.teamchile.smartapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ie.teamchile.smartapp.R;
import ie.teamchile.smartapp.api.SmartApiClient;
import ie.teamchile.smartapp.model.BaseModel;
import ie.teamchile.smartapp.model.Clinic;
import ie.teamchile.smartapp.model.ClinicTimeRecord;
import ie.teamchile.smartapp.model.PostingData;
import ie.teamchile.smartapp.util.AppointmentHelper;
import ie.teamchile.smartapp.util.CustomDialogs;
import ie.teamchile.smartapp.util.SharedPrefs;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ClinicTimeRecordActivity extends BaseActivity {
    private List<Integer> clinicStopped = new ArrayList<>();
    private List<Integer> clinicStarted = new ArrayList<>();
    private List<Integer> clinicNotStarted = new ArrayList<>();
    private List<Integer> idList = new ArrayList<>();
    private List<String> clinicNotStartedName = new ArrayList<>();
    private List<String> clinicStartedName = new ArrayList<>();
    private List<String> clinicStoppedName = new ArrayList<>();
    private Map<String, List<Integer>> clinicDayMap = new HashMap<>();
    private Map<Integer, Clinic> clinicIdMap = new HashMap<>();
    private List<ClinicTimeRecord> clinicTimeRecords = new ArrayList<>();
    private ListView lvNotStarted;
    private ListView lvStarted;
    private ListView lvStopped;
    private String todayDay;
    private String todayDate;
    private String now;
    private String then;
    private String date;
    private int clinicStartedId;
    private int clinicNotStartedId;
    private int clinicStoppedId;
    private Button btnStartClinic;
    private Button btnStopClinic;
    private Button btnResetRecord;
    private ArrayAdapter adapterNotStart;
    private ArrayAdapter adapterStart;
    private ArrayAdapter adapterStop;
    private int recordId = 0;
    private int recordIdForDelete;
    private int clinicIdForDelete;
    private int recordGetDone;
    private SharedPrefs sharedPrefs = new SharedPrefs();
    private AppointmentHelper appointmentHelper = new AppointmentHelper();
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentForNav(R.layout.activity_clinic_time_record);
        c = Calendar.getInstance();
        todayDay = dfDayLong.format(c.getTime());
        todayDay = "Tuesday";
        todayDate = dfDateOnly.format(c.getTime());
        lvNotStarted = (ListView) findViewById(R.id.lv_clinics_not_started);
        lvNotStarted.setOnItemClickListener(new ItemClicky());
        lvStarted = (ListView) findViewById(R.id.lv_clinics_started);
        lvStarted.setOnItemClickListener(new ItemClicky());
        lvStopped = (ListView) findViewById(R.id.lv_clinics_stopped);
        lvStopped.setOnItemLongClickListener(new ItemClicky());
        btnStartClinic = (Button) findViewById(R.id.btn_start_clinic);
        btnStartClinic.setOnClickListener(new ButtonClicky());
        btnStopClinic = (Button) findViewById(R.id.btn_stop_clinic);
        btnStopClinic.setOnClickListener(new ButtonClicky());
        btnResetRecord = (Button) findViewById(R.id.btn_reset_clinic_record);
        btnResetRecord.setOnClickListener(new ButtonClicky());

        disableButtons();

        setActionBarTitle("Start/Stop Clinics");

        if (!todayDay.equals("Saturday") || !todayDay.equals("Sunday"))
            getDataFromDb();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("bugs", "setting data to singleton");

        BaseModel.getInstance().setClinicTimeRecords(clinicTimeRecords);
        BaseModel.getInstance().setClinicStopped(clinicStopped);
        BaseModel.getInstance().setClinicStarted(clinicStarted);
        BaseModel.getInstance().setClinicNotStarted(clinicNotStarted);
        BaseModel.getInstance().setClinicMap(clinicIdMap);
        BaseModel.getInstance().setClinicDayMap(clinicDayMap);
    }

    private void disableButtons() {
        btnStartClinic.setEnabled(false);
        btnStopClinic.setEnabled(false);
        btnResetRecord.setEnabled(false);
    }

    private void getDataFromDb() {
        recordGetDone = 0;

        List<Clinic> clinics = BaseModel.getInstance().getClinics();

        for (int i = 0; i < clinics.size(); i++) {
            List<String> trueDays = clinics.get(i).getTrueDays();
            for (int j = 0; j < trueDays.size(); j++) {
                Clinic clinic = clinics.get(i);
                int clinicId = clinics.get(i).getId();
                clinicIdMap.put(clinicId, clinic);

                if (clinicDayMap.get(trueDays.get(j)) != null) {
                    idList = new ArrayList<>();
                    idList = clinicDayMap.get(trueDays.get(j));
                    idList.add(clinicId);
                    clinicDayMap.put(trueDays.get(j), idList);
                } else {
                    idList = new ArrayList<>();
                    idList.add(clinicId);
                    clinicDayMap.put(trueDays.get(j), idList);
                }
            }
        }
        Log.d("Clinic", "Record map = " + clinicDayMap);

        idList = clinicDayMap.get(todayDay);

        if (clinicDayMap.containsKey(todayDay)) {
            pd = new CustomDialogs().showProgressDialog(
                    ClinicTimeRecordActivity.this,
                    "Updating Time Records");
            for (int i = 0; i < idList.size(); i++) {
                getTimeRecords(idList.get(i), todayDate);
            }
        } else {
            setNotStartedList();
            setStartedList();
            setStoppedList();
        }
        timer = new CountDownTimer(200, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (recordGetDone >= 3)
                    pd.dismiss();
                else
                    timer.start();
            }
        }.start();
    }

    private void getTimeRecords(final int clinicId, String date) {
        SmartApiClient.getAuthorizedApiClient().getTimeRecords(
                clinicId,
                date,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Log.d("retro", "get time record success");
                        if (baseModel.getClinicTimeRecords().size() > 0) {
                            clinicTimeRecords.add(baseModel.getClinicTimeRecords().get(0));
                            if (baseModel.getClinicTimeRecords().get(0).getEndTime() == null) {
                                if (!clinicStarted.contains(clinicId))
                                    clinicStarted.add(clinicId);
                            } else if (baseModel.getClinicTimeRecords().get(0).getEndTime() == null &&
                                    baseModel.getClinicTimeRecords().get(0).getStartTime() == null) {
                                clinicNotStarted.add(clinicId);
                            } else {
                                if (!clinicStopped.contains(clinicId))
                                    clinicStopped.add(clinicId);
                            }
                        } else {
                            if (!clinicNotStarted.contains(clinicId))
                                clinicNotStarted.add(clinicId);
                        }

                        setNotStartedList();
                        setStartedList();
                        setStoppedList();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("retro", "get time record failure = " + error);
                    }
                }
        );
    }

    private void putStartTime(String now, final int clinicId) {
        PostingData timeRecord = new PostingData();
        timeRecord.putStartTimeRecord(
                now,
                clinicId,
                date);

        pd = new CustomDialogs().showProgressDialog(
                ClinicTimeRecordActivity.this,
                "Updating Clinic Time Records");

        SmartApiClient.getAuthorizedApiClient().postTimeRecords(
                timeRecord,
                clinicId,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Log.d("SMART", "retro success");
                        disableButtons();

                        clinicTimeRecords.add(baseModel.getClinicTimeRecord());
                        clinicNotStarted.remove(clinicNotStarted.indexOf(clinicId));
                        clinicStarted.add(clinicId);
                        setNotStartedList();
                        setStartedList();
                        pd.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("SMART", "retro error = " + error.getResponse());
                        pd.dismiss();
                        disableButtons();
                    }
                });
    }

    private void putEndTime(String then, String date, final int clinicId) {
        for (int i = 0; i < clinicTimeRecords.size(); i++) {
            if (clinicTimeRecords.get(i).getClinicId() == clinicId)
                recordId = clinicTimeRecords.get(i).getId();
        }

        PostingData timeRecord = new PostingData();
        timeRecord.putEndTimeRecord(
                then,
                date,
                clinicId);

        pd = new CustomDialogs().showProgressDialog(
                ClinicTimeRecordActivity.this,
                "Updating Clinic Time Records");

        SmartApiClient.getAuthorizedApiClient().putTimeRecords(
                timeRecord,
                clinicId,
                recordId,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Log.d("SMART", "retro success");
                        disableButtons();

                        for (int i = 0; i < clinicTimeRecords.size(); i++) {
                            if (clinicTimeRecords.get(i).getId() == recordId) {
                                clinicTimeRecords.remove(i);
                                clinicStarted.remove(clinicStarted.indexOf(clinicId));
                                clinicStopped.add(clinicId);
                                setStartedList();
                                setStoppedList();
                            }
                        }
                        clinicTimeRecords.add(baseModel.getClinicTimeRecord());
                        pd.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("SMART", "retro error = " + error.getResponse());
                        pd.dismiss();
                        disableButtons();
                    }
                });
    }

    private void deleteTimeRecord(final int clinicIdForDelete, final int recordIdForDelete) {
        pd = new CustomDialogs().showProgressDialog(
                ClinicTimeRecordActivity.this,
                "Deleting Time Record");

        SmartApiClient.getAuthorizedApiClient().deleteTimeRecordById(
                clinicIdForDelete,
                recordIdForDelete,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Log.d("retro", "deleteTimeRecord success");
                        disableButtons();

                        for (int i = 0; i < clinicTimeRecords.size(); i++) {
                            if (clinicTimeRecords.get(i).getId() == recordIdForDelete) {
                                clinicTimeRecords.remove(i);
                                clinicStopped.remove(clinicStopped.indexOf(clinicIdForDelete));
                                clinicNotStarted.add(clinicIdForDelete);
                                setStartedList();
                                setStoppedList();
                                setNotStartedList();
                            }
                        }
                        pd.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("retro", "deleteTimeRecord failure = " + error);
                        pd.dismiss();
                        disableButtons();
                    }
                }
        );
    }


    private void setNotStartedList() {
        recordGetDone++;
        String clinicName;
        clinicNotStartedName = new ArrayList<>();
        if (clinicNotStarted.size() == 0) {
            clinicNotStartedName.add("No clinics to be started");
            lvNotStarted.setEnabled(false);
        } else {
            lvNotStarted.setEnabled(true);
            for (int i = 0; i < clinicNotStarted.size(); i++) {
                clinicName = clinicIdMap.get(clinicNotStarted.get(i)).getName();
                int clinicId = clinicNotStarted.get(i);
                switch (clinicId) {
                    case 6:
                        clinicName += " (Domino)";
                        break;
                    case 10:
                        clinicName += " (Satellite)";
                        break;
                }
                clinicNotStartedName.add(clinicName);
            }
        }

        adapterNotStart = new ArrayAdapter(
                ClinicTimeRecordActivity.this,
                android.R.layout.simple_list_item_1,
                clinicNotStartedName);

        lvNotStarted.setAdapter(adapterNotStart);
        adapterNotStart.notifyDataSetChanged();
    }

    private void setStartedList() {
        recordGetDone++;
        clinicStartedName = new ArrayList<>();
        if (clinicStarted.size() == 0) {
            clinicStartedName.add("No clinics currently started");
            lvStarted.setEnabled(false);
        } else {
            lvStarted.setEnabled(true);
            for (int i = 0; i < clinicStarted.size(); i++) {
                String clinicName = clinicIdMap.get(clinicStarted.get(i)).getName();
                int clinicId = clinicStarted.get(i);
                switch (clinicId) {
                    case 6:
                        clinicName += " (Domino)";
                        break;
                    case 10:
                        clinicName += " (Satellite)";
                        break;
                }
                clinicStartedName.add(clinicName);
            }
        }

        adapterStart = new ArrayAdapter(
                ClinicTimeRecordActivity.this,
                android.R.layout.simple_list_item_1,
                clinicStartedName);

        lvStarted.setAdapter(adapterStart);
        adapterStart.notifyDataSetChanged();
    }

    private void setStoppedList() {
        recordGetDone++;
        clinicStoppedName = new ArrayList<>();

        if (clinicStopped.size() == 0) {
            clinicStoppedName.add("No clinics currently stopped");
            lvStopped.setEnabled(false);
        } else {
            lvStopped.setEnabled(true);
            for (int i = 0; i < clinicStopped.size(); i++) {
                String clinicName = clinicIdMap.get(clinicStopped.get(i)).getName();
                int clinicId = clinicStopped.get(i);
                switch (clinicId) {
                    case 6:
                        clinicName += " (Domino)";
                        break;
                    case 10:
                        clinicName += " (Satellite)";
                        break;
                }
                clinicStoppedName.add(clinicName);
            }
        }

        adapterStop = new ArrayAdapter(
                ClinicTimeRecordActivity.this,
                android.R.layout.simple_list_item_1,
                clinicStoppedName);

        lvStopped.setAdapter(adapterStop);
        adapterStop.notifyDataSetChanged();
    }

    private void sortListAlpha(List<String> badList) {
        Collections.sort(badList, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareToIgnoreCase(rhs);
            }
        });
    }

    private class ItemClicky implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getId()) {
                case R.id.lv_clinics_not_started:
                    btnStartClinic.setEnabled(true);
                    clinicStartedId = 0;
                    adapterStart.notifyDataSetChanged();
                    lvStarted.setAdapter(adapterStart);
                    Log.d("bugs", "not started clinic list pressed");
                    clinicNotStartedId = clinicNotStarted.get(position);
                    Log.d("bugs", "clinicNotStartedId = " + clinicNotStartedId);
                    break;
                case R.id.lv_clinics_started:
                    btnStopClinic.setEnabled(true);
                    clinicNotStartedId = 0;
                    adapterNotStart.notifyDataSetChanged();
                    lvNotStarted.setAdapter(adapterNotStart);
                    clinicStartedId = clinicStarted.get(position);
                    break;
            }
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Vibrator vibe = (Vibrator) ClinicTimeRecordActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
            switch (parent.getId()) {
                case R.id.lv_clinics_stopped:
                    btnResetRecord.setEnabled(true);
                    vibe.vibrate(50);
                    clinicIdForDelete = clinicStopped.get(position);
                    for (int i = 0; i < clinicTimeRecords.size(); i++) {
                        if (clinicTimeRecords.get(i).getClinicId() == clinicIdForDelete) {
                            recordIdForDelete = clinicTimeRecords.get(i).getId();
                        }
                    }
                    break;
            }
            return false;
        }
    }

    private class ButtonClicky implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            c = Calendar.getInstance();
            now = dfDateTime.format(c.getTime());
            c.add(Calendar.HOUR, 1);
            then = dfDateTime.format(c.getTime());
            date = dfDateOnly.format(c.getTime());
            switch (v.getId()) {
                case R.id.btn_start_clinic:
                    if (clinicNotStartedId != 0) {
                        sharedPrefs.overWriteStringPrefs(ClinicTimeRecordActivity.this,
                                "clinic_started", String.valueOf(clinicNotStartedId));
                        sharedPrefs.addToStringSetPrefs(ClinicTimeRecordActivity.this,
                                "appts_got", String.valueOf(clinicNotStartedId));
                        putStartTime(now, clinicNotStartedId);
                        c = Calendar.getInstance();
                        Date todayDate = c.getTime();
                        appointmentHelper.weekDateLooper(todayDate, clinicNotStartedId);
                    }
                    break;
                case R.id.btn_stop_clinic:
                    if (clinicStartedId != 0)
                        putEndTime(then, date, clinicStartedId);
                    break;
                case R.id.btn_reset_clinic_record:
                    if (clinicIdForDelete != 0)
                        deleteTimeRecord(clinicIdForDelete, recordIdForDelete);
                    break;
            }
            clinicNotStartedId = 0;
            clinicStartedId = 0;
        }
    }
}
