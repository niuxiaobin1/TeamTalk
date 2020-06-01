package com.mogujie.tt.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.widget.QRCodeDialog;

public class AddMoreActivity extends TTBaseActivity {

    private EditText searchEt;
    private TextView searchTv;
    private TextView actionTv;
    private TextView mIdTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_add_more, topContentView);

        setTitle(getResources().getString(R.string.new_friend));
        setLeftButton(R.mipmap.ic_back_black);

        searchEt = findViewById(R.id.searchEt);
        searchTv = findViewById(R.id.searchTv);
        actionTv = findViewById(R.id.actionTv);

        mIdTv = findViewById(R.id.mIdTv);
        mIdTv.setText(String.format(getResources().getString(R.string.self_profile_id1)
                , new String("nchat@xinyi.com")));

        findViewById(R.id.mIdLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QRCodeDialog qrCodeDialog = new QRCodeDialog(AddMoreActivity.this);
                qrCodeDialog.show();
            }
        });

        findViewById(R.id.search_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchEt.getVisibility() != View.VISIBLE) {
                    searchTv.setVisibility(View.GONE);
                    searchEt.setVisibility(View.VISIBLE);
                    actionTv.setVisibility(View.VISIBLE);
                }
            }
        });

    }
}
