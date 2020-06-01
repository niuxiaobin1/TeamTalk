package com.mogujie.tt.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.mogujie.tt.R;


public class QRCodeDialog extends Dialog {

    ImageView mImgClose;

    public QRCodeDialog(Context context) {
        super(context, R.style.MyDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_code_dialog);

        mImgClose = findViewById(R.id.img_close);
        mImgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);
    }
}
