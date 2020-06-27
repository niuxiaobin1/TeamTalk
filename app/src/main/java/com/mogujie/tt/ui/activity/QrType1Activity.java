package com.mogujie.tt.ui.activity;

import android.content.Intent;
import android.os.Bundle;
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
import com.mogujie.tt.bean.QrType0Data;
import com.mogujie.tt.config.Constants;
import com.mogujie.tt.config.RequestCode;
import com.mogujie.tt.config.ServerHostConfig;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.widget.InputPasswordWindow;
import com.mogujie.tt.utils.LocationUtils;
import com.mogujie.tt.utils.SPUtils;
import com.mogujie.tt.utils.SoftKeyBoardUtil;
import com.mogujie.tt.utils.ToastUtil;

import static com.mogujie.tt.app.IMApplication.INSTITUTION_NUMBER;

public class QrType1Activity extends TTBaseActivity {

    public static final String QR_TYPE_BEAN = "QR_TYPE_BEAN";
    private QrType0Data data;

    private TextView payToTv;
    private TextView amountTv;
    private Button btn_pay;
    private InputPasswordWindow inputPasswordWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_qr_type1, topContentView);

        payToTv = findViewById(R.id.payToTv);
        amountTv = findViewById(R.id.amountTv);
        btn_pay = findViewById(R.id.btn_pay);

        setTitle(getResources().getString(R.string.pay_confirm));
        setLeftButton(R.mipmap.ic_back_black);


        btn_pay.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           SoftKeyBoardUtil.hideKeyBoard(btn_pay.getWindowToken());
                                           if (data == null) {
                                               return;
                                           }

                                           try {
                                               float money = Float.parseFloat(data.getAmount());
                                               if (money <= 0f) {
                                                   ToastUtil.toastShortMessage("error amount ");
                                                   return;
                                               }
                                           } catch (NumberFormatException e) {
                                               ToastUtil.toastShortMessage(e.getMessage());
                                               return;
                                           }


                                           if (inputPasswordWindow != null) {
                                               inputPasswordWindow = null;
                                           }
                                           inputPasswordWindow = new InputPasswordWindow(QrType1Activity.this,
                                                   data.getSubMerchantName(), data.getAmount(),
                                                   new InputPasswordWindow.OnInputFinishCallBack() {
                                                       @Override
                                                       public void onFinish(String psw, String cardId) {
                                                           showDialog();
                                                           verifyPsw(data.getMerchantName(),
                                                                   data.getSubMerchantName(),
                                                                   data.getMch_no(),
                                                                   data.getSub_mch_no(),
                                                                   data.getAmount(), psw, cardId);
                                                       }
                                                   });
                                           inputPasswordWindow.setAlignBackground(true);
                                           inputPasswordWindow.setPopupGravity(Gravity.BOTTOM);
                                           if (!inputPasswordWindow.isShowing()) {
                                               inputPasswordWindow.showPopupWindow();
                                           }
                                       }
                                   }
        );


        data = (QrType0Data) getIntent().getSerializableExtra(QR_TYPE_BEAN);
        if (data == null) {
            finish();
        }
        setData();
    }

    private void setData() {
        payToTv.setText(data.getMerchantName() + "(" + data.getSubMerchantName() + ")");
        amountTv.setText(data.getAmount());
    }

    private void verifyPsw(final String m_name, final String sub_name, final String mch_no, final String sub_mch_no,
                           final String amount, final String psw, final String cardId) {
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
                            pay(m_name, sub_name, mch_no, sub_mch_no, amount, cardId);
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

    private void pay(String m_name, final String sub_name, String mch_no, String sub_mch_no,
                     final String amount, String cardId) {
        String user_openid = (String) SPUtils.get(getApplicationContext(), Constants.N_OPENID, "");
        HttpParams param = new HttpParams();
        param.put("institution_number", INSTITUTION_NUMBER);
        param.put("channel", "1");
        param.put("mch_no", mch_no);
        param.put("sub_mch_no", sub_mch_no);
        param.put("user_openid", user_openid);
        String gps = LocationUtils.getInstance().getLongitude() + "," + LocationUtils.getInstance().getLatitude();
        param.put("user_gps", gps);
        param.put("amount", amount);
        param.put("order_type", "1");
        param.put("card_id", cardId);
        param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        OkGo.<String>post(ServerHostConfig.GATEWAY_PAY).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        BaseBean bean = new Gson().fromJson(response.body(), BaseBean.class);
                        if (RequestCode.SUCCESS.equals(bean.getStatus())) {
                            dismissDialog();
                            Intent it = new Intent(QrType1Activity.this, PayResultActivity.class);
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

}