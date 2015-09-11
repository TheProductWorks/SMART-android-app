package ie.teamchile.smartapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ie.teamchile.smartapp.R;
import ie.teamchile.smartapp.model.BaseModel;
import ie.teamchile.smartapp.model.Baby;
import ie.teamchile.smartapp.util.DividerItemDecoration;

public class ParityDetailsActivity extends BaseActivity {
	private BaseAdapter adapter;
	private String patientName, patientParity;
	private String sex_male = "ale", sex_female = "emale";
	private List<String> nameBaby = new ArrayList<>();
	private List<String> hospitalNumber = new ArrayList<>();
	private List<String> dobBaby = new ArrayList<>();
	private List<String> genderBaby = new ArrayList<>();
	private List<String> gestationBaby = new ArrayList<>();
	private List<Integer> weightBaby = new ArrayList<>();
	private List<String> weightBabyInKg = new ArrayList<>();
	private List<String> birthMode = new ArrayList<>();
	private List<String> birthOutcome = new ArrayList<>();
	private List<String> dobStr = new ArrayList<>();
	private List<String> hearing = new ArrayList<>();
	private List<String> nbstList = new ArrayList<>();
	private List<String> vitKList = new ArrayList<>();
	private ListView lvParity;
	private int orientation;
	private RecyclerView rvParity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
		setContentForNav(R.layout.activity_parity_details);
		/*lvParity = (ListView)findViewById(R.id.lv_parity);*/
		rvParity = (RecyclerView) findViewById(R.id.rv_parity);

		patientName = BaseModel.getInstance().getServiceUsers().get(0).getPersonalFields().getName();
		patientParity = BaseModel.getInstance().getServiceUsers().get(0).getClinicalFields().getParity();
		setActionBarTitle(patientName + " (" + patientParity + ")");

		List<Baby> babyList = BaseModel.getInstance().getBabies();

		for(int i = 0; i < babyList.size(); i++){
			nameBaby.add(babyList.get(i).getName());
			hospitalNumber.add(babyList.get(i).getHospitalNumber());
			dobBaby.add(babyList.get(i).getDeliveryDateTime());
			genderBaby.add(babyList.get(i).getGender());
			gestationBaby.add(BaseModel.getInstance().getPregnancies().get(i).getGestation());
			weightBaby.add(babyList.get(i).getWeight());
			birthMode.add(putArrayToString(BaseModel.getInstance().getPregnancies().get(i).getBirthMode()));
			birthOutcome.add(babyList.get(i).getBirthOutcome());
			hearing.add(babyList.get(i).getHearing());
			nbstList.add(babyList.get(i).getNbst());
			vitKList.add(babyList.get(i).getVitK());
		}

        dobStr = new ArrayList<>();

		for(int i = 0; i < dobBaby.size(); i++){
			dobStr.add(dobBaby.get(i) + "\n");
		}
		  
		for(int i = 0; i < weightBaby.size(); i ++){
			String kg = getGramsToKg(weightBaby.get(i));
			weightBabyInKg.add(kg);
		}

		switch(getScreenOrientation()){
		case 1:				//ORIENTATION_PORTRAIT
			//portraitCode();
            setUpNewLayout();
			break;
		case 2:				//ORIENTATION_LANDSCAPE
			landscapeCode();
			break;
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    setIntent(intent);
	}

	private int getScreenOrientation() {
	    Display getOrient = this.getWindowManager().getDefaultDisplay();
	    int orientation = Configuration.ORIENTATION_UNDEFINED;
	    if(getOrient.getWidth()==getOrient.getHeight()){
	        orientation = Configuration.ORIENTATION_SQUARE;
	    } else{ 
	        if(getOrient.getWidth() < getOrient.getHeight()){
	            orientation = Configuration.ORIENTATION_PORTRAIT;
	        }else { 
	             orientation = Configuration.ORIENTATION_LANDSCAPE;
	        }
	    }
	    this.orientation = orientation;
	    return orientation;
	}
	
	private void portraitCode(){
		adapter = new ListAdapter(ParityDetailsActivity.this, 
				 orientation, nameBaby, hospitalNumber, dobStr, 
				 genderBaby, gestationBaby, weightBabyInKg,
				 birthMode, birthOutcome);
		lvParity.setAdapter(adapter);
	}
	
	private void landscapeCode(){
		LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		rvParity.setLayoutManager(layoutManager);
        rvParity.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
		RecyclerView.Adapter rvAdapter = new MyRecycleAdapter(nameBaby, hospitalNumber, dobBaby, genderBaby, gestationBaby,
				weightBaby, birthMode, birthOutcome, hearing, nbstList, vitKList);
		rvParity.setAdapter(rvAdapter);
	}
	
    public String getGramsToKg(int grams){
    	double kg;
    	kg = grams/1000.0;
    	return String.valueOf(kg);
    }
	
	private class ListAdapter extends BaseAdapter {
		Context context;
		int position, orientation;
		LayoutInflater layoutInflater;
		List<String> babyDob, hospitalNumber, babyGender, gestation,
				 	 weight, name, birthMode, birthOutcome; 	

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
		}

