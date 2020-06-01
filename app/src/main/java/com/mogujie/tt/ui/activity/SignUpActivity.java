package com.mogujie.tt.ui.activity;

import android.os.Bundle;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.ui.base.TTBaseActivity;

public class SignUpActivity extends TTBaseActivity {

    public static final String ISFORGETPASSWORD="_forgetPassWord";

    private boolean isForgetPassWord=false;

    private TextView titleTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        titleTv = findViewById(R.id.titleTv);

        isForgetPassWord=getIntent().getBooleanExtra(ISFORGETPASSWORD,false);
        if (isForgetPassWord){
            titleTv.setText(getResources().getString(R.string.app_forgetPassword));
        }

    }
}
