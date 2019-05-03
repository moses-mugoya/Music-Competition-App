package com.example.android.musicapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SignUpActivity extends AppCompatActivity {
    Toolbar mToolbar;

    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mUsername;

    private TextInputLayout mEmailInput;
    private TextInputLayout mPasswordInput;
    private  TextInputLayout mUsernameInput;

    private Button mSignUpBtn;

    private LinearLayout mLinearLayout;

    private ProgressDialog mProgressDialog;

    private FirebaseAuth mFirebaseAuth;

    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mLinearLayout = findViewById(R.id.root_layout);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        mFirebaseAuth = FirebaseAuth.getInstance();

        mProgressDialog = new ProgressDialog(this);

        mToolbar = findViewById(R.id.signToolbar);
        mToolbar.setTitle(getString(R.string.signup_title));
        mToolbar.setTitleTextColor(Color.WHITE);

        mEmailEditText = findViewById(R.id.email_edit_text);
        mPasswordEditText = findViewById(R.id.password_text);
        mUsername = findViewById(R.id.username_edit_text);

        mEmailInput = findViewById(R.id.input_layout_email);
        mPasswordInput = findViewById(R.id.input_layout_password);
        mUsernameInput = findViewById(R.id.input_layout_username);

        mSignUpBtn = findViewById(R.id.sign_button);

        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }

    private void signUp() {
        boolean isValid = true;

        String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();
        final String username = mUsername.getText().toString().trim();

        if (mUsername.getText().toString().isEmpty()) {
            mUsernameInput.setError(getString(R.string.username_error_text));
            isValid = false;
            return;
        } else {
            mEmailInput.setErrorEnabled(false);
        }

        if (mEmailEditText.getText().toString().isEmpty()) {
            mEmailInput.setError(getString(R.string.email_field_error_text));
            isValid = false;
            return;
        } else {
            mEmailInput.setErrorEnabled(false);
        }

        if (mPasswordEditText.getText().toString().isEmpty()) {
            mPasswordInput.setError(getString(R.string.password_error_text));
            isValid = false;
            return;
        } else {
            mPasswordInput.setErrorEnabled(false);
        }



        mProgressDialog.setMessage("Signing Up...");
        mProgressDialog.show();
        mProgressDialog.setCanceledOnTouchOutside(false);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        if(networkInfo != null && networkInfo.isConnected()){
            mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        String user_id = mFirebaseAuth.getCurrentUser().getUid();
                        DatabaseReference current_db_user = mDatabaseReference.child(user_id);
                        current_db_user.child("username").setValue(username);

                        mProgressDialog.dismiss();
                        Intent mainIntent = new Intent(SignUpActivity.this, ProfileActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);

                    }else {
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.valid_email_error, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                        View view = toast.getView();
                        view.getBackground().setColorFilter(Color.parseColor("#EF5350"), PorterDuff.Mode.SRC_IN);
                        TextView textView = view.findViewById(android.R.id.message);
                        textView.setTextColor(Color.WHITE);
                        toast.show();
//                    Snackbar snackbar = Snackbar.make(mLinearLayout, R.string.valid_email_error, Snackbar.LENGTH_LONG);
//                    snackbar.show();
                        mProgressDialog.dismiss();
                    }

                }
            });

        } else {
            Toast toast = Toast.makeText(getApplicationContext(),"No Internet Connection", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
            View view = toast.getView();
            view.getBackground().setColorFilter(Color.parseColor("#EF5350"), PorterDuff.Mode.SRC_IN);
            TextView textView = view.findViewById(android.R.id.message);
            textView.setTextColor(Color.WHITE);
            toast.show();
            mProgressDialog.dismiss();
        }




    }

    public void toLogIn(View view) {
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
