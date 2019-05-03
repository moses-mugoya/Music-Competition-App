package com.example.android.musicapp;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class CompetitionFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabaseReference, databaseReference;
    private int voteCount;
    Context context = getContext();

    private static final String TAG = "QuizActivity";


    public CompetitionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_competition, container, false);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Videos");
        mDatabaseReference.keepSynced(true);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Votes");
        databaseReference.keepSynced(true);

        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));



        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Competition> options = new FirebaseRecyclerOptions.Builder<Competition>().setQuery(
                mDatabaseReference,
                Competition.class
        ).build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Competition, CompetitionFragment.CompetitionViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CompetitionFragment.CompetitionViewHolder holder, int position, @NonNull Competition model) {
                final String video_key = getRef(position).getKey();
                getVideoVotes(video_key);
                        holder.setUsername(model.getUsername());
                        holder.setSongTitle(model.getSongTitle());
                        holder.setVideo(model.getVideo(), getContext());
                        holder.setVotes(Long.toString(model.getVotes())+ " Votes");
                        holder.setComments(Long.toString(model.getComments())+ " Comments");

                Log.d(TAG, "The votes are "+ voteCount);


                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent singleVideoIntent = new Intent(getContext(), VideoDetailActivity.class);
                                singleVideoIntent.putExtra("VIDEO_KEY", video_key);
                                singleVideoIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(singleVideoIntent);
                            }
                        });






            }

            @NonNull
            @Override
            public CompetitionFragment.CompetitionViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.competition_items, viewGroup, false);
                return new CompetitionFragment.CompetitionViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();

        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    private static class CompetitionViewHolder extends RecyclerView.ViewHolder{
        View mView;


        public CompetitionViewHolder(@NonNull View itemView) {
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

        public void setVideo(String video_url, Context context){
            VideoView videoView = mView.findViewById(R.id.video);
            cachingUrl(video_url, context);
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

        public String cachingUrl(String urlPath, Context context) {

            return MusicApp.getProxy(context).getProxyUrl(urlPath, true);

        }

    }

    public void getVideoVotes(final String key){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                voteCount = (int) dataSnapshot.child(key).getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