        public ListAdapter(Context context, int orientation, List<String> name,
				List<String>hospitalNumber, List<String>babyDob, List<String>babyGender, 
				List<String>gestation, List<String>weight, List<String>birthMode, List<String>birthOutcome) {
			super();
			
			this.context = context;
			this.orientation = orientation;
			this.babyDob = babyDob;
			this.hospitalNumber = hospitalNumber;
			this.name = name;
			this.babyGender = babyGender;
			this.gestation = gestation;
			this.weight = weight;
			this.birthMode = birthMode;
			this.birthOutcome = birthOutcome;
			layoutInflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			return gestation.size();
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
		public View getView(int position, View convertView, ViewGroup parent) {
			//ButterKnife.inject(this, convertView);
			this.position = position;
			
			convertView = layoutInflater.inflate(R.layout.list_layout_parity, parent, false);
			TextView dobText = (TextView) convertView.findViewById(R.id.tv_parity_dob);
			TextView genderText = (TextView) convertView.findViewById(R.id.tv_parity_gender);
			TextView gestationText = (TextView) convertView.findViewById(R.id.tv_parity_gestation);
			TextView weightText = (TextView) convertView.findViewById(R.id.tv_parity_weight);
			TextView modeText = (TextView) convertView.findViewById(R.id.tv_parity_birth_mode);
			TextView birthOutcomeParity = (TextView) convertView.findViewById(R.id.tv_parity_outcome);

			if(babyGender.get(position).equalsIgnoreCase("M")){
				genderText.setText(babyGender.get(position) + sex_male);
			}
			else if (babyGender.get(position).equalsIgnoreCase("F")){
				genderText.setText(babyGender.get(position) + sex_female);
			}	
			gestationText.setText(gestation.get(position));			
			weightText.setText(formatWeight(weight.get(position)));
			modeText.setText(formatArrayString(birthMode.get(position)));
			birthOutcomeParity.setText(birthOutcome.get(position));
			
			switch(orientation){
			case 1:				//ORIENTATION_PORTRAIT
				TextView nameOfBaby = (TextView) convertView.findViewById(R.id.tv_parity_name);
				TextView hospitalNo = (TextView) convertView.findViewById(R.id.tv_parity_hospital_number);
				
				nameOfBaby.setText(name.get(position));
				hospitalNo.setText(hospitalNumber.get(position));
				dobText.setText(getDeliveryDate(babyDob.get(position), 0));
				break;
			case 2:				//ORIENTATION_LANDSCAPE
				dobText.setText(getDeliveryDate(babyDob.get(position), 1));
				break;
			}			
			
			return convertView;			
		}
	}

	private class MyRecycleAdapter extends RecyclerView.Adapter<MyRecycleAdapter.ViewHolder>{
		private List<String> nameBaby, hospitalNumber, dobBaby, genderBaby, gestationBaby,
				birthMode, birthOutcome, hearing, nbst, vitK;
		private List<Integer> weightBaby;

		public MyRecycleAdapter(List<String> nameBaby,
								List<String> hospitalNumber,
								List<String> dobBaby,
								List<String> genderBaby,
								List<String> gestationBaby,
								List<Integer> weightBaby,
								List<String> birthMode,
								List<String> birthOutcome,
								List<String> hearing,
								List<String> nbst,
								List<String> vitK){
			this.nameBaby = nameBaby;
			this.hospitalNumber = hospitalNumber;
			this.dobBaby = dobBaby;
			this.genderBaby = genderBaby;
			this.gestationBaby = gestationBaby;
			this.weightBaby = weightBaby;
			this.birthMode = birthMode;
			this.birthOutcome = birthOutcome;
			this.hearing = hearing;
			this.nbst = nbst;
			this.vitK = vitK;
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			private TextView tvDob, tvGest, tvGender, tvWeight, tvMode, tvOutcome,
						tvHearing, tvNbst, tvVitK, tvName, tvHospitalNum;
			public ViewHolder(View view) {
				super(view);

				tvDob = (TextView) view.findViewById(R.id.tv_parity_dob);
				tvGest = (TextView) view.findViewById(R.id.tv_parity_gestation);
				tvGender = (TextView) view.findViewById(R.id.tv_parity_gender);
				tvWeight = (TextView) view.findViewById(R.id.tv_parity_weight);
				tvMode = (TextView) view.findViewById(R.id.tv_parity_birth_mode);
				tvOutcome = (TextView) view.findViewById(R.id.tv_parity_outcome);
				tvName = (TextView) view.findViewById(R.id.tv_parity_name);
				tvHospitalNum = (TextView) view.findViewById(R.id.tv_parity_hospital_number);
				tvHearing = (TextView) view.findViewById(R.id.tv_parity_hearing);
				tvNbst = (TextView) view.findViewById(R.id.tv_parity_nbst);
				tvVitK = (TextView) view.findViewById(R.id.tv_parity_vit_k);
			}
		}

		@Override
		public MyRecycleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout_parity, parent, false);
			ViewHolder myViewHolder = new ViewHolder(view);

            //RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_parity);

