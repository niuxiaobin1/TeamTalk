package com.mogujie.tt.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.mogujie.tt.R;
import com.mogujie.tt.ui.base.TTBaseActivity;

public class PaySettingActivity extends TTBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_pay_setting, topContentView);

        setTitle(getResources().getString(R.string.pay_setting));
        setLeftButton(R.mipmap.ic_back_black);

        findViewById(R.id.lin_message_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PaySettingActivity.this, BankCardListActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.lin_set_pin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PaySettingActivity.this, SetPINActivity.class);
                startActivity(intent);
            }
        });


    }

}
