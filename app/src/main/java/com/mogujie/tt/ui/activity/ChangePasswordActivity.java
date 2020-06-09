package com.mogujie.tt.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.mogujie.tt.R;
import com.mogujie.tt.ui.base.TTBaseActivity;

public class ChangePasswordActivity extends TTBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_change_password, topContentView);

        setTitle(getResources().getString(R.string.setting_change_psw));
        setLeftButton(R.mipmap.ic_back_black);

    }

}
