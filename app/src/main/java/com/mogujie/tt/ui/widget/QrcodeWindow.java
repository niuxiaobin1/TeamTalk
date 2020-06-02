package com.mogujie.tt.ui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.mogujie.tt.R;
import com.mogujie.tt.utils.CommonUtils;

import razerdp.basepopup.BasePopupWindow;

public class QrcodeWindow extends BasePopupWindow implements View.OnClickListener {

    private ImageView imageView;
    private ImageView qrcode;
    private ImageView closeImg;
    private TextView nameTv;
    private TextView idTv;

    public QrcodeWindow(Context context) {
        super(context);

        imageView = findViewById(R.id.imageView);
        qrcode = findViewById(R.id.qrcode);
        nameTv = findViewById(R.id.nameTv);
        idTv = findViewById(R.id.idTv);
        closeImg = findViewById(R.id.closeImg);


//        if (profile.getGender() == 0) {
//            nameTv.setCompoundDrawablesWithIntrinsicBounds(null, null,
//                    getContext().getResources().getDrawable(R.drawable.ic_male), null);
//        } else {
//            nameTv.setCompoundDrawablesWithIntrinsicBounds(null, null,
//                    getContext().getResources().getDrawable(R.drawable.ic_felame), null);
//        }

//        int width = ScreenUtil.getScreenWidth(getContext());
//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) qrcode.getLayoutParams();
//        params.width = width * 3 / 5;
//        params.height = width * 3 / 5;
//        qrcode.setLayoutParams(params);
//        qrcode.setImageBitmap(CommonUtils.createQRImage(
//                GeneralConfig.FALG_NCAHT_ADD_FRIEND + profile.getIdentifier(),
//                1000, 1000));

        closeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public View onCreateContentView() {
        return createPopupById(R.layout.qrcode_popup);
    }
}
