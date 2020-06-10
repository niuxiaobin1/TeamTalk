package com.mogujie.tt.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

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
import com.mogujie.tt.ui.widget.LineControllerView;
import com.mogujie.tt.utils.SPUtils;
import com.mogujie.tt.utils.ToastUtil;

import static com.mogujie.tt.config.GeneralConfig.INSTITUTION_NUMBER;

public class ManageCardInfoActivity extends TTBaseActivity {

    public  static  final String ISDEFAULTPAY="_isDefaultPayCard";
    public  static  final String MANAGERCARD_ID="_manager_card_id";

    private LineControllerView setDefaultPayCard;
    private Button delete_btn;
    private String isDefault="";
    private String cardId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_message_card_info, topContentView);
        setDefaultPayCard=findViewById(R.id.setDefaultPayCard);
        delete_btn=findViewById(R.id.delete_btn);
        setTitle(getResources().getString(R.string.card_manager));
        setLeftButton(R.mipmap.ic_back_black);

        isDefault=getIntent().getStringExtra(ISDEFAULTPAY);
        cardId=getIntent().getStringExtra(MANAGERCARD_ID);


        if ("1".equals(isDefault)){
            setDefaultPayCard.setChecked(true);
        }else{
            setDefaultPayCard.setChecked(false);
        }

        setDefaultPayCard.setCheckListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    isDefault="1";
                    changeDefaultPayCard();
                }else{
                    setDefaultPayCard.setChecked(true);
                    isDefault="1";
                }
            }
        });

        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCard();
            }
        });

    }


    private void changeDefaultPayCard(){
        String user_openid = (String) SPUtils.get(getApplicationContext(), Constants.N_OPENID, "");
        HttpParams param = new HttpParams();
        param.put("institution_number", INSTITUTION_NUMBER);
        param.put("user_openid", user_openid);
        param.put("card_id", cardId);
        param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        OkGo.<String>post(ServerHostConfig.CUSTOMER_BANK_CARD_DEFAULT).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        BaseBean bean = new Gson().fromJson(response.body(), BaseBean.class);
                        if (RequestCode.SUCCESS.equals(bean.getStatus())) {

                        } else {
                            ToastUtil.toastShortMessage(bean.getReturn_msg());
                        }
                    }
                });
    }

    private void deleteCard(){
        String user_openid = (String) SPUtils.get(getApplicationContext(), Constants.N_OPENID, "");
        HttpParams param = new HttpParams();
        param.put("institution_number", INSTITUTION_NUMBER);
        param.put("user_openid", user_openid);
        param.put("card_id", cardId);
        param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        OkGo.<String>post(ServerHostConfig.CUSTOMER_BANK_CARD_DELETE).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        BaseBean bean = new Gson().fromJson(response.body(), BaseBean.class);
                        if (RequestCode.SUCCESS.equals(bean.getStatus())) {
                            Intent it=new Intent();
                            it.putExtra("isDelete","1");
                            it.putExtra("isDefault",isDefault);
                            it.putExtra(MANAGERCARD_ID,cardId);
                            setResult(RESULT_OK,it);
                            finish();
                        } else {
                            ToastUtil.toastShortMessage(bean.getReturn_msg());
                        }
                    }
                });
    }

    @Override
    public void finish() {

        Intent it=new Intent();
        it.putExtra("isDelete","0");
        it.putExtra("isDefault",isDefault);
        it.putExtra(MANAGERCARD_ID,cardId);
        setResult(RESULT_OK,it);

        super.finish();
    }

}
