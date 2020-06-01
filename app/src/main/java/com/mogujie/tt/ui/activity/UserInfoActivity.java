package com.mogujie.tt.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.mogujie.tt.R;
import com.mogujie.tt.ui.base.TTBaseFragmentActivity;

public class UserInfoActivity extends  TTBaseFragmentActivity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_fragment_activity_userinfo);

    }
}
