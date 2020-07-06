package com.mogujie.tt.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.CodedInputStream;
import com.mogujie.tt.DB.entity.PeerEntity;
import com.mogujie.tt.config.DBConstant;
import com.mogujie.tt.DB.entity.GroupEntity;
import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.DB.sp.ConfigurationSp;
import com.mogujie.tt.R;
import com.mogujie.tt.config.IntentConstant;
import com.mogujie.tt.config.TUIKitConstants;
import com.mogujie.tt.imservice.callback.Packetlistener;
import com.mogujie.tt.imservice.event.OtherUserInfoUpdateEvent;
import com.mogujie.tt.protobuf.IMBuddy;
import com.mogujie.tt.ui.activity.SelectionActivity;
import com.mogujie.tt.ui.adapter.GroupManagerAdapter;
import com.mogujie.tt.ui.helper.CheckboxConfigHelper;
import com.mogujie.tt.imservice.event.GroupEvent;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.ui.base.TTBaseFragment;
import com.mogujie.tt.imservice.support.IMServiceConnector;
import com.mogujie.tt.utils.ToastUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.mogujie.tt.imservice.event.OtherUserInfoUpdateEvent.UpdateEvent.USER_UPDATE_INFO_OK;


/**
 * @YM 个人与群组的聊天详情都会来到这个页面
 * single: 这有sessionId的头像，以及加号"+" ， 创建群成功之后，跳到聊天的页面
 * group:  群成员，加减号 ， 修改成功之后，跳到群管理页面
 * 临时群任何人都可以加人，但是只有群主可以踢人”这个逻辑修改下，正式群暂时只给createId开放
 */
public class GroupManagerFragment extends TTBaseFragment {
    private View curView = null;
    /**
     * adapter配置
     */
    private GridView gridView;
    private GroupManagerAdapter adapter;


    /**
     * 详情的配置  勿扰以及指定聊天
     */
    CheckboxConfigHelper checkBoxConfiger = new CheckboxConfigHelper();
    CheckBox noDisturbCheckbox;
    CheckBox topSessionCheckBox;

    /**
     * 需要的状态参数
     */
    private IMService imService;
    private String curSessionKey;
    private PeerEntity peerEntity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(getActivity());
        EventBus.getDefault().register(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_group_manage, topContentView);
        noDisturbCheckbox = (CheckBox) curView.findViewById(R.id.NotificationNoDisturbCheckbox);
        topSessionCheckBox = (CheckBox) curView.findViewById(R.id.NotificationTopMessageCheckbox);

        curView.findViewById(R.id.group_manager_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SelectionActivity.class);
                intent.putExtra("title", "Group Name");
                intent.putExtra("hint", "Enter Group Name");
                startActivity(intent);
            }
        });
        curView.findViewById(R.id.lin_group_notic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SelectionActivity.class);
                intent.putExtra("title", "Group Notic");
                startActivity(intent);
            }
        });
        curView.findViewById(R.id.lin_approva).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(getActivity(), SelectionActivity.class);
