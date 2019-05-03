package com.example.android.musicapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SingleVideoActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private DatabaseReference mDatabaseReference, votesRef, videoRef;
    private VideoView videoView;
    private String video_url, mCurrentUserId;
    private int current_position;
    private long votesCount;
    private String username, video_key;
    private ImageView voteBtn, commentBtn;
    private FirebaseAuth mAuth;
    private TextView mVoteCount;
    private DataSnapshot mSnapshot;
    private String user;
    private static final String TAG = "QuizActivity";
    private List<String> votedUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_video);


        mToolbar = findViewById(R.id.singleVideoToolbar);
        mToolbar.setTitle("Video Details");
        mToolbar.setTitleTextColor(Color.WHITE);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        commentBtn = findViewById(R.id.comment_btn);

        mVoteCount = findViewById(R.id.vote_count);

        video_key = getIntent().getExtras().getString("VIDEO_KEY");


        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CommentActivity.class);
                intent.putExtra("VID_KEY", video_key);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        mAuth = FirebaseAuth.getInstance();

        mCurrentUserId = mAuth.getCurrentUser().getUid();

        votedUsers = new ArrayList<>();
        votedUsers.add("test");



        voteBtn = findViewById(R.id.vote_btn);



        votesRef = FirebaseDatabase.getInstance().getReference().child("Votes");
        votesRef.keepSynced(true);

        voteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                votesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(video_key).hasChild(mCurrentUserId)){
                            votesCount = (int) dataSnapshot.child(video_key).getChildrenCount();
                            Log.d(TAG, "onCreate: now the value is "+ votesCount);
                            videoRef.child(video_key).child("votes").setValue(votesCount);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                showDialogVote();

            }
        });

        videoRef = FirebaseDatabase.getInstance().getReference().child("Videos");
        videoRef.keepSynced(true);

        mDatabaseReference  = FirebaseDatabase.getInstance().getReference().child("Videos");
        mDatabaseReference.keepSynced(true);
        mDatabaseReference.child(video_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                video_url = (String) dataSnapshot.child("Video").getValue();
                String status = (String) dataSnapshot.child("CompetitionStatus").getValue();
                String song_title = (String) dataSnapshot.child("SongTitle").getValue();
                username = (String) dataSnapshot.child("Username").getValue();
                String profile_pic = (String) dataSnapshot.child("Profile_url").getValue();
                long votes = (long) dataSnapshot.child("votes").getValue();
                long comments = (long) dataSnapshot.child("comments").getValue();

                TextView commentCount = findViewById(R.id.comment_count);
                commentCount.setText(Long.toString(comments) +" Comments");
                TextView VotesCount = findViewById(R.id.vote_count);
                VotesCount.setText(Long.toString(votes) +" Votes");
                TextView mUsername = findViewById(R.id.username);
                mUsername.setText(username);
                TextView mSongTitle = findViewById(R.id.song_title);
                mSongTitle.setText(song_title);
                CircleImageView circleImageView = findViewById(R.id.prof_image);
                Picasso.get().load(profile_pic).into(circleImageView);
                if (video_url != null){
                    Uri uri = Uri.parse(video_url);
                    videoView = findViewById(R.id.video_player);
                    videoView.setVideoURI(uri);
                    videoView.start();

                    MediaController mediaController = new MediaController(SingleVideoActivity.this);
                    videoView.setMediaController(mediaController);
                    mediaController.setAnchorView(videoView);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("VIDEO_KEY", video_url);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        savedInstanceState.getString("VIDEO_KEY");
    }

    @Override
    protected void onPause() {
        super.onPause();
        current_position= videoView.getCurrentPosition();
        if(videoView.isPlaying()){
            videoView.pause();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        setVotes();
        setComments();
        if (videoView != null){
            videoView.seekTo(current_position);
            videoView.start();
        }
    }

    public void setVotes(){
        votesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(video_key).hasChild(mCurrentUserId)){
                    votesCount = (int) dataSnapshot.child(video_key).getChildrenCount();
                    videoRef.child(video_key).child("votes").setValue(votesCount);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public  void showDialogVote(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirm Vote");
        builder.setMessage("Are you sure you want to vote for this act?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                validateVoter();
            }

        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // I do not need any action here you might
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
    public void validateVoter(){
        final DatabaseReference voted = FirebaseDatabase.getInstance().getReference().child("Voted").child(mCurrentUserId);
        voted.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Toast toast = Toast.makeText(getApplicationContext(),"You have already voted", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                    View view = toast.getView();
                    view.getBackground().setColorFilter(Color.parseColor("#EF5350"), PorterDuff.Mode.SRC_IN);
                    TextView textView = view.findViewById(android.R.id.message);
                    textView.setTextColor(Color.WHITE);
                    toast.show();
                }else {
                    votesRef.child(video_key).child(mCurrentUserId).setValue(true);
                    voted.child(mCurrentUserId).setValue(true);
                    Toast toast = Toast.makeText(getApplicationContext(), "Voted Successfully!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                    View view = toast.getView();
                    view.getBackground().setColorFilter(Color.parseColor("#FF60AB8B"), PorterDuff.Mode.SRC_IN);
                    TextView textView = view.findViewById(android.R.id.message);
                    textView.setTextColor(Color.WHITE);
                    toast.show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public  void voteMethod(){
//        votesRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
//                    if(snapshot.hasChild(mCurrentUserId)){
//                        Toast toast = Toast.makeText(getApplicationContext(),"You have already voted", Toast.LENGTH_LONG);
//                        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
//                        View view = toast.getView();
//                        view.getBackground().setColorFilter(Color.parseColor("#EF5350"), PorterDuff.Mode.SRC_IN);
//                        TextView textView = view.findViewById(android.R.id.message);
//                        textView.setTextColor(Color.WHITE);
//                        toast.show();
//                        return;
//
//                    }else{
//                        votesRef.child(video_key).child(mCurrentUserId).setValue(true);
//                        votedUsers.add(mCurrentUserId);
//                        Toast toast = Toast.makeText(getApplicationContext(), "Voted Successfully!", Toast.LENGTH_LONG);
//                        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
//                        View view = toast.getView();
//                        view.getBackground().setColorFilter(Color.parseColor("#FF60AB8B"), PorterDuff.Mode.SRC_IN);
//                        TextView textView = view.findViewById(android.R.id.message);
//                        textView.setTextColor(Color.WHITE);
//                        toast.show();
//                    }
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

    public void setComments(){
        DatabaseReference mCommentRef = FirebaseDatabase.getInstance().getReference().child("Comments");
        final DatabaseReference mVideoRef = FirebaseDatabase.getInstance().getReference().child("Videos");
        mCommentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(video_key).hasChild(mCurrentUserId)){
                    int commentsCount = (int) dataSnapshot.child(video_key).getChildrenCount();
                    mVideoRef.child(video_key).child("comments").setValue(commentsCount);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
