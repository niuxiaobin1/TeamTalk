package com.mogujie.tt.ui.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.imservice.entity.CardInfo;
import com.mogujie.tt.imservice.entity.PayOrderInfoData;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.widget.PayDefaultTypeWindow;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class NChatPayActivity extends TTBaseActivity {

    private RecyclerView mTransactionList;
    private MyAdapter myAdapter;
    private MyMethodAdapter myMethodAdapter;
    private LinearLayout mPaymentMethod;
    private PayDefaultTypeWindow payDefaultTypeWindow;

    private List<PayOrderInfoData> payOrderInfoDataArrayList = new ArrayList<>();
    private List<CardInfo> cardInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_n_chat_pay, topContentView);

        mTransactionList = findViewById(R.id.transactionList);
        mPaymentMethod = findViewById(R.id.payment_method_layout);

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

        initData();

    }

    private void initData() {
        PayOrderInfoData payOrderInfoData = new PayOrderInfoData();
        payOrderInfoData.setB_time("15:00, March 5");
        payOrderInfoData.setAmount("-16.2");
        payOrderInfoData.setM_name("Transfer to Tony");
        payOrderInfoData.setUser_name("Transfer to Tony");
        payOrderInfoData.setPayee_user_name("Transfer to Tony");
        payOrderInfoData.setOrder_sn("Transfer to Tony");
        payOrderInfoData.setB_type("Transfer to Tony");

        payOrderInfoDataArrayList.add(payOrderInfoData);
        payOrderInfoDataArrayList.add(payOrderInfoData);
        payOrderInfoDataArrayList.add(payOrderInfoData);

        myAdapter.notifyDataSetChanged();

        CardInfo cardInfo = new CardInfo();
        cardInfo.setCard_id("ICBC");
        cardInfo.setCard_bank_no("3344");
        cardInfo.setCard_bank_name("ICBC");
        cardInfo.setCard_account_name("ICBC");
        cardInfo.setCard_account_number("ICBC");
        cardInfo.setCard_default("ICBC");

        cardInfos.add(cardInfo);

        myMethodAdapter.notifyDataSetChanged();

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
            PayOrderInfoData data = payOrderInfoDataArrayList.get(position);
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
            return payOrderInfoDataArrayList.size();
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
            if (!TextUtils.isEmpty(info.getCard_account_number())) {
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

}
