package com.example.android.musicapp;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.VideoView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MusicianDetail extends AppCompatActivity {
    private Toolbar mToolbar;
    private DatabaseReference mDatabaseReference;
    private String musician_key;
    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth;
    private String mCurentUser;
    private Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musician_detail);

        mToolbar = findViewById(R.id.musician_detail_toolbar);
        mToolbar.setTitle("Musician Details");
        mToolbar.setTitleTextColor(Color.WHITE);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        mAuth = FirebaseAuth.getInstance();
        mCurentUser = mAuth.getCurrentUser().getUid();

        musician_key = getIntent().getExtras().getString("MUSICIAN_KEY");

        DatabaseReference videoRef = FirebaseDatabase.getInstance().getReference().child("Videos");
        videoRef.keepSynced(true);

        query = videoRef.orderByChild("userId").equalTo(musician_key);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseReference.keepSynced(true);



        mDatabaseReference.child(musician_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String profile_url = (String) dataSnapshot.child("Image").getValue();
                String username = (String) dataSnapshot.child("username").getValue();
                String phone = (String) dataSnapshot.child("PhoneNumber").getValue();
                String age = (String) dataSnapshot.child("Age").getValue();

                CircleImageView circleImageView = findViewById(R.id.prof_image);
                Picasso.get().load(profile_url).into(circleImageView);
                TextView userName = findViewById(R.id.username);
                userName.setText(username);
                TextView phoneNumber = findViewById(R.id.phone);
                phoneNumber.setText(phone);
                TextView Age = findViewById(R.id.age);
                Age.setText(age+" Yrs");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseRecyclerOptions<Competition> options = new FirebaseRecyclerOptions.Builder<Competition>().setQuery(
                query,
                Competition.class
        ).build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Competition, MusicianDetail.MusicViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MusicianDetail.MusicViewHolder holder, int position, @NonNull Competition model) {
                final String video_key = getRef(position).getKey();
                holder.setUsername(model.getUsername());
                holder.setSongTitle(model.getSongTitle());
                holder.setVideo(model.getVideo());
                holder.setVotes(Long.toString(model.getVotes())+ " Votes");
                holder.setComments(Long.toString(model.getComments())+ " Comments");

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleVideoIntent = new Intent(getApplicationContext(), VideoDetailActivity.class);
                        singleVideoIntent.putExtra("VIDEO_KEY", video_key);
                        singleVideoIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(singleVideoIntent);
                    }
                });
            }

            @NonNull
            @Override
            public MusicianDetail.MusicViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.competition_items, viewGroup, false);
                return new MusicianDetail.MusicViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();

        mRecyclerView.setAdapter(firebaseRecyclerAdapter);


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private class MusicViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUsername(String username){
            TextView userName = mView.findViewById(R.id.username);
            userName.setText(username);
        }

//        public void setProfile_url(String profile_url){
//            CircleImageView circleImageView = mView.findViewById(R.id.prof_image);
//            Picasso.get().load(profile_url).into(circleImageView);
//
//        }

        public void setSongTitle(String song_title){
            TextView songTitle = mView.findViewById(R.id.song_title);
            songTitle.setText(song_title);
        }

        public void setVideo(String video_url){
            VideoView videoView = mView.findViewById(R.id.video);
            if(video_url != null){
                videoView.setVideoURI(Uri.parse(video_url));
            }
            videoView.seekTo(1);

        }
        public void setVotes(String votes_count){
            TextView voteTextView = mView.findViewById(R.id.video_votes);
            voteTextView.setText(votes_count);
        }

        public void setComments(String comment_count){
            TextView commentText = mView.findViewById(R.id.video_comments);
            commentText.setText(comment_count);
        }


    }
}
