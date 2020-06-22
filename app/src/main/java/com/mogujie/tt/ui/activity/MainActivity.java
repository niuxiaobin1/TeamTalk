package com.mogujie.tt.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.mogujie.tt.R;
import com.mogujie.tt.config.IntentConstant;
import com.mogujie.tt.imservice.event.LoginEvent;
import com.mogujie.tt.imservice.event.UnreadEvent;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.imservice.support.IMServiceConnector;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.fragment.ChatFragment;
import com.mogujie.tt.ui.fragment.ContactFragment;
import com.mogujie.tt.ui.widget.NaviTabButton;
import com.mogujie.tt.utils.LocationUtils;
import com.mogujie.tt.utils.Logger;
import com.mogujie.tt.utils.ToastUtil;

import de.greenrobot.event.EventBus;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends TTBaseActivity {
    private Fragment[] mFragments;
    private NaviTabButton[] mTabButtons;
    private Logger logger = Logger.getLogger(MainActivity.class);
    private IMService imService;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            imService = imServiceConnector.getIMService();
        }

        @Override
        public void onServiceDisconnected() {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger.d("MainActivity#savedInstanceState:%s", savedInstanceState);
        //todo eric when crash, this will be called, why?
        if (savedInstanceState != null) {
            logger.w("MainActivity#crashed and restarted, just exit");
            jumpToLoginPage();
            finish();
        }

        // 在这个地方加可能会有问题吧
        EventBus.getDefault().register(this);
        imServiceConnector.connect(this);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tt_activity_main);

        initTab();
        initFragment();
        setFragmentIndicator(0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.navigation_bar_color));
            int vis = getWindow().getDecorView().getSystemUiVisibility();
            vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            vis |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            getWindow().getDecorView().setSystemUiVisibility(vis);
        }
        MainActivityPermissionsDispatcher.getLocationWithPermissionCheck(MainActivity.this);

    }

    @Override
    public void onBackPressed() {
        //don't let it exit
        //super.onBackPressed();

        //nonRoot	If false then this only works if the activity is the root of a task; if true it will work for any activity in a task.
        //document http://developer.android.com/reference/android/app/Activity.html

        //moveTaskToBack(true);

        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);

    }


    private void initFragment() {
        mFragments = new Fragment[4];
        mFragments[0] = getSupportFragmentManager().findFragmentById(R.id.fragment_chat);
        mFragments[1] = getSupportFragmentManager().findFragmentById(R.id.fragment_contact);
        mFragments[2] = getSupportFragmentManager().findFragmentById(R.id.fragment_internal);
        mFragments[3] = getSupportFragmentManager().findFragmentById(R.id.fragment_my);
    }

    private void initTab() {
        mTabButtons = new NaviTabButton[4];

        mTabButtons[0] = (NaviTabButton) findViewById(R.id.tabbutton_chat);
        mTabButtons[1] = (NaviTabButton) findViewById(R.id.tabbutton_contact);
        mTabButtons[2] = (NaviTabButton) findViewById(R.id.tabbutton_internal);
        mTabButtons[3] = (NaviTabButton) findViewById(R.id.tabbutton_my);

        mTabButtons[0].setTitle(getString(R.string.tab_conversation_tab_text));
        mTabButtons[0].setIndex(0);
        mTabButtons[0].setSelectedImage(getResources().getDrawable(R.mipmap.conversation_selected));
        mTabButtons[0].setUnselectedImage(getResources().getDrawable(R.mipmap.conversation_normal));

        mTabButtons[1].setTitle(getString(R.string.tab_contact_tab_text));
        mTabButtons[1].setIndex(1);
        mTabButtons[1].setSelectedImage(getResources().getDrawable(R.mipmap.contact_selected));
        mTabButtons[1].setUnselectedImage(getResources().getDrawable(R.mipmap.contact_normal));

        mTabButtons[2].setTitle(getString(R.string.main_innernet));
        mTabButtons[2].setIndex(2);
        mTabButtons[2].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_internal_select));
        mTabButtons[2].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_internal_nor));

        mTabButtons[3].setTitle(getString(R.string.tab_profile_tab_text));
        mTabButtons[3].setIndex(3);
        mTabButtons[3].setSelectedImage(getResources().getDrawable(R.mipmap.myself_selected));
        mTabButtons[3].setUnselectedImage(getResources().getDrawable(R.mipmap.myself_normal));
    }

    public void setFragmentIndicator(int which) {
        getSupportFragmentManager().beginTransaction().hide(mFragments[0]).hide(mFragments[1]).hide(mFragments[2]).hide(mFragments[3]).show(mFragments[which]).commit();

        mTabButtons[0].setSelectedButton(false);
        mTabButtons[1].setSelectedButton(false);
        mTabButtons[2].setSelectedButton(false);
        mTabButtons[3].setSelectedButton(false);

        switch (which) {
            case 0:
            case 1:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                    getWindow().setStatusBarColor(getResources().getColor(R.color.white));
                    getWindow().setNavigationBarColor(getResources().getColor(R.color.navigation_bar_color));
                    int vis = getWindow().getDecorView().getSystemUiVisibility();
                    vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    vis |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                    getWindow().getDecorView().setSystemUiVisibility(vis);
                }
                break;
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                    getWindow().setStatusBarColor(Color.parseColor("#48B464"));
                    getWindow().setNavigationBarColor(getResources().getColor(R.color.navigation_bar_color));
                    int vis = getWindow().getDecorView().getSystemUiVisibility();
                    vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    vis |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                    getWindow().getDecorView().setSystemUiVisibility(vis);
                }
                break;
        }


        mTabButtons[which].setSelectedButton(true);
    }

    public void setUnreadMessageCnt(int unreadCnt) {
        mTabButtons[0].setUnreadNotify(unreadCnt);
    }


    /**
     * 双击事件
     */
    public void chatDoubleListener() {
        setFragmentIndicator(0);
        ((ChatFragment) mFragments[0]).scrollToUnreadPosition();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleLocateDepratment(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void handleLocateDepratment(Intent intent) {
        int departmentIdToLocate = intent.getIntExtra(IntentConstant.KEY_LOCATE_DEPARTMENT, -1);
        if (departmentIdToLocate == -1) {
            return;
        }

        logger.d("department#got department to locate id:%d", departmentIdToLocate);
        setFragmentIndicator(1);
        ContactFragment fragment = (ContactFragment) mFragments[1];
        if (fragment == null) {
            logger.e("department#fragment is null");
            return;
        }
        fragment.locateDepartment(departmentIdToLocate);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        logger.d("mainactivity#onDestroy");
        EventBus.getDefault().unregister(this);
        imServiceConnector.disconnect(this);
        super.onDestroy();
    }


    public void onEventMainThread(UnreadEvent event) {
        switch (event.event) {
            case SESSION_READED_UNREAD_MSG:
            case UNREAD_MSG_LIST_OK:
            case UNREAD_MSG_RECEIVED:
                showUnreadMessageCount();
                break;
        }
    }

    private void showUnreadMessageCount() {
        //todo eric when to
        if (imService != null) {
            int unreadNum = imService.getUnReadMsgManager().getTotalUnreadCount();
            mTabButtons[0].setUnreadNotify(unreadNum);
        }

    }

    public void onEventMainThread(LoginEvent event) {
        switch (event) {
            case LOGIN_OUT:
                handleOnLogout();
                break;
        }
    }

    private void handleOnLogout() {
        logger.d("mainactivity#login#handleOnLogout");
        finish();
        logger.d("mainactivity#login#kill self, and start login activity");
        jumpToLoginPage();

    }

    private void jumpToLoginPage() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(IntentConstant.KEY_LOGIN_NOT_AUTO, true);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION})
    public void getLocation() {
        LocationUtils.getInstance().startLocate();
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    void showRecordDenied() {
        ToastUtil.toastShortMessage("拒绝定位权限无法获取门店位置");
    }


    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    void showRationaleForRecord(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton("不给", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage("使用APP需要定位信息")
                .show();
    }
}
