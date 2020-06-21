package com.mogujie.tt.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.OkgoCallBack.NigeriaCallBack;
import com.mogujie.tt.R;
import com.mogujie.tt.bean.BaseBean;
import com.mogujie.tt.config.Constants;
import com.mogujie.tt.config.RequestCode;
import com.mogujie.tt.config.ServerHostConfig;
import com.mogujie.tt.imservice.manager.IMContactManager;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.widget.InputPasswordWindow;
import com.mogujie.tt.utils.LocationUtils;
import com.mogujie.tt.utils.MoneyTextWatcher;
import com.mogujie.tt.utils.SPUtils;
import com.mogujie.tt.utils.SoftKeyBoardUtil;
import com.mogujie.tt.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import static com.mogujie.tt.config.Constants.CUSTOM_MSG_RED_PACKET;
import static com.mogujie.tt.config.GeneralConfig.INSTITUTION_NUMBER;

public class RedPacketActivity extends TTBaseActivity {

    private EditText amountEt;
    private TextView amountTv;
    private Button btn_send;
    private UserEntity mChatInfo;

    private InputPasswordWindow inputPasswordWindow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_red_packet, topContentView);
        mChatInfo = IMContactManager.instance().findContact(getIntent().getIntExtra(Constants.CHAT_INFO,0)) ;
        setLeftButton(R.mipmap.ic_back_black);
        setTitle(getResources().getString(R.string.red_packet_title));
        setRightText(getResources().getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });



        btn_send = findViewById(R.id.btn_send);
        amountEt = findViewById(R.id.amountEt);
        amountTv = findViewById(R.id.amountTv);
        amountEt.addTextChangedListener(new MoneyTextWatcher(amountEt, amountTv));

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String money = amountEt.getText().toString().trim();
                if (!TextUtils.isEmpty(money)) {
                    SoftKeyBoardUtil.hideKeyBoard(amountEt.getWindowToken());
                    if (inputPasswordWindow != null) {
                        inputPasswordWindow = null;
                    }
                    inputPasswordWindow = new InputPasswordWindow(RedPacketActivity.this,
                            mChatInfo.getMainName(), money, new InputPasswordWindow.OnInputFinishCallBack() {
                        @Override
                        public void onFinish(String psw, String cardId) {
                            showDialog();
                            verifyPsw(money, psw, cardId);

                        }
                    });
                    inputPasswordWindow.setAlignBackground(true);
                    inputPasswordWindow.setPopupGravity(Gravity.BOTTOM);
                    if (!inputPasswordWindow.isShowing()) {
                        inputPasswordWindow.showPopupWindow();
                    }

                }

//                try {
//                    JSONObject param = new JSONObject();
//                    param.put("institution_number", INSTITUTION_NUMBER);
//                    param.put("channel", "1");
//                    param.put("payer_user_openid", "226bdb573c2a48f3dff7f610fd208721");
//                    param.put("payee_user_openid", mChatInfo.getOpenid());
//                    param.put("user_gps", "");
//                    param.put("amount", "66");
//                    param.put("order_type", "5");
//                    param.put("payer_card_id", "1");
//                    param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
//                    param.put("result", "result");
//                    param.put("type", CUSTOM_MSG_RED_PACKET);
//                    Intent it = new Intent();
//                    it.putExtra("data", param.toString());
//                    setResult(Activity.RESULT_OK, it);
//                    finish();
//                } catch (JSONException e) {
//
//                }
            }
        });

    }



    private void verifyPsw(final String amount, final String psw, final String cardId) {
        String user_openid = (String) SPUtils.get(getApplicationContext(), Constants.N_OPENID, "");
        HttpParams param = new HttpParams();
        param.put("institution_number", INSTITUTION_NUMBER);
        param.put("user_openid", user_openid);
        param.put("user_pay_password", psw);
        param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        OkGo.<String>post(ServerHostConfig.CUSTOMER_USER_VERIFY_PASSWORD).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {

                        BaseBean bean = new Gson().fromJson(response.body(), BaseBean.class);
                        if (RequestCode.SUCCESS.equals(bean.getStatus())) {
                            sendPacket(amount, cardId);
                        } else {
                            dismissDialog();
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


    private void sendPacket(final String amount, final String card) {
        final String user_openid = (String) SPUtils.get(getApplicationContext(), Constants.N_OPENID, "");
        HttpParams param = new HttpParams();
        param.put("institution_number", INSTITUTION_NUMBER);
        param.put("channel", "1");
        param.put("payee_user_openid",mChatInfo.getOpenid());
        param.put("payer_user_openid", user_openid);
        param.put("payer_card_id", card);
        String gps = LocationUtils.getInstance().getLongitude() + "," + LocationUtils.getInstance().getLatitude();
        param.put("user_gps", gps);
        param.put("amount", amount);
        param.put("order_type", "5");
        param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        OkGo.<String>post(ServerHostConfig.GATEWAY_RED_ENVELOPES).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        dismissDialog();
                        BaseBean bean = new Gson().fromJson(response.body(), BaseBean.class);
                        if (RequestCode.SUCCESS.equals(bean.getStatus())) {

                            try {
                                JSONObject result=new JSONObject(response.body());
                                JSONObject param = new JSONObject();
                                param.put("institution_number", INSTITUTION_NUMBER);
                                param.put("channel", "1");
                                param.put("payer_user_openid", user_openid);
                                param.put("payee_user_openid", mChatInfo.getOpenid());
                                param.put("user_gps", gps);
                                param.put("amount", amount);
                                param.put("order_type", "5");
                                param.put("payer_card_id", card);
                                param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
                                param.put("result", result.getJSONObject("data").toString());
                                param.put("type", CUSTOM_MSG_RED_PACKET);
                                Intent it = new Intent();
                                it.putExtra("data", param.toString());
                                setResult(Activity.RESULT_OK, it);
                                finish();
                            } catch (JSONException e) {

                            }
                        } else {
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
