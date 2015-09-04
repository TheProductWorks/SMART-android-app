package ie.teamchile.smartapp.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ie.teamchile.smartapp.R;
import ie.teamchile.smartapp.api.SmartApiClient;
import ie.teamchile.smartapp.model.Appointment;
import ie.teamchile.smartapp.model.BaseModel;
import ie.teamchile.smartapp.model.PostingData;
import ie.teamchile.smartapp.util.CustomDialogs;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AppointmentCalendarActivity extends BaseActivity {
    protected static Date daySelected;
    private static int serviceOptionSelected, weekSelected, clinicSelected, visitOptionSelected;
    private final int sdkVersion = Build.VERSION.SDK_INT;
    private Date openingAsDate, closingAsDate;
    private String clinicOpening, clinicClosing, closingMinusInterval,
            dateSelectedStr, timeBefore, timeAfter, nameOfClinic;
    private int appointmentInterval, dayOfWeek;
    private List<String> timeSingle;
    private List<Integer> listOfApptId = new ArrayList<>();
    private Calendar c = Calendar.getInstance(), myCalendar = Calendar.getInstance();
    private Intent intent;
    private List<String> timeList = new ArrayList<>();
    private List<Integer> idList = new ArrayList<>();
    private int serviceOptionId;
    private Button dateInList, btnPrevWeek, btnNextWeek;
    private BaseAdapter adapter;
    private ListView listView;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentForNav(R.layout.activity_appointment_calendar);

        Log.d("bugs", "clinicSelected = " + clinicSelected);

        dateInList = (Button) findViewById(R.id.btn_date);
        listView = (ListView) findViewById(R.id.lv_appointment_list);
        btnPrevWeek = (Button) findViewById(R.id.btn_prev);
        btnPrevWeek.setOnClickListener(new ButtonClick());
        btnNextWeek = (Button) findViewById(R.id.btn_next);
        btnNextWeek.setOnClickListener(new ButtonClick());

        serviceOptionId = BaseModel.getInstance().getClinicMap().get(clinicSelected).getServiceOptionIds().get(0);
        clinicOpening = BaseModel.getInstance().getClinicMap().get(clinicSelected).getOpeningTime();
        clinicClosing = BaseModel.getInstance().getClinicMap().get(clinicSelected).getClosingTime();
        appointmentInterval = BaseModel.getInstance().getClinicMap().get(clinicSelected).getAppointmentInterval();
        try {
            openingAsDate = dfTimeOnly.parse(String.valueOf(clinicOpening));
            closingAsDate = dfTimeOnly.parse(String.valueOf(clinicClosing));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        myCalendar.setTime(closingAsDate);
        myCalendar.add(Calendar.MINUTE, (-appointmentInterval));
        closingMinusInterval = dfTimeOnly.format(myCalendar.getTime());

        c.setTime(daySelected);
        dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        idList = new ArrayList<>();
        adapter = new ListElementAdapter();
        listView.setAdapter(adapter);

        newSetToList(daySelected);
        createDatePicker();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("bugs", "in onResume");
        Log.d("bugs", "daySelected: " + daySelected);
        idList = new ArrayList<>();
        adapter.notifyDataSetChanged();
        newSetToList(daySelected);
    }

    public void pauseButton() {
        btnNextWeek.setEnabled(false);
        btnPrevWeek.setEnabled(false);
        CountDownTimer nextTimer = new CountDownTimer(250, 250) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                btnNextWeek.setEnabled(true);
                btnPrevWeek.setEnabled(true);
            }
        };
        nextTimer.start();
    }

    public List<Integer> removeZeros(List<Integer> badList) {
        if (badList != null)
            for (int i = 0; i < badList.size(); i++)
                if (badList.get(i).equals(0))
                    badList.remove(i);
        return badList;
    }

    private void createDatePicker() {
        myCalendar.setTime(daySelected);
        final DatePickerDialog.OnDateSetListener pickerDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (pd != null) {
                    pd.dismiss();
                }
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                Log.d("postAppointment", "datePicker: " + myCalendar.getTime());
                Log.d("postAppointment", "datePicker formatted: " +
                        dfDateOnly.format(myCalendar.getTime()));
                Log.d("bugs", "c.getDay: " + dayOfWeek);
                Log.d("bugs", "myCalendar.getDay: " + myCalendar.get(Calendar.DAY_OF_WEEK));
                if (myCalendar.get(Calendar.DAY_OF_WEEK) == dayOfWeek) {
                    dateInList.setText(dfDateWMonthName.format(myCalendar.getTime()));
                    newSetToList(myCalendar.getTime());
                } else {
                    Log.d("bugs", "wrong date");
                    pd = new CustomDialogs().showProgressDialog(AppointmentCalendarActivity.this,
                            "Invalid day selected\nPlease choose another");
                    Handler mHandler = new Handler();
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            pd.dismiss();
                        }
                    }, 2000);
                }
            }
        };
        dateInList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AppointmentCalendarActivity.this, pickerDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void newSetToList(Date dateSelected) {
        timeSingle = new ArrayList<>();
        listOfApptId = new ArrayList<>();

        timeList = new ArrayList<>();
        idList = new ArrayList<>();

        Date apptTime = openingAsDate;
        daySelected = dateSelected;

        dateSelectedStr = dfDateOnly.format(dateSelected);
        dateInList.setText(dfDateWMonthName.format(dateSelected));
        nameOfClinic = BaseModel.getInstance().getClinicMap().get(clinicSelected).getName();
        setActionBarTitle(nameOfClinic);

        while (!closingAsDate.before(apptTime)) {
            timeList.add(dfTimeOnly.format(apptTime));
            idList.add(0);
            c.setTime(apptTime);
            c.add(Calendar.MINUTE, appointmentInterval);
            apptTime = c.getTime();
        }

        if (BaseModel.getInstance().getClinicVisitClinicDateApptIdMap().containsKey(clinicSelected)) {
            listOfApptId = BaseModel.getInstance().getClinicVisitClinicDateApptIdMap().get(clinicSelected).get(dateSelectedStr);

            if (listOfApptId != null) {
                for (int i = 0; i < listOfApptId.size(); i++) {
                    Appointment appointment = BaseModel.getInstance().getClinicVisitIdApptMap().get(listOfApptId.get(i));
                    String timeOfAppt = "";
                    try {
                        timeOfAppt = dfTimeOnly.format(dfTimeWSec.parse(appointment.getTime()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if(timeList.contains(timeOfAppt)){
                        idList.set(timeList.indexOf(timeOfAppt), appointment.getId());
                    }
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void changeAttendStatus(Boolean status, int position) {
        pd = new CustomDialogs().showProgressDialog(AppointmentCalendarActivity.this, "Changing Attended Status");

        PostingData attendedStatus = new PostingData();
        attendedStatus.putAppointmentStatus(
                status,
                clinicSelected,
                BaseModel.getInstance().getLogin().getId(),
                BaseModel.getInstance().getClinicVisitIdApptMap().get(idList.get(position)).getServiceUserId());

        SmartApiClient.getAuthorizedApiClient().putAppointmentStatus(
                attendedStatus,
                idList.get(position),
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Toast.makeText(AppointmentCalendarActivity.this,
                                "status changed", Toast.LENGTH_LONG).show();
                        pd.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("Attended", "retro error = " + error);
                        pd.dismiss();
                    }
                }
        );
    }

    private void searchServiceUser(int serviceUserId, final Intent intent) {
        SmartApiClient.getAuthorizedApiClient().getServiceUserById(serviceUserId,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        BaseModel.getInstance().setServiceUsers(baseModel.getServiceUsers());
                        BaseModel.getInstance().setBabies(baseModel.getBabies());
                        BaseModel.getInstance().setPregnancies(baseModel.getPregnancies());
                        startActivity(intent);
                        pd.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        pd.dismiss();
                        Toast.makeText(
                                AppointmentCalendarActivity.this,
                                "Error Search Patient: " + error,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void setServiceOptionSelected(int serviceOptionSelected) {
        AppointmentCalendarActivity.serviceOptionSelected = serviceOptionSelected;
    }

    public void setClinicSelected(int clinicSelected) {
        AppointmentCalendarActivity.clinicSelected = clinicSelected;
    }

    public void setWeekSelected(int weekSelected) {
        AppointmentCalendarActivity.weekSelected = weekSelected;
    }

    public void setDaySelected(Date daySelected) {
        AppointmentCalendarActivity.daySelected = daySelected;
    }

    public void setVisitOption(int visitOptionSelected) {
        AppointmentCalendarActivity.visitOptionSelected = visitOptionSelected;
    }

    private class ButtonClick implements View.OnClickListener {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_prev:
                    c.setTime(daySelected);
                    c.add(Calendar.DAY_OF_YEAR, -7);
                    daySelected = c.getTime();
                    myCalendar.setTime(daySelected);
                    createDatePicker();
                    newSetToList(c.getTime());
                    pauseButton();
                    break;
                case R.id.btn_next:
                    c.setTime(daySelected);
                    c.add(Calendar.DAY_OF_YEAR, 7);
                    daySelected = c.getTime();
                    myCalendar.setTime(daySelected);
                    createDatePicker();
                    newSetToList(c.getTime());
                    pauseButton();
                    break;
            }
        }
    }

    private class ListElementAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return idList.size();
        }

        @Override
        public Object getItem(int position) {
            return idList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return idList.get(position).hashCode();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            final Appointment appointment = BaseModel.getInstance().getClinicVisitIdApptMap().get(getItem(position));
            final Boolean attended;

            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(AppointmentCalendarActivity.this);
                convertView = layoutInflater.inflate(R.layout.list_layout_appointment, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else
                holder = (ViewHolder) convertView.getTag();

            holder.timeText.setText(timeList.get(position));

            if(getItem(position) == 0){
                holder.nameText.setText("Free Slot");
                holder.gestText.setText("");
                attended = false;
                holder.swipeLayout.setSwipeEnabled(false);
                holder.nameText.setTextColor(getResources().getColor(R.color.free_slot));
                holder.nameText.setTypeface(null, Typeface.ITALIC);
            } else {
                holder.nameText.setText(appointment.getServiceUser().getName());
                holder.gestText.setText(appointment.getServiceUser().getGestation());
                attended = appointment.getAttended();
                holder.swipeLayout.setSwipeEnabled(true);
                holder.nameText.setTextColor(holder.gestText.getTextColors().getDefaultColor());
                holder.nameText.setTypeface(null, Typeface.NORMAL);
            }

            if (attended) {
                holder.ivAttend.setBackgroundResource(R.color.attended);
                holder.btnChangeStatus.setText("No");
            } else {
                holder.ivAttend.setBackgroundResource(R.color.unattended);
                holder.btnChangeStatus.setText("Yes");
            }

            holder.llApptListItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (getItem(position).equals(0)) {
                        intent = new Intent(AppointmentCalendarActivity.this, CreateAppointmentActivity.class);
                        intent.putExtra("from", "clinic-appointment");
                        intent.putExtra("time", timeList.get(position));
                        intent.putExtra("clinicID", String.valueOf(clinicSelected));
                        intent.putExtra("serviceOptionId", String.valueOf(serviceOptionId));
                        startActivity(intent);
                    } else {
                        int serviceUserId = appointment.getServiceUserId();
                        Log.d("bugs", "db string: " + "service_users" + "/" + serviceUserId);
                        pd = new CustomDialogs().showProgressDialog(AppointmentCalendarActivity.this,
                                "Fetching Information");
                        intent = new Intent(AppointmentCalendarActivity.this, ServiceUserActivity.class);
                        searchServiceUser(serviceUserId, intent);
                    }
                    return true;
                }
            });

            holder.btnChangeStatus.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Button", "position = " + position);
                    holder.swipeLayout.close();
                    if (!attended) {
                        appointment.setAttended(true);
                        changeAttendStatus(true, position);
                        appointment.setAttended(true);
                        adapter.notifyDataSetChanged();
                    } else if (attended) {
                        changeAttendStatus(false, position);
                        appointment.setAttended(false);
                        adapter.notifyDataSetChanged();
                    }
                }
            });
            return convertView;
        }

        private class ViewHolder {
            private TextView timeText;
            private TextView nameText;
            private TextView gestText;
            private Button btnChangeStatus;
            private ImageView ivAttend;
            private SwipeLayout swipeLayout;
            private LinearLayout llApptListItem;

             public ViewHolder(View view) {
                 timeText = (TextView) view.findViewById(R.id.tv_time);
                 nameText = (TextView) view.findViewById(R.id.tv_name);
                 gestText = (TextView) view.findViewById(R.id.tv_gestation);
                 btnChangeStatus = (Button) view.findViewById(R.id.btn_change_status);
                 ivAttend = (ImageView) view.findViewById(R.id.img_attended);
                 swipeLayout = (SwipeLayout) view.findViewById(R.id.swipe_appt_list);
                 llApptListItem = (LinearLayout) view.findViewById(R.id.ll_appt_list_item);
             }
        }
    }
}