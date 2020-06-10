package com.mogujie.tt.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.R;
import com.mogujie.tt.config.TUIKitConstants;
import com.mogujie.tt.imservice.event.UserInfoEvent;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.imservice.support.IMServiceConnector;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.widget.LineControllerView;
import com.mogujie.tt.ui.widget.QRCodeDialog;
import com.mogujie.tt.ui.widget.QrcodeWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MyProfileActivity extends TTBaseActivity implements View.OnClickListener {

    private LineControllerView mModifyUserIconView;
    private LineControllerView mModifyNickNameView;
    private LineControllerView mModifyGenderView;
    private LineControllerView mModifyPhoneView;
    private LineControllerView mModifyAccountView;
    private LineControllerView mModifyQrcodeView;

    private LineControllerView mModifyAllowTypeView;



    private ArrayList<String> mGenderList = new ArrayList<>();

    private int mGenderIndex = 0;
    private String mIconUrl;
    private List<LocalMedia> selectList = new ArrayList<>();
    private QrcodeWindow qrcodeWindow;
    private UserEntity loginContact;

    private IMService imService;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                return;
            }
            if (!imService.getContactManager().isUserDataReady()) {
                logger.i("detail#contact data are not ready");
            } else {
                init(imService);
            }
        }

        @Override
        public void onServiceDisconnected() {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_my_profile, topContentView);
        EventBus.getDefault().register(this);
        imServiceConnector.connect(this);
        setTitle(getResources().getString(R.string.my_profile));
        setLeftButton(R.mipmap.ic_back_black);

        mModifyUserIconView = findViewById(R.id.modify_user_icon);
        mModifyUserIconView.setCanNav(true);
        mModifyUserIconView.setOnClickListener(this);
        mModifyPhoneView = findViewById(R.id.modify_phone);
        mModifyPhoneView.setCanNav(true);
        mModifyPhoneView.setOnClickListener(this);
        mModifyNickNameView = findViewById(R.id.modify_nick_name);
        mModifyNickNameView.setCanNav(true);
        mModifyNickNameView.setOnClickListener(this);
        mModifyAccountView = findViewById(R.id.modify_account);
        mModifyAccountView.setCanNav(false);
        mModifyAccountView.setOnClickListener(this);

        mModifyGenderView = findViewById(R.id.modify_gender);
        mModifyGenderView.setCanNav(true);
        mModifyGenderView.setOnClickListener(this);

        mModifyQrcodeView = findViewById(R.id.modify_qrcode);
        mModifyQrcodeView.setCanNav(true);
        mModifyQrcodeView.setOnClickListener(this);


        mModifyAllowTypeView = findViewById(R.id.modify_allow_type);
        mModifyAllowTypeView.setCanNav(true);
        mModifyAllowTypeView.setOnClickListener(this);


        mGenderList.addAll(Arrays.asList(getResources().getStringArray(R.array.group_gender)));


    }



    public void onEventMainThread(UserInfoEvent event) {
        switch (event) {
            case USER_INFO_OK:
                init(imServiceConnector.getIMService());
        }
    }

    private void init(IMService imService) {
        if (imService == null) {
            return;
        }

        loginContact = imService.getLoginManager().getLoginInfo();
        if (loginContact == null) {
            return;
        }

        mModifyUserIconView.getImageView().setDefaultImageRes(R.drawable.tt_round_bg);
        mModifyUserIconView.getImageView().setCorner(8);
        mModifyUserIconView.getImageView().setImageUrl(loginContact.getAvatar());


        mModifyNickNameView.setContent(loginContact.getMainName());
        if (loginContact.getGender()==1){
            mModifyGenderView.setContent(mGenderList.get(0));
        }else{
            mModifyGenderView.setContent(mGenderList.get(1));
        }
        mModifyPhoneView.setContent(loginContact.getPhone());
        mModifyAccountView.setContent(loginContact.getEmail());
        mModifyAllowTypeView.setContent(getResources().getString(R.string.allow_type_need_confirm));
//        if (TextUtils.equals(TIMFriendAllowType.TIM_FRIEND_ALLOW_ANY, profile.getAllowType())) {
//            mModifyAllowTypeView.setContent(getResources().getString(R.string.allow_type_allow_any));
//        } else if (TextUtils.equals(TIMFriendAllowType.TIM_FRIEND_DENY_ANY, profile.getAllowType())) {
//            mModifyAllowTypeView.setContent(getResources().getString(R.string.allow_type_deny_any));
//        } else if (TextUtils.equals(TIMFriendAllowType.TIM_FRIEND_NEED_CONFIRM, profile.getAllowType())) {
//            mModifyAllowTypeView.setContent(getResources().getString(R.string.allow_type_need_confirm));
//        } else {
//            mModifyAllowTypeView.setContent(profile.getAllowType());
//        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.modify_user_icon) {
            //权限已经被授予，在这里直接写要执行的相应方法即可
            PictureSelector.create(MyProfileActivity.this)
                    .openGallery(PictureMimeType.ofImage())
                    .maxSelectNum(1)// 最大图片选择数量 int
                    .isCamera(true)// 是否显示拍照按钮
                    .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                    .enableCrop(true)// 是否裁剪
                    .compress(true)// 是否压缩
                    .synOrAsy(true)//同步true或异步false 压缩 默认同步
                    .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                    .glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                    .hideBottomControls(false)// 是否显示uCrop工具栏，默认不显示
                    .isGif(false)// 是否显示gif图片
                    .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
                    .circleDimmedLayer(false)// 是否圆形裁剪
                    .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                    .showCropGrid(true)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                    .selectionMedia(selectList)// 是否传入已选图片
                    .forResult(PictureConfig.CHOOSE_REQUEST);

        } else if (v.getId() == R.id.modify_nick_name) {
            Bundle bundle = new Bundle();
            bundle.putString(TUIKitConstants.Selection.TITLE, getResources().getString(R.string.modify_nick_name));
            bundle.putString(TUIKitConstants.Selection.INIT_HINT, getResources().getString(R.string.modify_nick_name_hint));
            bundle.putString(TUIKitConstants.Selection.INIT_CONTENT, mModifyNickNameView.getContent());
            bundle.putInt(TUIKitConstants.Selection.LIMIT, 20);
            SelectionActivity.startTextSelection(MyProfileActivity.this, bundle, new SelectionActivity.OnResultReturnListener() {
                @Override
                public void onReturn(Object text) {
                    mModifyNickNameView.setContent(text.toString());
                    updateUserInfo(null);
                }
            });
        } else if (v.getId() == R.id.modify_phone) {
            Bundle bundle = new Bundle();
            bundle.putString(TUIKitConstants.Selection.TITLE, getResources().getString(R.string.modify_phone));
            bundle.putString(TUIKitConstants.Selection.INIT_HINT, getResources().getString(R.string.modify_phone_hint));
            bundle.putString(TUIKitConstants.Selection.INIT_CONTENT, mModifyPhoneView.getContent());
            bundle.putInt(TUIKitConstants.Selection.LIMIT, 20);
            SelectionActivity.startTextSelection(MyProfileActivity.this, bundle, new SelectionActivity.OnResultReturnListener() {
                @Override
                public void onReturn(Object text) {
                    mModifyPhoneView.setContent(text.toString());
                    updateUserInfo(null);
                }
            });
        } else if (v.getId() == R.id.modify_gender) {
            Bundle bundle = new Bundle();
            bundle.putString(TUIKitConstants.Selection.TITLE, getResources().getString(R.string.add_setGender));
            bundle.putStringArrayList(TUIKitConstants.Selection.LIST, mGenderList);
            bundle.putInt(TUIKitConstants.Selection.DEFAULT_SELECT_ITEM_INDEX, mGenderIndex);
            SelectionActivity.startListSelection(MyProfileActivity.this, bundle, new SelectionActivity.OnResultReturnListener() {
                @Override
                public void onReturn(Object text) {
                    mModifyGenderView.setContent(mGenderList.get((Integer) text));
                    mGenderIndex = (Integer) text;
                    updateUserInfo(null);
                }
            });
        }else if(v.getId()==R.id.modify_qrcode){
            if (loginContact==null){
                return;
            }
            if (qrcodeWindow==null){
                qrcodeWindow=new QrcodeWindow(MyProfileActivity.this,loginContact);
                qrcodeWindow.setAlignBackground(true);
                qrcodeWindow.setPopupGravity(Gravity.BOTTOM);
            }
            qrcodeWindow.showPopupWindow();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PictureConfig.CHOOSE_REQUEST) {
            // 图片选择结果回调
            selectList = PictureSelector.obtainMultipleResult(data);
            // 例如 LocalMedia 里面返回三种path
            // 1.media.getPath(); 为原图path
            // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
            // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
            // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的
            String iconPath = "";
            for (LocalMedia media : selectList) {
                iconPath = media.getCompressPath();
            }
            if (!TextUtils.isEmpty(iconPath)) {
                updateUserInfo(iconPath);
            }
        }
    }
    private void updateUserInfo(String path) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(MyProfileActivity.this);
        EventBus.getDefault().unregister(this);
    }
}
