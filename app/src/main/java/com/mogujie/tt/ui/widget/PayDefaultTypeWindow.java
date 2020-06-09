package com.mogujie.tt.ui.widget;

import android.content.Context;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mogujie.tt.R;

import razerdp.basepopup.BasePopupWindow;

public class PayDefaultTypeWindow extends BasePopupWindow implements View.OnClickListener {

    private OnAddClickListener onAddClickListener;
    private RecyclerView paymentMthodList;
    private ImageView closeImage;
    private TextView addNewCardTv;

    public PayDefaultTypeWindow(Context context, RecyclerView.Adapter adapter) {
        super(context);
        paymentMthodList = findViewById(R.id.paymentMthodList);
        closeImage = findViewById(R.id.closeImage);
        addNewCardTv = findViewById(R.id.addNewCardTv);
        paymentMthodList.setLayoutManager(new LinearLayoutManager(getContext()));
        paymentMthodList.setAdapter(adapter);

        closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        addNewCardTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onAddClickListener!=null){
                    onAddClickListener.onClick();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public View onCreateContentView() {
        return createPopupById(R.layout.payment_mthod_popup);
    }


    public interface OnAddClickListener{
        void onClick();
    }

    public void setOnAddClickListener(OnAddClickListener onAddClickListener){
        this.onAddClickListener=onAddClickListener;
    }
}
