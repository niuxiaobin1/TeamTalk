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

import com.mogujie.tt.R;
import com.mogujie.tt.dto.CardListDto;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.utils.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class MessageCardActivity extends TTBaseActivity {

    private RecyclerView cardsList;
    private MyAdapter myAdapter;

    private List<CardListDto.Data.ListInfoBean> cardInfos = new ArrayList<>();

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
                Intent intent = new Intent(MessageCardActivity.this, AddNewCardActivity.class);
                startActivity(intent);
            }
        });

        cardsList.setLayoutManager(new LinearLayoutManager(this));
        cardsList.addItemDecoration(new DividerItemDecoration(this, VERTICAL_LIST, 20,
                R.color.color_f6));
        myAdapter = new MyAdapter();
        cardsList.setAdapter(myAdapter);


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


            holder.cardsName.setText(cardInfos.get(position).cardBankName);

            if (!TextUtils.isEmpty(cardInfos.get(position).cardAccountNumber)) {
                String num = cardInfos.get(position).cardAccountNumber;
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
                    Intent it = new Intent(MessageCardActivity.this, MessageCardInfoActivity.class);
                    startActivity(it);
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


}
