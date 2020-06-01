package com.mogujie.tt.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.mogujie.tt.R;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.widget.QRCodeDialog;

public class MyProfileActivity extends TTBaseActivity {

    private LinearLayout mLinName;
    private LinearLayout mLinNhonenumber;
    private LinearLayout mLinMyqrcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_my_profile, topContentView);

        setTitle(getResources().getString(R.string.my_profile));
        setLeftButton(R.mipmap.ic_back_black);

        mLinName = findViewById(R.id.lin_name);
        mLinNhonenumber = findViewById(R.id.lin_phonenumber);
        mLinMyqrcode = findViewById(R.id.lin_myqrcode);

        mLinName.setOnClickListener(onClickListener);
        mLinNhonenumber.setOnClickListener(onClickListener);
        mLinMyqrcode.setOnClickListener(onClickListener);

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MyProfileActivity.this, EditAliasActivity.class);
            switch (view.getId()) {
                case R.id.lin_name:
                    intent.putExtra("title", "Name");
                    intent.putExtra("hint", "Input Name");
                    startActivity(intent);
                    break;
                case R.id.lin_phonenumber:
                    intent.putExtra("title", "PhoneNumber");
                    intent.putExtra("hint", "Input Phone Number");
                    startActivity(intent);
                    break;
                case R.id.lin_myqrcode:
                    QRCodeDialog qrCodeDialog = new QRCodeDialog(MyProfileActivity.this);
                    qrCodeDialog.show();
                    break;
            }
        }
    };

}
