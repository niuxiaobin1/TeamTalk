package com.mogujie.tt.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.mogujie.tt.OkgoCallBack.NigeriaCallBack;
import com.mogujie.tt.R;
import com.mogujie.tt.app.IMApplication;
import com.mogujie.tt.bean.BaseBean;
import com.mogujie.tt.bean.LoginBean;
import com.mogujie.tt.config.GeneralConfig;
import com.mogujie.tt.config.RequestCode;
import com.mogujie.tt.config.ServerHostConfig;
import com.mogujie.tt.dto.InstitutionDto;
import com.mogujie.tt.imservice.event.LoginEvent;
import com.mogujie.tt.imservice.event.SocketEvent;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.imservice.support.IMServiceConnector;
import com.mogujie.tt.protobuf.IMLogin;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.utils.AES;
import com.mogujie.tt.utils.CommonUtils;
import com.mogujie.tt.utils.IMUIHelper;
import com.mogujie.tt.utils.Logger;
import com.mogujie.tt.utils.PhoneUtil;
import com.mogujie.tt.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

import static com.mogujie.tt.app.IMApplication.INSTITUTION_NUMBER;
import static com.mogujie.tt.config.SysConstant.INSTITUTION_AESKEY;

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
    private Logger logger = Logger.getLogger(SignUpActivity.class);
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
        EventBus.getDefault().register(this);
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
            showDialog();
            getNchatParams();
        }
    }

    private void signUp(String openId){

        String account = mUserAccountEt.getText().toString().trim();
        String code = userVerifiCodeEt.getText().toString().trim();
        String psw = userPswEt.getText().toString().trim();
        String confirmPsw = userConfirmPswEt.getText().toString().trim();
        if (TextUtils.isEmpty(account)||TextUtils.isEmpty(code)||TextUtils.isEmpty(psw)) {
            return;
        }
        if (!psw.equals(confirmPsw)){
            ToastUtil.toastShortMessage(getResources().getString(R.string.app_password_atypism));
            return;
        }

        if (psw.length()<6){
            ToastUtil.toastShortMessage(getResources().getString(R.string.pswLengthAlert));
            return;
        }
        imService.getLoginManager().register(account,psw,code,openId);
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
        if (psw.length()<6){
            ToastUtil.toastShortMessage(getResources().getString(R.string.pswLengthAlert));
            return;
        }
        imService.getLoginManager().forgetPsw(account,psw,code);

    }

    private void sendCode(String account) {
        imService.getLoginManager().sendMsgCode(account,isForgetPassWord);
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


    public void onEventMainThread(LoginEvent event) {
        switch (event) {
            case REGISTER_INNER_FAILED:
            case MODIFY_PSW_FAILED:
            case REGISTER_FAILED:
                String errorTip = getString(IMUIHelper.getLoginErrorTip(event));
                Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void onEventMainThread(IMLogin.IMRegGetCodeRsp imRegGetCodeRsp) {
        logger.e("sendMsg#getCode---"+imRegGetCodeRsp.getResultString());
        if (imRegGetCodeRsp.getResultCode()!=0){
            ToastUtil.toastShortMessage(imRegGetCodeRsp.getResultString());
            return;
        }
        countDownNum=60;
        timer=new Timer();
        timer.schedule(new CountDownTask(),100,1000);
        sendCodeTv.setEnabled(false);
//        userVerifiCodeEt.setText(imRegGetCodeRsp.getCode());
        ToastUtil.toastShortMessage(getResources().getString(R.string.app_code_sended));
    }

    public void onEventMainThread(IMLogin.IMRegUserRsp imRegUserRsp) {
        logger.e("register#"+imRegUserRsp.getResultString());
        Intent it=new Intent();
        it.putExtra("data",imRegUserRsp.getResultString());
        setResult(Activity.RESULT_OK,it);
        finish();
    }

    public void onEventMainThread(IMLogin.IMForgetPassChangeRsp imForgetPassChangeRsp) {
        logger.e("modify psw#"+imForgetPassChangeRsp.getResultString());
        Intent it=new Intent();
        it.putExtra("data",imForgetPassChangeRsp.getResultString());
        setResult(Activity.RESULT_OK,it);
        finish();
    }

    public void onEventMainThread(SocketEvent event) {
        switch (event) {
            case CONNECT_MSG_SERVER_FAILED:
            case REQ_MSG_SERVER_ADDRS_FAILED:
                String errorTip = getString(IMUIHelper.getSocketErrorTip(event));
                Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(SignUpActivity.this);
        EventBus.getDefault().unregister(this);
        if (timer!=null){
            timer.cancel();
            timer=null;
        }
    }

    private void getNchatParams(){
        HttpParams param=new HttpParams();
        OkGo.<String>post(ServerHostConfig.GET_INSTITUTION_NUMBER).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        InstitutionDto bean = new Gson().fromJson(response.body(), InstitutionDto.class);
                        JSONObject jsonObject= null;
                        try {
                            jsonObject = new JSONObject(AES.AES_Decrypt(URLDecoder.decode(bean.data.institutionNumber, "utf-8"),INSTITUTION_AESKEY));
                            INSTITUTION_NUMBER=jsonObject.getString("institution_number");
                            GeneralConfig.INSTITUTION_NUMBER=jsonObject.getString("institution_number");
                            IMApplication.API_KEY=jsonObject.getString("api_key");
                            GeneralConfig.API_KEY=jsonObject.getString("api_key");
                            NchatSignUp();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        dismissDialog();
                    }
                });
    }

    private void NchatSignUp(){
        String account = mUserAccountEt.getText().toString().trim();
        String code = userVerifiCodeEt.getText().toString().trim();
        String psw = userPswEt.getText().toString().trim();
        String confirmPsw = userConfirmPswEt.getText().toString().trim();
        if (TextUtils.isEmpty(account)||TextUtils.isEmpty(code)||TextUtils.isEmpty(psw)) {
            dismissDialog();
            return;
        }
        if (!psw.equals(confirmPsw)){
            dismissDialog();
            ToastUtil.toastShortMessage(getResources().getString(R.string.app_password_atypism));
            return;
        }

        HttpParams param=new HttpParams();
        param.put("institution_number",INSTITUTION_NUMBER);
        param.put("user_account",account);
        param.put("user_password",psw);
        param.put("user_device_brand", PhoneUtil.getBrand());
        param.put("user_device_model",PhoneUtil.getModel());
        param.put("user_device_no",PhoneUtil.getIMEI(this));
        param.put("timestamp",String.valueOf(System.currentTimeMillis()/1000));

        OkGo.<String>post(ServerHostConfig.CUSTOMER_REGISTER).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        dismissDialog();
                        BaseBean bean=new Gson().fromJson(response.body(),BaseBean.class);
                        if ( RequestCode.SUCCESS.equals(bean.getStatus())){
                            LoginBean loginBean = new Gson().fromJson(response.body(), LoginBean.class);
                            signUp(loginBean.getData().getUser_openid());
                        }else{
                            ToastUtil.toastShortMessage(bean.getReturn_msg());
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        dismissDialog();
                    }
                });
    }

}
