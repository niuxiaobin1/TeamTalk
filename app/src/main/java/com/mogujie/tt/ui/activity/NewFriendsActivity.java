package com.mogujie.tt.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.mogujie.tt.DB.DBInterface;
import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.R;
import com.mogujie.tt.imservice.event.UserApplyInfoEvent;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.imservice.support.IMServiceConnector;
import com.mogujie.tt.ui.adapter.NewFriendListAdapter;
import com.mogujie.tt.ui.base.TTBaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class NewFriendsActivity extends TTBaseActivity {

    private ListView mNewFriendLv;
    private TextView mEmptyView;
    private List<UserEntity> mList = new ArrayList<>();
    private IMService imService;
    private NewFriendListAdapter mAdapter;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            logger.d("NewFriendsActivity#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            imService.getContactManager().reqApplyUsers();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_new_friends, topContentView);
        imServiceConnector.connect(NewFriendsActivity.this);
        EventBus.getDefault().register(this);
        setTitle(getResources().getString(R.string.add_friend));
        setLeftButton(R.mipmap.ic_back_black);
        setRightText(getResources().getString(R.string.add), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewFriendsActivity.this, AddMoreActivity.class);
                startActivity(intent);
            }
        });

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void init() {
        mNewFriendLv = findViewById(R.id.new_friend_list);
        mEmptyView = findViewById(R.id.empty_text);
    }


    public void onEventMainThread(UserApplyInfoEvent event) {
        switch (event) {
            case USER_APPLY_INFO_OK:
                initPendency();
                break;
        }
    }

    private void initPendency() {
        Map<Integer, UserEntity> applyUsers = imService.getContactManager().getApplyUserMap();
        List<UserEntity> userlist = DBInterface.instance().loadAllUsers();
        if (applyUsers.size() == 0) {
            mEmptyView.setText(getResources().getString(R.string.no_friend_apply));
            mNewFriendLv.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            return;
        } else {
            for (int i = 0; i < userlist.size(); i++) {
                if (applyUsers.containsKey(userlist.get(i).getPeerId())) {
                    applyUsers.remove(userlist.get(i).getPeerId());
                }
            }

        }
        mNewFriendLv.setVisibility(View.VISIBLE);
        mList.clear();
        mList.addAll(applyUsers.values());
        mAdapter = new NewFriendListAdapter(NewFriendsActivity.this, R.layout.contact_new_friend_item, mList, new NewFriendListAdapter.AgreeClickListener() {
            @Override
            public void onClick(int id) {
                imService.getContactManager().reqApplyActionUsers(id);
            }
        });
        mNewFriendLv.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(NewFriendsActivity.this);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void finish() {
        setResult(Activity.RESULT_OK);
        super.finish();
    }
}
