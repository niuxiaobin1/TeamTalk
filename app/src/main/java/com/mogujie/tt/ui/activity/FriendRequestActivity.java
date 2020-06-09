package com.mogujie.tt.ui.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class FriendRequestActivity extends TTBaseActivity {

    private RecyclerView selectList;

    private MySelectAdapter mySelectAdapter;

    private ArrayList<String> data;
    private String key = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_friend_request, topContentView);

        selectList = findViewById(R.id.selectList);

        setTitle(getResources().getString(R.string.chat_friedn_request));
        setRightText(getResources().getString(R.string.cancel),null);
        setLeftButton(R.mipmap.ic_back_black);

        selectList.setLayoutManager(new LinearLayoutManager(this));

        data = new ArrayList<>();
        data.add("Allow Anyone");
        data.add("Deny Anyone");
        data.add("Need Request");

        mySelectAdapter = new MySelectAdapter(data, 0);
        selectList.setAdapter(mySelectAdapter);
    }

    class MySelectAdapter extends RecyclerView.Adapter<MySelectHolder> {

        private List<String> list = new ArrayList<>();
        private int curSelectP = 0;

        public MySelectAdapter(List<String> mData, int curSelectP) {
            this.list.addAll(mData);
            this.curSelectP = curSelectP;
        }

        public void screenData() {
            list.clear();
            if (TextUtils.isEmpty(key)) {
                list.addAll(data);
            } else {
                for (int i = 0; i < data.size(); i++) {
                    if (!TextUtils.isEmpty(data.get(i)) && data.get(i).contains(key)) {
                        list.add(data.get(i));
                    }
                }
            }
            notifyDataSetChanged();
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
