package com.mogujie.tt.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.mogujie.tt.R;
import com.mogujie.tt.ui.base.TTBaseActivity;

public class NewFriendsActivity extends TTBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_new_friends, topContentView);

        setTitle(getResources().getString(R.string.add_friend));
        setLeftButton(R.mipmap.ic_back_black);
        setRightText(getResources().getString(R.string.add), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewFriendsActivity.this, AddMoreActivity.class);
                startActivity(intent);
            }
        });

    }
}
