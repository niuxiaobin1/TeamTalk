package com.mogujie.tt.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.protobuf.CodedInputStream;
import com.mogujie.tt.R;
import com.mogujie.tt.imservice.callback.Packetlistener;
import com.mogujie.tt.imservice.manager.IMLoginManager;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.imservice.support.IMServiceConnector;
import com.mogujie.tt.protobuf.IMBuddy;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.utils.ToastUtil;

import java.io.IOException;

public class ChangePasswordActivity extends TTBaseActivity {

    private EditText newPswConfirmEt;
    private EditText newPswEt;
    private EditText oriPswEt;


    private IMService imService;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            imService = imServiceConnector.getIMService();
            try {
                do {
                    if (imService == null) {
                        //后台服务启动链接失败
                        break;
                    }

                    return;
                } while (false);

            } catch (Exception e) {
                // 任何未知的异常
                logger.w("loadIdentity failed");

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_change_password, topContentView);

        setTitle(getResources().getString(R.string.setting_change_psw));
        setLeftButton(R.mipmap.ic_back_black);

        imServiceConnector.connect(ChangePasswordActivity.this);

        oriPswEt = findViewById(R.id.oriPswEt);
        newPswEt = findViewById(R.id.newPswEt);
        newPswConfirmEt = findViewById(R.id.newPswConfirmEt);
    }

    public void echoClick(View v) {

        changePsw();
    }


    private void changePsw() {

        String oldPsw = oriPswEt.getText().toString().trim();
        String newPsw = newPswEt.getText().toString().trim();
        String newPswConfirm = newPswConfirmEt.getText().toString().trim();
        if (TextUtils.isEmpty(oldPsw) || TextUtils.isEmpty(newPsw) || TextUtils.isEmpty(newPswConfirm)) {
            return;
        }
        if (!newPsw.equals(newPswConfirm)) {
            ToastUtil.toastShortMessage(getResources().getString(R.string.app_password_atypism));
            return;
        }

        if (newPsw.length()<6){
            ToastUtil.toastShortMessage(getResources().getString(R.string.pswLengthAlert));
            return;
        }
        imService.getContactManager().reqChangePsw(oldPsw, newPsw, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMBuddy.IMChangePasswordRsp imChangePasswordRsp = IMBuddy.IMChangePasswordRsp.parseFrom((CodedInputStream) response);
                    if (imChangePasswordRsp.getResultCode()==0){
                        IMLoginManager.instance().setKickout(false);
                        IMLoginManager.instance().logOut();
                        finish();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFaild() {
                ToastUtil.toastShortMessage("changePsw onFailed");
            }

            @Override
            public void onTimeout() {
                ToastUtil.toastShortMessage("changePsw onTimeout");
            }
        });
    }


}
