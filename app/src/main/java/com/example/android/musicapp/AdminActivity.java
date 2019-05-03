package com.example.android.musicapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText mCompNameEditText;
    private EditText mCompDateEditText;
    private TextInputLayout mInputCompName;
    private TextInputLayout mInputCompDate;
    private Button mCreateCompBtn;

    private DatabaseReference mDatabaseReference;
    private ProgressDialog mProgressDialog;

    private String name, date, compID;
    private ListView mListView;
    private FirebaseListAdapter firebaseListAdapter;
    private Admin admin;
    private Button closeBtn;
    private boolean checkValue = false;
    private CheckBox mCheckBox;

    private static final String TAG = "QuizActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mToolbar = findViewById(R.id.AdminToolbar);
        mToolbar.setTitle("Administration");
        mToolbar.setTitleTextColor(Color.WHITE);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mListView = findViewById(R.id.listview);

        mProgressDialog = new ProgressDialog(this);

        mCompDateEditText = findViewById(R.id.compDate_edit_text);
        mCompNameEditText = findViewById(R.id.compName_edit_text);
        mInputCompName = findViewById(R.id.input_comp_name);
        mInputCompDate = findViewById(R.id.input_comp_date);
        mCreateCompBtn = findViewById(R.id.adminBtn);



        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Competitions");

        FirebaseListOptions<Admin> options = new FirebaseListOptions.Builder<Admin>()
                .setQuery(mDatabaseReference, Admin.class)
                .setLayout(R.layout.admin_items)
                .build();
        firebaseListAdapter = new FirebaseListAdapter<Admin>(options){

            @Override
            protected void populateView(@NonNull View v, @NonNull Admin model, int position) {
                TextView name  = v.findViewById(R.id.compName);
                TextView date = v.findViewById(R.id.compDate);
                TextView status = v.findViewById(R.id.compStatus);
                TextView joinDate = v.findViewById(R.id.compJoinDate);

                name.setText("Title: "+model.getCompetitionName());
                date.setText("End Date: "+model.getCompetitionDate());
                status.setText("Status: "+model.getStatus());
                joinDate.setText("Join End Date:"+model.getCompetitionJoinDate());
                Log.d(TAG, "populateView: "+ model.getCompetitionName());

            }
        };

        mListView.setAdapter(firebaseListAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                admin = (Admin) parent.getAdapter().getItem(position);
                showUpdateDialog(admin.getCompId(),admin.getCompetitionName(), admin.getCompetitionDate(),admin.getCompetitionJoinDate(), admin.getStatus(), admin.getComplete());

            }
        });

        mCreateCompBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCompetition();
            }
        });
    }

    private void showUpdateDialog(final String competitionID, String compName, String compDate,String joinDate, String compStatus, boolean state){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_competition, null);
        builder.setView(view);

        final EditText competitionName = view.findViewById(R.id.name_edit_text);
        final EditText competitionDate = view.findViewById(R.id.date_edit_text);
        final EditText competitionStatus = view.findViewById(R.id.status_edit_text);
        final EditText competitionJoinDte = view.findViewById(R.id.JoinDate_edit_text);
        final Button competitionBtn = view.findViewById(R.id.update_compBtn);
        final TextInputLayout inputName = view.findViewById(R.id.input_compName);
        final TextInputLayout inputDate = view.findViewById(R.id.input_compDate);
        final TextInputLayout inputStatus = view.findViewById(R.id.input_status);
        final TextInputLayout inputJoinDate= view.findViewById(R.id.input_JoincompDate);
        mCheckBox = view.findViewById(R.id.checkbox);

        builder.setTitle("Updating "+ compName + " Competition");
        final AlertDialog alertDialog = builder.create();

        competitionName.setText(compName);
        competitionDate.setText(compDate);
        competitionStatus.setText(compStatus);
        competitionJoinDte.setText(joinDate);
        alertDialog.show();

        competitionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = competitionName.getText().toString().trim();
                String date = competitionDate.getText().toString().trim();
                String joiDate = competitionJoinDte.getText().toString().trim();
                String status = competitionStatus.getText().toString().trim();

                if (mCheckBox.isChecked()){
                    checkValue = true;
                }else {
                    checkValue = false;
                }



                boolean isValid = true;

                if (competitionName.getText().toString().isEmpty()) {
                    inputName.setError("Competition name must not be empty");
                    isValid = false;
                    return;
                } else {
                    inputName.setErrorEnabled(false);
                }

                if (competitionDate.getText().toString().isEmpty()) {
                    inputJoinDate.setError("Date/Time field must not be empty");
                    isValid = false;
                    return;
                } else {
                    inputJoinDate.setErrorEnabled(false);
                }

                if (competitionJoinDte.getText().toString().isEmpty()) {
                    inputDate.setError("Date/Time field must not be empty");
                    isValid = false;
                    return;
                } else {
                    inputDate.setErrorEnabled(false);
                }

                if (competitionStatus.getText().toString().isEmpty()) {
                    inputStatus.setError("Status field must not be empty");
                    isValid = false;
                    return;
                } else {
                    inputStatus.setErrorEnabled(false);
                }

                updateCompetition(competitionID, name, date, joiDate, status, checkValue);
                if(checkValue){
                    updateVideo();
                    deleteValues();
                }
                alertDialog.dismiss();
            }
        });

    }

    private boolean updateCompetition(String cid, String competName, String competDate, String joinDte, String compeStatus, boolean comp){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Competitions").child(cid);
        Admin admin = new Admin(cid, competName, competDate, joinDte, compeStatus, comp);
        databaseReference.setValue(admin);

        Toast toast = Toast.makeText(getApplicationContext(), "Competition updated Successfully!", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
        View view = toast.getView();
        view.getBackground().setColorFilter(Color.parseColor("#FF60AB8B"), PorterDuff.Mode.SRC_IN);
        TextView textView = view.findViewById(android.R.id.message);
        textView.setTextColor(Color.WHITE);
        toast.show();
        return true;
    }

    private void createCompetition(){
         name = mCompNameEditText.getText().toString().trim();
         date = mCompDateEditText.getText().toString().trim();

        boolean isValid = true;

        if (mCompNameEditText.getText().toString().isEmpty()) {
            mInputCompName.setError("Competition name must not be empty");
            isValid = false;
            return;
        } else {
            mInputCompName.setErrorEnabled(false);
        }

        if (mCompDateEditText.getText().toString().isEmpty()) {
            mInputCompDate.setError("Competition end date must not be empty");
            isValid = false;
            return;
        } else {
            mInputCompDate.setErrorEnabled(false);
        }
        mProgressDialog.setMessage("Creating competition...");
        mProgressDialog.show();
        mProgressDialog.setCanceledOnTouchOutside(false);

        String competionID = mDatabaseReference.push().getKey();
        mDatabaseReference.child(competionID).child("compId").setValue(competionID);
        mDatabaseReference.child(competionID).child("competitionName").setValue(name);
        mDatabaseReference.child(competionID).child("competitionDate").setValue(date);
        mDatabaseReference.child(competionID).child("competitionJoinDate").setValue(date);
        mDatabaseReference.child(competionID).child("status").setValue("Ongoing");
        mDatabaseReference.child(competionID).child("complete").setValue(false);

        mProgressDialog.dismiss();
        startActivity(new Intent(AdminActivity.this, MainActivity.class));
    }


    @Override
    protected void onStart() {
        super.onStart();
        firebaseListAdapter.startListening();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateCompetition(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Competitions").orderByChild("status").equalTo("Ongoing").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                                String key = snapshot.getKey();

                                String compId = (String) snapshot.child("compId").getValue();
                                String compDate = (String) snapshot.child("competitionDate").getValue();
                                String compJoinDate = (String) snapshot.child("competitionJoinDate").getValue();
                                String compName = (String) snapshot.child("competitionName").getValue();

                                Log.d(TAG,"The value is"+ key);

                                databaseReference.child("Competitions").child(key).child("compId").setValue(compId);
                                databaseReference.child("Competitions").child(key).child("competitionDate").setValue(compDate);
                                databaseReference.child("Competitions").child(key).child("competitionJoinDate").setValue(compJoinDate);
                                databaseReference.child("Competitions").child(key).child("competitionName").setValue(compName);
                                databaseReference.child("Competitions").child(key).child("status").setValue("finished");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }

    private void updateVideo() {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Videos").orderByChild("CompetitionComplete").equalTo(false).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                                String key = snapshot.getKey();
                                String compName = (String) snapshot.child("CompetitionName").getValue();
                                String compStatus = (String) snapshot.child("CompetitionStatus").getValue();
                                String prof_url = (String) snapshot.child("Profile_url").getValue();
                                String songTitle = (String) snapshot.child("SongTitle").getValue();
                                String username = (String) snapshot.child("Username").getValue();
                                String video = (String) snapshot.child("Video").getValue();

                                databaseReference.child("Videos").child(key).child("CompetitionName").setValue(compName);
                                databaseReference.child("Videos").child(key).child("CompetitionStatus").setValue("finished");
                                databaseReference.child("Videos").child(key).child("Profile_url").setValue(prof_url);
                                databaseReference.child("Videos").child(key).child("SongTitle").setValue(songTitle);
                                databaseReference.child("Videos").child(key).child("Username").setValue(username);
                                databaseReference.child("Videos").child(key).child("Video").setValue(video);
                                databaseReference.child("Videos").child(key).child("CompetitionComplete").setValue(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

    }

    public void deleteValues(){
        DatabaseReference vote = FirebaseDatabase.getInstance().getReference().child("Winners");
        vote.child("votes").removeValue();
        DatabaseReference votePeople = FirebaseDatabase.getInstance().getReference().child("Voted");
        votePeople.removeValue();
    }

}
