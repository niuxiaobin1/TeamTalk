package com.mogujie.tt.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mogujie.tt.R;

import razerdp.basepopup.BasePopupWindow;

public class SimpleListWindow extends BasePopupWindow  {

    private RecyclerView contentRecycler;
    private View topView;
    private View bottomView;
    private String[] items ;
    private OnItemClickListener onItemClickListener;

    public SimpleListWindow(Context context, String[] items, OnItemClickListener onItemClickListener) {
        super(context);
        if (items == null) {
            items = new String[]{};
        }
        this.items = items;
        this.onItemClickListener = onItemClickListener;
        contentRecycler = findViewById(R.id.contentRecycler);
        bottomView = findViewById(R.id.bottomView);
        topView = findViewById(R.id.topView);
        contentRecycler.setLayoutManager(new LinearLayoutManager(context));
//        contentRecycler.addItemDecoration(new DividerItemDecoration(context, VERTICAL_LIST, 1,
//                R.color.text_tips_color));
        contentRecycler.setAdapter(new MyAdapter());
        topView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        bottomView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }


    @Override
    public View onCreateContentView() {
        return createPopupById(R.layout.list_popup);
    }

    class MyAdapter extends RecyclerView.Adapter<MyHolder> {

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.option_item, parent, false);
            return new MyHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, int position) {
            holder.textView.setText(items[position]);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(position);
                    }
                    dismiss();
                }
            });
            if (position==items.length-1){
                holder.lineView.setVisibility(View.GONE);
            }else{
                holder.lineView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return items.length;
        }
    }

    class MyHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private View lineView;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            lineView = itemView.findViewById(R.id.lineView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    @Override
    public boolean onOutSideTouch() {
        return true;
    }
}
