package ie.teamchile.smartapp.activities;

import ie.teamchile.smartapp.R;
import ie.teamchile.smartapp.connecttodb.AccessDBTable;
import ie.teamchile.smartapp.connecttodb.PostAppointment;
import ie.teamchile.smartapp.utility.AppointmentSingleton;
import ie.teamchile.smartapp.utility.ClinicSingleton;
import ie.teamchile.smartapp.utility.ServiceUserSingleton;
import ie.teamchile.smartapp.utility.ToastAlert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CreateAppointmentActivity extends MenuInheritActivity {
	private SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
	private SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
	private SimpleDateFormat sdfDateMonthName = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
	private EditText userName;
	private TextView textDate, textClinic;
	private Button confirmAppointment;
	private Spinner visitTimeSpinner, visitDurationSpinner, visitTypeSpinner, visitPrioritySpinner;
	private String name, clinic, apptDate, time, duration, priority, visitType;
	private PostAppointment postAppt = new PostAppointment();
	private AccessDBTable db = new AccessDBTable();
	private String clinicIDStr, clinicName, userID = ""; 
	private int clinicID, appointmentIntervalAsInt;
	private ProgressDialog pd;
	private Calendar c, myCalendar;
	private ArrayList<String> timeList = new ArrayList<String>();
	private ArrayList<String> durationList = new ArrayList<String>();
	private String timeBefore, timeAfter;
	private Date beforeAsDate, afterAsDate, afterAsDateMinusInterval;
	private String appointmentInterval;
	private AppointmentCalendarActivity passOptions = new AppointmentCalendarActivity();
	private SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_appointment);
		
		c = Calendar.getInstance();
		myCalendar = Calendar.getInstance();
		
        userName = (EditText)findViewById(R.id.edit_service_user);
        confirmAppointment = (Button) findViewById(R.id.btn_confirm_appointment);
        confirmAppointment.setOnClickListener(new ButtonClick());
        visitTimeSpinner = (Spinner) findViewById(R.id.visit_time_spinner);
        visitTimeSpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());
        visitDurationSpinner = (Spinner) findViewById(R.id.visit_duration_spinner);
        visitDurationSpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());
        visitPrioritySpinner = (Spinner) findViewById(R.id.visit_priority_spinner);
        visitPrioritySpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());
        visitTypeSpinner = (Spinner) findViewById(R.id.visit_type_spinner);
        visitTypeSpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());
        
        prefs = getSharedPreferences("SMART", MODE_PRIVATE);
        
		if(prefs != null && prefs.getBoolean("reuse", false)) {
			userName.setText(prefs.getString("name", null));
			userID = prefs.getString("id", null);
		}
        
        textDate = (TextView)findViewById(R.id.visit_date_text);
        textClinic = (TextView)findViewById(R.id.visit_clinic_text);
        
        Log.d("postAppointment", "time now: " + c.getTime());

		myCalendar.setTime(AppointmentCalendarActivity.daySelected);
		textDate.setText(sdfDateMonthName.format(AppointmentCalendarActivity.daySelected));

		clinicID = Integer.parseInt(getIntent().getStringExtra("clinicID"));
		clinicName = ClinicSingleton.getInstance().getClinicName(String.valueOf(clinicID));
		textClinic.setText(clinicName);
		
        appointmentInterval = ClinicSingleton.getInstance().getAppointmentInterval(String.valueOf(clinicID));
        timeBefore = getIntent().getStringExtra("timeBefore");
        timeAfter = getIntent().getStringExtra("timeAfter");       
        
        Log.d("postAppointment", "timeBefore: " + timeBefore);
		Log.d("postAppointment", "timeAfter: " + timeAfter);
		
		setTimeSpinner();
		setDurationSpinner();
	}

	private void setDurationSpinner(){
		durationList.add(appointmentInterval + " minutes");
		durationList.add(String.valueOf(appointmentIntervalAsInt + appointmentIntervalAsInt) + " minutes");
		
		visitDurationSpinner = (Spinner) findViewById(R.id.visit_duration_spinner);
	    ArrayAdapter<String> myArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, durationList);
	    myArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    myArrayAdapter.setDropDownViewResource(R.layout.spinner_layout);
	    visitDurationSpinner.setAdapter(myArrayAdapter);
	}
	private void setTimeSpinner(){
        try {
        	appointmentIntervalAsInt = Integer.parseInt(appointmentInterval);
        	beforeAsDate = sdfTime.parse(timeBefore);
			afterAsDate = sdfTime.parse(timeAfter);
			
			c.setTime(afterAsDate);
			c.add(Calendar.MINUTE, - appointmentIntervalAsInt);			
			afterAsDateMinusInterval = c.getTime();
			Log.d("postAppointment", "afterAsDateMinusInterval: " + afterAsDateMinusInterval);
			timeList.add("Select Time");
			
			while(beforeAsDate.before(afterAsDateMinusInterval)){
				Log.d("postAppointment", "beforeAsDate: " + beforeAsDate);
				Log.d("postAppointment", "afterAsDate: " + afterAsDate);
				c.setTime(beforeAsDate);
				c.add(Calendar.MINUTE, appointmentIntervalAsInt);
				beforeAsDate = c.getTime();
				timeList.add(sdfTime.format(c.getTime()));
			}
			
			Log.d("postAppointment", "timeList: " + timeList);
		    visitTimeSpinner = (Spinner) findViewById(R.id.visit_time_spinner);
		    ArrayAdapter<String> myArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, timeList);
		    myArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    myArrayAdapter.setDropDownViewResource(R.layout.spinner_layout);
		    visitTimeSpinner.setAdapter(myArrayAdapter);		    
        } catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private class ButtonClick implements View.OnClickListener {
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btn_confirm_appointment:
            	name = userName.getText().toString();
            	apptDate = sdfDate.format(myCalendar.getTime());
            	passOptions.setDaySelected(myCalendar.getTime());
            	           	
            	Log.d("appointment", "name: " + name + "\nclinic: " +  clinic  + "\nclinic id: " + clinicID + "\nDate: " + apptDate + 
            						 "\nTime: " + time + "\nDuration: " + duration + "\nPriority: " + priority +
            						 "\nVisit Type: " + visitType);
            	
            	if(!name.isEmpty() && visitPrioritySpinner.getSelectedItemPosition() != 0
            					   && visitTypeSpinner.getSelectedItemPosition() != 0) {
            		new LongOperation(CreateAppointmentActivity.this).execute("service_users?name=" + name);
            	}else {
            		ToastAlert ta = new ToastAlert(CreateAppointmentActivity.this, "Cannot proceed, \nSome fields are empty!", true);
            	}
            	       	
            	Log.d("postAppointment", "clinicID: " + clinicID);
            	Log.d("postAppointment", "clinicName: " + ClinicSingleton.getInstance().getClinicName(String.valueOf(clinicID)));
            	break;
            } 
        }
	}
	
    private class LongOperation extends AsyncTask<String, Void, Boolean> {
		private Context context;
		private JSONObject json;
		private Boolean userFound;
		
		public LongOperation(Context context){
			this.context = context;
		}
		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(context);
            pd.setMessage("Creating Appointment");
            pd.show();
            clinicIDStr = String.valueOf(clinicID);
            
		}
		protected Boolean doInBackground(String... params) {
			Log.d("bugs", "userID start: " + userID);
			
			if(!prefs.getBoolean("reuse", false)){
				json = db.accessDB(params[0]);
				ServiceUserSingleton.getInstance().setPatientInfo(json);
				Log.d("bugs", "json: " + json);
				Log.d("bugs", "singleton size: " + ServiceUserSingleton.getInstance().getUserID().size());
				if(ServiceUserSingleton.getInstance().getUserID().size() > 0){
					Log.d("bugs", "user found");
					userID = ServiceUserSingleton.getInstance().getUserID().get(0);
					userFound = true;
				}else {
					Log.d("bugs", "user not found");
					userFound = false;
				}
				postAppt.postAppointment(userID, clinicIDStr, apptDate, time, duration, priority, visitType);
			} else {
				postAppt.postAppointment(userID, clinicIDStr, apptDate, time, duration, priority, visitType);
				userFound = true;
			}
			
			return userFound;
		}
		@Override
		protected void onProgressUpdate(Void... values) {
		}
		@Override
        protected void onPostExecute(Boolean result) {
			if (result) {
				AppointmentSingleton.getInstance().updateLocal(CreateAppointmentActivity.this);
				CountDownTimer timer = new CountDownTimer(2000, 1000) {
					@Override
					public void onTick(long millisUntilFinished) {
					}

					@Override
					public void onFinish() {
						Intent intent = new Intent(CreateAppointmentActivity.this, AppointmentCalendarActivity.class);
						startActivity(intent);
					}
				};
				timer.start();
			}else {
				Toast.makeText(CreateAppointmentActivity.this, "No user found, try again", Toast.LENGTH_LONG).show();
			}
			pd.dismiss();			        		
        }
	}
    private class MySpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getId()) {
                case R.id.visit_duration_spinner:
                	switch(position){
                	case 0: 
                		break;
                	default:
                	duration = durationList.get(position);
                	break;
                	}
                case R.id.visit_type_spinner:
                    switch (position) {
                    case 0:
                    	//Select Visit Type
                    	break;
                    case 1:
                    	//Ante-Natal
                    	visitType = "ante-natal";
                    	break;
                    case 2:
                    	//Post-Natal
                    	visitType = "post-natal";
                    	break;
                    }
                    break;
                case R.id.visit_priority_spinner:
                    switch (position) {
                    case 0:
                    	//Select Visit Priority
                    	break;
                    case 1:
                    	priority = "scheduled";
                    	//Scheduled
                    	break;
                    }
                    break;
                case R.id.visit_time_spinner:
                    time = timeList.get(position);
                    break;
            }
        }

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {			
		}
    }
}