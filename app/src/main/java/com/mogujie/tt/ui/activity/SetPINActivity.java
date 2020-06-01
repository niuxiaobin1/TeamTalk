package com.mogujie.tt.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.mogujie.tt.R;
import com.mogujie.tt.ui.base.TTBaseActivity;

public class SetPINActivity extends TTBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_set_p_i_n, topContentView);

        setTitle(getResources().getString(R.string.pay_set_psw));
        setLeftButton(R.mipmap.ic_back_black);

    }

}
