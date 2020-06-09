package com.mogujie.tt.ui.activity;

import android.graphics.Typeface;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mogujie.tt.R;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.utils.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.mogujie.tt.utils.DividerItemDecoration.VERTICAL_LIST;

public class SelectBankActivity extends TTBaseActivity {

    private RecyclerView selectList;
    private MySelectAdapter mySelectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_select_bank, topContentView);

        setTitle(getResources().getString(R.string.result_card_hint));
        setLeftButton(R.mipmap.ic_back_black);
        setRightText(getResources().getString(R.string.cancel), null);

        selectList = findViewById(R.id.selectList);

        selectList.setLayoutManager(new LinearLayoutManager(this));
        selectList.addItemDecoration(new DividerItemDecoration(this, VERTICAL_LIST, 1,
                R.color.text_tips_color));

        initData();

    }

    private void initData() {
        List<String> list = new ArrayList<>();
        list.add("Sterling Bank");
        list.add("Sterling Bank");
        list.add("Sterling Bank");
        list.add("Sterling Bank");
        list.add("Sterling Bank");
        list.add("Sterling Bank");
        list.add("Sterling Bank");
        list.add("Sterling Bank");
        list.add("Sterling Bank");
        list.add("Sterling Bank");
        list.add("Sterling Bank");
        list.add("Sterling Bank");
        list.add("Sterling Bank");
        list.add("Sterling Bank");
        list.add("Sterling Bank");
        list.add("Sterling Bank");
        list.add("Sterling Bank");

        mySelectAdapter = new MySelectAdapter(list, 0);
        selectList.setAdapter(mySelectAdapter);
    }


    class MySelectAdapter extends RecyclerView.Adapter<MySelectHolder> {

        private List<String> list = new ArrayList<>();
        private int curSelectP = 0;

        public MySelectAdapter(List<String> mData, int curSelectP) {
            this.list.addAll(mData);
            this.curSelectP = curSelectP;
        }

        public int getCurSelectP() {
            return curSelectP;
        }

        public String getCurSelectName() {
            return list.get(curSelectP);
        }

        @NonNull
        @Override
        public MySelectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_item, parent, false);
            return new MySelectHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MySelectHolder holder, final int position) {

            holder.select_content.setText(list.get(position));
            if (position == curSelectP) {
                holder.select_content.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                holder.select_check.setVisibility(VISIBLE);
            } else {
                holder.select_content.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                holder.select_check.setVisibility(GONE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    curSelectP = position;
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    class MySelectHolder extends RecyclerView.ViewHolder {

        private TextView select_content;
        private ImageView select_check;

        public MySelectHolder(View itemView) {
            super(itemView);
            select_content = itemView.findViewById(R.id.select_content);
            select_check = itemView.findViewById(R.id.select_check);
        }
    }

}