//                intent.putExtra("title","Approva");
//                startActivity(intent);
            }
        });
        curView.findViewById(R.id.lin_my_alia).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView groupAlias = curView.findViewById(R.id.group_alisa_tv);
                Bundle bundle = new Bundle();
                bundle.putString(TUIKitConstants.Selection.TITLE, getResources().getString(R.string.profile_remark_edit));
                bundle.putString(TUIKitConstants.Selection.INIT_HINT, getResources().getString(R.string.profile_remark_hint));
                bundle.putString(TUIKitConstants.Selection.INIT_CONTENT, groupAlias.getText().toString());
                SelectionActivity.startTextSelection(getActivity(), bundle, new SelectionActivity.OnResultReturnListener() {
                    @Override
                    public void onReturn(Object text) {
                        // update remake
                        if (imService != null) {
                            imService.getGroupManager().reqChangeGroupNick(text.toString(),
                                    imService.getLoginManager().getLoginId(), peerEntity.getPeerId(), new Packetlistener() {
                                        @Override
                                        public void onSuccess(Object response) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    groupAlias.setText(text.toString());
                                                }
                                            });

                                        }

                                        @Override
                                        public void onFaild() {
                                            ToastUtil.toastShortMessage("group remake fail");
                                        }

                                        @Override
                                        public void onTimeout() {
                                            ToastUtil.toastShortMessage("group remake timeout");
                                        }
                                    });
                        }
                    }
                });
            }
        });

        initRes();
        return curView;
    }

    private void initRes() {
        // 设置标题栏
        setTopLeftButton(R.drawable.tt_top_back);
        setTopLeftText(getActivity().getString(R.string.top_left_back));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        imServiceConnector.disconnect(getActivity());
    }

    @Override
    protected void initHandler() {
    }


    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            logger.d("groupmgr#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                Toast.makeText(GroupManagerFragment.this.getActivity(),
                        getResources().getString(R.string.im_service_disconnected), Toast.LENGTH_SHORT).show();
                return;
            }
            checkBoxConfiger.init(imService.getConfigSp());
            initView();
            initAdapter();
        }
    };


    private void initView() {
        if (null == imService || null == curView) {
            logger.e("groupmgr#init failed,cause by imService or curView is null");
            return;
        }

        curSessionKey = getActivity().getIntent().getStringExtra(IntentConstant.KEY_SESSION_KEY);
        if (TextUtils.isEmpty(curSessionKey)) {
            logger.e("groupmgr#getSessionInfoFromIntent failed");
            return;
        }
        peerEntity = imService.getSessionManager().findPeerEntity(curSessionKey);
        if (peerEntity == null) {
            logger.e("groupmgr#findPeerEntity failed,sessionKey:%s", curSessionKey);
            return;
        }
        switch (peerEntity.getType()) {
            case DBConstant.SESSION_TYPE_GROUP: {
                GroupEntity groupEntity = (GroupEntity) peerEntity;
                // 群组名称的展示
                TextView groupNameView = (TextView) curView.findViewById(R.id.group_manager_title);
                groupNameView.setText(groupEntity.getMainName());
                setTopTitle(groupEntity.getMainName());
            }
            break;

            case DBConstant.SESSION_TYPE_SINGLE: {
                // 个人不显示群聊名称
                View groupNameContainerView = curView.findViewById(R.id.group_manager_name);
                groupNameContainerView.setVisibility(View.GONE);
            }
            break;
        }
        // 初始化配置checkBox
        initCheckbox();
    }

    private void initAdapter() {
        logger.d("groupmgr#initAdapter");

        gridView = (GridView) curView.findViewById(R.id.group_manager_grid);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));// 去掉点击时的黄色背影
        gridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));

        adapter = new GroupManagerAdapter(getActivity(), imService, peerEntity);
        gridView.setAdapter(adapter);
    }

    /**
     * 事件驱动通知
     */
    public void onEventMainThread(GroupEvent event) {
        switch (event.getEvent()) {

            case CHANGE_GROUP_MEMBER_FAIL:
            case CHANGE_GROUP_MEMBER_TIMEOUT: {
                Toast.makeText(getActivity(), getString(R.string.change_temp_group_failed), Toast.LENGTH_SHORT).show();
                return;
            }
            case CHANGE_GROUP_MEMBER_SUCCESS: {
                onMemberChangeSuccess(event);
            }
            break;
        }
    }

    private void onMemberChangeSuccess(GroupEvent event) {
        int groupId = event.getGroupEntity().getPeerId();
        if (groupId != peerEntity.getPeerId()) {
            return;
        }
        List<Integer> changeList = event.getChangeList();
        if (changeList == null || changeList.size() <= 0) {
            return;
        }
        int changeType = event.getChangeType();

        switch (changeType) {
            case DBConstant.GROUP_MODIFY_TYPE_ADD:
                ArrayList<UserEntity> newList = new ArrayList<>();
                for (Integer userId : changeList) {
                    UserEntity userEntity = imService.getContactManager().findContact(userId);
                    if (userEntity != null) {
                        newList.add(userEntity);
                    }
                }
                adapter.add(newList);
                break;
            case DBConstant.GROUP_MODIFY_TYPE_DEL:
                for (Integer userId : changeList) {
                    adapter.removeById(userId);
                }
                break;
        }
    }

    private void initCheckbox() {
        checkBoxConfiger.initCheckBox(noDisturbCheckbox, curSessionKey, ConfigurationSp.CfgDimension.NOTIFICATION);
        checkBoxConfiger.initTopCheckBox(topSessionCheckBox, curSessionKey);
    }
}
