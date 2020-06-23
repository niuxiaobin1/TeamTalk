package com.mogujie.tt.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.mogujie.tt.OkgoCallBack.NigeriaCallBack;
import com.mogujie.tt.R;
import com.mogujie.tt.bean.BaseBean;
import com.mogujie.tt.bean.OrderBean;
import com.mogujie.tt.config.Constants;
import com.mogujie.tt.config.RequestCode;
import com.mogujie.tt.config.ServerHostConfig;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.widget.InputPasswordWindow;
import com.mogujie.tt.utils.SPUtils;
import com.mogujie.tt.utils.SoftKeyBoardUtil;
import com.mogujie.tt.utils.ToastUtil;

import static com.mogujie.tt.app.IMApplication.INSTITUTION_NUMBER;

public class ScanPayActivity extends TTBaseActivity {
    private TextView payToTv;
    private TextView amountTv;
    private Button btn_pay;

    public static final String ORDER = "_order_sn";
    private String order_sn = "";

    private InputPasswordWindow inputPasswordWindow;
    private String x_Location;
    private String y_Location;

    private OrderBean orderBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_scan_pay, topContentView);
        payToTv = findViewById(R.id.payToTv);
        amountTv = findViewById(R.id.amountTv);
        btn_pay = findViewById(R.id.btn_pay);
        order_sn = getIntent().getStringExtra(ORDER);
        if (TextUtils.isEmpty(order_sn)) {
            return;
        }
        setLeftButton(R.mipmap.ic_back_black);
        setTitle(R.string.pay_confirm);
        queryOrder(order_sn);

        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoftKeyBoardUtil.hideKeyBoard(btn_pay.getWindowToken());
                if (orderBean == null) {
                    return;
                }

                if (inputPasswordWindow != null) {
                    inputPasswordWindow = null;
                }
                inputPasswordWindow = new InputPasswordWindow(ScanPayActivity.this,
                        orderBean.getData().getSubMerchantName(), orderBean.getData().getAmount(),
                        new InputPasswordWindow.OnInputFinishCallBack() {
                            @Override
                            public void onFinish(String psw, String cardId) {
                                showDialog();
                                verifyPsw(orderBean.getData().getAmount(), psw, cardId);
                            }
                        });
                inputPasswordWindow.setAlignBackground(true);
                inputPasswordWindow.setPopupGravity(Gravity.BOTTOM);
                if (!inputPasswordWindow.isShowing()) {
                    inputPasswordWindow.showPopupWindow();
                }
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
                            payagain(orderBean.getData().getSubMerchantName(),
                                    orderBean.getData().getOrderSn(), amount, cardId);
                        } else {
                            ToastUtil.toastShortMessage(bean.getReturn_msg());
                            dismissDialog();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        dismissDialog();
                    }
                });


    }


    private void queryOrder(String order_sn) {
        showDialog();
        HttpParams param = new HttpParams();
        param.put("institution_number", INSTITUTION_NUMBER);
        param.put("order_sn", order_sn);
        param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        OkGo.<String>post(ServerHostConfig.GATEWAY_CODEPAYQUERY).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        OrderBean bean = new Gson().fromJson(response.body(), OrderBean.class);
                        if (RequestCode.SUCCESS.equals(bean.getStatus())) {
                            orderBean = bean;
                            payToTv.setText(bean.getData().getSubMerchantName());
                            amountTv.setText(getResources().getString(R.string.transfer_unit) +
                                    " " + bean.getData().getAmount());
                        } else {
                            ToastUtil.toastShortMessage(bean.getReturn_msg());
                        }
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        dismissDialog();
                    }
                });
    }

    private void payagain(final String sub_name, String order_sn, final String amount, String card_id) {
        String user_openid = (String) SPUtils.get(getApplicationContext(), Constants.N_OPENID, "");
        HttpParams param = new HttpParams();
        param.put("user_openid", user_openid);
        param.put("order_sn", order_sn);
        String gps = x_Location + "," + y_Location;
        if (TextUtils.isEmpty(x_Location) || TextUtils.isEmpty(y_Location)) {
            gps = "6.5167863,3.3869949";
        }

        param.put("user_gps", gps);
        param.put("order_type", "4");
        param.put("card_id", card_id);
        param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        OkGo.<String>post(ServerHostConfig.GATEWAY_PAYAGAIN).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        BaseBean bean = new Gson().fromJson(response.body(), BaseBean.class);
                        if (RequestCode.SUCCESS.equals(bean.getStatus())) {
                            dismissDialog();
                            Intent it = new Intent(ScanPayActivity.this, PayResultActivity.class);
                            it.putExtra(PayResultActivity.TRANSFOR_NAME, sub_name);
                            it.putExtra(PayResultActivity.TRANSFOR_AMOUNT, amount);
                            it.putExtra(PayResultActivity.TRANSFOR_RESULT, true);
                            startActivity(it);
                            finish();
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
    }
}