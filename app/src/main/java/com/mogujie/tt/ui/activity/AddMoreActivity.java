package com.mogujie.tt.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.R;
import com.mogujie.tt.imservice.event.UserInfoEvent;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.imservice.support.IMServiceConnector;
import com.mogujie.tt.scanResult.ScanResultCallBack;
import com.mogujie.tt.scanResult.ScanResultUtil;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.widget.QrcodeWindow;
import com.mogujie.tt.utils.IMUIHelper;
import com.mogujie.tt.utils.SoftKeyBoardUtil;
import com.mogujie.tt.utils.ToastUtil;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import de.greenrobot.event.EventBus;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class AddMoreActivity extends TTBaseActivity {
    private final static int REQUEST_CODE = 01;
    private EditText mUserID;
    private EditText searchEt;
    private TextView searchTv;
    private TextView actionTv;
    private RelativeLayout search_layout;
    private EditText mAddWording;
    private LinearLayout mIdLayout;
    private LinearLayout scan_layout;
    private UserEntity loginContact;

    private TextView mIdTv;

    private QrcodeWindow qrcodeWindow;

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
        LayoutInflater.from(this).inflate(R.layout.activity_add_more, topContentView);
        EventBus.getDefault().register(this);
        imServiceConnector.connect(this);
        setTitle(getResources().getString(R.string.new_friend));
        setLeftButton(R.mipmap.ic_back_black);

        mUserID = findViewById(R.id.user_id);
        searchEt = findViewById(R.id.searchEt);
        searchTv = findViewById(R.id.searchTv);
        actionTv = findViewById(R.id.actionTv);
        mIdTv = findViewById(R.id.mIdTv);
        search_layout = findViewById(R.id.search_layout);
        mAddWording = findViewById(R.id.add_wording);
        mIdLayout = findViewById(R.id.mIdLayout);
        scan_layout = findViewById(R.id.scan_layout);


        search_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchEt.getVisibility() != View.VISIBLE) {
                    searchTv.setVisibility(View.GONE);
                    searchEt.setVisibility(View.VISIBLE);
                    actionTv.setVisibility(View.VISIBLE);
                }
            }
        });

        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                if (TextUtils.isEmpty(s)) {
                    actionTv.setText(R.string.cancel);
                } else {
                    actionTv.setText(R.string.tuikit_search1);
                }
            }
        });

        actionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getResources().getString(R.string.cancel).equals(actionTv.getText().toString())) {
                    SoftKeyBoardUtil.hideKeyBoard(mUserID.getWindowToken());
                    searchTv.setVisibility(View.VISIBLE);
                    searchEt.setVisibility(View.GONE);
                    actionTv.setVisibility(View.GONE);
                } else {
                    SoftKeyBoardUtil.hideKeyBoard(mUserID.getWindowToken());
                    queryUser();
                }
            }
        });

        mIdLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (loginContact == null) {
                    return;
                }

                if (qrcodeWindow == null) {
                    qrcodeWindow = new QrcodeWindow(AddMoreActivity.this, loginContact);
                    qrcodeWindow.setAlignBackground(true);
                    qrcodeWindow.setPopupGravity(Gravity.BOTTOM);
                }
                qrcodeWindow.showPopupWindow();
            }
        });

        scan_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddMoreActivityPermissionsDispatcher.openCameraScanWithPermissionCheck(AddMoreActivity.this);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(AddMoreActivity.this);
        EventBus.getDefault().unregister(this);
    }

    private void queryUser() {
        String account = searchEt.getText().toString();
        if (TextUtils.isEmpty(account)) {
            return;
        }
        if (imService != null) {
            imService.getContactManager().reqSearchUsers(account);
        }

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
        mIdTv.setText(String.format(getResources().getString(R.string.self_profile_id1)
                , loginContact.getEmail()));
    }

    @NeedsPermission({Manifest.permission.CAMERA})
    public void openCameraScan() {
        Intent intent = new Intent(AddMoreActivity.this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AddMoreActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    doResult(result);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    ToastUtil.toastShortMessage( "scan fail");
                }
            }
        }
    }

    private void doResult(String result) {
        ScanResultUtil.doResult(result, new ScanResultCallBack() {
            @Override
            public void addCallBack(String openId) {
                if (imService != null) {
                    imService.getContactManager().reqSearchUsers(openId);
                }
            }

            @Override
            public void payCallBack(String payCode) {

            }

            @Override
            public void bScCallBack() {

            }

            @Override
            public void cSbCallBack1(String url) {

//                Intent it = new Intent(AddMoreActivity.this, WebViewActivity.class);
//                it.putExtra(WebViewActivity.WEB_TITLE, "");
//                it.putExtra(WebViewActivity.WEB_URL, url);
//                startActivity(it);
            }

            @Override
            public void cSbCallBack2(String order_sn) {
//                Intent it = new Intent(AddMoreActivity.this, ScanPayActivity.class);
//                it.putExtra(ScanPayActivity.ORDER, order_sn);
//                startActivity(it);
            }

            @Override
            public void cSbCallBack3(String qr_cate, String sub_no) {
                ScanResultUtil.queryMer(AddMoreActivity.this,qr_cate,sub_no);
            }
        });
    }
}
