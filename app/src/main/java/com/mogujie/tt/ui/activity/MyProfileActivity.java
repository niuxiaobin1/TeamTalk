package com.mogujie.tt.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.animators.AnimationType;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.language.LanguageConfig;
import com.luck.picture.lib.style.PictureCropParameterStyle;
import com.luck.picture.lib.style.PictureParameterStyle;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;
import com.luck.picture.lib.tools.SdkVersionUtils;
import com.mogujie.tt.DB.DBInterface;
import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.R;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.config.TUIKitConstants;
import com.mogujie.tt.imservice.event.ChangeHeaderEvent;
import com.mogujie.tt.imservice.event.UploadHeaderEvent;
import com.mogujie.tt.imservice.event.UserInfoEvent;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.imservice.service.LoadImageService;
import com.mogujie.tt.imservice.support.IMServiceConnector;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.widget.LineControllerView;
import com.mogujie.tt.ui.widget.QrcodeWindow;
import com.mogujie.tt.utils.GlideEngine;
import com.mogujie.tt.utils.ToastUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MyProfileActivity extends TTBaseActivity implements View.OnClickListener {

    public static final int REQUEST_CODE = 8;

    private LineControllerView mModifyUserIconView;
    private LineControllerView mModifyNickNameView;
    private LineControllerView mModifyGenderView;
    private LineControllerView mModifyPhoneView;
    private LineControllerView mModifyAccountView;
    private LineControllerView mModifyQrcodeView;

    private LineControllerView mModifyAllowTypeView;

    private PictureParameterStyle mPictureParameterStyle;
    private PictureCropParameterStyle mCropParameterStyle;

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

    private boolean isUpdate = false;

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
        getWeChatStyle();

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
        if (loginContact.getGender() == 1) {
            mModifyGenderView.setContent(mGenderList.get(0));
        } else {
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
            PictureSelector.create(MyProfileActivity.this)
                    .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                    .imageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                    .theme(R.style.picture_WeChat_style)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style v2.3.3后 建议使用setPictureStyle()动态方式
                    .isWeChatStyle(true)// 是否开启微信图片选择风格
                    .isUseCustomCamera(true)// 是否使用自定义相机
                    .setLanguage(LanguageConfig.ENGLISH)// 设置语言，默认中文
                    .isPageStrategy(true)// 是否开启分页策略 & 每页多少条；默认开启
                    .setPictureStyle(mPictureParameterStyle)// 动态自定义相册主题
                    .setPictureCropStyle(mCropParameterStyle)// 动态自定义裁剪主题
                    //.setPictureWindowAnimationStyle()// 自定义相册启动退出动画
                    .setRecyclerAnimationMode(AnimationType.SLIDE_IN_BOTTOM_ANIMATION)// 列表动画效果
                    .isWithVideoImage(true)// 图片和视频是否可以同选,只在ofAll模式下有效
                    .isMaxSelectEnabledMask(true)// 选择数到了最大阀值列表是否启用蒙层效果
                    //.isAutomaticTitleRecyclerTop(false)// 连续点击标题栏RecyclerView是否自动回到顶部,默认true
                    //.loadCacheResourcesCallback(GlideCacheEngine.createCacheEngine())// 获取图片资源缓存，主要是解决华为10部分机型在拷贝文件过多时会出现卡的问题，这里可以判断只在会出现一直转圈问题机型上使用
                    //.setOutputCameraPath()// 自定义相机输出目录，只针对Android Q以下，例如 Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) +  File.separator + "Camera" + File.separator;
                    //.setButtonFeatures(CustomCameraView.BUTTON_STATE_BOTH)// 设置自定义相机按钮状态
                    .maxSelectNum(1)// 最大图片选择数量
                    .minSelectNum(1)// 最小选择数量
                    .maxVideoSelectNum(1) // 视频最大选择数量
                    //.minVideoSelectNum(1)// 视频最小选择数量
                    //.closeAndroidQChangeVideoWH(!SdkVersionUtils.checkedAndroid_Q())// 关闭在AndroidQ下获取图片或视频宽高相反自动转换
                    .imageSpanCount(4)// 每行显示个数
                    .isReturnEmpty(false)// 未选择数据时点击按钮是否可以返回
                    .closeAndroidQChangeWH(true)//如果图片有旋转角度则对换宽高,默认为true
                    .closeAndroidQChangeVideoWH(!SdkVersionUtils.checkedAndroid_Q())// 如果视频有旋转角度则对换宽高,默认为false
                    //.isAndroidQTransform(false)// 是否需要处理Android Q 拷贝至应用沙盒的操作，只针对compress(false); && .isEnableCrop(false);有效,默认处理
                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)// 设置相册Activity方向，不设置默认使用系统
                    .isOriginalImageControl(true)// 是否显示原图控制按钮，如果设置为true则用户可以自由选择是否使用原图，压缩、裁剪功能将会失效
                    //.bindCustomPlayVideoCallback(new MyVideoSelectedPlayCallback(getContext()))// 自定义视频播放回调控制，用户可以使用自己的视频播放界面
                    //.bindCustomPreviewCallback(new MyCustomPreviewInterfaceListener())// 自定义图片预览回调接口
                    //.bindCustomCameraInterfaceListener(new MyCustomCameraInterfaceListener())// 提供给用户的一些额外的自定义操作回调
                    //.cameraFileName(System.currentTimeMillis() +".jpg")    // 重命名拍照文件名、如果是相册拍照则内部会自动拼上当前时间戳防止重复，注意这个只在使用相机时可以使用，如果使用相机又开启了压缩或裁剪 需要配合压缩和裁剪文件名api
                    //.renameCompressFile(System.currentTimeMillis() +".jpg")// 重命名压缩文件名、 如果是多张压缩则内部会自动拼上当前时间戳防止重复
                    //.renameCropFileName(System.currentTimeMillis() + ".jpg")// 重命名裁剪文件名、 如果是多张裁剪则内部会自动拼上当前时间戳防止重复
                    .selectionMode(PictureConfig.SINGLE)// 多选 or 单选 PictureConfig.MULTIPLE :
                    .isSingleDirectReturn(true)// 单选模式下是否直接返回，PictureConfig.SINGLE模式下有效
                    .isPreviewImage(true)// 是否可预览图片
                    .isPreviewVideo(true)// 是否可预览视频
                    //.querySpecifiedFormatSuffix(PictureMimeType.ofJPEG())// 查询指定后缀格式资源
                    //.isEnablePreviewAudio() // 是否可播放音频
                    .isCamera(true)// 是否显示拍照按钮
                    //.isMultipleSkipCrop(false)// 多图裁剪时是否支持跳过，默认支持
                    //.isMultipleRecyclerAnimation(false)// 多图裁剪底部列表显示动画效果
                    .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                    //.imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg,Android Q使用PictureMimeType.PNG_Q
                    .isEnableCrop(true)// 是否裁剪
                    //.basicUCropConfig()//对外提供所有UCropOptions参数配制，但如果PictureSelector原本支持设置的还是会使用原有的设置
                    .isCompress(true)// 是否压缩
                    .compressQuality(80)// 图片压缩后输出质量 0~ 100
                    .synOrAsy(true)//同步true或异步false 压缩 默认同步
                    //.queryMaxFileSize(10)// 只查多少M以内的图片、视频、音频  单位M
//                    .compressSavePath(getPath())//压缩图片保存地址
                    //.sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效 注：已废弃
                    //.glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度 注：已废弃
                    .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                    .hideBottomControls(true)// 是否显示uCrop工具栏，默认不显示
                    .isGif(false)// 是否显示gif图片
                    .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
                    .circleDimmedLayer(false)// 是否圆形裁剪
                    //.setCropDimmedColor(ContextCompat.getColor(getContext(), R.color.app_color_white))// 设置裁剪背景色值
                    //.setCircleDimmedBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.app_color_white))// 设置圆形裁剪边框色值
                    //.setCircleStrokeWidth(3)// 设置圆形裁剪边框粗细
                    .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                    .showCropGrid(true)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                    .isOpenClickSound(false)// 是否开启点击声音
                    .selectionData(selectList)// 是否传入已选图片
                    //.isDragFrame(false)// 是否可拖动裁剪框(固定)
                    //.videoMinSecond(10)// 查询多少秒以内的视频
                    //.videoMaxSecond(15)// 查询多少秒以内的视频
                    //.recordVideoSecond(10)//录制视频秒数 默认60s
                    //.isPreviewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                    //.cropCompressQuality(90)// 注：已废弃 改用cutOutQuality()
                    .cutOutQuality(90)// 裁剪输出质量 默认100
                    .minimumCompressSize(100)// 小于多少kb的图片不压缩
                    //.cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                    //.cropImageWideHigh()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                    //.rotateEnabled(false) // 裁剪是否可旋转图片
                    //.scaleEnabled(false)// 裁剪是否可放大缩小图片
                    //.videoQuality()// 视频录制质量 0 or 1
                    .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
//                    .forResult(new MyResultCallback(mAdapter));

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
        } else if (v.getId() == R.id.modify_qrcode) {
            if (loginContact == null) {
                return;
            }
            if (qrcodeWindow == null) {
                qrcodeWindow = new QrcodeWindow(MyProfileActivity.this, loginContact);
                qrcodeWindow.setAlignBackground(true);
                qrcodeWindow.setPopupGravity(Gravity.BOTTOM);
            }
            qrcodeWindow.showPopupWindow();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片选择结果回调
                    selectList = PictureSelector.obtainMultipleResult(data);
                    // 例如 LocalMedia 里面返回五种path
                    // 1.media.getPath(); 原图path
                    // 2.media.getCutPath();裁剪后path，需判断media.isCut();切勿直接使用
                    // 3.media.getCompressPath();压缩后path，需判断media.isCompressed();切勿直接使用
                    // 4.media.getOriginalPath()); media.isOriginal());为true时此字段才有值
                    // 5.media.getAndroidQToPath();Android Q版本特有返回的字段，但如果开启了压缩或裁剪还是取裁剪或压缩路径；注意：.isAndroidQTransform 为false 此字段将返回空
                    // 如果同时开启裁剪和压缩，则取压缩路径为准因为是先裁剪后压缩
                    String iconPath = "";
                    for (LocalMedia media : selectList) {
                        if (media.isCompressed()) {
                            iconPath = media.getCompressPath();
                        } else if (media.isCut()) {
                            iconPath = media.getCutPath();
                        } else {
                            iconPath = media.getPath();
                        }
                    }
                    if (!TextUtils.isEmpty(iconPath)) {
                        updateUserInfo(iconPath);
                    }
                    break;
            }
        }
    }

    private void updateUserInfo(String path) {
        if (!TextUtils.isEmpty(path)) {
            Intent loadImageIntent = new Intent(this, LoadImageService.class);
            loadImageIntent.putExtra(SysConstant.UPLOAD_HEADER_IMAGE_INTENT_PARAMS, path);
            startService(loadImageIntent);
        }




    }

    public void onEventMainThread(UploadHeaderEvent event) {
        switch (event.getEvent()) {
            case HEADER_IMAGE_UPLOAD_SUCCESS:
                mModifyUserIconView.getImageView().setImageUrl(event.getUrl());
                loginContact.setAvatar(event.getUrl());
                updateUserHeader(event.getUrl());
                break;
            case HEADER_IMAGE_UPLOAD_FAILD:
                ToastUtil.toastShortMessage("上传失败");
                break;
        }
    }

    private void updateUserHeader(String url) {
        if (imService != null) {
            imService.getContactManager().reqChangeUsersHeader(url);
        }
    }

    public void onEventMainThread(ChangeHeaderEvent event) {
        switch (event) {
            case USER_CHANGE_HEADER_INFO_OK:
                isUpdate = true;
                DBInterface.instance().insertOrUpdateUser(loginContact);
                imService.getLoginManager().setLoginInfo(loginContact);
                break;
            case USER_CHANGE_HEADER_INFO_FAIL:
                ToastUtil.toastShortMessage("更新失败");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(MyProfileActivity.this);
        EventBus.getDefault().unregister(this);
    }

    private void getWeChatStyle() {
        // 相册主题
        mPictureParameterStyle = new PictureParameterStyle();
        // 是否改变状态栏字体颜色(黑白切换)
        mPictureParameterStyle.isChangeStatusBarFontColor = false;
        // 是否开启右下角已完成(0/9)风格
        mPictureParameterStyle.isOpenCompletedNumStyle = false;
        // 是否开启类似QQ相册带数字选择风格
        mPictureParameterStyle.isOpenCheckNumStyle = true;
        // 状态栏背景色
        mPictureParameterStyle.pictureStatusBarColor = Color.parseColor("#393a3e");
        // 相册列表标题栏背景色
        mPictureParameterStyle.pictureTitleBarBackgroundColor = Color.parseColor("#393a3e");
        // 相册父容器背景色
        mPictureParameterStyle.pictureContainerBackgroundColor = ContextCompat.getColor(this, R.color.black);
        // 相册列表标题栏右侧上拉箭头
        mPictureParameterStyle.pictureTitleUpResId = R.drawable.picture_icon_wechat_up;
        // 相册列表标题栏右侧下拉箭头
        mPictureParameterStyle.pictureTitleDownResId = R.drawable.picture_icon_wechat_down;
        // 相册文件夹列表选中圆点
        mPictureParameterStyle.pictureFolderCheckedDotStyle = R.drawable.picture_orange_oval;
        // 相册返回箭头
        mPictureParameterStyle.pictureLeftBackIcon = R.drawable.picture_icon_close;
        // 标题栏字体颜色
        mPictureParameterStyle.pictureTitleTextColor = ContextCompat.getColor(this, R.color.picture_color_white);
        // 相册右侧按钮字体颜色  废弃 改用.pictureRightDefaultTextColor和.pictureRightDefaultTextColor
        mPictureParameterStyle.pictureCancelTextColor = ContextCompat.getColor(this, R.color.picture_color_53575e);
        // 相册右侧按钮字体默认颜色
        mPictureParameterStyle.pictureRightDefaultTextColor = ContextCompat.getColor(this, R.color.picture_color_53575e);
        // 相册右侧按可点击字体颜色,只针对isWeChatStyle 为true时有效果
        mPictureParameterStyle.pictureRightSelectedTextColor = ContextCompat.getColor(this, R.color.picture_color_white);
        // 相册右侧按钮背景样式,只针对isWeChatStyle 为true时有效果
        mPictureParameterStyle.pictureUnCompleteBackgroundStyle = R.drawable.picture_send_button_default_bg;
        // 相册右侧按钮可点击背景样式,只针对isWeChatStyle 为true时有效果
        mPictureParameterStyle.pictureCompleteBackgroundStyle = R.drawable.picture_send_button_bg;
        // 选择相册目录背景样式
        mPictureParameterStyle.pictureAlbumStyle = R.drawable.picture_new_item_select_bg;
        // 相册列表勾选图片样式
        mPictureParameterStyle.pictureCheckedStyle = R.drawable.picture_wechat_num_selector;
        // 相册标题背景样式 ,只针对isWeChatStyle 为true时有效果
        mPictureParameterStyle.pictureWeChatTitleBackgroundStyle = R.drawable.picture_album_bg;
        // 微信样式 预览右下角样式 ,只针对isWeChatStyle 为true时有效果
        mPictureParameterStyle.pictureWeChatChooseStyle = R.drawable.picture_wechat_select_cb;
        // 相册返回箭头 ,只针对isWeChatStyle 为true时有效果
        mPictureParameterStyle.pictureWeChatLeftBackStyle = R.drawable.picture_icon_back;
        // 相册列表底部背景色
        mPictureParameterStyle.pictureBottomBgColor = ContextCompat.getColor(this, R.color.picture_color_grey);
        // 已选数量圆点背景样式
        mPictureParameterStyle.pictureCheckNumBgStyle = R.drawable.picture_num_oval;
        // 相册列表底下预览文字色值(预览按钮可点击时的色值)
        mPictureParameterStyle.picturePreviewTextColor = ContextCompat.getColor(this, R.color.picture_color_white);
        // 相册列表底下不可预览文字色值(预览按钮不可点击时的色值)
        mPictureParameterStyle.pictureUnPreviewTextColor = ContextCompat.getColor(this, R.color.picture_color_9b);
        // 相册列表已完成色值(已完成 可点击色值)
        mPictureParameterStyle.pictureCompleteTextColor = ContextCompat.getColor(this, R.color.picture_color_white);
        // 相册列表未完成色值(请选择 不可点击色值)
        mPictureParameterStyle.pictureUnCompleteTextColor = ContextCompat.getColor(this, R.color.picture_color_53575e);
        // 预览界面底部背景色
        mPictureParameterStyle.picturePreviewBottomBgColor = ContextCompat.getColor(this, R.color.picture_color_half_grey);
        // 外部预览界面删除按钮样式
        mPictureParameterStyle.pictureExternalPreviewDeleteStyle = R.drawable.picture_icon_delete;
        // 原图按钮勾选样式  需设置.isOriginalImageControl(true); 才有效
        mPictureParameterStyle.pictureOriginalControlStyle = R.drawable.picture_original_wechat_checkbox;
        // 原图文字颜色 需设置.isOriginalImageControl(true); 才有效
        mPictureParameterStyle.pictureOriginalFontColor = ContextCompat.getColor(this, R.color.white);
        // 外部预览界面是否显示删除按钮
        mPictureParameterStyle.pictureExternalPreviewGonePreviewDelete = true;
        // 设置NavBar Color SDK Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP有效
        mPictureParameterStyle.pictureNavBarColor = Color.parseColor("#393a3e");

        // 完成文案是否采用(%1$d/%2$d)的字符串，只允许两个占位符哟
//        mPictureParameterStyle.isCompleteReplaceNum = true;
        // 自定义相册右侧文本内容设置
//        mPictureParameterStyle.pictureUnCompleteText = getString(R.string.app_wechat_send);
        //自定义相册右侧已选中时文案 支持占位符String 但只支持两个 必须isCompleteReplaceNum为true
//        mPictureParameterStyle.pictureCompleteText = getString(R.string.app_wechat_send_num);
//        // 自定义相册列表不可预览文字
//        mPictureParameterStyle.pictureUnPreviewText = "";
//        // 自定义相册列表预览文字
//        mPictureParameterStyle.picturePreviewText = "";
//        // 自定义预览页右下角选择文字文案
//        mPictureParameterStyle.pictureWeChatPreviewSelectedText = "";

//        // 自定义相册标题文字大小
//        mPictureParameterStyle.pictureTitleTextSize = 9;
//        // 自定义相册右侧文字大小
//        mPictureParameterStyle.pictureRightTextSize = 9;
//        // 自定义相册预览文字大小
//        mPictureParameterStyle.picturePreviewTextSize = 9;
//        // 自定义相册完成文字大小
//        mPictureParameterStyle.pictureCompleteTextSize = 9;
//        // 自定义原图文字大小
//        mPictureParameterStyle.pictureOriginalTextSize = 9;
//        // 自定义预览页右下角选择文字大小
//        mPictureParameterStyle.pictureWeChatPreviewSelectedTextSize = 9;

        // 裁剪主题
        mCropParameterStyle = new PictureCropParameterStyle(
                ContextCompat.getColor(this, R.color.app_color_grey),
                ContextCompat.getColor(this, R.color.app_color_grey),
                Color.parseColor("#393a3e"),
                ContextCompat.getColor(this, R.color.white),
                mPictureParameterStyle.isChangeStatusBarFontColor);
    }

    @Override
    public void finish() {
        if (isUpdate) {
            setResult(Activity.RESULT_OK);
        }
        super.finish();
    }
}
