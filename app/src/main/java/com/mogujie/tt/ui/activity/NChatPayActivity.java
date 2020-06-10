package com.mogujie.tt.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.mogujie.tt.OkgoCallBack.NigeriaCallBack;
import com.mogujie.tt.R;
import com.mogujie.tt.bean.BaseBean;
import com.mogujie.tt.bean.CardInfo;
import com.mogujie.tt.bean.CardListBean;
import com.mogujie.tt.bean.PayCodeBean;
import com.mogujie.tt.bean.PayOrderInfoData1;
import com.mogujie.tt.bean.PayOrderListBean;
import com.mogujie.tt.bean.QueryAuthPayBean;
import com.mogujie.tt.bean.QueryPswBean;
import com.mogujie.tt.config.Constants;
import com.mogujie.tt.config.GeneralConfig;
import com.mogujie.tt.config.RequestCode;
import com.mogujie.tt.config.ServerHostConfig;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.widget.InputPasswordWindow;
import com.mogujie.tt.ui.widget.PayDefaultTypeWindow;
import com.mogujie.tt.utils.CommonUtils;
import com.mogujie.tt.utils.LocationUtils;
import com.mogujie.tt.utils.SPUtils;
import com.mogujie.tt.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.mogujie.tt.config.GeneralConfig.INSTITUTION_NUMBER;

public class NChatPayActivity extends TTBaseActivity {
    private LinearLayout noCardLayout;
    private Button btn_add;

    private RecyclerView mTransactionList;
    private MyAdapter myAdapter;
    private MyMethodAdapter myMethodAdapter;
    private LinearLayout mPaymentMethod;

    private LinearLayout payCodeLayout;
    private LinearLayout transactionLayout;
    private ImageView barCodeImage;
    private ImageView QrCodeImage;
    private TextView balanceTv;

    private PayDefaultTypeWindow payDefaultTypeWindow;
    private static final int REQUEST_CODE = 0x22;

