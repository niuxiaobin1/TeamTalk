package com.mogujie.tt.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.mogujie.tt.R;
import com.mogujie.tt.config.TUIKitConstants;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.imservice.support.IMServiceConnector;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.widget.LineControllerView;

import java.util.ArrayList;

public class PrivacyActivity extends TTBaseActivity {

    private LineControllerView friendRequestView;

    private ArrayList<String> mJoinTypeTextList = new ArrayList<>();
    private ArrayList<Integer> mJoinTypeIdList = new ArrayList<>();
    private int mJoinTypeIndex = 0;
    private IMService imService;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            imService = imServiceConnector.getIMService();

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_privace, topContentView);
        imServiceConnector.connect(PrivacyActivity.this);
        setTitle(getResources().getString(R.string.app_privacy));
        setLeftButton(R.mipmap.ic_back_black);

        friendRequestView = findViewById(R.id.FriendReuestView);

        friendRequestView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(TUIKitConstants.Selection.TITLE, getResources().getString(R.string.chat_friedn_request));
                bundle.putStringArrayList(TUIKitConstants.Selection.LIST, mJoinTypeTextList);
                bundle.putInt(TUIKitConstants.Selection.DEFAULT_SELECT_ITEM_INDEX, mJoinTypeIndex);
                SelectionActivity.startListSelection(PrivacyActivity.this, bundle, new SelectionActivity.OnResultReturnListener() {
                    @Override
                    public void onReturn(Object text) {
                        mJoinTypeIndex=(Integer)text;
                        friendRequestView.setContent(mJoinTypeTextList.get(mJoinTypeIndex));
                        if (imService!=null){
                            imService.getContactManager().reqChangeValidate(mJoinTypeIdList.get(mJoinTypeIndex));
                        }
                    }
                });
            }
        });


        mJoinTypeTextList.add(getResources().getString(R.string.allow_type_allow_any));
        mJoinTypeTextList.add(getResources().getString(R.string.allow_type_deny_any));
        mJoinTypeTextList.add(getResources().getString(R.string.allow_type_need_confirm));
        mJoinTypeIdList.add(1);
        mJoinTypeIdList.add(2);
        mJoinTypeIdList.add(0);


//        if (TextUtils.equals(TIMFriendAllowType.TIM_FRIEND_ALLOW_ANY, profile.getAllowType())) {
//            mJoinTypeIndex = 0;
//        } else if (TextUtils.equals(TIMFriendAllowType.TIM_FRIEND_DENY_ANY, profile.getAllowType())) {
//            mJoinTypeIndex = 1;
//        } else if (TextUtils.equals(TIMFriendAllowType.TIM_FRIEND_NEED_CONFIRM, profile.getAllowType())) {
//            mJoinTypeIndex = 2;
//        } else {
//            mJoinTypeIndex = 0;
//        }
//
//        friendRequestView.setContent(mJoinTypeTextList.get(mJoinTypeIndex));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(PrivacyActivity.this);
    }
}
