package com.mogujie.tt.ui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.mogujie.tt.OkgoCallBack.NigeriaCallBack;
import com.mogujie.tt.R;
import com.mogujie.tt.bean.CardInfo;
import com.mogujie.tt.bean.CardListBean;
import com.mogujie.tt.config.Constants;
import com.mogujie.tt.config.RequestCode;
import com.mogujie.tt.config.ServerHostConfig;
import com.mogujie.tt.utils.SPUtils;
import com.mogujie.tt.utils.ToastUtil;


import java.util.ArrayList;
import java.util.List;

import razerdp.basepopup.BasePopupWindow;

import static com.mogujie.tt.config.GeneralConfig.INSTITUTION_NUMBER;


public class InputPasswordWindow extends BasePopupWindow implements View.OnClickListener {

    private static final String[] KEY = new String[]{
            "1", "2", "3",
            "4", "5", "6",
            "7", "8", "9",
            "", "0", ""
    };

    private List<CardInfo> cardInfos = new ArrayList<>();

    private Keyboard Keyboard_pay;
    private ImageView closeImage;
    private TextView payToTv;
    private TextView cardNoTv;
    private TextView amountTv;
    private LinearLayout payMethodLayout;
    private PayEditText PayEditText_pay;

    private String payCardId;

    public InputPasswordWindow(Context context, String name, String amount, final OnInputFinishCallBack onInputFinishCallBack) {
        super(context);
        Keyboard_pay = findViewById(R.id.Keyboard_pay);
        closeImage = findViewById(R.id.closeImage);
        payToTv = findViewById(R.id.payToTv);
        amountTv = findViewById(R.id.amountTv);
        payMethodLayout = findViewById(R.id.payMethodLayout);
        PayEditText_pay = findViewById(R.id.PayEditText_pay);
        cardNoTv = findViewById(R.id.cardNoTv);

        payToTv.setText(String.format(getContext().getString(R.string.pay_to), name));
        amountTv.setText(getContext().getResources().getString(R.string.transfer_unit)
                + " " + amount);

        Keyboard_pay.setKeyboardKeys(KEY);


        //键盘键的点击事件
        Keyboard_pay.setOnClickKeyboardListener(new Keyboard.OnClickKeyboardListener() {
            @Override
            public void onKeyClick(int position, String value) {
                if (position < 11 && position != 9) {
                    PayEditText_pay.add(value);
                } else if (position == 11) {
                    PayEditText_pay.remove();
                } else if (position == 9) {
                    //当点击d键盘上的完成时候，也可以通过payEditText.getText()获取密码，此时不应该注册OnInputFinishedListener接口
                    done(onInputFinishCallBack);
                }

            }
        });
        PayEditText_pay.setOnInputFinishedListener(new PayEditText.OnInputFinishedListener() {
            @Override
            public void onInputFinished(String password) {
                done(onInputFinishCallBack);
            }
        });

        closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        getCardsList();
    }

    private void done(OnInputFinishCallBack onInputFinishCallBack){
        if (TextUtils.isEmpty(payCardId)) {
            return;
        }
        if (TextUtils.isEmpty(PayEditText_pay.getText().trim().trim())) {
            return;
        }
        if (PayEditText_pay.getText().trim().trim().length() != 6) {
            return;
        }
        onInputFinishCallBack.onFinish(PayEditText_pay.getText().trim().trim(), payCardId);
        dismiss();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public View onCreateContentView() {
        return createPopupById(R.layout.input_pay_password_layout);
    }

    private void getCardsList() {
        String user_openid = (String) SPUtils.get(getContext(), Constants.N_OPENID, "");
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
                            for (int i = 0; i < cardInfos.size(); i++) {
                                if ("1".equals(cardInfos.get(i).getCard_default())) {
                                    payCardId = cardInfos.get(i).getCard_id();
                                    final String cardN = cardInfos.get(i).getCard_account_number();
                                    cardNoTv.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            String cardName = "";
                                            String num = cardN;
                                            if (num.length() > 4) {
                                                cardName += num.substring(num.length() - 4);
                                            } else {
                                                cardName += num;
                                            }
                                            cardNoTv.setText(cardName);
                                        }
                                    });
                                    break;
                                }
                            }
                        } else {
                            ToastUtil.toastShortMessage(bean.getReturn_msg());
                        }
                    }
                });
    }


    public interface OnInputFinishCallBack {

        void onFinish(String psw, String cardId);
    }
}
