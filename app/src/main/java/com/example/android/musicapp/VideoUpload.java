package com.example.android.musicapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class VideoUpload extends AppCompatActivity {
    Toolbar mToolbar;
    private EditText mSongTitle;
    private TextInputLayout mSongInput;
    private Button mUploadBtn;
    private VideoView mVideoView;
    private ImageButton mPlayBtn;

    private FirebaseAuth mFirebaseAuth;
    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;
    private ProgressDialog mProgressDialog;
    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;
    private DatabaseReference mDatabaseUser;



    private static final int GALLERY_REQUEST = 2;

    private Uri mVideoUrl = null;
    private String downloadUrl = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_upload);

        mToolbar = findViewById(R.id.video_upload_toolbar);
        mToolbar.setTitle("Upload Video");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressDialog = new ProgressDialog(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mFirebaseAuth.getCurrentUser().getUid();

        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mStorageReference = FirebaseStorage.getInstance().getReference().child("Videos");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Videos");

        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mFirebaseUser.getUid());

        mSongTitle = findViewById(R.id.song_title_edit_text);
        mUploadBtn = findViewById(R.id.video_upload_button);
        mSongInput = findViewById(R.id.input_layout_song_title);
        mVideoView = findViewById(R.id.upload_video_btn);
        mPlayBtn = findViewById(R.id.play_button);

        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mVideoUrl == null){
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("video/*");
                    startActivityForResult(galleryIntent, GALLERY_REQUEST);
                }else {
                    if(mVideoView.isPlaying()){
                        mVideoView.pause();
                        mPlayBtn.setVisibility(View.VISIBLE);
                    } else {
                        mVideoView.start();
                        mPlayBtn.setVisibility(View.GONE);
                    }
                }

            }
        });

        mVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoView.isPlaying()){
                    mVideoView.pause();
                    mPlayBtn.setVisibility(View.VISIBLE);
                }else {
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("video/*");
                    startActivityForResult(galleryIntent, GALLERY_REQUEST);
                }


            }
        });

        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadVideo();
            }
        });

    }

    public void uploadVideo(){
        final String song_title = mSongTitle.getText().toString().trim();

        boolean isValid = true;

        if(mVideoUrl == null){
            Toast toast = Toast.makeText(getApplicationContext(),"Please choose your video", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
            View view = toast.getView();
            view.getBackground().setColorFilter(Color.parseColor("#EF5350"), PorterDuff.Mode.SRC_IN);
            TextView textView = view.findViewById(android.R.id.message);
            textView.setTextColor(Color.WHITE);
            toast.show();
            return;

        }

        if(mSongTitle.getText().toString().isEmpty()){
            mSongInput.setError(getString(R.string.song_title_error));
            isValid = false;
            return;
        }else{
            mSongInput.setErrorEnabled(false);
        }

        mProgressDialog.setMessage(getString(com.example.android.musicapp.R.string.upload_video_text));
        mProgressDialog.show();
        mProgressDialog.setCanceledOnTouchOutside(false);

        final StorageReference filepath = mStorageReference.child(mVideoUrl.getLastPathSegment());
        final UploadTask uploadTask = filepath.putFile(mVideoUrl);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri>  then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();

                        }
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            final DatabaseReference newVideo = mDatabaseReference.push();
                            downloadUrl = task.getResult().toString();
                            mDatabaseUser.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    newVideo.child("CompetitionName").setValue("NonSpecified");
                                    newVideo.child("CompetitionStatus").setValue("NonSpecified");
                                    newVideo.child("SongTitle").setValue(song_title);
                                    newVideo.child("Video").setValue(downloadUrl);
                                    newVideo.child("Profile_url").setValue(dataSnapshot.child("Image").getValue());
                                    newVideo.child("Username").setValue(dataSnapshot.child("username").getValue());
                                    newVideo.child("userId").setValue(mCurrentUserId);
                                    newVideo.child("votes").setValue(0);
                                    newVideo.child("comments").setValue(0);


                                    mProgressDialog.dismiss();
                                    Toast toast = Toast.makeText(getApplicationContext(), "Video Uploaded Successfully!", Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                                    View view = toast.getView();
                                    view.getBackground().setColorFilter(Color.parseColor("#FF60AB8B"), PorterDuff.Mode.SRC_IN);
                                    TextView textView = view.findViewById(android.R.id.message);
                                    textView.setTextColor(Color.WHITE);
                                    toast.show();

                                    Intent intent = new Intent(VideoUpload.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }

                    }
                });
            }
        });
//        filepath.putFile(mVideoUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                final String downloadUri = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
//                final DatabaseReference newVideo = mDatabaseReference.push();
//
//                mDatabaseUser.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                        newVideo.child("SongTitle").setValue(song_title);
////                        newVideo.child("User_id").setValue(mFirebaseAuth.getCurrentUser().getUid());
//                        newVideo.child("Video").setValue(downloadUri);
//                        newVideo.child("Profile_url").setValue(dataSnapshot.child("Image").getValue());
//                        newVideo.child("Username").setValue(dataSnapshot.child("username").getValue());
//
//                        mProgressDialog.dismiss();
//                        Toast toast = Toast.makeText(getApplicationContext(), "Video Uploaded Successfully!", Toast.LENGTH_LONG);
//                        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
//                        View view = toast.getView();
//                        view.getBackground().setColorFilter(Color.parseColor("#FF60AB8B"), PorterDuff.Mode.SRC_IN);
//                        TextView textView = view.findViewById(android.R.id.message);
//                        textView.setTextColor(Color.WHITE);
//                        toast.show();
//
//                        Intent intent = new Intent(VideoUpload.this, MainActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(intent);

//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
//            }
//        });




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            mVideoUrl = data.getData();
            mVideoView.setVideoURI(mVideoUrl);
            mVideoView.start();
            mPlayBtn.setVisibility(View.GONE);


        }
    }
}
