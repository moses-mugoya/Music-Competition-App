package com.example.android.musicapp;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.VideoView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class WinnerActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private Query query;
    private DatabaseReference winref;
    private RecyclerView mRecyclerView;
    private static final String TAG = "QuizActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        winningsVote();
        setContentView(R.layout.activity_winner);

        mToolbar = findViewById(R.id.winnerToolbar);
        mToolbar.setTitle("Winner");
        mToolbar.setTitleTextColor(Color.WHITE);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));



    }

    private class WinnerViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public WinnerViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUsername(String username){
            TextView userName = mView.findViewById(R.id.username);
            userName.setText(username);
        }

        public void setProfile_url(String profile_url){
            CircleImageView circleImageView = mView.findViewById(R.id.prof_image);
            Picasso.get().load(profile_url).into(circleImageView);

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

    }

    public void winningsVote(){
        winref = FirebaseDatabase.getInstance().getReference().child("Winners").child("votes");
        winref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long winningVote;
                if(dataSnapshot.exists()){
                    winningVote = (long)dataSnapshot.getValue();
                    Log.d(TAG, "onDataChange: winner votes "+ winningVote);
                    setRecycler(winningVote);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void setRecycler(long win){

        query = FirebaseDatabase.getInstance().getReference().child("Videos").orderByChild("votes").equalTo(win);

        FirebaseRecyclerOptions<Competition> options = new FirebaseRecyclerOptions.Builder<Competition>().setQuery(
                query,
                Competition.class
        ).build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Competition, WinnerActivity.WinnerViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull WinnerActivity.WinnerViewHolder holder, int position, @NonNull Competition model) {
                final String video_key = getRef(position).getKey();
                holder.setUsername(model.getUsername());
                holder.setVideo(model.getVideo());
                holder.setProfile_url(model.getProfile_url());
                holder.setVotes(Long.toString(model.getVotes())+ " Votes");

            }

            @NonNull
            @Override
            public WinnerActivity.WinnerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.winners, viewGroup, false);
                return new WinnerActivity.WinnerViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();

        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }
}
