package com.mogujie.tt.ui.widget;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import com.mogujie.tt.R;

import razerdp.basepopup.BasePopupWindow;

public class UpdatePopupWindow extends BasePopupWindow {

    private RelativeLayout root;
    private ImageView updateImage;

    public UpdatePopupWindow(Context context, OnUpdateListener onUpdateListener) {
        super(context);
        root=findViewById(R.id.root);
        updateImage=findViewById(R.id.updateImage);
        updateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onUpdateListener!=null){
                    onUpdateListener.update();
                }
                dismiss();
            }
        });
    }

    @Override
    public View onCreateContentView() {
        return createPopupById(R.layout.update_popup_window);
    }

    public interface OnUpdateListener{
        void update();
    }
}
