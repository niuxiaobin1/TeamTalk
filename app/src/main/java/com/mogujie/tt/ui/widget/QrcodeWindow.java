package com.mogujie.tt.ui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.R;
import com.mogujie.tt.config.GeneralConfig;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.utils.CommonUtils;

import razerdp.basepopup.BasePopupWindow;

public class QrcodeWindow extends BasePopupWindow implements View.OnClickListener {

    private IMBaseImageView imageView;
    private ImageView qrcode;
    private ImageView closeImg;
    private TextView nameTv;
    private TextView idTv;

    public QrcodeWindow(Context context, UserEntity userEntity) {
        super(context);

        imageView = findViewById(R.id.imageView);
        qrcode = findViewById(R.id.qrcode);
        nameTv = findViewById(R.id.nameTv);
        idTv = findViewById(R.id.idTv);
        closeImg = findViewById(R.id.closeImg);

        imageView.setDefaultImageRes(R.drawable.tt_round_bg);
        imageView.setCorner(8);
        imageView.setImageUrl(userEntity.getAvatar());
        idTv.setText(userEntity.getEmail());
        nameTv.setText(userEntity.getMainName());
        qrcode.setImageBitmap(CommonUtils.createQRImage(
                GeneralConfig.FALG_NCAHT_ADD_FRIEND + userEntity.getEmail(),
                1000, 1000));

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
