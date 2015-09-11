package ie.teamchile.smartapp.activities;

import android.os.Bundle;
import android.view.WindowManager;

public class TodayAppointmentActivity extends BaseActivity   {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
	}
}