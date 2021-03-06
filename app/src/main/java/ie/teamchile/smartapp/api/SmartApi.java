package ie.teamchile.smartapp.api;

import ie.teamchile.smartapp.model.BaseModel;
import ie.teamchile.smartapp.model.PostingData;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by user on 5/26/15.
 */
public interface SmartApi {
    @POST("/login")
    void postLogin(
            @Body PostingData login,
            Callback<BaseModel> callback);

    @POST("/logout")
    void postLogout(
            @Body String body,
            Callback<BaseModel> callback);

    @POST("/appointments")
    void postAppointment(
            @Body PostingData appointment,
            Callback<BaseModel> callback);

    @GET("/appointments")
    void getAllAppointments(
            Callback<BaseModel> callback);

    @GET("/appointments")
    void getHomeVisitApptByDateId(
            @Query("priority") String priority,
            @Query("date") String date,
            @Query("service_option_id") int serviceOptionId,
            Callback<BaseModel> callback);

    @GET("/appointments/{appointment_id}")
    void getAppointmentById(
            @Path("appointment_id") int appointmentId,
            Callback<BaseModel> callback);

    @DELETE("/appointments/{appointment_id}")
    void deleteAppointmentById(
            @Path("appointment_id") String appointmentId,
            Callback<BaseModel> callback);

    @GET("/appointments")
    void getAppointmentsForDayClinic(
            @Query("date") String date,
            @Query("clinic_id") int clinicId,
            Callback<BaseModel> callback);

    @PUT("/appointments/{id}")
    void putAppointmentStatus(
            @Body PostingData appointmentStatus,
            @Path("id") int appointmentId,
            Callback<BaseModel> callback);

    @GET("/service_options")
    void getAllServiceOptions(
            Callback<BaseModel> callback);

    @GET("/service_options/{service_option_id}")
    void getServiceOptionById(
            @Path("service_option_id") String serviceOptionId,
            Callback<BaseModel> callback);

    @GET("/clinics")
    void getAllClinics(
            Callback<BaseModel> callback);

    @GET("/clinics/{clinic_id}")
    void getClinicById(
            @Path("clinic_id") String clinicId,
            Callback<BaseModel> callback);

    @GET("/clinics/{clinic_id}/announcements")
    void getAllAnnouncementsForClinic(
            @Path("clinic_id") String clinicId,
            Callback<BaseModel> callback);

    @GET("/clinics/{clinic_id}/announcements/{announcement_id}")
    void getAnnouncementForClinicById(
            @Path("clinic_id") String clinicId,
            @Path("announcement_id") String announcementId,
            Callback<BaseModel> callback);

    @GET("/service_users")
    void getAllServiceUsers(
            Callback<BaseModel> callback);

    @GET("/service_users/{service_user_id}")
    void getServiceUserById(
            @Path("service_user_id") int serviceUserId,
            Callback<BaseModel> callback);

    @GET("/service_users")
    void getServiceUserByName(
            @Query("name") String serviceUserName,
            Callback<BaseModel> callback);

    @GET("/service_users")
    void getServiceUserByNameDobHospitalNum(
            @Query("name") String name,
            @Query("hospital_number") String hospitalNumber,
            @Query("dob") String dob,
            Callback<BaseModel> callback);

    @GET("/service_providers")
    void getAllServiceProviders(
            Callback<BaseModel> callback);

    @GET("/service_providers/{service_provider_id}")
    void getServiceProviderById(
            @Path("service_provider_id") int serviceProviderId,
            Callback<BaseModel> callback);

    @GET("/pregnancies")
    void getAllPregnancies(
            Callback<BaseModel> callback);

    @GET("/pregnancies/{pregnancy_ids}")
    void getPregnancyById(
            @Path("pregnancy_ids") String pregnancyIds,
            Callback<BaseModel> callback);

    @GET("/babies")
    void getAllBabies(
            Callback<BaseModel> callback);

    @GET("/babies/{baby_id}")
    void getBabyById(
            @Path("baby_id") int babyId,
            Callback<BaseModel> callback);

    @GET("/babies/{baby_id}/vit_khistories")
    void getVitKHistories(
            @Path("baby_id") int babyId,
            Callback<BaseModel> callback);

    @PUT("/babies/{baby_id}")
    void putVitK(
            @Body PostingData vitKUpdate,
            @Path("baby_id") int babyId,
            Callback<BaseModel> callback);

