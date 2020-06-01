package com.mogujie.tt.ui.activity;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.mogujie.tt.DB.sp.LoginSp;
import com.mogujie.tt.R;
import com.mogujie.tt.imservice.manager.IMLoginManager;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.imservice.support.IMServiceConnector;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.utils.CommonUtils;

import java.util.Timer;
import java.util.TimerTask;

public class SignUpActivity extends TTBaseActivity {

    public static final String ISFORGETPASSWORD="_forgetPassWord";

    private boolean isForgetPassWord=false;

    private TextView titleTv;
    private ImageView iv_back;

    private EditText mUserAccountEt;
    private EditText userVerifiCodeEt;
    private EditText userPswEt;
    private EditText userConfirmPswEt;
    private TextView sendCodeTv;
    private TextInputLayout confirmPswInputLayout;
    private TextInputLayout pswInputLayout;
    private TextInputLayout codeInputLayout;
    private TextInputLayout emailInputLayout;

    private Timer timer;
    private int countDownNum=60;

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
        setContentView(R.layout.activity_sign_up);
        imServiceConnector.connect(SignUpActivity.this);
        titleTv = findViewById(R.id.titleTv);
        iv_back = findViewById(R.id.iv_back);
        mUserAccountEt = findViewById(R.id.userAccount);
        userVerifiCodeEt = findViewById(R.id.userVerifiCodeEt);
        userPswEt = findViewById(R.id.userPswEt);
        userConfirmPswEt = findViewById(R.id.userConfirmPswEt);
        sendCodeTv = findViewById(R.id.sendCodeTv);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        codeInputLayout = findViewById(R.id.codeInputLayout);
        pswInputLayout = findViewById(R.id.pswInputLayout);
        confirmPswInputLayout = findViewById(R.id.confirmPswInputLayout);

        isForgetPassWord=getIntent().getBooleanExtra(ISFORGETPASSWORD,false);
        if (isForgetPassWord){
            titleTv.setText(getResources().getString(R.string.app_forgetPassword));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.navigation_bar_color));
            int vis = getWindow().getDecorView().getSystemUiVisibility();
            vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            vis |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            getWindow().getDecorView().setSystemUiVisibility(vis);
        }

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        userPswEt.setTypeface(Typeface.DEFAULT);
        userPswEt.setTransformationMethod(new PasswordTransformationMethod());
        userConfirmPswEt.setTypeface(Typeface.DEFAULT);
        userConfirmPswEt.setTransformationMethod(new PasswordTransformationMethod());

        sendCodeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = mUserAccountEt.getText().toString().trim();
                if (CommonUtils.isEmail(account)) {
                    emailInputLayout.setErrorEnabled(false);
                    sendCode(account);
                } else {
                    emailInputLayout.setErrorEnabled(true);
                    emailInputLayout.setError(getResources().getString(R.string.app_emailFormat_error));
                }
            }
        });
    }

    public void onClickNext(View v) {
        if (isForgetPassWord){
            forgetPsw();
        }else{
            signUp();
        }
    }

    private void signUp(){

    }


    private void forgetPsw(){
        String account = mUserAccountEt.getText().toString().trim();
        String code = userVerifiCodeEt.getText().toString().trim();
        String psw = userPswEt.getText().toString().trim();
        String confirmPsw = userConfirmPswEt.getText().toString().trim();
        if (TextUtils.isEmpty(account)) {
            emailInputLayout.setErrorEnabled(true);
            emailInputLayout.setError(getResources().getString(R.string.app_emailFormat_null));
            return;
        }else{
            emailInputLayout.setErrorEnabled(false);
        }

        if (TextUtils.isEmpty(code)) {
            codeInputLayout.setErrorEnabled(true);
            codeInputLayout.setError(getResources().getString(R.string.app_codeFormat_null));
            return;
        }else{
            codeInputLayout.setErrorEnabled(false);
        }

        if (TextUtils.isEmpty(psw)) {
            pswInputLayout.setErrorEnabled(true);
            pswInputLayout.setError(getResources().getString(R.string.app_pswFormat_null));
            return;
        }else{
            pswInputLayout.setErrorEnabled(false);
        }

        if (!psw.equals(confirmPsw)){
            confirmPswInputLayout.setErrorEnabled(true);
            confirmPswInputLayout.setError(getResources().getString(R.string.app_password_atypism));
//            ToastUtil.toastShortMessage(getResources().getString(R.string.app_password_atypism));
            return;
        }else{
            confirmPswInputLayout.setErrorEnabled(false);
        }

    }

    private void sendCode(String account) {
        imService.getLoginManager().sendMsgCode(account);
    }

    class  CountDownTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (countDownNum<=0){
                        if (timer!=null){
                            timer.cancel();
                            timer=null;
                        }
                        sendCodeTv.setEnabled(true);
                        sendCodeTv.setText(R.string.app_receiver);
                    }else{
                        sendCodeTv.setText(countDownNum+"s");
                        countDownNum--;
                    }

                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer!=null){
            timer.cancel();
            timer=null;
        }
    }


}
