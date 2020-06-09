package com.mogujie.tt.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.mogujie.tt.R;
import com.mogujie.tt.ui.base.TTBaseActivity;

public class SettingsActivity extends TTBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_settings, topContentView);

        setTitle(getResources().getString(R.string.app_settings));
        setLeftButton(R.mipmap.ic_back_black);

        findViewById(R.id.lin_change_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

    }
}
