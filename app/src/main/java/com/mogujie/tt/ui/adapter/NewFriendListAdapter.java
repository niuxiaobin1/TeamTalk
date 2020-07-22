package com.mogujie.tt.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mogujie.tt.DB.DBInterface;
import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.R;
import com.mogujie.tt.protobuf.IMBaseDefine;
import com.mogujie.tt.ui.widget.CircleImageView;
import com.mogujie.tt.ui.widget.IMBaseImageView;
import com.mogujie.tt.utils.IMUIHelper;
import com.mogujie.tt.utils.ImageLoaderUtil;


import java.util.ArrayList;
import java.util.List;

/**
 * 好友关系链管理消息adapter
 */
public class NewFriendListAdapter extends ArrayAdapter<UserEntity> {

    private static final String TAG = NewFriendListAdapter.class.getSimpleName();

    private int mResourceId;
    private View mView;
    private ViewHolder mViewHolder;
    private  AgreeClickListener agreeClickListener;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout ic_chat_input_file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public NewFriendListAdapter(Context context, int resource, List<UserEntity> objects,AgreeClickListener agreeClickListener) {
        super(context, resource, objects);
        mResourceId = resource;
        this.agreeClickListener=agreeClickListener;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final UserEntity data = getItem(position);
        if (convertView != null) {
            mView = convertView;
            mViewHolder = (ViewHolder) mView.getTag();
        } else {
            mView = LayoutInflater.from(getContext()).inflate(mResourceId, null);
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IMUIHelper.openUserProfileActivity(getContext(), data.getPeerId());
                }
            });
            mViewHolder = new ViewHolder();
            mViewHolder.avatar =mView.findViewById(R.id.avatar);
            mViewHolder.name = mView.findViewById(R.id.name);
            mViewHolder.des = mView.findViewById(R.id.description);
            mViewHolder.agree = mView.findViewById(R.id.agree);
            mView.setTag(mViewHolder);
        }
        mViewHolder.avatar.setDefaultImageRes(R.mipmap.default_user_icon);
        mViewHolder.avatar.setCorner(15);
        mViewHolder.avatar.setImageUrl(data.getAvatar());
        mViewHolder.name.setText(data.getMainName());
        mViewHolder.des.setText("");
        mViewHolder.agree.setText(getContext().getString(R.string.request_agree));
        mViewHolder.agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextView vv = (TextView) v;
                vv.setText(getContext().getResources().getString(R.string.request_accepted));
                DBInterface.instance().insertOrUpdateUser(data);
                if (agreeClickListener!=null){
                    agreeClickListener.onClick(data.getPeerId());
                }
            }
        });

        return mView;
    }

    public interface AgreeClickListener{
        void onClick(int id);
    }


    public class ViewHolder {
        IMBaseImageView avatar;
        TextView name;
        TextView des;
        Button agree;
    }



}
