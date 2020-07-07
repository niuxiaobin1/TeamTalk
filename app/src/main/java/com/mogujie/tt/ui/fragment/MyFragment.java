package com.mogujie.tt.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.R;
import com.mogujie.tt.config.ServerHostConfig;
import com.mogujie.tt.imservice.event.UserInfoEvent;
import com.mogujie.tt.imservice.manager.IMLoginManager;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.imservice.support.IMServiceConnector;
import com.mogujie.tt.ui.activity.MyProfileActivity;
import com.mogujie.tt.ui.activity.NChatPayActivity;
import com.mogujie.tt.ui.activity.PrivacyActivity;
import com.mogujie.tt.ui.activity.SettingActivity;
import com.mogujie.tt.ui.activity.SettingsActivity;
import com.mogujie.tt.ui.activity.WebViewActivity;
import com.mogujie.tt.ui.widget.IMBaseImageView;
import com.mogujie.tt.utils.FileUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

import de.greenrobot.event.EventBus;

public class MyFragment extends MainFragment {
    private View curView = null;
    private View contentView;
    private View exitView;
    private View clearView;
    private View settingView;

    private LinearLayout mNigeriaChatPayLayout;
    private LinearLayout mShanghuLayout;
    private LinearLayout mNigeriaPrivacyLayout;
    private LinearLayout mNigeriaSettingsLayout;
    private LinearLayout mNigeriaAboutLayout;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            if (curView == null) {
                return;
            }
            IMService imService = imServiceConnector.getIMService();
            if (imService == null) {
                return;
            }
            if (!imService.getContactManager().isUserDataReady()) {
                logger.i("detail#contact data are not ready");
            } else {
                init(imService);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        imServiceConnector.connect(getActivity());
        EventBus.getDefault().register(this);

        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_my, topContentView);

        initRes();

        return curView;
    }

    /**
     * @Description 初始化资源
     */
    private void initRes() {
        super.init(curView);

        contentView = curView.findViewById(R.id.content);
        exitView = curView.findViewById(R.id.exitPage);
        clearView = curView.findViewById(R.id.clearPage);
        settingView = curView.findViewById(R.id.settingPage);
        mNigeriaChatPayLayout = curView.findViewById(R.id.nigeriaChatPayLayout);
        mShanghuLayout = curView.findViewById(R.id.shanghuLayout);
        mNigeriaPrivacyLayout = curView.findViewById(R.id.nigeriaPrivacyLayout);
        mNigeriaSettingsLayout = curView.findViewById(R.id.nigeriaSettingsLayout);
        mNigeriaAboutLayout = curView.findViewById(R.id.about_layout);

        clearView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog));
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialog_view = inflater.inflate(R.layout.tt_custom_dialog, null);
                final EditText editText = (EditText) dialog_view.findViewById(R.id.dialog_edit_content);
                editText.setVisibility(View.GONE);
                TextView textText = (TextView) dialog_view.findViewById(R.id.dialog_title);
                textText.setText(R.string.clear_cache_tip);
                builder.setView(dialog_view);

                builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ImageLoader.getInstance().clearMemoryCache();
                        ImageLoader.getInstance().clearDiskCache();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                FileUtil.deleteHistoryFiles(new File(Environment.getExternalStorageDirectory().toString()
                                        + File.separator + "MGJ-IM" + File.separator), System.currentTimeMillis());
                                Toast toast = Toast.makeText(getActivity(), R.string.thumb_remove_finish, Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                        }, 500);

                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(getString(R.string.tt_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });
        exitView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog));
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialog_view = inflater.inflate(R.layout.tt_custom_dialog, null);
                final EditText editText = (EditText) dialog_view.findViewById(R.id.dialog_edit_content);
                editText.setVisibility(View.GONE);
                TextView textText = (TextView) dialog_view.findViewById(R.id.dialog_title);
                textText.setText(R.string.exit_teamtalk_tip);
                builder.setView(dialog_view);
                builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IMLoginManager.instance().setKickout(false);
                        IMLoginManager.instance().logOut();
                        getActivity().finish();
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(getString(R.string.tt_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();

            }
        });

        settingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到配置页面
                startActivity(new Intent(MyFragment.this.getActivity(), SettingActivity.class));
            }
        });

        mNigeriaChatPayLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), NChatPayActivity.class);
                startActivity(intent);
            }
        });

        mNigeriaPrivacyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PrivacyActivity.class);
                startActivity(intent);
            }
        });

        mNigeriaSettingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        mNigeriaAboutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getContext(), WebViewActivity.class);
//                it.putExtra(WebViewActivity.WEB_TITLE, getContext().getResources().getString(
//                        R.string.app_about_nchat));
                it.putExtra(WebViewActivity.WEB_URL, ServerHostConfig.HTML_ABOUT);
                getContext().startActivity(it);
            }
        });

        hideContent();
        hideTopBar();
    }

    private void hideContent() {
        if (contentView != null) {
            contentView.setVisibility(View.GONE);
        }
    }

    private void showContent() {
        if (contentView != null) {
            contentView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 应该放在这里嘛??
        imServiceConnector.disconnect(getActivity());
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initHandler() {
    }

    public void onEventMainThread(UserInfoEvent event) {
        switch (event) {
            case USER_INFO_OK:
                init(imServiceConnector.getIMService());
        }
    }


    private void init(IMService imService) {
        showContent();
        hideProgressBar();

        if (imService == null) {
            return;
        }

        final UserEntity loginContact = imService.getLoginManager().getLoginInfo();
        if (loginContact == null) {
            return;
        }
        TextView nickNameView = (TextView) curView.findViewById(R.id.nickName);
        TextView userNameView = (TextView) curView.findViewById(R.id.userName);
        IMBaseImageView portraitImageView = (IMBaseImageView) curView.findViewById(R.id.user_portrait);

        nickNameView.setText(loginContact.getMainName1());
        userNameView.setText(loginContact.getRealName());

        //头像设置
        portraitImageView.setDefaultImageRes(R.drawable.tt_round_bg);
        portraitImageView.setCorner(15);
        portraitImageView.setImageResource(R.drawable.tt_default_user_portrait_corner);
        portraitImageView.setImageUrl(loginContact.getAvatar());

        LinearLayout userContainer = (LinearLayout) curView.findViewById(R.id.user_container);
        userContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
//                IMUIHelper.openUserProfileActivity(getActivity(), loginContact.getPeerId());
                startActivityForResult(new Intent(getActivity(), MyProfileActivity.class), MyProfileActivity.REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == MyProfileActivity.REQUEST_CODE) {
            if (imServiceConnector != null && imServiceConnector.getIMService() != null) {
                init(imServiceConnector.getIMService());
            }
        }
    }

    private void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        } else {
            logger.e("fragment#deleteFilesByDirectory, failed");
        }
    }
}
