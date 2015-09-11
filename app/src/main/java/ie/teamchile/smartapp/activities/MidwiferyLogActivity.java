package ie.teamchile.smartapp.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ie.teamchile.smartapp.R;
import ie.teamchile.smartapp.api.SmartApiClient;
import ie.teamchile.smartapp.model.BaseModel;
import ie.teamchile.smartapp.model.PostingData;
import ie.teamchile.smartapp.model.PregnancyNote;
import ie.teamchile.smartapp.util.CustomDialogs;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MidwiferyLogActivity extends BaseActivity {
    private ListView lvNotes;
    private Button btnAddNote;
    private EditText etNote;
    private String note;
    private int pregnancyId;
    private List<String> dateList = new ArrayList<>();
    private List<String> authorList = new ArrayList<>();
    private List<String> notesList = new ArrayList<>();
    private BaseAdapter adapter;
    private AlertDialog.Builder alertDialog;
    private AlertDialog ad;
    private int size;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentForNav(R.layout.activity_midwifery_log);

        setActionBarTitle(getResources().getString(R.string.midwifery_log));

        pregnancyId = Integer.parseInt(getIntent().getStringExtra("pregnancyId"));
        lvNotes = (ListView) findViewById(R.id.lv_midwifery_log);
        btnAddNote = (Button) findViewById(R.id.btn_add_midwifery_note);
        btnAddNote.setOnClickListener(new Clicky());
        etNote = (EditText) findViewById(R.id.et_midwifery_notes);

        pd = new CustomDialogs().showProgressDialog(
                MidwiferyLogActivity.this,
                "Updating Notes");
        getMidwiferyNotes();
    }

    private void getMidwiferyNotes(){
        SmartApiClient.getAuthorizedApiClient().getPregnancyNotes(
                BaseModel.getInstance().getServiceUsers().get(0).getPregnancyIds().get(p),
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Log.d("retro", "put getMidwiferyNotes retro success");
                        BaseModel.getInstance().setPregnancyNotes(baseModel.getPregnancyNotes());
                        pd.dismiss();
                        setData();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("retro", "put getMidwiferyNotes retro failure = " + error);
                        pd.dismiss();
                    }
                }
        );
    }

    private void setData() {
        dateList = new ArrayList<>();
        authorList = new ArrayList<>();
        notesList = new ArrayList<>();

        size = BaseModel.getInstance().getPregnancyNotes().size();
        Log.d("bugs", "size = " + size);

        Collections.sort(BaseModel.getInstance().getPregnancyNotes(), new Comparator<PregnancyNote>() {

            @Override
            public int compare(PregnancyNote a, PregnancyNote b) {
                int valA;
                int valB;

                valA = a.getId();
                valB = b.getId();

                return ((Integer) valA).compareTo(valB);
            }
        });

        for (int i = size - 1; i >= 0; i--) {
            PregnancyNote theNote = BaseModel.getInstance().getPregnancyNotes().get(i);
            try {
                dateList.add(dfHumanReadableDate.format(dfDateOnly.parse(theNote.getCreatedAt())));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            authorList.add(theNote.getServiceProviderName());
            notesList.add(theNote.getNote());
        }

        Log.d("bugs", "notesList = " + notesList);
        adapter = new MyAdapter(
                this,
                dateList,
                authorList,
                notesList);
        lvNotes.setAdapter(adapter);
    }

    private void postNote(String note) {
        PostingData postNote = new PostingData();

        postNote.postNote(note);

        pd = new CustomDialogs().showProgressDialog(
                this,
                "Adding Note");

        SmartApiClient.getAuthorizedApiClient().postPregnancyNote(
                postNote,
                pregnancyId,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Log.d("retro", "retro success");
                        BaseModel.getInstance().addPregnancyNote(baseModel.getPregnancyNote());

                        Log.d("bugs", "note = " + baseModel.getPregnancyNote().getNote());

                        adapter.notifyDataSetChanged();
                        lvNotes.setAdapter(null);
                        setData();
                        pd.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("retro", "retro failure = " + error);
                        pd.dismiss();
                    }
                }
        );
    }

    private void addNoteDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.dialog_add_note, null);
        final TextView tvCharCount = (TextView) convertView.findViewById(R.id.tv_note_char_count);
        TextView tvDialogTitle = (TextView) convertView.findViewById(R.id.tv_dialog_title);
        final EditText etEnterNote = (EditText) convertView.findViewById(R.id.et_midwifery_notes);
        final int max = 140;
        tvCharCount.setText(String.valueOf(etEnterNote.getText().length()) + "/" + max);
        etEnterNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCharCount.setText(String.valueOf(s.length()) + "/" + max);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > max)
                    etEnterNote.setError("Error character limit exceeded");
            }
        });
        ImageView ivExit = (ImageView) convertView.findViewById(R.id.iv_exit_dialog);
        ivExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.dismiss();
            }
        });
        Button btnSaveNote = (Button) convertView.findViewById(R.id.btn_save_note);
        btnSaveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etEnterNote.getText().toString().equals("")) {
                    etEnterNote.setError("This Cannot Be Empty");
                } else if (etEnterNote.getText().length() > max) {
                    pd = new CustomDialogs().showProgressDialog(
                            MidwiferyLogActivity.this,
                            "Error character limit exceeded");
                    new CountDownTimer(2000, 1000) {

                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            pd.dismiss();
                        }
                    }.start();

                } else {
                    postNote(etEnterNote.getText().toString());
                    ad.dismiss();
                }
            }
        });

        alertDialog = new AlertDialog.Builder(MidwiferyLogActivity.this);
        alertDialog.setView(convertView);
        tvDialogTitle.setText("Add Note Below");
        ad = alertDialog.create();
        ad.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        ad.show();
    }

    private class Clicky implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_add_midwifery_note:
                    addNoteDialog();
                    break;
            }
        }
    }

    private class MyAdapter extends BaseAdapter {
        private Context context;
        private List<String> dateList;
        private List<String> authorList;
        private List<String> notesList;

        public MyAdapter(Context context,
                         List<String> dateList,
                         List<String> authorList,
                         List<String> notesList) {
            this.context = context;
            this.dateList = dateList;
            this.authorList = authorList;
            this.notesList = notesList;
        }

        @Override
        public int getCount() {
            return notesList.size();
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
            LayoutInflater inflater = getLayoutInflater();
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_layout_midwife_note, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else
                holder = (ViewHolder) convertView.getTag();

            holder.tvDate.setText(dateList.get(position));
            holder.tvName.setText(authorList.get(position));
            holder.tvNote.setText(notesList.get(position));
            return convertView;
        }

        private class ViewHolder {
            TextView tvDate;
            TextView tvName;
            TextView tvNote;

            public ViewHolder(View view) {
                tvDate = (TextView) view.findViewById(R.id.tv_note_date);
                tvName = (TextView) view.findViewById(R.id.tv_note_provider_name);
                tvNote = (TextView) view.findViewById(R.id.tv_note);
            }
        }
    }
}
