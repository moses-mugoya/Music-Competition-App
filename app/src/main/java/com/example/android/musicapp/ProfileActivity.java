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
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    Toolbar mToolbar;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;

    private ProgressDialog mProgressDialog;

    private EditText mAgeEditText;
    private EditText mPhoneEditText;
    private RadioGroup mRadioGroup1;
    private RadioGroup mRadioGroup2;
    private RadioButton mMusicianRadio;
    private RadioButton mAudienceRadio;
    private RadioButton mMaleRadio;
    private RadioButton mFemaleRadio;
    private Button mSetProfile;

    private CircleImageView mImageButton;


    private TextInputLayout mAgeInput;
    private TextInputLayout mPhoneInput;

    String radioGender;
    String radioMusicType;

    private Uri mImageUri = null;
    private String downloadUrl = null;

    private static final int GALLERY_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mToolbar = findViewById(R.id.profile_toolbar);

        mToolbar.setTitle(R.string.profile_title);
        mToolbar.setTitleTextColor(Color.WHITE);

        mProgressDialog  = new ProgressDialog(this);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        mStorageReference = FirebaseStorage.getInstance().getReference().child("Profile_images");

        mImageButton = findViewById(R.id.prof_image);
        mAgeEditText = findViewById(R.id.age_edit_text);
        mPhoneEditText = findViewById(R.id.phone_number);
        mRadioGroup1 = findViewById(R.id.radio_group);
        mRadioGroup2 = findViewById(R.id.radio_group2);
        mMaleRadio = findViewById(R.id.radio_male);
        mFemaleRadio = findViewById(R.id.radio_female);
        mMusicianRadio = findViewById(R.id.radio_music);
        mAudienceRadio = findViewById(R.id.radio_aud);
        mSetProfile = findViewById(R.id.set_prof_button);

        mAgeInput = findViewById(R.id.input_layout_age);
        mPhoneInput = findViewById(R.id.input_layout_phone);

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });



        mRadioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radio_male:
                        radioGender = mMaleRadio.getText().toString();
                        break;
                    case R.id.radio_female:
                        radioGender = mFemaleRadio.getText().toString();
                    default:
                }
            }
        });

        mRadioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radio_aud:
                        radioMusicType = mAudienceRadio.getText().toString();
                        break;
                    case R.id.radio_music:
                        radioMusicType = mMusicianRadio.getText().toString();
                        break;
                }
            }
        });



        mSetProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpProfile();
            }
        });
    }

    private void setUpProfile(){
        final String age = mAgeEditText.getText().toString().trim();
        final String phone_number = mPhoneEditText.getText().toString().trim();

        boolean isValid = true;

        if(mImageUri == null){
            Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.prof_pic_error), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
            View view = toast.getView();
            view.getBackground().setColorFilter(Color.parseColor("#EF5350"), PorterDuff.Mode.SRC_IN);
            TextView textView = view.findViewById(android.R.id.message);
            textView.setTextColor(Color.WHITE);
            toast.show();
            return;

        }

        if(mAgeEditText.getText().toString().isEmpty()){
            mAgeInput.setError(getString(R.string.age_error_text));
            isValid = false;
            return;
        }else{
             mAgeInput.setErrorEnabled(false);
        }
        if(mPhoneEditText.getText().toString().isEmpty()){
            mPhoneInput.setError(getString(R.string.phone_error_text));
            isValid = false;
            return;
        }else {
            mPhoneInput.setErrorEnabled(false);
        }

        if(TextUtils.isEmpty(radioGender)){
            Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.gender_error), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
            View view = toast.getView();
            view.getBackground().setColorFilter(Color.parseColor("#EF5350"), PorterDuff.Mode.SRC_IN);
            TextView textView = view.findViewById(android.R.id.message);
            textView.setTextColor(Color.WHITE);
            toast.show();
            return;
        }
        if(TextUtils.isEmpty(radioMusicType)){
            Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.identity_error_text), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
            View view = toast.getView();
            view.getBackground().setColorFilter(Color.parseColor("#EF5350"), PorterDuff.Mode.SRC_IN);
            TextView textView = view.findViewById(android.R.id.message);
            textView.setTextColor(Color.WHITE);
            toast.show();
            toast.show();
            return;
        }


        mProgressDialog.setMessage("Setting up profile...");
        mProgressDialog.show();
        mProgressDialog.setCanceledOnTouchOutside(false);

        final StorageReference filepath = mStorageReference.child(mImageUri.getLastPathSegment());

        final UploadTask uploadTask = filepath.putFile(mImageUri);

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
                            downloadUrl = task.getResult().toString();
                            String user_id = mFirebaseAuth.getCurrentUser().getUid();
                            mDatabaseReference.child(user_id).child("Age").setValue(age);
                            mDatabaseReference.child(user_id).child("PhoneNumber").setValue(phone_number);
                            mDatabaseReference.child(user_id).child("Gender").setValue(radioGender);
                            mDatabaseReference.child(user_id).child("UserType").setValue(radioMusicType);
                            mDatabaseReference.child(user_id).child("Image").setValue(downloadUrl);

                            mProgressDialog.dismiss();
                            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }

                    }
                });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();

                mImageButton.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
