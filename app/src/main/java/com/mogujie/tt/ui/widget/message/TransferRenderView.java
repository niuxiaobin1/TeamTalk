package com.mogujie.tt.ui.widget.message;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.mogujie.tt.DB.entity.MessageEntity;
import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.R;
import com.mogujie.tt.imservice.entity.RedPacketMessage;
import com.mogujie.tt.imservice.entity.TransferMessage;
import com.mogujie.tt.imservice.manager.IMLoginManager;
import com.mogujie.tt.utils.AppContext;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author : yingmu on 15-1-9.
 * @email : yingmu@mogujie.com.
 * <p>
 * 样式根据mine 与other不同可以分成两个
 */
public class TransferRenderView extends BaseMsgRenderView {
    private TextView titleTv;
    private TextView amountTv;

    public static TransferRenderView inflater(Context context, ViewGroup viewGroup, boolean isMine) {
        int resource = isMine ? R.layout.tt_mine_transfer_message_item : R.layout.tt_other_transfer_message_item;

        TransferRenderView textRenderView = (TransferRenderView) LayoutInflater.from(context).inflate(resource, viewGroup, false);
        textRenderView.setMine(isMine);
        textRenderView.setParentView(viewGroup);
        return textRenderView;
    }

    public TransferRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        titleTv = findViewById(R.id.titleTv);
        amountTv = findViewById(R.id.amountTv);
    }


    /**
     * 控件赋值
     *
     * @param messageEntity
     * @param userEntity
     */
    @Override
    public void render(MessageEntity messageEntity, UserEntity userEntity, Context context) {
        super.render(messageEntity, userEntity, context);
        TransferMessage textMessage = (TransferMessage) messageEntity;
        // 按钮的长按也是上层设定的
        // url 路径可以设定 跳转哦哦
        String content = textMessage.getContent();
        if (messageEntity.isSend(IMLoginManager.instance().getLoginId())) {
            titleTv.setText(AppContext.getResources().getString(R.string.chat_transfer));
        } else {
            titleTv.setText(AppContext.getResources().getString(R.string.tansfer_title));
        }
        JSONObject jsonObject= null;
        try {
            jsonObject = new JSONObject(textMessage.getContent());
            amountTv.setText(
                    context.getResources().getString(R.string.transfer_unit) +
                            " " + jsonObject.get("amount")
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private static final String SCHEMA = "com.mogujie.tt://message_private_url";
    private static final String PARAM_UID = "uid";
    private String urlRegex = "((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?((?:(?:[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}\\.)+(?:(?:aero|arpa|asia|a[cdefgilmnoqrstuwxz])|(?:biz|b[abdefghijmnorstvwyz])|(?:cat|com|coop|c[acdfghiklmnoruvxyz])|d[ejkmoz]|(?:edu|e[cegrstu])|f[ijkmor]|(?:gov|g[abdefghilmnpqrstuwy])|h[kmnrtu]|(?:info|int|i[delmnoqrst])|(?:jobs|j[emop])|k[eghimnrwyz]|l[abcikrstuvy]|(?:mil|mobi|museum|m[acdghklmnopqrstuvwxyz])|(?:name|net|n[acefgilopruz])|(?:org|om)|(?:pro|p[aefghklmnrstwy])|qa|r[eouw]|s[abcdeghijklmnortuvyz]|(?:tel|travel|t[cdfghjklmnoprtvwz])|u[agkmsyz]|v[aceginu]|w[fs]|y[etu]|z[amw]))|(?:(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])))(?:\\:\\d{1,5})?)(\\/(?:(?:[a-zA-Z0-9\\;\\/\\?\\:\\@\\&\\=\\#\\~\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?(?:\\b|$)";



    @Override
    public void msgFailure(MessageEntity messageEntity) {
        super.msgFailure(messageEntity);
    }

    /**
     * ----------------set/get---------------------------------
     */

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }

    public ViewGroup getParentView() {
        return parentView;
    }

    public void setParentView(ViewGroup parentView) {
        this.parentView = parentView;
    }

    public TextView getTitleTv() {
        return titleTv;
    }

    public void setTitleTv(TextView titleTv) {
        this.titleTv = titleTv;
    }

    public TextView getAmountTv() {
        return amountTv;
    }

    public void setAmountTv(TextView amountTv) {
        this.amountTv = amountTv;
    }
}