    @PUT("/babies/{baby_id}")
    void putHearing(
            @Body PostingData hearingUpdate,
            @Path("baby_id") int babyId,
            Callback<BaseModel> callback);

    @GET("/babies/{baby_id}/hearing_histories")
    void getHearingHistories(
            @Path("baby_id") int babyId,
            Callback<BaseModel> callback);

    @PUT("/babies/{baby_id}")
    void putNBST(
            @Body PostingData nbstUpdate,
            @Path("baby_id") int babyId,
            Callback<BaseModel> callback);

    @GET("/babies/{baby_id}/nbst_histories")
    void getNbstHistories(
            @Path("baby_id") int babyId,
            Callback<BaseModel> callback);

    @PUT("/pregnancies/{pregnancy_id}")
    void putFeeding(
            @Body PostingData feedingUpdate,
            @Path("pregnancy_id") int pregnancyId,
            Callback<BaseModel> callback);

    @GET("/feeding_histories")
    void getFeedingHistoriesByPregId(
            @Query("pregnancy_id") int pregnancyId,
            Callback<BaseModel> callback);

    @GET("/clinics/{clinic_id}/time_records")
    void getTimeRecords(
            @Path("clinic_id") int clinicId,
            @Query("date") String date,
            Callback<BaseModel> callback);

    @POST("/clinics/{clinic_id}/time_records")
    void postTimeRecords(
            @Body PostingData timeRecords,
            @Path("clinic_id") int clinicId,
            Callback<BaseModel> callback);

    @PUT("/clinics/{clinic_id}/time_records/{record_id}")
    void putTimeRecords(
            @Body PostingData timeRecords,
            @Path("clinic_id") int clinicId,
            @Path("record_id") int recordId,
            Callback<BaseModel> callback);

    @DELETE("/clinics/{clinic_id}/time_records/{record_id}")
    void deleteTimeRecordById(
            @Path("clinic_id") int clinicId,
            @Path("record_id") int recordId,
            Callback<BaseModel> callback);

    @PUT("/clinics/{clinic_id}/time_records/{record_id}")
    void resetTimeRecordById(
            @Body PostingData timeRecords,
            @Path("clinic_id") int clinicId,
            @Path("record_id") int recordId,
            Callback<BaseModel> callback);

    @GET("/pregnancies/{pregnancy_id}/notes")
    void getPregnancyNotes(
            @Path("pregnancy_id") int recordId,
            Callback<BaseModel> callback);

    @GET("/pregnancies/{pregnancy_id}/notes/{note_id}")
    void getPregnancyNoteById(
            @Path("pregnancy_id") int pregnancyId,
            @Path("pregnancy_id") int noteId,
            Callback<BaseModel> callback);

    @POST("/pregnancies/{pregnancy_id}/notes")
    void postPregnancyNote(
            @Body PostingData pregnancyNote,
            @Path("pregnancy_id") int pregnancyId,
            Callback<BaseModel> callback);

    @GET("/anti_dhistories")
    void getAntiDHistories(
            Callback<BaseModel> callback);

    @GET("/anti_dhistories/{history_id}")
    void getAntiDHistoriesById(
            @Path("history_id") int historyId,
            Callback<BaseModel> callback);

    @GET("/anti_dhistories")
    void getAntiDHistoriesForPregnacy(
            @Query("pregnancy_id") int pregnancyId,
            Callback<BaseModel> callback);

    @PUT("/pregnancies/{pregnancy_id}")
    void putAnitD(
            @Body PostingData antiDUpdate,
            @Path("pregnancy_id") int pregnancyId,
            Callback<BaseModel> callback);

    @GET("/service_user_actions")
    void getServiceUserActions(
            Callback<BaseModel> callback);

    @GET("/pregnancies/{pregnancy_id}/actions")
    void getPregnancyActions(
            @Path("pregnancy_id") int pregnancyId,
            Callback<BaseModel> callback);

    @POST("/pregnancies/{pregnancy_id}/actions")
    void postPregnancyAction(
            @Body PostingData postPregnancyAction,
            @Path("pregnancy_id") int pregnancyId,
            Callback<BaseModel> callback);

    @PUT("/pregnancies/{pregnancy_id}/actions/{action_id}")
    void putPregnancyAction(
            @Body PostingData postPregnancyAction,
            @Path("pregnancy_id") int pregnancyId,
            @Path("action_id") int actionId,
            Callback<BaseModel> callback);
}
