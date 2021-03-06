package ie.teamchile.smartapp.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ie.teamchile.smartapp.activities.BaseActivity;
import ie.teamchile.smartapp.api.SmartApiClient;
import ie.teamchile.smartapp.model.Appointment;
import ie.teamchile.smartapp.model.BaseModel;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by user on 7/14/15.
 */
public class AppointmentHelper extends BaseActivity {

    public AppointmentHelper() {
    }

    public void weekDateLooper(Date todayDate, int clinicId) {
        Log.d("MYLOG", "getAppointmentsForClinic called");
        Log.d("MYLOG", "todayDate = " + dfDateOnly.format(todayDate) + ", clinicId = " + clinicId);
        Calendar c = Calendar.getInstance();
        //Date todayDate = c.getTime();
        c.setTime(todayDate);
        String today = dfDateOnly.format(c.getTime());
        c.add(Calendar.WEEK_OF_YEAR, 10);
        Date todayPlus10Weeks = c.getTime();

        while (todayDate.before(todayPlus10Weeks)) {
            Log.d("MYLOG", "todayDate = " + c.getTime());
            c.setTime(todayDate);
            String date = dfDateOnly.format(c.getTime());
            c.add(Calendar.WEEK_OF_YEAR, 1);
            todayDate = c.getTime();

            getAppointmentsForClinic(date, clinicId);
        }
    }

    public void getAppointmentsForClinic(String date, int clinicId) {
        SmartApiClient.getAuthorizedApiClient().getAppointmentsForDayClinic(
                date,
                clinicId,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Log.d("retro", "getAppointmentsForClinic success");
                        BaseActivity.apptDone++;
                        if (baseModel.getAppointments().size() > 0)
                            addApptsToMaps(baseModel.getAppointments());
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("retro", "getAppointmentsForClinic failure = " + error);
                        BaseActivity.apptDone++;
                    }
                }
        );
    }

    public void getAppointmentsHomeVisit(String date, int serviceOptionId) {
        SmartApiClient.getAuthorizedApiClient().getHomeVisitApptByDateId(
                "home-visit",
                date,
                serviceOptionId,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Log.d("retro", "getAppointmentsForClinic success");
                        BaseActivity.apptDone++;
                        //if (baseModel.getAppointments().size() > 0)
                        addApptsToMaps(baseModel.getAppointments());
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("retro", "getAppointmentsForClinic failure = " + error);
                        BaseActivity.apptDone++;
                    }
                }
        );
    }

    public void addApptsToMaps(List<Appointment> appointments) {
        List<Integer> clinicApptIdList;
        Map<String, List<Integer>> clinicVisitdateApptIdMap;
        Map<Integer, Map<String, List<Integer>>> clinicVisitClinicDateApptIdMap =
                BaseModel.getInstance().getClinicVisitClinicDateApptIdMap();
        Map<Integer, Appointment> clinicVisitIdApptMap =
                BaseModel.getInstance().getClinicVisitIdApptMap();

        List<Integer> homeApptIdList;
        Map<String, List<Integer>> homeVisitdateApptIdMap;
        Map<Integer, Map<String, List<Integer>>> homeVisitClinicDateApptIdMap =
                BaseModel.getInstance().getHomeVisitOptionDateApptIdMap();
        Map<Integer, Appointment> homeVisitIdApptMap =
                BaseModel.getInstance().getHomeVisitIdApptMap();

        for (Appointment appt : appointments) {
            clinicApptIdList = new ArrayList<>();
            homeApptIdList = new ArrayList<>();
            clinicVisitdateApptIdMap = new HashMap<>();
            homeVisitdateApptIdMap = new HashMap<>();
            String apptDate = appt.getDate();
            int apptId = appt.getId();
            int clinicId = appt.getClinicId();
            int serviceOptionId = 0;
            if (appt.getServiceOptionIds().size() > 0)
                serviceOptionId = appt.getServiceOptionIds().get(0);

            if (appt.getPriority().equals("home-visit")) {
                if (homeVisitClinicDateApptIdMap.get(serviceOptionId) != null) {
                    homeVisitdateApptIdMap = homeVisitClinicDateApptIdMap.get(serviceOptionId);
                    if (homeVisitdateApptIdMap.get(apptDate) != null)
                        homeApptIdList = homeVisitdateApptIdMap.get(apptDate);
                }
                homeApptIdList.add(apptId);
                homeVisitdateApptIdMap.put(apptDate, homeApptIdList);

                homeVisitClinicDateApptIdMap.put(serviceOptionId, homeVisitdateApptIdMap);
                homeVisitIdApptMap.put(apptId, appt);
            } else {
                if (clinicVisitClinicDateApptIdMap.get(clinicId) != null) {
                    clinicVisitdateApptIdMap = clinicVisitClinicDateApptIdMap.get(clinicId);
                    if (clinicVisitdateApptIdMap.get(apptDate) != null)
                        clinicApptIdList = clinicVisitdateApptIdMap.get(apptDate);
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
    }
}
