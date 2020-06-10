package com.mogujie.tt.ui.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.mogujie.tt.OkgoCallBack.NigeriaCallBack;
import com.mogujie.tt.R;
import com.mogujie.tt.bean.BaseBean;
import com.mogujie.tt.config.Constants;
import com.mogujie.tt.config.RequestCode;
import com.mogujie.tt.config.ServerHostConfig;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.utils.SPUtils;
import com.mogujie.tt.utils.ToastUtil;

import static com.mogujie.tt.config.GeneralConfig.INSTITUTION_NUMBER;

public class SetPINActivity extends TTBaseActivity {

    private EditText payPswEt;
    private EditText confimPayPswEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_set_p_i_n, topContentView);

        setTitle(getResources().getString(R.string.pay_set_psw));
        setLeftButton(R.mipmap.ic_back_black);
        payPswEt = findViewById(R.id.payPswEt);
        confimPayPswEt = findViewById(R.id.payPswConfirmEt);


        payPswEt.setTypeface(Typeface.DEFAULT);
        payPswEt.setTransformationMethod(new PasswordTransformationMethod());
        confimPayPswEt.setTypeface(Typeface.DEFAULT);
        confimPayPswEt.setTransformationMethod(new PasswordTransformationMethod());

    }

    public void echoClick(View v){
        String psw=payPswEt.getText().toString().trim();
        String confirmPsw=confimPayPswEt.getText().toString().trim();

        if (TextUtils.isEmpty(psw)||TextUtils.isEmpty(confirmPsw)){
            return;
        }
        if (psw.length()!=6||confirmPsw.length()!=6){
            ToastUtil.toastShortMessage(getResources().getString(R.string.pay_psw_hint));
            return;
        }
        if (!psw.equals(confirmPsw)) {
            ToastUtil.toastShortMessage(getResources().getString(R.string.app_password_atypism));
            return;
        }

        setPayPsw(psw);
    }

    private void setPayPsw(String psw){
        String user_openid = (String) SPUtils.get(getApplicationContext(), Constants.N_OPENID, "");
        HttpParams param = new HttpParams();
        param.put("institution_number", INSTITUTION_NUMBER);
        param.put("user_openid", user_openid);
        param.put("user_pay_password", psw);
        param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        OkGo.<String>post(ServerHostConfig.CUSTOMER_USER_SET_PASSWORD).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        BaseBean bean = new Gson().fromJson(response.body(), BaseBean.class);
                        if (RequestCode.SUCCESS.equals(bean.getStatus())) {
                            ToastUtil.toastShortMessage(bean.getReturn_msg());
                            finish();
                        } else {
                            ToastUtil.toastShortMessage(bean.getReturn_msg());
                        }
                    }
                });
    }
}
