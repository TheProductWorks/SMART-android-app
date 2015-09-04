package ie.teamchile.smartapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ie.teamchile.smartapp.R;
import ie.teamchile.smartapp.model.BaseModel;
import ie.teamchile.smartapp.util.AdapterSpinner;
import ie.teamchile.smartapp.util.AppointmentHelper;
import ie.teamchile.smartapp.util.CustomDialogs;
import ie.teamchile.smartapp.util.SharedPrefs;

public class AppointmentTypeSpinnerActivity extends BaseActivity {
    private ArrayAdapter<String> appointmentAdapter, visitAdapter, visitDayAdapter,
            serviceOptionAdapter, clinicAdapter, dayAdapter, weekAdapter;
    private List<String> serviceOptionNameList;
    private List<Integer> idList;
    private int serviceOptionSelected, clinicSelected, weekSelected, visitOptionSelected;
	private Date daySelected, dayOfWeek;
    private Calendar c = Calendar.getInstance();
    private AppointmentCalendarActivity passOptions = new AppointmentCalendarActivity();
    private int spinnerWarning;
    private Spinner appointmentTypeSpinner, serviceOptionSpinner,
    		visitOptionSpinner, visitDaySpinner, clinicSpinner, daySpinner, weekSpinner;
    private TextView tvAppointmentType, tvServiceOption, tvVisit, tvVisitDay, tvClinic, tvDay, tvWeek;
    private SharedPrefs sharedPrefs = new SharedPrefs();
    private AppointmentHelper apptHelp = new AppointmentHelper();
    private CountDownTimer timer;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentForNav(R.layout.activity_appointment_type_spinner);

        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Log.d("MYLOG", "Date set to " + c.getTime());

        spinnerWarning = getResources().getColor(R.color.spinner_warning);

        tvAppointmentType = (TextView) findViewById(R.id.tv_appointment_type);
        tvServiceOption = (TextView) findViewById(R.id.tv_service_option);
        tvVisit = (TextView) findViewById(R.id.tv_visit_option);
        tvVisitDay = (TextView) findViewById(R.id.tv_visit_day);
        tvClinic = (TextView) findViewById(R.id.tv_clinic);
        tvDay = (TextView) findViewById(R.id.tv_day);
        tvWeek = (TextView) findViewById(R.id.tv_week);

        appointmentTypeSpinner = (Spinner) findViewById(R.id.spnr_appointment_type);
        serviceOptionSpinner = (Spinner) findViewById(R.id.spnr_service_option);
        visitOptionSpinner = (Spinner) findViewById(R.id.spnr_visit_option);
        visitDaySpinner = (Spinner) findViewById(R.id.spnr_visit_day);
        clinicSpinner = (Spinner) findViewById(R.id.spnr_clinic);
        daySpinner = (Spinner) findViewById(R.id.spnr_day);
        weekSpinner = (Spinner) findViewById(R.id.spnr_week);

        setAppointmentTypeSpinner();
        setServiceOptionSpinner();
        setVisitSpinner();
        setVisitDaySpinner();

        serviceOptionSpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());
        weekSpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());
        visitOptionSpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());

        tvAppointmentType.setVisibility(View.VISIBLE);
        tvServiceOption.setVisibility(View.GONE);
        tvVisit.setVisibility(View.GONE);
        tvVisitDay.setVisibility(View.GONE);
        tvClinic.setVisibility(View.GONE);
        tvDay.setVisibility(View.GONE);
        tvWeek.setVisibility(View.GONE);

        appointmentTypeSpinner.setVisibility(View.VISIBLE);
        serviceOptionSpinner.setVisibility(View.GONE);
        visitOptionSpinner.setVisibility(View.GONE);
        visitDaySpinner.setVisibility(View.GONE);
        clinicSpinner.setVisibility(View.GONE);
        daySpinner.setVisibility(View.GONE);
        weekSpinner.setVisibility(View.GONE);
    }

	private void setServiceOptionSpinner(){
        int mapSize = BaseModel.getInstance().getServiceOptionsClinicMap().size();
        serviceOptionNameList = new ArrayList<>();
        serviceOptionNameList.add("Select Service Option");

        for(int i = 1; i <= mapSize; i++){
            serviceOptionNameList.add("- " + BaseModel.getInstance().getServiceOptionsClinicMap().get(i).getName());
        }
        serviceOptionAdapter = new AdapterSpinner(this, R.layout.spinner_layout, serviceOptionNameList, R.id.tv_spinner_item);
        serviceOptionAdapter.setDropDownViewResource(R.layout.spinner_layout);
        serviceOptionSpinner.setAdapter(serviceOptionAdapter);
	}

	private void setClinicSpinner(int z){
        idList = BaseModel.getInstance().getServiceOptionsClinicMap().get(z).getClinicIds();
		List<String> clinicNames = new ArrayList<>();
		clinicNames.add("Select Clinic");

		if(idList != null){
			for(int i = 0; i < idList.size(); i++){
				clinicNames.add("- " + BaseModel.getInstance().getClinicMap().get(idList.get(i)).getName());
			}
		}

        clinicSpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());
        clinicAdapter = new AdapterSpinner(this, R.layout.spinner_layout, clinicNames, R.id.tv_spinner_item);
        clinicAdapter.setDropDownViewResource(R.layout.spinner_layout);
        clinicSpinner.setAdapter(clinicAdapter);
	}

    private void setVisitSpinner(){
        int size = BaseModel.getInstance().getServiceOptionsHomeList().size();

        Log.d("Retrofit", "clinic id list = " + idList);
        List<String> visitClinics = new ArrayList<>();
        visitClinics.add("Select Visit Option");

        if(size != 0){
            for(int i = 0; i < size; i++){
                visitClinics.add("- " + BaseModel.getInstance().getServiceOptionsHomeList().get(i).getName());
            }
        }

        visitOptionSpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());
        visitAdapter = new AdapterSpinner(this, R.layout.spinner_layout, visitClinics, R.id.tv_spinner_item);
        visitAdapter.setDropDownViewResource(R.layout.spinner_layout);
        visitOptionSpinner.setAdapter(visitAdapter);
    }

    private void setVisitDaySpinner(){
        Calendar cal = Calendar.getInstance();
        List<String> visitDayList = new ArrayList<>();
        visitDayList.add("Select Day");

        for(int i = 0; i < 10; i ++){
            String day = dfDayShort.format(cal.getTime());
            String date = dfDateWMonthName.format(cal.getTime());

            visitDayList.add("- " + day + ", " + date);

            cal.add(Calendar.DATE, 1);
        }

        visitDaySpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());
        visitDayAdapter = new AdapterSpinner(this, R.layout.spinner_layout, visitDayList, R.id.tv_spinner_item);
        visitDayAdapter.setDropDownViewResource(R.layout.spinner_layout);
        visitDaySpinner.setAdapter(visitDayAdapter);
    }

    private void setAppointmentTypeSpinner(){
        appointmentTypeSpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());
        appointmentAdapter = new AdapterSpinner(this, R.array.appointment_type_list, R.layout.spinner_layout,  R.id.tv_spinner_item);
        appointmentAdapter.setDropDownViewResource(R.layout.spinner_layout);
        appointmentTypeSpinner.setAdapter(appointmentAdapter);
    }

	private void setWeekSpinner(Date dayOfWeek){
		List<String> weeks = new ArrayList<>();
		weeks.add("Select Week");

		for(int i = 1; i <= 10; i++){
            c = Calendar.getInstance();
            Log.d("MYLOG", "Week " + i + " selected");
    		c.add(Calendar.DAY_OF_YEAR, 7 * i);
    		c.set(Calendar.DAY_OF_WEEK, dayOfWeek.getDay() + 1);
    		weeks.add("- Week " + i + " (" + dfDowMonthDay.format(c.getTime()) + ")");
		}

		weekAdapter = new AdapterSpinner(this, R.layout.spinner_layout, weeks, R.id.tv_spinner_item);
        weekAdapter.setDropDownViewResource(R.layout.spinner_layout);
        weekSpinner.setAdapter(weekAdapter);
	}

	private void setDaySpinner(List<String> days){
		for(int i = 0; i < days.size(); i++){
			String dayFirstLetterUpperCase = Character.toString(days.get(i).charAt(0))
					.toUpperCase(Locale.getDefault())
					+ days.get(i).substring(1);
			days.set(i, dayFirstLetterUpperCase);
		}
		days.add(0, "Select Day");

		daySpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());
        dayAdapter = new AdapterSpinner(this, R.layout.spinner_layout, days, R.id.tv_spinner_item);
        dayAdapter.setDropDownViewResource(R.layout.spinner_layout);
        daySpinner.setAdapter(dayAdapter);
	}

    private class MySpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getId()) {
                case R.id.spnr_appointment_type:
                    switch (position) {
                        case 0:
                            tvAppointmentType.setVisibility(View.VISIBLE);
                            tvServiceOption.setVisibility(View.GONE);
                            tvVisit.setVisibility(View.GONE);
                            tvVisitDay.setVisibility(View.GONE);
                            tvClinic.setVisibility(View.GONE);
                            tvDay.setVisibility(View.GONE);
                            tvWeek.setVisibility(View.GONE);

                        	appointmentTypeSpinner.setBackgroundColor(spinnerWarning);
                            appointmentTypeSpinner.setVisibility(View.VISIBLE);
                            serviceOptionSpinner.setVisibility(View.GONE);
                            visitOptionSpinner.setVisibility(View.GONE);
                            visitDaySpinner.setVisibility(View.GONE);
                            clinicSpinner.setVisibility(View.GONE);
                            weekSpinner.setVisibility(View.GONE);
                            daySpinner.setVisibility(View.GONE);
                            break;
                        case 1:     //Clinic
                            tvServiceOption.setVisibility(View.VISIBLE);
                            tvVisit.setVisibility(View.GONE);
                            tvVisitDay.setVisibility(View.GONE);

                        	appointmentTypeSpinner.setBackgroundColor(Color.TRANSPARENT);
                        	visitOptionSpinner.setVisibility(View.GONE);
                            visitDaySpinner.setVisibility(View.GONE);
                            serviceOptionSpinner.setVisibility(View.VISIBLE);
                            serviceOptionSpinner.setSelection(0);
                            break;
                        case 2:     //Visit
                            tvAppointmentType.setVisibility(View.VISIBLE);
                            tvServiceOption.setVisibility(View.GONE);
                            tvVisit.setVisibility(View.VISIBLE);
                            tvVisitDay.setVisibility(View.GONE);
                            tvClinic.setVisibility(View.GONE);
                            tvDay.setVisibility(View.GONE);
                            tvWeek.setVisibility(View.GONE);

                            appointmentTypeSpinner.setBackgroundColor(Color.TRANSPARENT);
                            appointmentTypeSpinner.setVisibility(View.VISIBLE);
                            serviceOptionSpinner.setVisibility(View.GONE);
                            visitOptionSpinner.setVisibility(View.VISIBLE);
                            visitDaySpinner.setVisibility(View.GONE);
                            clinicSpinner.setVisibility(View.GONE);
                            weekSpinner.setVisibility(View.GONE);
                            daySpinner.setVisibility(View.GONE);
                            visitOptionSpinner.setSelection(0);
                            break;
                    }
                    break;
                case R.id.spnr_visit_option:
                    switch(position) {
                        case 0:
                            visitOptionSpinner.setBackgroundColor(spinnerWarning);
                            break;
                        default:
                            visitOptionSpinner.setBackgroundColor(Color.TRANSPARENT);
                            visitDaySpinner.setVisibility(View.VISIBLE);
                            tvVisitDay.setVisibility(View.VISIBLE);
                            visitDaySpinner.setSelection(0);
                            visitOptionSelected = BaseModel.getInstance().
                                    getServiceOptionsHomeList().get(position - 1).getId();
                            loopForServiceOptionDay(visitOptionSelected);

                            HomeVisitAppointmentActivity.visitOptionSelected = visitOptionSelected;
                            break;
                    }
                    break;
                case R.id.spnr_visit_day:
                    switch(position) {
                        case 0:
                            visitDaySpinner.setBackgroundColor(spinnerWarning);
                            break;
                        default:
                            visitDaySpinner.setBackgroundColor(Color.TRANSPARENT);
                            Calendar cal = Calendar.getInstance();
                            cal.add(Calendar.DATE, position - 1);
                            daySelected = cal.getTime();

                            HomeVisitAppointmentActivity.daySelected = daySelected;
                            Intent intent = new Intent(AppointmentTypeSpinnerActivity.this, HomeVisitAppointmentActivity.class);
                            doneChecker(intent);
                            break;
                    }
                    break;
                case R.id.spnr_service_option:
                    switch (position) {
                        case 0:
                            serviceOptionSelected = 0;

                            tvAppointmentType.setVisibility(View.VISIBLE);
                            tvServiceOption.setVisibility(View.VISIBLE);
                            tvVisit.setVisibility(View.GONE);
                            tvVisitDay.setVisibility(View.GONE);
                            tvClinic.setVisibility(View.GONE);
                            tvDay.setVisibility(View.GONE);
                            tvWeek.setVisibility(View.GONE);

                            serviceOptionSpinner.setBackgroundColor(spinnerWarning);
                            appointmentTypeSpinner.setVisibility(View.VISIBLE);
                            serviceOptionSpinner.setVisibility(View.VISIBLE);
                            visitOptionSpinner.setVisibility(View.GONE);
                            visitDaySpinner.setVisibility(View.GONE);
                            clinicSpinner.setVisibility(View.GONE);
                            weekSpinner.setVisibility(View.GONE);
                            daySpinner.setVisibility(View.GONE);
                            break;
                        default:
                            tvClinic.setVisibility(View.VISIBLE);

                            serviceOptionSpinner.setBackgroundColor(Color.TRANSPARENT);
                        	clinicSpinner.setVisibility(View.VISIBLE);
                        	serviceOptionSelected = position;
                        	setClinicSpinner(position);
                        	clinicSpinner.setSelection(0);
                            break;
                    }
                    break;
                case R.id.spnr_clinic:
                    switch (position) {
                        case 0:
                            clinicSelected = 0;

                            tvAppointmentType.setVisibility(View.VISIBLE);
                            tvServiceOption.setVisibility(View.VISIBLE);
                            tvVisit.setVisibility(View.GONE);
                            tvVisitDay.setVisibility(View.GONE);
                            tvClinic.setVisibility(View.VISIBLE);
                            tvDay.setVisibility(View.GONE);
                            tvWeek.setVisibility(View.GONE);

                            clinicSpinner.setBackgroundColor(spinnerWarning);
                            appointmentTypeSpinner.setVisibility(View.VISIBLE);
                            serviceOptionSpinner.setVisibility(View.VISIBLE);
                            visitOptionSpinner.setVisibility(View.GONE);
                            visitDaySpinner.setVisibility(View.GONE);
                            clinicSpinner.setVisibility(View.VISIBLE);
                            daySpinner.setVisibility(View.GONE);
                            weekSpinner.setVisibility(View.GONE);
                            break;
                        default:
                            tvDay.setVisibility(View.GONE);
                            tvWeek.setVisibility(View.GONE);

                        	clinicSpinner.setBackgroundColor(Color.TRANSPARENT);
                        	daySpinner.setVisibility(View.GONE);
                        	daySpinner.setSelection(0);
                        	weekSpinner.setVisibility(View.GONE);
                        	weekSpinner.setSelection(0);

                        	clinicSelected = idList.get(position - 1);
                            List<String> trueDays = BaseModel.getInstance().getClinicMap().get(clinicSelected).getTrueDays();

                            if(trueDays.size() > 1){
                                setDaySpinner(trueDays);
                                tvDay.setVisibility(View.VISIBLE);
                                daySpinner.setVisibility(View.VISIBLE);
                                daySpinner.setSelection(0);
                            } else {
                                try {
                                    tvWeek.setVisibility(View.VISIBLE);
                                    weekSpinner.setVisibility(View.VISIBLE);
                                    weekSpinner.setSelection(0);
                                    dayOfWeek = dfDayShort.parse(trueDays.get(0));
                                    getAppointment(clinicSelected, dayOfWeek);
                                    setWeekSpinner(dayOfWeek);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        	break;
                    }
                    break;
                case R.id.spnr_day:
                    switch (position) {
                        case 0:
                            tvWeek.setVisibility(View.GONE);
                        	weekSpinner.setVisibility(View.GONE);
                        	daySpinner.setBackgroundColor(spinnerWarning);
                            break;
                        default:
                            tvWeek.setVisibility(View.VISIBLE);
                        	daySpinner.setBackgroundColor(Color.TRANSPARENT);
                        	weekSpinner.setVisibility(View.VISIBLE);
                        	weekSpinner.setSelection(0);
							try {
								dayOfWeek = dfDayShort.parse(daySpinner.getSelectedItem().toString());
                                getAppointment(clinicSelected, dayOfWeek);
								setWeekSpinner(dayOfWeek);
							} catch (ParseException e) {
								e.printStackTrace();
							}
                        	break;
                    }
                    break;
                case R.id.spnr_week:
                    switch (position) {
                        case 0:
                            weekSelected = 0;
                            weekSpinner.setBackgroundColor(spinnerWarning);
                            c = Calendar.getInstance();
                            break;
                        default:
                        	weekSelected = position;
                        	weekSpinner.setBackgroundColor(Color.TRANSPARENT);
                            c = Calendar.getInstance();
                    		c.add(Calendar.DAY_OF_YEAR, 7 * position);
                			Log.d("MYLOG", "Plus " + (7 * position) + " days is: " + c.getTime());
                			daySelected = c.getTime();
                			addDayToTime(dayOfWeek);
                            Intent intent = new Intent(AppointmentTypeSpinnerActivity.this, AppointmentCalendarActivity.class);
                            doneChecker(intent);
                        	break;
                    }
                    break;
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    }

    private void loopForServiceOptionDay(int visitOption){
        Calendar c = Calendar.getInstance();
        Date todayDate = c.getTime();
        c.setTime(todayDate);
        c.add(Calendar.DAY_OF_YEAR, 10);
        Date todayPlus10Day = c.getTime();

        while (todayDate.before(todayPlus10Day)) {
            Log.d("MYLOG", "todayDate = " + c.getTime());
            c.setTime(todayDate);
            String date = dfDateOnly.format(c.getTime());
            c.add(Calendar.DAY_OF_YEAR, 1);
            todayDate = c.getTime();

            apptHelp.getAppointmentsHomeVisit(date, visitOption);
        }
    }

    private void getAppointment(int clinicId, Date dayOfWeek){
        Calendar myCal = Calendar.getInstance();
        myCal.setTime(dayOfWeek);
        int dayAsInt = myCal.get(Calendar.DAY_OF_WEEK);
        myCal = Calendar.getInstance();
        myCal.set(Calendar.DAY_OF_WEEK, dayAsInt);
        Date todayDate = myCal.getTime();

        if(!sharedPrefs.getStringSetPrefs(AppointmentTypeSpinnerActivity.this,
                "appts_got").contains(String.valueOf(clinicId))) {
            BaseActivity.apptDone = 0;
            apptHelp.weekDateLooper(todayDate, clinicId);
            sharedPrefs.addToStringSetPrefs(AppointmentTypeSpinnerActivity.this,
                    "appts_got", String.valueOf(clinicId));
        }
    }

    private void doneChecker(final Intent intent){
        pd = new CustomDialogs().showProgressDialog(AppointmentTypeSpinnerActivity.this,
                "Gettting Appointments");
        timer = new CountDownTimer(200, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("bugs", "getDoneCounter() = " + BaseActivity.apptDone);
            }

            @Override
            public void onFinish() {
                if (BaseActivity.apptDone >= 10) {
                    pd.dismiss();
                    changeActivity(intent);
                } else
                    timer.start();
            }
        }.start();
    }

    public void changeActivity(Intent intent){
    	passOptions.setServiceOptionSelected(serviceOptionSelected);
        passOptions.setClinicSelected(clinicSelected);
        passOptions.setWeekSelected(weekSelected);
        passOptions.setDaySelected(daySelected);
        passOptions.setVisitOption(visitOptionSelected);

        startActivity(intent);
    }

    public void addDayToTime(Date dayOfWeek){
        c.setTime(dayOfWeek);
        int dayAsInt = c.get(Calendar.DAY_OF_WEEK);
        c.setTime(daySelected);
        c.set(Calendar.DAY_OF_WEEK, dayAsInt);
        daySelected = c.getTime();
    }
}