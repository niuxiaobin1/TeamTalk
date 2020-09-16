package com.mogujie.tt.ui.widget.message;

import android.app.Activity;
import android.content.Context;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.luck.picture.lib.PictureSelector;
import com.mogujie.tt.DB.entity.MessageEntity;
import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.R;
import com.mogujie.tt.config.MessageConstant;
import com.mogujie.tt.imservice.entity.FileMessage;
import com.mogujie.tt.imservice.manager.IMLoginManager;
import com.mogujie.tt.imservice.manager.IMMessageManager;
import com.mogujie.tt.ui.widget.MGProgressbar;
import com.mogujie.tt.utils.CommonUtil;
import com.mogujie.tt.utils.FileUtil;
import com.mogujie.tt.utils.Logger;
import com.mogujie.tt.utils.ToastUtil;

import java.io.File;

/**
 * @author : yingmu on 15-1-9.
 * @email : yingmu@mogujie.com.
 */
public class FileRenderView extends BaseMsgRenderView {
    private Logger logger = Logger.getLogger(FileRenderView.class);

    // 上层必须实现的接口
    private BtnFileListener btnFileListener;

    /**
     * 可点击的view
     */
    private View messageLayout;
    /**
     * file消息体
     */
    private ImageView messageImage;

    private TextView titleTv;
    private TextView sizeTv;
    private ProgressBar progress_bar_h;
    /**
     * 图片状态指示
     */
    private MGProgressbar imageProgress;

    public FileRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static FileRenderView inflater(Context context, ViewGroup viewGroup, boolean isMine) {
        int resource = isMine ? R.layout.tt_mine_file_message_item : R.layout.tt_other_file_message_item;
        FileRenderView imageRenderView = (FileRenderView) LayoutInflater.from(context).inflate(resource, viewGroup, false);
        imageRenderView.setMine(isMine);
        imageRenderView.setParentView(viewGroup);
        return imageRenderView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        progress_bar_h = findViewById(R.id.progress_bar_h);
        messageLayout = findViewById(R.id.message_layout);
        messageImage = (ImageView) findViewById(R.id.file_image);
        titleTv = (TextView) findViewById(R.id.titleTv);
        sizeTv = (TextView) findViewById(R.id.sizeTv);
        imageProgress = (MGProgressbar) findViewById(R.id.tt_image_progress);
        imageProgress.setShowText(false);
    }

    /**
     *
     * */

    /**
     * 控件赋值
     *
     * @param messageEntity
     * @param userEntity    对于mine。 图片send_success 就是成功了直接取地址
     *                      对于sending  就是正在上传
     *                      <p>
     *                      对于other，消息一定是success，接受成功额
     *                      2. 然后分析loadStatus 判断消息的展示状态
     */
    @Override
    public void render(final MessageEntity messageEntity, final UserEntity userEntity, Context ctx) {
        super.render(messageEntity, userEntity, ctx);
        FileMessage fileMessage = (FileMessage) messageEntity;
        boolean isRecieved = fileMessage.getFromId() != IMLoginManager.instance().getLoginId();
        if (isRecieved && !fileMessage.isDownLoaded() && !fileMessage.isDownloading()) {
            //下载
            fileMessage.setDownloading(true);
            IMMessageManager.instance().startSaveFile(fileMessage);
        }
        if (FileUtil.getExtensionName(((FileMessage) messageEntity).getPath()).toUpperCase().equals("MP4")) {
            //video
            messageImage.setImageResource(R.mipmap.msg_file_video_icon);
            messageLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (new File(fileMessage.getPath()).exists()) {
                        PictureSelector.create((Activity) getContext()).externalPictureVideo(fileMessage.getPath());
                    } else {
                        ToastUtil.toastShortMessage("file not exists...");
                    }

                }
            });
        }
        if (((FileMessage) messageEntity).getProgress() != 0
                && ((FileMessage) messageEntity).getProgress() != 100
//                && messageEntity.getStatus() != MessageConstant.MSG_SUCCESS
        ) {
            progress_bar_h.setVisibility(VISIBLE);
            progress_bar_h.setProgress(((FileMessage) messageEntity).getProgress());
        } else {
            progress_bar_h.setVisibility(GONE);
        }

        titleTv.setText(CommonUtil.getFileNameWithSuffix(((FileMessage) messageEntity).getPath()));
        sizeTv.setText(Formatter.formatFileSize(ctx, ((FileMessage) messageEntity).getSize()));
    }


    /**
     * 多端同步也不会拉到本地失败的数据
     * 只有isMine才有的状态，消息发送失败
     * 1. 图片上传失败。点击图片重新上传??[也是重新发送]
     * 2. 图片上传成功，但是发送失败。 点击重新发送??
     * 3. 比较悲剧的是 图片上传失败和消息发送失败都是这个状态 不过可以通过另外一个状态来区别 图片load状态
     *
     * @param entity
     */
    @Override
    public void msgFailure(final MessageEntity entity) {
        super.msgFailure(entity);
        messageImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /**判断状态，重新发送resend*/
                btnFileListener.onMsgFailure();
            }
        });

        imageProgress.hideProgress();
    }


    @Override
    public void msgStatusError(final MessageEntity entity) {
        super.msgStatusError(entity);
        imageProgress.hideProgress();
    }


    /**
     * 图片信息正在发送的过程中
     * 1. 上传图片
     * 2. 发送信息
     */
    @Override
    public void msgSendinging(final MessageEntity entity) {
        super.msgSendinging(entity);
        imageProgress.showProgress();
    }


    /**
     * 消息成功
     * 1. 对方图片消息
     * 2. 自己多端同步的消息
     * 说明imageUrl不会为空的
     */
    @Override
    public void msgSuccess(final MessageEntity entity) {
        super.msgSuccess(entity);
        imageProgress.hideProgress();
        boolean isRecieved = entity.getFromId() != IMLoginManager.instance().getLoginId();
        if (!isRecieved) {
            progress_bar_h.setVisibility(GONE);
        }

    }

    /**
     * ---------------------图片下载相关、点击、以及事件回调start-----------------------------------
     */
    public interface BtnFileListener {
        public void onMsgSuccess();

        public void onMsgFailure();
    }

    public void setBtnFileListener(BtnFileListener btnFileListener) {
        this.btnFileListener = btnFileListener;
    }


    /**---------------------图片下载相关、以及事件回调 end-----------------------------------*/


    /**
     * ----------------------set/get------------------------------------
     */
    public View getMessageLayout() {
        return messageLayout;
    }

    public ImageView getMessageImage() {
        return messageImage;
    }

    public MGProgressbar getImageProgress() {
        return imageProgress;
    }

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

}
