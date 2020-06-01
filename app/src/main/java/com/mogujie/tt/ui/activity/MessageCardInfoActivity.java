package com.mogujie.tt.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.mogujie.tt.R;
import com.mogujie.tt.ui.base.TTBaseActivity;

public class MessageCardInfoActivity extends TTBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_message_card_info, topContentView);

        setTitle(getResources().getString(R.string.card_manager));
        setLeftButton(R.mipmap.ic_back_black);

    }
}
