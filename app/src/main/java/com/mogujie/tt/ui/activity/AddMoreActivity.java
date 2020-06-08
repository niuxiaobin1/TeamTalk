package com.mogujie.tt.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.imservice.support.IMServiceConnector;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.widget.QRCodeDialog;
import com.mogujie.tt.ui.widget.QrcodeWindow;
import com.mogujie.tt.utils.SoftKeyBoardUtil;

public class AddMoreActivity extends TTBaseActivity {
    private final static int REQUEST_CODE = 01;
    private EditText mUserID;
    private EditText searchEt;
    private TextView searchTv;
    private TextView actionTv;
    private RelativeLayout search_layout;
    private EditText mAddWording;
    private LinearLayout mIdLayout;
    private LinearLayout scan_layout;

    private TextView mIdTv;

    private QrcodeWindow qrcodeWindow;

    private IMService imService;
    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            imService = imServiceConnector.getIMService();
        }

        @Override
        public void onServiceDisconnected() {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_add_more, topContentView);

        imServiceConnector.connect(this);
        setTitle(getResources().getString(R.string.new_friend));
        setLeftButton(R.mipmap.ic_back_black);

        mUserID = findViewById(R.id.user_id);
        searchEt = findViewById(R.id.searchEt);
        searchTv = findViewById(R.id.searchTv);
        actionTv = findViewById(R.id.actionTv);
        mIdTv = findViewById(R.id.mIdTv);
        search_layout = findViewById(R.id.search_layout);
        mAddWording = findViewById(R.id.add_wording);
        mIdLayout = findViewById(R.id.mIdLayout);
        scan_layout = findViewById(R.id.scan_layout);

        mIdTv.setText(String.format(getResources().getString(R.string.self_profile_id1)
                , new String("nchat@xinyi.com")));

        search_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchEt.getVisibility() != View.VISIBLE) {
                    searchTv.setVisibility(View.GONE);
                    searchEt.setVisibility(View.VISIBLE);
                    actionTv.setVisibility(View.VISIBLE);
                }
            }
        });

        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                if (TextUtils.isEmpty(s)) {
                    actionTv.setText(R.string.cancel);
                } else {
                    actionTv.setText(R.string.tuikit_search1);
                }
            }
        });

        actionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getResources().getString(R.string.cancel).equals(actionTv.getText().toString())) {
                    SoftKeyBoardUtil.hideKeyBoard(mUserID.getWindowToken());
                    searchTv.setVisibility(View.VISIBLE);
                    searchEt.setVisibility(View.GONE);
                    actionTv.setVisibility(View.GONE);
                } else {
                    SoftKeyBoardUtil.hideKeyBoard(mUserID.getWindowToken());
                    queryUser();
                }
            }
        });

        mIdLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (qrcodeWindow == null) {
                    qrcodeWindow = new QrcodeWindow(AddMoreActivity.this);
                    qrcodeWindow.setAlignBackground(true);
                    qrcodeWindow.setPopupGravity(Gravity.BOTTOM);
                }
                qrcodeWindow.showPopupWindow();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(AddMoreActivity.this);
    }

    private void queryUser(){
        String account = searchEt.getText().toString();
        if (TextUtils.isEmpty(account)) {
            return;
        }
        if (imService!=null){
            imService.getContactManager().reqSearchUsers(account);
        }

    }
}
