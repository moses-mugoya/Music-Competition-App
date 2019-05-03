package com.example.android.musicapp;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CommentActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private FloatingActionButton mFloatingActionButton;
    private String videoKey, mCurrentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference mCommentRef, mVideoRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        mToolbar = findViewById(R.id.commentToolbar);
        mToolbar.setTitle("Comments");
        mToolbar.setTitleTextColor(Color.WHITE);
        mFloatingActionButton = findViewById(R.id.floatingBtn);

        mAuth = FirebaseAuth.getInstance();

        mCurrentUserId = mAuth.getCurrentUser().getUid();


        mVideoRef = FirebaseDatabase.getInstance().getReference().child("Videos");
        mCommentRef = FirebaseDatabase.getInstance().getReference().child("Comments");

        videoKey = getIntent().getExtras().getString("VID_KEY");

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });







    }

    public void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.add_comment, null);
        builder.setView(view);

        final EditText comment_edit = view.findViewById(R.id.comment_edit_text);
        final TextInputLayout comment_input = view.findViewById(R.id.input_comment);
        final Button addCmnt  = view.findViewById(R.id.add_comment_btn);


        builder.setTitle("Add Comment");
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        addCmnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = comment_edit.getText().toString();
                boolean isValid = true;

                if (comment_edit.getText().toString().isEmpty()) {
                    comment_input.setError("Please enter a comment");
                    isValid = false;
                    return;
                } else {
                    comment_input.setErrorEnabled(false);
                }

                mCommentRef.child(videoKey).child(mCurrentUserId).setValue(comment);
                alertDialog.dismiss();



            }
        });


    }

    public void setComments(){
        mCommentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(videoKey).hasChild(mCurrentUserId)){
                    int commentsCount = (int) dataSnapshot.child(videoKey).getChildrenCount();
                    mVideoRef.child(videoKey).child("comments").setValue(commentsCount);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        setComments();
    }
}
