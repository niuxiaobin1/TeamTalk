package com.mogujie.tt.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.R;
import com.mogujie.tt.config.Constants;
import com.mogujie.tt.imservice.manager.IMContactManager;
import com.mogujie.tt.imservice.manager.IMLoginManager;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.ui.widget.IMBaseImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OpenRedPacketResultActivity extends TTBaseActivity {


    private UserEntity mChatInfo;
    private String msg;

    private ImageView backImage;
    private IMBaseImageView sendUserImage;
    private IMBaseImageView receiverUserImage;
    private TextView sendUserTv;
    private TextView receiverTimeTv;
    private TextView receiverAmountTv;
    private TextView receiverUserTv;
    private SimpleDateFormat formatter=new SimpleDateFormat("HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_red_packet_result);
//        LayoutInflater.from(this).inflate(, topContentView);

        mChatInfo = IMContactManager.instance().findContact(getIntent().getIntExtra(Constants.CHAT_INFO,0));
        msg = getIntent().getStringExtra(Constants.REDPACKET_INFO);
        if (mChatInfo == null) {
            return ;
        }

        backImage=findViewById(R.id.backImage);
        sendUserImage=findViewById(R.id.sendUserImage);
        receiverUserImage=findViewById(R.id.receiverUserImage);
        sendUserTv=findViewById(R.id.sendUserTv);
        receiverTimeTv=findViewById(R.id.receiverTimeTv);
        receiverAmountTv=findViewById(R.id.receiverAmountTv);
        receiverUserTv=findViewById(R.id.receiverUserTv);

        sendUserImage.setImageUrl(mChatInfo.getAvatar());
        receiverUserImage.setImageUrl(IMLoginManager.instance().getLoginInfo().getAvatar());

        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        sendUserTv.setText(String.format(getResources().getString(R.string.red_packet_from1),
                mChatInfo.getMainName()));

        try {
            JSONObject resultJson = new JSONObject(msg);
            receiverAmountTv.setText(getResources().getString(R.string.transfer_unit)+
                    resultJson.getString("amount"));
            receiverTimeTv.setText(formatter.format(new Date(
                    Long.parseLong(resultJson.getString("timestamp"))*1000
            )));
        } catch (JSONException e) {
            Log.e("nxb",e.toString());
        }
    }
}
