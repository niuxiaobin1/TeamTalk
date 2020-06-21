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
import com.mogujie.tt.bean.PayOrderListBean;
import com.mogujie.tt.config.Constants;
import com.mogujie.tt.config.RequestCode;
import com.mogujie.tt.config.ServerHostConfig;
import com.mogujie.tt.imservice.manager.IMContactManager;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.widget.IMBaseImageView;
import com.mogujie.tt.ui.widget.InputPasswordWindow;
import com.mogujie.tt.utils.LocationUtils;
import com.mogujie.tt.utils.MoneyTextWatcher;
import com.mogujie.tt.utils.SPUtils;
import com.mogujie.tt.utils.SoftKeyBoardUtil;
import com.mogujie.tt.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import static com.mogujie.tt.app.IMApplication.INSTITUTION_NUMBER;
import static com.mogujie.tt.config.Constants.CUSTOM_MSG_TRANSFER;

public class TransferActivity extends TTBaseActivity {

    private IMBaseImageView userImage;
    private TextView userName;
    private EditText amountEt;
    private Button btn_transfer;

    private UserEntity mChatInfo;
    private InputPasswordWindow inputPasswordWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_transfer, topContentView);
        setLeftButton(R.mipmap.ic_back_black);

        mChatInfo = IMContactManager.instance().findContact(getIntent().
                getIntExtra(Constants.CHAT_INFO,0));
        userImage = findViewById(R.id.userImage);
        userName = findViewById(R.id.userName);
        amountEt = findViewById(R.id.amountEt);
        btn_transfer = findViewById(R.id.btn_transfer);

        userName.setText(mChatInfo.getMainName());

        amountEt.addTextChangedListener(new MoneyTextWatcher(amountEt));

        btn_transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backData("66","");
//                final String money = amountEt.getText().toString().trim();
//                if (!TextUtils.isEmpty(money)) {
//                    SoftKeyBoardUtil.hideKeyBoard(amountEt.getWindowToken());
//                    if (inputPasswordWindow != null) {
//                        inputPasswordWindow = null;
//                    }
//                    inputPasswordWindow = new InputPasswordWindow(TransferActivity.this,
//                            mChatInfo.getMainName(), money, new InputPasswordWindow.OnInputFinishCallBack() {
//                        @Override
//                        public void onFinish(String psw, String cardId) {
//                            verifyPsw(money, psw, cardId);
//                        }
//                    });
//                    inputPasswordWindow.setAlignBackground(true);
//                    inputPasswordWindow.setPopupGravity(Gravity.BOTTOM);
//                    if (!inputPasswordWindow.isShowing()) {
//                        inputPasswordWindow.showPopupWindow();
//                    }
//
//                }
            }
        });
        userImage.setImageUrl(mChatInfo.getAvatar());

    }


    private void verifyPsw(final String amount, final String psw, final String cardId) {
        showDialog();
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
                            transfer(amount, cardId);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (inputPasswordWindow != null && inputPasswordWindow.isShowing()) {
            inputPasswordWindow.dismiss();
        }
    }


    private void transfer(final String amount, final String cardId) {
        String user_openid = (String) SPUtils.get(getApplicationContext(), Constants.N_OPENID, "");
        HttpParams param = new HttpParams();
        param.put("institution_number", INSTITUTION_NUMBER);
        param.put("channel", "1");
        param.put("payer_user_openid", user_openid);
        param.put("payee_user_openid", mChatInfo.getOpenid());
        String gps = LocationUtils.getInstance().getLongitude() + "," + LocationUtils.getInstance().getLatitude();
        param.put("user_gps", gps);
        param.put("amount", amount);
        param.put("order_type", "3");
        param.put("payer_card_id", cardId);
        param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        OkGo.<String>post(ServerHostConfig.GATEWAY_TRANSFER_ACCOUNTS).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        dismissDialog();
                        PayOrderListBean bean = new Gson().fromJson(response.body(), PayOrderListBean.class);
                        if (RequestCode.SUCCESS.equals(bean.getStatus())) {
//                            Intent it = new Intent(TransferActivity.this, PayResultActivity.class);
//                            it.putExtra(PayResultActivity.TRANSFOR_NAME, mChatInfo.getChatName());
//                            it.putExtra(PayResultActivity.TRANSFOR_AMOUNT, amount);
//                            it.putExtra(PayResultActivity.TRANSFOR_RESULT, true);
//                            startActivity(it);
//                            finish();
                            backData(amount,cardId);
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


    private void backData(String amount,String cardId){
        try {
            String user_openid = (String) SPUtils.get(getApplicationContext(), Constants.N_OPENID, "");
            JSONObject param = new JSONObject();
            param.put("institution_number", INSTITUTION_NUMBER);
            param.put("channel", "1");
            param.put("payer_user_openid", user_openid);
            param.put("payee_user_openid", mChatInfo.getId());
            String gps = LocationUtils.getInstance().getLongitude() + "," + LocationUtils.getInstance().getLatitude();
            param.put("user_gps", gps);
            param.put("amount", amount);
            param.put("order_type", "3");
            param.put("payer_card_id", cardId);
            param.put("type", CUSTOM_MSG_TRANSFER);
            Intent it = new Intent();
            it.putExtra("data",param.toString());
            setResult(Activity.RESULT_OK, it);
            finish();
        }catch (JSONException e){

        }

    }
}
