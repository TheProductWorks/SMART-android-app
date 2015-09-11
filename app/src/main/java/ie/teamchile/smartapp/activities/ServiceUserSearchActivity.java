package ie.teamchile.smartapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ie.teamchile.smartapp.R;
import ie.teamchile.smartapp.api.SmartApiClient;
import ie.teamchile.smartapp.model.BaseModel;
import ie.teamchile.smartapp.model.FeedingHistory;
import ie.teamchile.smartapp.model.HearingHistory;
import ie.teamchile.smartapp.model.NbstHistory;
import ie.teamchile.smartapp.model.ServiceUser;
import ie.teamchile.smartapp.model.VitKHistory;
import ie.teamchile.smartapp.util.CustomDialogs;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class ServiceUserSearchActivity extends BaseActivity {
    private BaseAdapter adapterListResults;
    private EditText searchName, searchHospitalNumber,
            searchDOBDay, searchDOBMonth, searchDOBYear;
    private Button search;
    private TextView tvSearchResults;
    private List<ServiceUser> serviceUserList = new ArrayList<>();
    private Intent intent;
    private ListView lvSearchResults;
    private LinearLayout llNoUserFound;
    private Boolean changeActivity;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentForNav(R.layout.activity_service_user_search);

        searchName = (EditText) findViewById(R.id.et_search_name);
        searchHospitalNumber = (EditText) findViewById(R.id.et_search_hospital_number);
        searchDOBDay = (EditText) findViewById(R.id.et_search_dob_day);
        searchDOBMonth = (EditText) findViewById(R.id.et_search_dob_month);
        searchDOBYear = (EditText) findViewById(R.id.et_search_dob_year);
        tvSearchResults = (TextView) findViewById(R.id.tv_search_results);
        llNoUserFound = (LinearLayout) findViewById(R.id.ll_no_user_found);

        search = (Button) findViewById(R.id.btn_search);
        search.setOnClickListener(new ButtonClick());

        lvSearchResults = (ListView) findViewById(R.id.lv_search_results);
        lvSearchResults.setOnItemClickListener(new onItemListener());

        llNoUserFound.setVisibility(View.GONE);
        //tvNoUserFound.setVisibility(View.GONE);
        tvSearchResults.setVisibility(View.GONE);

        createResultList();
    }

    private void createResultList() {
        adapterListResults = new AdapterListResultsInner();
        adapterListResults.notifyDataSetChanged();
        lvSearchResults.setAdapter(adapterListResults);
        //lvSearchResults.setEmptyView(llNoUserFound);
    }

    private void getHistories() {
        getRecentBabyPosition();
        getRecentBabyId();
        getRecentPregnancy();
        SmartApiClient.getAuthorizedApiClient().getVitKHistories(
                bId,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Collections.sort(baseModel.getVitKHistories(), new Comparator<VitKHistory>() {

                            @Override
                            public int compare(VitKHistory a, VitKHistory b) {
                                int valA;
                                int valB;

                                valA = a.getId();
                                valB = b.getId();

                                return -((Integer) valA).compareTo(valB);
                            }
                        });
                        BaseModel.getInstance().setVitKHistories(baseModel.getVitKHistories());
                        Timber.d("vit k history done");
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Timber.d("vit k history failure = " + error);
                    }
                }
        );
        SmartApiClient.getAuthorizedApiClient().getHearingHistories(
                bId,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Collections.sort(baseModel.getHearingHistories(), new Comparator<HearingHistory>() {

                            @Override
                            public int compare(HearingHistory a, HearingHistory b) {
                                int valA;
                                int valB;

                                valA = a.getId();
                                valB = b.getId();

                                return -((Integer) valA).compareTo(valB);
                            }
                        });
                        BaseModel.getInstance().setHearingHistories(baseModel.getHearingHistories());
                        Timber.d("hearing history done");
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Timber.d("hearing history failure = " + error);
                    }
                }
        );
        SmartApiClient.getAuthorizedApiClient().getNbstHistories(
                bId,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Collections.sort(baseModel.getNbstHistories(), new Comparator<NbstHistory>() {

                            @Override
                            public int compare(NbstHistory a, NbstHistory b) {
                                int valA;
                                int valB;

                                valA = a.getId();
                                valB = b.getId();

                                return -((Integer) valA).compareTo(valB);
                            }
                        });
                        BaseModel.getInstance().setNbstHistories(baseModel.getNbstHistories());
                        Timber.d("nbst history done");
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Timber.d("nbst history failure = " + error);
                    }
                }
        );
        SmartApiClient.getAuthorizedApiClient().getFeedingHistoriesByPregId(
                BaseModel.getInstance().getPregnancies().get(p).getId(),
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Collections.sort(baseModel.getFeedingHistories(), new Comparator<FeedingHistory>() {

                            @Override
                            public int compare(FeedingHistory a, FeedingHistory b) {
                                int valA;
                                int valB;

                                valA = a.getId();
                                valB = b.getId();

                                return -((Integer) valA).compareTo(valB);
                            }
                        });
                        BaseModel.getInstance().setFeedingHistories(baseModel.getFeedingHistories());
                        Timber.d("feeding history done");
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Timber.d("feeding history failure = " + error);
                    }
                }
        );
    }

    private void searchForPatient(String name, String hospitalNumber, String dob) {
        if (name.equals("."))
            name = " ";
        else
            name = name.trim();
        pd = new CustomDialogs().showProgressDialog(
                ServiceUserSearchActivity.this,
                "Fetching Information");

        SmartApiClient.getAuthorizedApiClient().getServiceUserByNameDobHospitalNum(
                name,
                hospitalNumber.trim(),
                dob.trim(),
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        if (baseModel.getServiceUsers().size() != 0) {
                            BaseModel.getInstance().setServiceUsers(baseModel.getServiceUsers());
                            BaseModel.getInstance().setPregnancies(baseModel.getPregnancies());
                            BaseModel.getInstance().setBabies(baseModel.getBabies());
                            BaseModel.getInstance().setAntiDHistories(baseModel.getAntiDHistories());

                            if (changeActivity) {
                                startActivity(intent);
                                getHistories();
                            } else {
                                serviceUserList = baseModel.getServiceUsers();
                                adapterListResults.notifyDataSetChanged();
                            }
                        } else {
                            llNoUserFound.setVisibility(View.VISIBLE);
                            adapterListResults.notifyDataSetChanged();
                        }
                        pd.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Timber.d("ServiceUserSearchActivity user search failure = " + error);
                        adapterListResults.notifyDataSetChanged();
                        llNoUserFound.setVisibility(View.VISIBLE);
                        pd.dismiss();
                    }
                }
        );
    }

    private class onItemListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String hospitalNumber = serviceUserList.get(position).getHospitalNumber();

            changeActivity = true;
            searchForPatient("", hospitalNumber, "");
            intent = new Intent(ServiceUserSearchActivity.this, ServiceUserActivity.class);
        }
    }

    private class ButtonClick implements View.OnClickListener {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_search:
                    serviceUserList = new ArrayList<>();
                    changeActivity = false;
                    llNoUserFound.setVisibility(View.GONE);
                    tvSearchResults.setVisibility(View.GONE);

                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                    String dob = "";

                    if (searchName.getText().toString().length() > 0 ||
                            searchHospitalNumber.getText().toString().length() > 0 ||
                            searchDOBDay.getText().toString().length() > 0 &&
                                    searchDOBMonth.getText().toString().length() > 0 &&
                                    searchDOBYear.getText().toString().length() > 0) {
                        tvSearchResults.setVisibility(View.VISIBLE);

                        if (searchDOBDay.getText().toString().length() > 0 &&
                                searchDOBMonth.getText().toString().length() > 0 &&
                                searchDOBYear.getText().toString().length() > 0) {

                            dob = searchDOBYear.getText() + "-" +
                                    searchDOBMonth.getText() + "-" +
                                    searchDOBDay.getText();
                        }

                        searchForPatient(
                                searchName.getText().toString(),
                                searchHospitalNumber.getText().toString(),
                                dob);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Please enter something in the search fields", Toast.LENGTH_LONG).show();
                        serviceUserList.clear();
                        adapterListResults.notifyDataSetChanged();
                    }
                    break;
            }
        }
    }

    public class AdapterListResultsInner extends BaseAdapter {

        @Override
        public int getCount() {
            return serviceUserList.size();
        }

        @Override
        public ServiceUser getItem(int position) {
            return serviceUserList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return serviceUserList.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String name = getItem(position).getPersonalFields().getName();
            String dob = getItem(position).getPersonalFields().getDob();
            String hospitalNumber = getItem(position).getHospitalNumber();

            Context context = ServiceUserSearchActivity.this;
            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.list_layout_search_results, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else
                viewHolder = (ViewHolder) convertView.getTag();

            viewHolder.tvName.setText(name);
            viewHolder.tvDob.setText(dob);
            viewHolder.tvHospitalNumber.setText(hospitalNumber);
            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }
    }

    protected class ViewHolder {
        TextView tvName;
        TextView tvDob;
        TextView tvHospitalNumber;

        public ViewHolder(View view) {
            tvName = (TextView) view.findViewById(R.id.tv_results_name);
            tvDob = (TextView) view.findViewById(R.id.tv_results_dob);
            tvHospitalNumber = (TextView) view.findViewById(R.id.tv_results_hospital_number);
        }
    }
}