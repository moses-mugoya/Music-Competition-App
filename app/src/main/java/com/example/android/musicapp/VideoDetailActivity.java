package com.example.android.musicapp;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class VideoDetailActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private DatabaseReference mDatabaseReference;
    private VideoView videoView;
    private String video_url;
    private int current_position;
    private String username;
    private ImageView voteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);

        mToolbar = findViewById(R.id.singleVideoToolbar);
        mToolbar.setTitle("Video Details");
        mToolbar.setTitleTextColor(Color.WHITE);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String video_key = getIntent().getExtras().getString("VIDEO_KEY");

        voteBtn = findViewById(R.id.vote_btn);

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

                    MediaController mediaController = new MediaController(VideoDetailActivity.this);
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
        if (videoView != null){
            videoView.seekTo(current_position);
            videoView.start();
        }
    }
}
