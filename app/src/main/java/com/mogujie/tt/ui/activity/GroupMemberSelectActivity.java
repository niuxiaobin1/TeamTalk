
package com.mogujie.tt.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.mogujie.tt.R;
import com.mogujie.tt.ui.base.TTBaseActivity;

public class GroupMemberSelectActivity extends TTBaseActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.tt_activity_group_member_select);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK != resultCode)
            return;
    }
}
