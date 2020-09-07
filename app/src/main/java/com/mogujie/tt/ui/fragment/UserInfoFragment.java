package com.mogujie.tt.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.protobuf.CodedInputStream;
import com.mogujie.tt.DB.entity.DepartmentEntity;
import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.R;
import com.mogujie.tt.config.DBConstant;
import com.mogujie.tt.config.IntentConstant;
import com.mogujie.tt.config.TUIKitConstants;
import com.mogujie.tt.imservice.callback.Packetlistener;
import com.mogujie.tt.imservice.event.OtherUserInfoUpdateEvent;
import com.mogujie.tt.imservice.event.UserInfoEvent;
import com.mogujie.tt.imservice.manager.IMLoginManager;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.imservice.support.IMServiceConnector;
import com.mogujie.tt.protobuf.IMBaseDefine;
import com.mogujie.tt.protobuf.IMBuddy;
import com.mogujie.tt.protobuf.helper.ProtoBuf2JavaBean;
import com.mogujie.tt.ui.activity.DetailPortraitActivity;
import com.mogujie.tt.ui.activity.SelectionActivity;
import com.mogujie.tt.ui.widget.IMBaseImageView;
import com.mogujie.tt.utils.IMUIHelper;
import com.mogujie.tt.utils.ToastUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import de.greenrobot.event.EventBus;

import static com.mogujie.tt.imservice.event.OtherUserInfoUpdateEvent.UpdateEvent.USER_UPDATE_INFO_OK;

/**
 * 1.18 添加currentUser变量
 */
public class UserInfoFragment extends MainFragment {

    private View curView = null;
    private IMService imService;
    private UserEntity currentUser;
    private int currentUserId;
    private IMBaseDefine.UserInfo userInfo;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("detail#onIMServiceConnected");

            imService = imServiceConnector.getIMService();
            if (imService == null) {
                logger.e("detail#imService is null");
                return;
            }

            currentUserId = getActivity().getIntent().getIntExtra(IntentConstant.KEY_PEERID, 0);
            userInfo = (IMBaseDefine.UserInfo) getActivity().getIntent().getSerializableExtra(IntentConstant.KEY_USER_INFO);

            if (currentUserId == 0 && userInfo == null) {
                logger.e("detail#intent params error!!");
                return;
            }

