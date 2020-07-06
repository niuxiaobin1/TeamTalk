package com.mogujie.tt.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mogujie.tt.DB.entity.GroupEntity;
import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.R;
import com.mogujie.tt.imservice.event.GroupEvent;
import com.mogujie.tt.imservice.manager.IMGroupManager;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.imservice.support.IMServiceConnector;
import com.mogujie.tt.ui.adapter.ContactAdapter;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.widget.SortSideBar;
import com.mogujie.tt.utils.IMUIHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.List;

import de.greenrobot.event.EventBus;

public class GroupListActivity extends TTBaseActivity implements SortSideBar.OnTouchingLetterChangedListener {

    private ListView allContactListView;
    private SortSideBar sortSideBar;
    private TextView dialog;
    private ProgressBar progressbar;

    private ContactAdapter contactAdapter;
    private IMGroupManager groupManager;
    private IMService imService;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                return;
            }
            groupManager = imService.getGroupManager();
            initAdapter();
            renderEntityList();
        }

        @Override
        public void onServiceDisconnected() {
        }
    };


    private void initAdapter() {
        contactAdapter = new ContactAdapter(this, imService);

        allContactListView.setAdapter(contactAdapter);

        // 单击视图事件
        allContactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object object = contactAdapter.getItem(position);
                if (object instanceof GroupEntity) {
                    GroupEntity groupEntity = (GroupEntity) object;
                    IMUIHelper.openChatActivity(GroupListActivity.this, groupEntity.getSessionKey());
                } else {
                }
            }
        });
    }

    /**
     * 刷新单个entity
     * 很消耗性能
     */
    private void renderEntityList() {
        hideProgressBar();

        if (groupManager.isGroupReady()) {
            renderGroupList();
            searchDataReady();
        }

    }

    private void renderGroupList() {
        List<GroupEntity> originList = groupManager.getNormalGroupSortedList();
        if (originList.size() <= 0) {
            return;
        }
        contactAdapter.putGroupList(originList);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().registerSticky(this);
        LayoutInflater.from(this).inflate(R.layout.activity_group_list, topContentView);
        imServiceConnector.connect(this);
        setTitle(getResources().getString(R.string.group));
        setLeftButton(R.mipmap.ic_back_black);

        initRes();
    }


    /**
     * @Description 初始化界面资源
     */
    private void initRes() {
        // 设置顶部标题栏
        sortSideBar = findViewById(R.id.sidrbar);
        dialog = findViewById(R.id.dialog);
        sortSideBar.setTextView(dialog);
        sortSideBar.setOnTouchingLetterChangedListener(this);
        progressbar = findViewById(R.id.progress_bar);
        allContactListView = findViewById(R.id.all_contact_list);

        //this is critical, disable loading when finger sliding, otherwise you'll find sliding is not very smooth
        allContactListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
        showProgressBar();
    }

    public void showProgressBar() {
        progressbar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressbar.setVisibility(View.GONE);
    }


    public void onEventMainThread(GroupEvent event) {
        switch (event.getEvent()) {
            case GROUP_INFO_UPDATED:
            case GROUP_INFO_OK:
                renderGroupList();
                break;
        }
    }

    public void searchDataReady() {
        if (imService.getGroupManager().isGroupReady()) {
            topSearchTv.setVisibility(View.VISIBLE);
            topSearchTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(this);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onTouchingLetterChanged(String s) {
        int position = -1;
        position = contactAdapter.getPositionForSection(s.charAt(0));

        if (position != -1) {
            allContactListView.setSelection(position);
        }
    }
}