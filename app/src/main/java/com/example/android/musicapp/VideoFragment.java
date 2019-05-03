package com.example.android.musicapp;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment {
    private TextView mTextView;
    private DatabaseReference mDatabaseReference, winnerRef;
    private TextView  mInfoTextView,mDateTextView, mJoinDateTextView;
    private Button mJoinButton;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FirebaseAuth mAuth;
    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabaseReference2;
    private TextView songTitle, userName;
    private VideoView videoView;
    private Query query;
    private String title, current_userID;
    private ArrayList<Long> arrayList = new ArrayList<>();
    private long winnerVote;

    private static final String TAG = "QuizActivity";

    public VideoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_video, container, false);

        mDatabaseReference2 = FirebaseDatabase.getInstance().getReference().child("Videos");
        query = mDatabaseReference2.orderByChild("CompetitionStatus").equalTo("Ongoing");
        mDatabaseReference2.keepSynced(true);

        winnerRef = FirebaseDatabase.getInstance().getReference();

        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mTextView = rootView.findViewById(R.id.hello);
        mDateTextView = rootView.findViewById(R.id.date);
        mInfoTextView = rootView.findViewById(R.id.infoText);
        mJoinButton = rootView.findViewById(R.id.join);
        mJoinDateTextView = rootView.findViewById(R.id.Joindate);

        refreshLayout();


        mAuth = FirebaseAuth.getInstance();

        current_userID = mAuth.getCurrentUser().getUid();


        return rootView;
    }



    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Competition> options = new FirebaseRecyclerOptions.Builder<Competition>().setQuery(
                query,
                Competition.class
        ).build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Competition, VideoFragment.CompetitionViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull VideoFragment.CompetitionViewHolder holder, int position, @NonNull Competition model) {
                final String video_key = getRef(position).getKey();
                    holder.setUsername(model.getUsername());
                    holder.setSongTitle(model.getSongTitle());
                    holder.setVideo(model.getVideo());
                    holder.setVotes(Long.toString(model.getVotes())+ " Votes");
                    holder.setComments(Long.toString(model.getComments())+ " Comments");

                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent singleVideoIntent = new Intent(getContext(), SingleVideoActivity.class);
                            singleVideoIntent.putExtra("VIDEO_KEY", video_key);
                            singleVideoIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(singleVideoIntent);
                        }
                    });
            }

            @NonNull
            @Override
            public VideoFragment.CompetitionViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.competition_items, viewGroup, false);
                return new VideoFragment.CompetitionViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();

        mRecyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    private class CompetitionViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public CompetitionViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUsername(String username){
            userName = mView.findViewById(R.id.username);
            userName.setText(username);
        }

//        public void setProfile_url(String profile_url){
//            CircleImageView circleImageView = mView.findViewById(R.id.prof_image);
//            Picasso.get().load(profile_url).into(circleImageView);
//
//        }

        public void setSongTitle(String song_title){
            songTitle = mView.findViewById(R.id.song_title);
            songTitle.setText(song_title);
        }

        public void setVideo(String video_url){
            videoView = mView.findViewById(R.id.video);
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

    private void updateVideo() {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);
        databaseReference.child("Videos").orderByChild("CompetitionStatus").equalTo("Ongoing").addListenerForSingleValueEvent(
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
                                databaseReference.child("Videos").child(key).child("CompetitionComplete").setValue(false);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

    }

    private void updateCompetition(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);
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

    public  void refreshLayout(){
        Query newDataBase = FirebaseDatabase.getInstance().getReference().child("Competitions").orderByChild("complete").equalTo(false);
        newDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date strDate = null;
                Date strJoinDate = null;
                if (dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        title = (String) snapshot.child("competitionName").getValue();
                        String date = (String) snapshot.child("competitionDate").getValue();
                        String joinDate = (String) snapshot.child("competitionJoinDate").getValue();
                        mTextView.setText(title);
                        mJoinDateTextView.setText("Joining ends on: "+ joinDate);
                        mDateTextView.setText("Competition ends on: "+ date);
                        mInfoTextView.setText("Competition is on...");
                        mJoinButton.setText("Join Here");
                        mJoinButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                joinCompetition();
                            }
                        });
                        mDateTextView.setVisibility(View.VISIBLE);
                        mJoinButton.setVisibility(View.VISIBLE);
                        mJoinDateTextView.setVisibility(View.VISIBLE);

                        try {
                            strDate = sdf.parse(date);
                            strJoinDate = sdf.parse(joinDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if(strJoinDate != null){
                            if (System.currentTimeMillis() > strJoinDate.getTime()) {
                                mJoinButton.setVisibility(View.GONE);
                                mJoinDateTextView.setVisibility(View.GONE);
                                mInfoTextView.setText("You can now vote for your favourite act");
                                mDateTextView.setText("Voting ends on: "+ date);
                            }
                        }

                        if (strDate != null){
                            if (System.currentTimeMillis() > strDate.getTime()) {
                                retrieveVotes();
                                mTextView.setText(title);
                                mInfoTextView.setText("Competition is finished");
                                mDateTextView.setVisibility(View.GONE);
                                mJoinButton.setVisibility(View.VISIBLE);
                                mJoinButton.setText("See Winner");
                                mJoinButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent newIntent = new Intent(getContext(), WinnerActivity.class);
                                        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(newIntent);
                                    }
                                });
                                updateCompetition();
                                updateVideo();

                            }else {
                                Log.d(TAG,"No match");
                            }
                        }


                    }
                }else {
                    mTextView.setVisibility(View.GONE);
                    mInfoTextView.setText("There is no active competition at the moment");
                    mDateTextView.setVisibility(View.GONE);
                    mJoinButton.setVisibility(View.GONE);
                    mJoinDateTextView.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }

    public void retrieveVotes(){
        Query query = FirebaseDatabase.getInstance().getReference().child("Videos").orderByChild("CompetitionComplete").equalTo(false);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        long votes = (long) snapshot.child("votes").getValue();
                        arrayList.add(votes);
                    }
                    winnerVote = Collections.max(arrayList);
                    winnerRef.child("Winners").child("votes").setValue(winnerVote);
                    Log.d(TAG,"The array contains"+ winnerVote);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    public void joinCompetition(){
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_userID).child("UserType");
        mDatabaseReference.keepSynced(true);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String current_userType = (String) dataSnapshot.getValue();
                if(current_userType.equals("Musician")){
                    Intent joinIntent = new Intent(getContext(), JoinCompetitionActivity.class);
                    joinIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(joinIntent);
                    Log.d(TAG, "onDataChange: user is being executed");
                }
                else{
                    Toast toast = Toast.makeText(getContext(),"You must be a musician to join a competition", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                    View view = toast.getView();
                    view.getBackground().setColorFilter(Color.parseColor("#EF5350"), PorterDuff.Mode.SRC_IN);
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


}