    private List<PayOrderInfoData1> payOrderInfoData = new ArrayList<>();
    private List<CardInfo> cardInfos = new ArrayList<>();
    private String defaultId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_n_chat_pay, topContentView);

        mTransactionList = findViewById(R.id.transactionList);
        mPaymentMethod = findViewById(R.id.payment_method_layout);
        btn_add = findViewById(R.id.btn_add);
        noCardLayout = findViewById(R.id.noCardLayout);
        payCodeLayout = findViewById(R.id.payCodeLayout);
        transactionLayout = findViewById(R.id.transactionLayout);
        barCodeImage = findViewById(R.id.barCodeImage);
        QrCodeImage = findViewById(R.id.QrCodeImage);
        balanceTv = findViewById(R.id.balanceTv);


        setTitle(getResources().getString(R.string.app_pay));
        setLeftButton(R.mipmap.ic_back_black);
        setRightButton(R.mipmap.pay_detail_settings);

        topRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NChatPayActivity.this, PaySettingActivity.class);
                startActivity(intent);
            }
        });

        mPaymentMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupWindow();
            }
        });

        myMethodAdapter = new MyMethodAdapter();

        myAdapter = new MyAdapter();
        mTransactionList.setLayoutManager(new LinearLayoutManager(this));
        mTransactionList.setAdapter(myAdapter);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(NChatPayActivity.this, AddNewCardActivity.class);
                startActivityForResult(it, REQUEST_CODE);
            }
        });

        showDialog();
    }


    @Override
    protected void onResume() {
        super.onResume();
        getCardsList();
    }


    private void getCardsList() {
        String user_openid = (String) SPUtils.get(getApplicationContext(), Constants.N_OPENID, "");
        HttpParams param = new HttpParams();
        param.put("institution_number", INSTITUTION_NUMBER);
        param.put("user_openid", user_openid);
        param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        OkGo.<String>post(ServerHostConfig.CUSTOMER_BANK_CARD_LIST).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        CardListBean bean = new Gson().fromJson(response.body(), CardListBean.class);
                        if (RequestCode.SUCCESS.equals(bean.getStatus())) {
                            cardInfos = bean.getData().getList_info();
                            myMethodAdapter.notifyDataSetChanged();
                            for (int i = 0; i < cardInfos.size(); i++) {
                                if ("1".equals(cardInfos.get(i).getCard_default())) {
                                    getUserAuthCode(cardInfos.get(i));
                                    getOrderList();
                                    break;
                                }
                            }

                            if (cardInfos.size() == 0) {
                                dismissDialog();
                                noCardLayout.setVisibility(VISIBLE);
                            } else {
                                checkPayPsw();
                                noCardLayout.setVisibility(GONE);
                            }
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


    private void checkPayPsw(){

        String openId= (String) SPUtils.get(getApplicationContext(), Constants.N_OPENID, "");

        HttpParams param = new HttpParams();
        param.put("institution_number", INSTITUTION_NUMBER);
        param.put("user_openid", openId);
        param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        OkGo.<String>post(ServerHostConfig.CUSTOMER_QUERY_USER_PAY_PASSWORD).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        QueryPswBean bean = new Gson().fromJson(response.body(), QueryPswBean.class);
                        if (RequestCode.SUCCESS.equals(bean.getStatus())) {
                            if (!"1".equals(bean.getData().is_pay_password())){
                                Intent it=new Intent(NChatPayActivity.this,SetPINActivity.class);
                                startActivity(it);
                            }
                        } else {
                            ToastUtil.toastShortMessage(bean.getReturn_msg());
                        }
                    }
                });

    }


    private void getOrderList() {
        String user_openid = (String) SPUtils.get(getApplicationContext(), Constants.N_OPENID, "");
        HttpParams param = new HttpParams();
        param.put("institution_number", INSTITUTION_NUMBER);
        param.put("user_openid", user_openid);
        param.put("page", "1");
        param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        OkGo.<String>post(ServerHostConfig.CUSTOMER_USER_BILL_LIST).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        PayOrderListBean bean = new Gson().fromJson(response.body(), PayOrderListBean.class);
                        if (RequestCode.SUCCESS.equals(bean.getStatus())) {
                            payOrderInfoData = bean.getData().getList_info();
                            if (payOrderInfoData.size() == 0) {
                                transactionLayout.setVisibility(GONE);
                            } else {
                                transactionLayout.setVisibility(VISIBLE);
                            }
                            myAdapter.notifyDataSetChanged();
                        } else {
                            ToastUtil.toastShortMessage(bean.getReturn_msg());
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                CardInfo info = (CardInfo) data.getSerializableExtra("card");
                cardInfos.add(info);
                getUserAuthCode(info);
                getOrderList();
                String is_pay_password=data.getStringExtra("is_pay_password");
                if (!"1".equals(is_pay_password)){
                    Intent it=new Intent(NChatPayActivity.this,SetPINActivity.class);
                    startActivity(it);
                }
            }
        }
    }



    private void getUserAuthCode(CardInfo cardInfo) {
        defaultId = cardInfo.getCard_id();
        String cardName = cardInfo.getCard_bank_name();
        if (!TextUtils.isEmpty(cardInfo.getCard_account_number())) {
            cardName += "(";
            String num = cardInfo.getCard_account_number();
            if (num.length() > 4) {
                cardName += num.substring(num.length() - 4);
            } else {
                cardName += num;
            }
            cardName += ")";
        }
        balanceTv.setText(cardName);
        String user_openid = (String) SPUtils.get(getApplicationContext(), Constants.N_OPENID, "");
        HttpParams param = new HttpParams();
        param.put("institution_number", INSTITUTION_NUMBER);
        param.put("user_openid", user_openid);
        param.put("card_id", cardInfo.getCard_id());
        param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        OkGo.<String>post(ServerHostConfig.CUSTOMER_USER_AUTH_CODE).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        PayCodeBean bean = new Gson().fromJson(response.body(), PayCodeBean.class);
                        if (RequestCode.SUCCESS.equals(bean.getStatus())) {
                            dismissDialog();
                            noCardLayout.setVisibility(GONE);
                            payCodeLayout.setVisibility(VISIBLE);
                            transactionLayout.setVisibility(VISIBLE);
                            barCodeImage.setImageBitmap(CommonUtils.createBarImage(
                                    bean.getData().getAuth_code(), 500, 500
                            ));
                            int w = CommonUtils.getBarCodeNoPaddingWidth(
                                    500, bean.getData().getAuth_code(), 500
                            );
                            QrCodeImage.setImageBitmap(CommonUtils.createQRImage(
                                    GeneralConfig.FALG_NCAHT_PAY_CODE + bean.getData().getAuth_code(),
                                    w,
                                    w));
                            authCodeQuery(bean.getData().getAuth_code());
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


    private void verifyPsw(final String sub_name,final String order_sn,
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
                            payagain(sub_name,
                                    order_sn,amount,cardId);
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

    private void payagain(final String sub_name, String order_sn, final String amount, String card_id){
        String user_openid = (String) SPUtils.get(getApplicationContext(), Constants.N_OPENID, "");
        HttpParams param = new HttpParams();
        param.put("user_openid", user_openid);
        param.put("order_sn", order_sn);
        String gps = LocationUtils.getInstance().getLongitude() + "," + LocationUtils.getInstance().getLatitude();

        param.put("user_gps", gps);
        param.put("order_type", "2");
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
                            Intent it = new Intent(NChatPayActivity.this, PayResultActivity.class);
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

    private void authCodeQuery(final String auth_code){
        HttpParams param = new HttpParams();
        param.put("institution_number", INSTITUTION_NUMBER);
        param.put("auth_code", auth_code);
        param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        OkGo.<String>post(ServerHostConfig.GATEWAY_AUTHCODEQUERY).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        final QueryAuthPayBean bean = new Gson().fromJson(response.body(), QueryAuthPayBean.class);
                        if (RequestCode.SUCCESS.equals(bean.getStatus())) {
                            InputPasswordWindow inputPasswordWindow = new InputPasswordWindow(
                                    NChatPayActivity.this,
                                    bean.getData().getSub_name(),  bean.getData().getAmount(),
                                    new InputPasswordWindow.OnInputFinishCallBack() {
                                        @Override
                                        public void onFinish(String psw, String cardId) {
                                            showDialog();
                                            verifyPsw(bean.getData().getSub_name(),
                                                    bean.getData().getOrder_sn(),
                                                    bean.getData().getAmount(), psw, cardId);
                                        }
                                    });
                            inputPasswordWindow.setAlignBackground(true);
                            inputPasswordWindow.setPopupGravity(Gravity.BOTTOM);
                            if (!inputPasswordWindow.isShowing()) {
                                inputPasswordWindow.showPopupWindow();
                            }
                        } else {
                            authCodeQuery(auth_code);
                        }
                    }
                });
    }


    private void showPopupWindow() {
        if (payDefaultTypeWindow == null) {
            payDefaultTypeWindow = new PayDefaultTypeWindow(NChatPayActivity.this,
                    myMethodAdapter);
            payDefaultTypeWindow.setAlignBackground(true);
            payDefaultTypeWindow.setPopupGravity(Gravity.BOTTOM);
            payDefaultTypeWindow.setOnAddClickListener(new PayDefaultTypeWindow.OnAddClickListener() {
                @Override
                public void onClick() {
                    Intent it = new Intent(NChatPayActivity.this, AddNewCardActivity.class);
                    startActivity(it);
                    finish();
                }
            });
        }
        payDefaultTypeWindow.showPopupWindow();
    }

    class MyAdapter extends RecyclerView.Adapter<MyHolder> {

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
            return new MyHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, int position) {
            PayOrderInfoData1 data = payOrderInfoData.get(position);
            if ("3".equals(data.getB_type()) || "5".equals(data.getB_type())) {
                holder.transferInfo.setText(data.getPayee_user_name());
            } else {
                holder.transferInfo.setText(data.getM_name());
            }

            holder.transferTime.setText(data.getB_time());
            holder.transferCount.setText(data.getAmount());
        }

        @Override
        public int getItemCount() {
            return payOrderInfoData.size();
        }
    }

    class MyHolder extends RecyclerView.ViewHolder {
        private TextView transferInfo;
        private TextView transferTime;
        private TextView transferCount;

        public MyHolder(View itemView) {
            super(itemView);
            transferCount = itemView.findViewById(R.id.transferCount);
            transferTime = itemView.findViewById(R.id.transferTime);
            transferInfo = itemView.findViewById(R.id.transferInfo);
        }
    }

    class MyMethodAdapter extends RecyclerView.Adapter<MyHolder1> {

        private int selectP = -1;

        @NonNull
        @Override
        public MyHolder1 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_method_item, parent, false);
            return new MyHolder1(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder1 holder, final int position) {
            final CardInfo info = cardInfos.get(position);
            String cardName = info.getCard_bank_name();
            if (!TextUtils.isEmpty(info.getCard_account_name())) {
                cardName += "(";
                String num = info.getCard_account_number();
                if (num.length() > 4) {
                    cardName += num.substring(num.length() - 4);
                } else {
                    cardName += num;
                }
                cardName += ")";
            }
            holder.methodName.setText(cardName);
            if (selectP == -1) {
                if ("1".equals(info.getCard_default())) {
                    holder.imageCheck.setVisibility(VISIBLE);
                } else {
                    holder.imageCheck.setVisibility(GONE);
                }
            } else {
                if (position == selectP) {
                    holder.imageCheck.setVisibility(VISIBLE);
                } else {
                    holder.imageCheck.setVisibility(GONE);
                }
            }


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selectP != position) {
                        selectP = position;
                        notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return cardInfos.size();
        }
    }


    class MyHolder1 extends RecyclerView.ViewHolder {
        private TextView methodName;
        private ImageView imageCheck;

        public MyHolder1(View itemView) {
            super(itemView);
            methodName = itemView.findViewById(R.id.methodName);
            imageCheck = itemView.findViewById(R.id.imageCheck);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
