package com.mogujie.tt.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.mogujie.tt.R;
import com.mogujie.tt.ui.base.TTBaseActivity;

public class TransferActivity extends TTBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_transfer, topContentView);

        setLeftButton(R.mipmap.ic_back_black);
    }
}
