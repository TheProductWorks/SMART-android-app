package ie.teamchile.smartapp;

import java.util.ArrayList;

import utility.ServiceUserSingleton;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

  public class ObstreticHistoryActivity extends MenuInheritActivity {
	  private ArrayList<String>items = new ArrayList<String>(); ;
	  private Button enter;
	  private EditText edit;
	  private ListView list;
	  private String obsHistory,item, Obsname, Obsdate;
	  private TextView name, date;
	//private String time;
	  	@Override
	  	protected void onCreate(Bundle savedInstanceState) {
	  		super.onCreate(savedInstanceState);
	  		setContentView(R.layout.list_layout);
	  		enter = (Button)findViewById(R.id.button1);
	  		edit = (EditText)findViewById(R.id.editField);
	  		name = (TextView)findViewById(R.id.obs_name);
	  		date = (TextView)findViewById(R.id.obs_date);
	  		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.activity_obstretic_history,
	  				R.id.textViewItem, items);

	  		list = (ListView)findViewById(R.id.notes);
	  		list.setAdapter(adapter);

	  		Obsname = ServiceUserSingleton.getInstance().getUserName().get(0);
	  		//Obsdate = ServiceUserSingleton.getInstance().getCreatedTime();
	  		//date.setText(Obsdate);
	  		name.setText(Obsname);

	  		obsHistory = ServiceUserSingleton.getInstance().getUserPreviousObstetricHistory().get(0);

	  		//time = ServiceUserSingleton.getInstance().getCreatedTime();
	  		//items.add(time);
	  		 items.add(obsHistory);
	  	     enter.setOnClickListener(new View.OnClickListener() {
	  		   
	  		        @Override
	  		        public void onClick(View v) {
	  		            // TODO Auto-generated method stub
	  		        	item = edit.getText().toString();
	  		        	if (item != null && item.trim().length() > 0) {
	  		        	adapter.notifyDataSetChanged();
	  		            items.add(item);
	  		            edit.setText(null);
	  		        	}
	          }
	  		            //Toast.makeText(null, item, Toast.LENGTH_SHORT).show();//Problem here
	  		        
	  		    });
	  	}
  }