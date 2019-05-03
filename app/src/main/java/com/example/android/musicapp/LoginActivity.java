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
import android.support.design.widget.Snackbar;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    Toolbar mToolbar;

    private LinearLayout mLinearLayout;

    private TextInputLayout mEmailInput;
    private TextInputLayout mPasswordInput;

    private EditText mEmailEditText;
    private EditText mPasswordEditText;

    private Button mLoginButton;

    private ProgressDialog mProgressDialog;

    private FirebaseAuth mFirebaseAuth;

    private DatabaseReference mDatabaseReference;

    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLinearLayout = findViewById(R.id.root_layout);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        mProgressDialog = new ProgressDialog(this);

        mToolbar = findViewById(R.id.loginToolbar);

        mToolbar.setTitle(R.string.login_title);
        mToolbar.setTitleTextColor(Color.WHITE);

        mEmailInput = findViewById(R.id.input_layout_email);
        mPasswordInput = findViewById(R.id.input_layout_password);

        mEmailEditText = findViewById(R.id.email_edit_text);
        mPasswordEditText = findViewById(R.id.password_text);

        mLoginButton = findViewById(R.id.login_button);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login(){
        String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();

        boolean isValid = true;

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

        mProgressDialog.setMessage("Logging In...");
        mProgressDialog.show();
        mProgressDialog.setCanceledOnTouchOutside(false);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        if (networkInfo != null && networkInfo.isConnected()) {
            mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        CheckUserLogin();
                        DatabaseReference newDataBaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id).child("UserType");
                        newDataBaseRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String userType = dataSnapshot.getValue().toString();
                                if(userType.equals("Admin")){
                                    Intent adminIntent = new Intent(LoginActivity.this, AdminActivity.class);
                                    adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(adminIntent);
                                }else {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }else {
                        Toast toast = Toast.makeText(getApplicationContext(),R.string.login_error, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                        View view = toast.getView();
                        view.getBackground().setColorFilter(Color.parseColor("#EF5350"), PorterDuff.Mode.SRC_IN);
                        TextView textView = view.findViewById(android.R.id.message);
                        textView.setTextColor(Color.WHITE);
                        toast.show();
//                    Snackbar snackbar = Snackbar.make(mLinearLayout, R.string.login_error, Snackbar.LENGTH_LONG);
//                    snackbar.show();
                        mProgressDialog.dismiss();
                    }
                }
            });

        }
        else
            {
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

    private void CheckUserLogin(){
        user_id = mFirebaseAuth.getCurrentUser().getUid();

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(user_id)){
                    mProgressDialog.dismiss();
                }else {
                    Snackbar snackbar = Snackbar.make(mLinearLayout,R.string.first_sign, Snackbar.LENGTH_LONG);
                    snackbar.show();
                    mProgressDialog.dismiss();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void toSignUp(View view) {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    public void resetPassword(View view) {
        Intent resetIntent = new Intent(LoginActivity.this, PasswordResetActivity.class);
        resetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(resetIntent);
    }

}
