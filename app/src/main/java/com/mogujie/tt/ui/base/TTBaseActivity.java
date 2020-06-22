
package com.mogujie.tt.ui.base;

import android.Manifest;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kongzue.dialog.v3.WaitDialog;
import com.lzy.okgo.OkGo;
import com.mogujie.tt.R;
import com.mogujie.tt.utils.ToastUtil;

import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;

/**
 * @author Nana
 * @Description
 * @date 2014-4-10
 */
public abstract class TTBaseActivity extends AppCompatActivity {
    protected ImageView topLeftBtn;
    protected ImageView topRightBtn;
    protected TextView topTitleTxt;
    protected TextView topRightTxt;
    protected ViewGroup topBar;
    protected ViewGroup topContentView;
    protected LinearLayout baseRoot;
    protected float x1, y1, x2, y2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        topContentView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.tt_activity_base, null);
        topBar = (ViewGroup) topContentView.findViewById(R.id.topbar);
        topTitleTxt = (TextView) topContentView.findViewById(R.id.base_activity_title);
        topLeftBtn = (ImageView) topContentView.findViewById(R.id.left_btn);
        topRightBtn = (ImageView) topContentView.findViewById(R.id.right_btn);
        topRightTxt = topContentView.findViewById(R.id.right_txt);
        baseRoot = (LinearLayout) topContentView.findViewById(R.id.act_base_root);

        topTitleTxt.setVisibility(View.GONE);
        topRightBtn.setVisibility(View.INVISIBLE);
        topLeftBtn.setVisibility(View.GONE);
        topRightTxt.setVisibility(View.GONE);

        setContentView(topContentView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.navigation_bar_color));
            int vis = getWindow().getDecorView().getSystemUiVisibility();
            vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            vis |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            getWindow().getDecorView().setSystemUiVisibility(vis);
        }

        topLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    protected void setLeftText(String text) {
        if (null == text) {
            return;
        }
    }

    protected void setTitle(String title) {
        if (title == null) {
            return;
        }
        if (title.length() > 20) {
            title = title.substring(0, 19) + "...";
        }
        topTitleTxt.setText(title);
        topTitleTxt.setVisibility(View.VISIBLE);
    }

    protected void setTextBold(boolean styleFlag) {
        if (styleFlag) {
            topTitleTxt.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        } else {
            topTitleTxt.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        }
    }


    @Override
    public void setTitle(int id) {
        String strTitle = getResources().getString(id);
        setTitle(strTitle);
    }

    protected void setLeftButton(int resID) {
        if (resID <= 0) {
            return;
        }

        topLeftBtn.setImageResource(resID);
        topLeftBtn.setVisibility(View.VISIBLE);
    }

    protected void setRightButton(int resID) {
        if (resID <= 0) {
            return;
        }

        topRightBtn.setImageResource(resID);
        topRightBtn.setVisibility(View.VISIBLE);
    }

    protected void setRightText(String txt, View.OnClickListener onClickListener) {
        topRightTxt.setText(txt);
        topRightTxt.setVisibility(View.VISIBLE);
        topRightTxt.setOnClickListener(onClickListener);
    }

    protected void setTopBar(int resID) {
        if (resID <= 0) {
            return;
        }
        topBar.setBackgroundResource(resID);
    }

    public void showDialog() {
        WaitDialog.show(TTBaseActivity.this, getResources()
                .getString(R.string.transfer_wait));
    }

    public void dismissDialog() {
        WaitDialog.dismiss();
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    protected void showDeniedForCamera() {
        ToastUtil.toastShortMessage(R.string.permission_camera_denied);
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    protected void showNeverAskForCamera() {
        ToastUtil.toastShortMessage(R.string.permission_camera_never_ask);
    }

    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    protected void showDeniedForReadStorage() {
        ToastUtil.toastShortMessage(R.string.permission_read_storage_denied);
    }

    @OnNeverAskAgain({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    protected void showNeverAskForReadStorage() {
        ToastUtil.toastShortMessage(R.string.permission_read_storage_never_ask);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkGo.getInstance().cancelTag(this);
    }
}