			return myViewHolder;
		}

		@Override
		public void onBindViewHolder(MyRecycleAdapter.ViewHolder holder, int position) {
			TextView tvDob = holder.tvDob;
			TextView tvGest = holder.tvGest;
			TextView tvGender = holder.tvGender;
			TextView tvWeight = holder.tvWeight;
			TextView tvMode = holder.tvMode;
			TextView tvOutcome = holder.tvOutcome;
			TextView tvName = holder.tvName;
			TextView tvHospitalNum = holder.tvHospitalNum;
			TextView tvHearing = holder.tvHearing;
			TextView tvNbst = holder.tvNbst;
			TextView tvVitK = holder.tvVitK;

			tvDob.setText(getDeliveryDate(dobBaby.get(position), 0));
			tvGest.setText(gestationBaby.get(position));
			tvGender.setText(genderBaby.get(position));
			tvWeight.setText(String.valueOf(weightBaby.get(position)));
			tvMode.setText(birthMode.get(position));
			tvOutcome.setText(birthOutcome.get(position));
			tvName.setText(nameBaby.get(position));
			tvHospitalNum.setText(hospitalNumber.get(position));
			tvHearing.setText(hearing.get(position));
			tvNbst.setText(nbst.get(position));
			tvVitK.setText(vitK.get(position));
		}

		@Override
		public int getItemCount() {
			return hospitalNumber.size();
		}
	}
	
	private String getDeliveryDate(String edd, int option) {
		Date date = null;
		String dateOfDevelivery = null;
		try {
			date = dfDateTimeWZone.parse(edd);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		switch (option) {
		case 0:
			    dateOfDevelivery = dfMonthFullName.format(date);
			break;
		case 1:
			    dateOfDevelivery = dfDateOnly.format(date);
			break;
            case 2:
                dateOfDevelivery = dfDateMonthNameYear.format(date);
            break;
		}
		return dateOfDevelivery;
	}
	
    private String formatArrayString(String toBeFormatted){
    	String formatedString = toBeFormatted
    		    .replace(",", "")  //remove the commas
    		    .replace("[", "")  //remove the right bracket
    		    .replace("]", "")  //remove the left bracket
    		    .replace("\"", "")
    		    .trim(); 
    	return formatedString;
    }
    
    private String formatWeight(String weightStr){
    	DecimalFormat df = new DecimalFormat("#.00"); 
    	double weight = Double.parseDouble(weightStr);
    	
    	return df.format(weight);
    }

    private class NewListAdapter extends BaseAdapter{
        Context context;
        int layoutResource;
        List<String> nameList;
        List<String> list1;
        List<String> list2;

        public NewListAdapter(Context context,
                              int layoutResource,
                              List<String> nameList,
                              List<String> list1,
                              List<String> list2){
            super();
            this.context = context;
            this.layoutResource = layoutResource;
            this.nameList = nameList;
            this.list1 = list1;
            this.list2 = list2;

        }

        @Override
        public int getCount() {
            return nameList.size();
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
            convertView = View.inflate(context, layoutResource, null);
            TextView tvName = (TextView) convertView.findViewById(R.id.tv_parity_name);
            TextView tvList1 = (TextView) convertView.findViewById(R.id.tv_parity_empty_1);
            TextView tvList2 = (TextView) convertView.findViewById(R.id.tv_parity_empty_2);

            tvName.setText(nameList.get(position));
            tvList1.setText(list1.get(position));
            tvList1.setText(list1.get(position));
            tvList2.setText(list2.get(position));
            return convertView;
        }
    }

    private void setUpNewLayout(){
        ListView lvNew;
        ListView lvNew2;
        ListView lvNew3;
        ListView lvNew4;

        BaseAdapter newAdapter;

        lvNew = (ListView) findViewById(R.id.lv_parity_new);
        lvNew2 = (ListView) findViewById(R.id.lv_parity_new_2);
        lvNew3 = (ListView) findViewById(R.id.lv_parity_new_3);
        lvNew4 = (ListView) findViewById(R.id.lv_parity_new_4);
        dobStr = new ArrayList<>();

        for (int i = 0; i < dobBaby.size(); i ++) {
            dobStr.add(getDeliveryDate(dobBaby.get(i), 2));
        }

        for (int i = 0; i < nameBaby.size(); i ++) {
            nameBaby.set(i, nameBaby.get(i) + " (" + genderBaby.get(i) + ")");
        }

        newAdapter = new NewListAdapter(this, R.layout.list_layout_parity, nameBaby, hospitalNumber, dobStr);
        lvNew.setAdapter(newAdapter);
        newAdapter = new NewListAdapter(this, R.layout.list_layout_parity, nameBaby, gestationBaby, weightBabyInKg);
        lvNew2.setAdapter(newAdapter);
        newAdapter = new NewListAdapter(this, R.layout.list_layout_parity, nameBaby, birthMode, birthOutcome);
        lvNew3.setAdapter(newAdapter);
        newAdapter = new NewListAdapter(this, R.layout.list_layout_parity, nameBaby, hearing, nbstList);
        lvNew4.setAdapter(newAdapter);
    }
}
