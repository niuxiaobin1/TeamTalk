package com.mogujie.tt.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.mogujie.tt.bean.CardInfo;
import com.mogujie.tt.bean.CardListBean;
import com.mogujie.tt.config.Constants;
import com.mogujie.tt.config.RequestCode;
import com.mogujie.tt.config.ServerHostConfig;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.utils.DividerItemDecoration;
import com.mogujie.tt.utils.SPUtils;
import com.mogujie.tt.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import static com.mogujie.tt.config.GeneralConfig.INSTITUTION_NUMBER;

public class BankCardListActivity extends TTBaseActivity {

    private RecyclerView cardsList;
    private MyAdapter myAdapter;
    private List<CardInfo> cardInfos = new ArrayList<>();
    private static final int REQUEST_CODE = 0x22;
    private static final int MANAGER_REQUEST_CODE = 0x33;

    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_message_card, topContentView);

        cardsList = findViewById(R.id.cardsList);

        setTitle(getResources().getString(R.string.card_manager));
        setLeftButton(R.mipmap.ic_back_black);
        setRightText(getResources().getString(R.string.add), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BankCardListActivity.this, AddNewCardActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        cardsList.setLayoutManager(new LinearLayoutManager(this));
        cardsList.addItemDecoration(new DividerItemDecoration(this, VERTICAL_LIST, 20,
                R.color.color_f6));
        myAdapter = new MyAdapter();
        cardsList.setAdapter(myAdapter);

        getCardsList();
    }




    class MyAdapter extends RecyclerView.Adapter<MyHolder> {

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cards_item, parent, false);
            return new MyHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, final int position) {


            holder.cardsName.setText(cardInfos.get(position).getCard_bank_name());

            if (!TextUtils.isEmpty(cardInfos.get(position).getCard_account_number())) {
                String num = cardInfos.get(position).getCard_account_number();
                if (num.length() > 4) {
                    holder.cardsNo.setText(
                            num.substring(num.length() - 4));
                } else {
                    holder.cardsNo.setText(num);
                }
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent it = new Intent(BankCardListActivity.this, ManageCardInfoActivity.class);
                    it.putExtra(ManageCardInfoActivity.ISDEFAULTPAY, cardInfos.get(position).getCard_default());
                    it.putExtra(ManageCardInfoActivity.MANAGERCARD_ID, cardInfos.get(position).getCard_id());
                    startActivityForResult(it, MANAGER_REQUEST_CODE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return cardInfos.size();
        }
    }

    class MyHolder extends RecyclerView.ViewHolder {

        private TextView cardsName;
        private TextView cardsType;
        private TextView cardsNo;

        public MyHolder(View itemView) {
            super(itemView);
            cardsName = itemView.findViewById(R.id.cardsName);
            cardsNo = itemView.findViewById(R.id.cardsNo);
            cardsType = itemView.findViewById(R.id.cardsType);
        }
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
                            if (myAdapter != null) {
                                myAdapter.notifyDataSetChanged();
                            }
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
                if (cardInfos.size() == 1) {
                    myAdapter.notifyDataSetChanged();
                } else {
                    myAdapter.notifyItemInserted(cardInfos.size() - 1);
                }
                String is_pay_password = data.getStringExtra("is_pay_password");
                if (!"1".equals(is_pay_password)) {
                    Intent it = new Intent(BankCardListActivity.this, SetPINActivity.class);
                    startActivity(it);
                }


            } else if (requestCode == MANAGER_REQUEST_CODE) {
                String isDelete = data.getStringExtra("isDelete");
                String id = data.getStringExtra(ManageCardInfoActivity.MANAGERCARD_ID);
                String isDefault = data.getStringExtra("isDefault");
                if ("1".equals(isDelete)) {
                    for (int i = 0; i < cardInfos.size(); i++) {
                        if (id.equals(cardInfos.get(i).getCard_id())) {
                            cardInfos.remove(i);
                            myAdapter.notifyItemRemoved(i);
                        }
                    }
                } else {
                    if (isDefault.equals("1")) {
                        for (int i = 0; i < cardInfos.size(); i++) {
                            if (cardInfos.get(i).getCard_id().equals(id)) {
                                cardInfos.get(i).setCard_default("1");
                            } else {
                                cardInfos.get(i).setCard_default("0");
                            }
                        }
                    }

                }

            }
        }
    }

}
