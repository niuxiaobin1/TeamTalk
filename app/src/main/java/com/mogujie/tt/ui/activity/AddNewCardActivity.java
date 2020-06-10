package com.mogujie.tt.ui.activity;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.mogujie.tt.OkgoCallBack.NigeriaCallBack;
import com.mogujie.tt.R;
import com.mogujie.tt.bean.AddCardBean;
import com.mogujie.tt.bean.BankBean;
import com.mogujie.tt.bean.BankInfo;
import com.mogujie.tt.bean.CardInfo;
import com.mogujie.tt.config.Constants;
import com.mogujie.tt.config.RequestCode;
import com.mogujie.tt.config.ServerHostConfig;
import com.mogujie.tt.config.TUIKitConstants;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.utils.SPUtils;
import com.mogujie.tt.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import static com.mogujie.tt.config.GeneralConfig.INSTITUTION_NUMBER;


public class AddNewCardActivity extends TTBaseActivity {

    private LinearLayout bankNameLayout;
    private TextView bankNameTv;

    private LinearLayout bankNoLayout;
    private TextView bankNoTv;

    private EditText cardHolderEt;
    private EditText cardNoEt;
    private Button confirm_btn;

    private String mBankName = "";
    private String mBankNo = "";

    private List<BankInfo> bankInfos = new ArrayList<>();
    private ArrayList<String> bankNameList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_add_new_card, topContentView);

        setTitle(getResources().getString(R.string.app_addCardTitle));
        setLeftButton(R.mipmap.ic_back_black);

        bankNameLayout = findViewById(R.id.bankNameLayout);
        bankNameTv = findViewById(R.id.bankNameTv);
        bankNoLayout = findViewById(R.id.bankNoLayout);
        bankNoTv = findViewById(R.id.bankNoTv);
        cardHolderEt = findViewById(R.id.cardHolderEt);
        cardNoEt = findViewById(R.id.cardNoEt);
        confirm_btn = findViewById(R.id.confirm_btn);

        bankNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (bankNameList.size() == 0) {
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString(TUIKitConstants.Selection.TITLE, getResources().getString(R.string.add_card_select_bank));
                bundle.putBoolean(TUIKitConstants.Selection.CAN_SEARCH, true);
                bundle.putStringArrayList(TUIKitConstants.Selection.LIST, bankNameList);
                bundle.putInt(TUIKitConstants.Selection.DEFAULT_SELECT_ITEM_INDEX, 0);
                SelectionActivity.startListSelection(AddNewCardActivity.this, bundle, new SelectionActivity.OnResultReturnListener1() {
                    @Override
                    public void onReturn(String text) {
                        mBankName = text;
                        for (int i = 0; i <bankInfos.size() ; i++) {
                            if (text.equals(bankInfos.get(i).getBank_name())){
                                mBankNo = bankInfos.get(i).getBank_no();
                                break;
                            }
                        }
                        bankNameTv.setText(mBankName);
                        bankNoTv.setText(mBankNo);
                    }
                });
            }
        });

        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cardName = cardHolderEt.getText().toString().trim();
                String cardNo = cardNoEt.getText().toString().trim();
                if (TextUtils.isEmpty(cardName) || TextUtils.isEmpty(cardNo)
                        || TextUtils.isEmpty(mBankName) || TextUtils.isEmpty(mBankNo)) {
                    return;
                }
                addCard(cardName, cardNo);

            }
        });

        getBankList();

    }


    private void addCard(final String cardName, final String cardNo) {

        String user_openid = (String) SPUtils.get(getApplicationContext(), Constants.N_OPENID, "");
        HttpParams param = new HttpParams();
        param.put("institution_number", INSTITUTION_NUMBER);
        param.put("user_openid", user_openid);
        param.put("card_bank_no", mBankNo);
        param.put("card_bank_name", mBankName);
        param.put("card_account_name", cardName);
        param.put("card_account_number", cardNo);
        param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        OkGo.<String>post(ServerHostConfig.CUSTOMER_BANK_CARD_ADD).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        AddCardBean bean = new Gson().fromJson(response.body(), AddCardBean.class);
                        if (RequestCode.SUCCESS.equals(bean.getStatus())) {
                            CardInfo info = new CardInfo(bean.getData().getCard_id(),
                                    mBankNo, mBankName, cardName, cardNo, "0");
                            Intent it = new Intent();
                            it.putExtra("card", info);
                            it.putExtra("is_pay_password", bean.getData().is_pay_password());
                            setResult(Activity.RESULT_OK, it);
                            finish();
                        } else {
                            ToastUtil.toastShortMessage(bean.getReturn_msg());
                        }
                    }
                });
    }


    private void getBankList() {
        HttpParams param = new HttpParams();
        param.put("institution_number", INSTITUTION_NUMBER);
        param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        OkGo.<String>post(ServerHostConfig.CUSTOMER_BANK_LIST).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        BankBean bean = new Gson().fromJson(response.body(), BankBean.class);
                        if (RequestCode.SUCCESS.equals(bean.getStatus())) {
                            bankInfos = bean.getData().getList_info();
                            bankNameList.clear();
                            for (int i = 0; i < bankInfos.size(); i++) {
                                bankNameList.add(bankInfos.get(i).getBank_name());
                            }
                        } else {
                            ToastUtil.toastShortMessage(bean.getReturn_msg());
                        }
                    }
                });
    }
}