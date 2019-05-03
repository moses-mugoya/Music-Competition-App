package com.example.android.musicapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;
    private Query query;

    public MusicianFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_musician, container, false);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseReference.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        query = mDatabaseReference.orderByChild("UserType").equalTo("Musician");
        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Musician> options = new FirebaseRecyclerOptions.Builder<Musician>().setQuery(
                query,
                Musician.class
        ).build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Musician, MusicianFragment.MusicianViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MusicianFragment.MusicianViewHolder holder, int position, @NonNull Musician model) {
                final String musician_key = getRef(position).getKey();
                holder.setUsername(model.getUsername());
                holder.setGender(model.getGender());
                holder.setProfile_url(model.getImage());
                holder.setAge(model.getAge()+" Yrs");

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent musicDetail = new Intent(getContext(), MusicianDetail.class);
                        musicDetail.putExtra("MUSICIAN_KEY", musician_key);
                        musicDetail.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(musicDetail);
                    }
                });
            }

            @NonNull
            @Override
            public MusicianFragment.MusicianViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.musician_items, viewGroup, false);
                return new MusicianFragment.MusicianViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();

        mRecyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    private class MusicianViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public MusicianViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUsername(String username){
            TextView userName= mView.findViewById(R.id.username);
            userName.setText(username);
        }

        public void setProfile_url(String profile_url){
            CircleImageView circleImageView = mView.findViewById(R.id.prof_image);
            Picasso.get().load(profile_url).into(circleImageView);

        }

        public void setGender(String gend){
             TextView gender= mView.findViewById(R.id.gender);
             gender.setText(gend);
        }

        public void setAge(String age){
            TextView Age = mView.findViewById(R.id.age);
            Age.setText(age);
        }



    }
}
