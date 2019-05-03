package com.example.android.musicapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends AppCompatActivity {
    Toolbar mToolbar;
    private EditText mEmailEditText;
    private TextInputLayout mEmailInput;

    private LinearLayout mLinearLayout;

    private Button mResetPassBtn;

    private FirebaseAuth mFirebaseAuth;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mProgressDialog = new ProgressDialog(this);

        mResetPassBtn = findViewById(R.id.reset_button);

        mLinearLayout = findViewById(R.id.root_layout);

        mToolbar = findViewById(R.id.resetToolbar);
        mToolbar.setTitle(R.string.reset_password_title);
        mToolbar.setTitleTextColor(Color.WHITE);

        mEmailEditText = findViewById(R.id.email_edit_text);
        mEmailInput = findViewById(R.id.input_layout_email);

        mResetPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });

    }

    private void sendEmail(){
        String email = mEmailEditText.getText().toString().trim();
        boolean isValid = true;

        if (mEmailEditText.getText().toString().isEmpty()) {
            mEmailInput.setError(getString(R.string.email_field_error_text));
            isValid = false;
            return;
        } else {
            mEmailInput.setErrorEnabled(false);
        }

        mProgressDialog.setMessage(getString(R.string.sending_email_text));
        mProgressDialog.show();
        mProgressDialog.setCanceledOnTouchOutside(false);

        mFirebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mProgressDialog.dismiss();
                    Toast toast = Toast.makeText(getApplicationContext(), "Email sent Successfully", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                    Intent loginIntent = new Intent(PasswordResetActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }else {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.email_reset_error, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                    View view = toast.getView();
                    view.getBackground().setColorFilter(Color.parseColor("#EF5350"), PorterDuff.Mode.SRC_IN);
                    TextView textView = view.findViewById(android.R.id.message);
                    textView.setTextColor(Color.WHITE);
                    toast.show();
//                    Snackbar snackbar = Snackbar.make(mLinearLayout,R.string.email_reset_error, Snackbar.LENGTH_LONG);
//                    snackbar.show();
                    mProgressDialog.dismiss();
                }
            }
        });


    }


}
