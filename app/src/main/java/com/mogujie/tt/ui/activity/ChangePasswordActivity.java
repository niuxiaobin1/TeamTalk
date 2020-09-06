package com.mogujie.tt.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.mogujie.tt.R;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.imservice.support.IMServiceConnector;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.utils.Logger;
import com.mogujie.tt.utils.ToastUtil;

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

        oriPswEt=findViewById(R.id.oriPswEt);
        newPswEt=findViewById(R.id.newPswEt);
        newPswConfirmEt=findViewById(R.id.newPswConfirmEt);
    }

    public void echoClick(View v){

//        signUp();
    }



    private void signUp(String openId){

//        String account = mUserAccountEt.getText().toString().trim();
//        String code = userVerifiCodeEt.getText().toString().trim();
//        String psw = userPswEt.getText().toString().trim();
//        String confirmPsw = userConfirmPswEt.getText().toString().trim();
//        if (TextUtils.isEmpty(account)||TextUtils.isEmpty(code)||TextUtils.isEmpty(psw)) {
//            return;
//        }
//        if (!psw.equals(confirmPsw)){
//            ToastUtil.toastShortMessage(getResources().getString(R.string.app_password_atypism));
//            return;
//        }
//        imService.getLoginManager().register(account,psw,code,openId);
    }


}
