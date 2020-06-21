package com.mogujie.tt.ui.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.OkgoCallBack.NigeriaCallBack;
import com.mogujie.tt.R;
import com.mogujie.tt.bean.BaseBean;
import com.mogujie.tt.config.Constants;
import com.mogujie.tt.config.RequestCode;
import com.mogujie.tt.config.ServerHostConfig;
import com.mogujie.tt.imservice.entity.RedPacketMessage;
import com.mogujie.tt.imservice.manager.IMContactManager;
import com.mogujie.tt.ui.activity.OpenRedPacketResultActivity;
import com.mogujie.tt.utils.ImageLoaderUtil;
import com.mogujie.tt.utils.ToastUtil;
import com.nostra13.universalimageloader.core.ImageLoader;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import razerdp.basepopup.BasePopupWindow;

public class RedPacketWindow extends BasePopupWindow implements View.OnClickListener {

    private OnOpenReadPacketListener onOpenReadPacketListener;
    private IMBaseImageView formUserImage;
    private TextView openView;
    private TextView formUserName;
    private TextView resultTv;
    private TextView bestWishTv;
    private ImageView closeImage;
    private RedPacketMessage redPacketMessage;
    private UserEntity userEntity;
    private String amount;
    private JSONObject jsonObject;
    private ObjectAnimator animator;

    public RedPacketWindow(final Context context, RedPacketMessage redPacketMessage) {
        super(context);
        this.redPacketMessage = redPacketMessage;
        userEntity = IMContactManager.instance().findContact(redPacketMessage.getFromId());
        try {
            this.jsonObject = new JSONObject(redPacketMessage.getContent());
            this.amount = jsonObject.getString("amount");
        } catch (JSONException e) {
        }
        formUserImage = findViewById(R.id.formUserImage);
        closeImage = findViewById(R.id.closeImage);
        openView = findViewById(R.id.openView);
        formUserName = findViewById(R.id.formUserName);
        resultTv = findViewById(R.id.resultTv);
        bestWishTv = findViewById(R.id.bestWishTv);
        closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        formUserName.setText(String.format(context.getString(R.string.red_packet_from),
                userEntity.getMainName()));
        formUserImage.setImageUrl(userEntity.getAvatar());

        openView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startAnim();
                try {
                    openPacket();
                } catch (JSONException e) {
                    Log.e("nxb", e.toString());
                }

            }
        });

    }

    private void startAnim() {
        animator = ObjectAnimator.ofFloat(openView, "rotationY", 0, 360);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(2000);
        animator.start();
    }

    private void stopAnim() {
        if (animator != null) {
            animator.pause();
            openView.setRotationY(0);
            animator = null;
        }
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public View onCreateContentView() {
        return createPopupById(R.layout.red_packet_popup);
    }


    public interface OnOpenReadPacketListener {
        void onOpen();
    }

    public void setOnOpenReadPacketListener(OnOpenReadPacketListener onOpenReadPacketListener) {
        this.onOpenReadPacketListener = onOpenReadPacketListener;
    }


    private void openPacket() throws JSONException {
        JSONObject resultJson = new JSONObject(jsonObject.getString("result"));
        HttpParams param = new HttpParams();
        param.put("institution_number", jsonObject.getString("institution_number"));
        param.put("session_id", resultJson.getString("SessionID"));
        param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        OkGo.<String>post(ServerHostConfig.CUSTOMER_UPDATE_RECEIVE).tag(this)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        stopAnim();
                        BaseBean bean = new Gson().fromJson(response.body(), BaseBean.class);
                        if (RequestCode.SUCCESS.equals(bean.getStatus())) {
                            if (onOpenReadPacketListener != null) {
                                onOpenReadPacketListener.onOpen();
                            }
                            skipResultActivity();
                        } else {
                            ToastUtil.toastShortMessage(bean.getReturn_msg());
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        stopAnim();
                    }
                });
    }


    private void skipResultActivity() {
        Intent it = new Intent(getContext(), OpenRedPacketResultActivity.class);
        it.putExtra(Constants.CHAT_INFO, redPacketMessage.getFromId());
        it.putExtra(Constants.REDPACKET_INFO, jsonObject.toString());
        getContext().startActivity(it);
        dismiss();
    }

}