            if (currentUserId == 0) {
                //搜索进来的
                currentUser = ProtoBuf2JavaBean.getUserEntity(userInfo);
            } else {
                if (currentUserId == imService.getLoginManager().getLoginId()) {
                    currentUser = imService.getLoginManager().getLoginInfo();
                } else {
                    currentUser = imService.getContactManager().findContact(currentUserId);
                }
            }
            if (currentUser != null) {
                initBaseProfile();
                initDetailProfile();
            }
//            ArrayList<Integer> userIds = new ArrayList<>(1);
//            //just single type
//            userIds.add(currentUserId);
//            imService.getContactManager().reqGetDetaillUsers(userIds);
        }

        @Override
        public void onServiceDisconnected() {
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        imServiceConnector.disconnect(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        imServiceConnector.connect(getActivity());
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_user_detail, topContentView);
        super.init(curView);
        showProgressBar();
        initRes();

        curView.findViewById(R.id.lin_alisa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView nickName = curView.findViewById(R.id.nickName);
                Bundle bundle = new Bundle();
                bundle.putString(TUIKitConstants.Selection.TITLE, getResources().getString(R.string.profile_remark_edit));
                bundle.putString(TUIKitConstants.Selection.INIT_HINT, getResources().getString(R.string.profile_remark_hint));
                bundle.putString(TUIKitConstants.Selection.INIT_CONTENT, nickName.getText().toString());
                SelectionActivity.startTextSelection(getActivity(), bundle, new SelectionActivity.OnResultReturnListener() {
                    @Override
                    public void onReturn(Object text) {

                        // update remake
                        if (imService != null) {
                            imService.getContactManager().reqModifyUserRemake(text.toString(),
                                    currentUserId, new Packetlistener() {
                                        @Override
                                        public void onSuccess(Object response) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        IMBuddy.IMChangeFriendRemarkRsp imChangeFriendRemarkRsp = IMBuddy.IMChangeFriendRemarkRsp.parseFrom((CodedInputStream) response);
                                                        nickName.setText(imChangeFriendRemarkRsp.getRemark());
                                                        currentUser.setPinyinName(imChangeFriendRemarkRsp.getRemark());
                                                        imService.getContactManager().updateRemake(currentUser);
                                                        EventBus.getDefault().post(new OtherUserInfoUpdateEvent(USER_UPDATE_INFO_OK, currentUser));
                                                    } catch (IOException e) {
                                                        ToastUtil.toastShortMessage(e.getMessage());
                                                    }
                                                }
                                            });


                                        }

                                        @Override
                                        public void onFaild() {
                                            ToastUtil.toastShortMessage("remake fail");
                                        }

                                        @Override
                                        public void onTimeout() {
                                            ToastUtil.toastShortMessage("remake timeout");
                                        }
                                    });
                        }
                    }
                });

            }
        });

        return curView;
    }

    @Override
    public void onResume() {
        Intent intent = getActivity().getIntent();
        if (null != intent) {
            String fromPage = intent.getStringExtra(IntentConstant.USER_DETAIL_PARAM);
            setTopLeftText(fromPage);
        }
        super.onResume();
    }

    /**
     * @Description 初始化资源
     */
    private void initRes() {
        // 设置标题栏
        setTopTitle(getActivity().getString(R.string.page_user_detail));
        setTopLeftButton(R.drawable.tt_top_back);
        setTopLeftText(getResources().getString(R.string.top_left_back));
        curView.findViewById(R.id.lin_pin).setVisibility(View.GONE);
        if (getActivity().getIntent().getIntExtra(IntentConstant.KEY_PEERID, 0) == 0) {
            curView.findViewById(R.id.lin_alisa).setVisibility(View.GONE);
            curView.findViewById(R.id.lin_delete).setVisibility(View.GONE);
            ((TextView) curView.findViewById(R.id.chat_btn)).setText("Add");
        }


        if (getActivity().getIntent().getBooleanExtra(IntentConstant.KEY_USER_PIN_TOP, false)) {
            curView.findViewById(R.id.lin_pin).setVisibility(View.VISIBLE);
        }


    }

    @Override
    protected void initHandler() {
    }

    public void onEventMainThread(UserInfoEvent event) {
        switch (event) {
            case USER_INFO_UPDATE:
                UserEntity entity = imService.getContactManager().findContact(currentUserId);
                if (entity != null) {
                    currentUser = entity;
                    initBaseProfile();
                    initDetailProfile();
                }
                break;
        }
    }


    private void initBaseProfile() {
        logger.d("detail#initBaseProfile");
        IMBaseImageView portraitImageView = (IMBaseImageView) curView.findViewById(R.id.user_portrait);

        setTextViewContent(R.id.nickName, currentUser.getPinyinName());
        setTextViewContent(R.id.userName, currentUser.getMainName());
        //头像设置
        portraitImageView.setDefaultImageRes(R.mipmap.default_user_icon);
        portraitImageView.setCorner(8);
        portraitImageView.setImageResource(R.drawable.tt_default_user_portrait_corner);
        portraitImageView.setImageUrl(currentUser.getAvatar());

        portraitImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (imService!=null){
                    ArrayList<Integer> list=new ArrayList<>();
                    list.add(currentUser.getPeerId());
                    imService.getContactManager().reqGetDetaillUsers(list, new Packetlistener() {
                        @Override
                        public void onSuccess(Object response) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        IMBuddy.IMUsersInfoRsp imUsersInfoRsp = IMBuddy.IMUsersInfoRsp.parseFrom((CodedInputStream) response);
                                        if (imUsersInfoRsp.getUserInfoListList().size()!=0){
                                            Intent intent = new Intent(getActivity(), DetailPortraitActivity.class);
                                            intent.putExtra(IntentConstant.KEY_AVATAR_URL, imUsersInfoRsp.getUserInfoList(0).getAvatarUrl());
                                            intent.putExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR, true);
                                            startActivity(intent);
                                        }

                                    } catch (IOException e) {
                                        ToastUtil.toastShortMessage(e.getMessage());
                                    }


                                }
                            });
                        }

                        @Override
                        public void onFaild() {
                            ToastUtil.toastShortMessage("modify userInfo failed");
                        }

                        @Override
                        public void onTimeout() {
                            ToastUtil.toastShortMessage("modify userInfo onTimeout");
                        }
                    });
                }
            }
        });

        // 设置界面信息
        TextView chatBtn = curView.findViewById(R.id.chat_btn);
        LinearLayout delete_btn = curView.findViewById(R.id.lin_delete);
        Switch pin_switch = curView.findViewById(R.id.pin_switch);
        if (currentUserId == imService.getLoginManager().getLoginId()) {
            chatBtn.setVisibility(View.GONE);
            delete_btn.setVisibility(View.GONE);
            curView.findViewById(R.id.lin_alisa).setVisibility(View.GONE);
            curView.findViewById(R.id.lin_pin).setVisibility(View.GONE);
        } else {
            chatBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (getActivity().getIntent().getIntExtra(IntentConstant.KEY_PEERID, 0) == 0) {
                        imService.getContactManager().reqAddUsers(currentUser.getPeerId(), "");
                    } else {
                        IMUIHelper.openChatActivity(getActivity(), currentUser.getSessionKey());
                        getActivity().finish();
                    }
                    getActivity().finish();

                }
            });
            //置顶操作
            boolean shouldCheck = false;
            HashSet<String> topList = imService.getConfigSp().getSessionTopList();
            if (topList != null && topList.size() > 0) {
                shouldCheck = topList.contains(currentUser.getSessionKey());
            }
            pin_switch.setChecked(shouldCheck);

            pin_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    imService.getConfigSp().setSessionTop(currentUser.getSessionKey(), pin_switch.isChecked());
                }
            });

            delete_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (imService != null) {
                        imService.getContactManager().reqDeleteUser(currentUserId, new Packetlistener() {
                            @Override
                            public void onSuccess(Object response) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            IMBuddy.IMDelFriendRsp imDelFriendRsp = IMBuddy.IMDelFriendRsp.parseFrom((CodedInputStream) response);
                                            imService.getContactManager().deleteContact(currentUser);
                                            getActivity().finish();
                                        } catch (IOException e) {
                                            ToastUtil.toastShortMessage(e.getMessage());
                                        }

                                    }
                                });

                            }

                            @Override
                            public void onFaild() {
                                ToastUtil.toastShortMessage("delete fail");
                            }

                            @Override
                            public void onTimeout() {
                                ToastUtil.toastShortMessage("delete timeout");
                            }
                        });
                    }
                }
            });

        }
    }

    private void initDetailProfile() {
        logger.d("detail#initDetailProfile");
        hideProgressBar();
        DepartmentEntity deptEntity = imService.getContactManager().findDepartment(currentUser.getDepartmentId());
        setTextViewContent(R.id.department, deptEntity.getDepartName());
        setTextViewContent(R.id.telno, currentUser.getPhone());
        setTextViewContent(R.id.email, currentUser.getEmail());

        View phoneView = curView.findViewById(R.id.phoneArea);
        View emailView = curView.findViewById(R.id.emailArea);
        IMUIHelper.setViewTouchHightlighted(phoneView);
//        IMUIHelper.setViewTouchHightlighted(emailView);

//        emailView.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                if (currentUserId == IMLoginManager.instance().getLoginId())
//                    return;
//                IMUIHelper.showCustomDialog(getActivity(),View.GONE,String.format(getString(R.string.confirm_send_email),currentUser.getEmail()),new IMUIHelper.dialogCallback() {
//                    @Override
//                    public void callback() {
//                        Intent data=new Intent(Intent.ACTION_SENDTO);
//                        data.setData(Uri.parse("mailto:" + currentUser.getEmail()));
//                        data.putExtra(Intent.EXTRA_SUBJECT, "");
//                        data.putExtra(Intent.EXTRA_TEXT, "");
//                        startActivity(data);
//                    }
//                });
//            }
//        });

        phoneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUserId == IMLoginManager.instance().getLoginId())
                    return;
                IMUIHelper.showCustomDialog(getActivity(), View.GONE, String.format(getString(R.string.confirm_dial), currentUser.getPhone()), new IMUIHelper.dialogCallback() {
                    @Override
                    public void callback() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                IMUIHelper.callPhone(getActivity(), currentUser.getPhone());
                            }
                        }, 0);
                    }
                });
            }
        });

        setSex(currentUser.getGender());
    }

    private void setTextViewContent(int id, String content) {
        TextView textView = (TextView) curView.findViewById(id);
        if (textView == null) {
            return;
        }

        textView.setText(content);
    }

    private void setSex(int sex) {
        if (curView == null) {
            return;
        }

        TextView sexTextView = (TextView) curView.findViewById(R.id.sex);
        if (sexTextView == null) {
            return;
        }

        int textColor = Color.rgb(255, 138, 168); //xiaoxian
        String text = getString(R.string.sex_female_name);

        if (sex == DBConstant.SEX_MAILE) {
            textColor = Color.rgb(144, 203, 1);
            text = getString(R.string.sex_male_name);
        }

        sexTextView.setVisibility(View.VISIBLE);
        sexTextView.setText(text);
        sexTextView.setTextColor(textColor);
    }

}
