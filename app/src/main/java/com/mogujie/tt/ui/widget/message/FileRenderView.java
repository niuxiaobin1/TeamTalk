package com.mogujie.tt.ui.widget.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mogujie.tt.DB.entity.MessageEntity;
import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.R;
import com.mogujie.tt.config.MessageConstant;
import com.mogujie.tt.imservice.entity.FileMessage;
import com.mogujie.tt.imservice.entity.ImageMessage;
import com.mogujie.tt.imservice.manager.IMMessageManager;
import com.mogujie.tt.ui.widget.BubbleImageView;
import com.mogujie.tt.ui.widget.MGProgressbar;
import com.mogujie.tt.utils.FileUtil;
import com.mogujie.tt.utils.Logger;

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
        messageLayout = findViewById(R.id.message_layout);
        messageImage = (ImageView) findViewById(R.id.file_image);
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
        FileMessage fileMessage= (FileMessage) messageEntity;
        if (fileMessage.getLoadStatus()==MessageConstant.FILE_UNLOAD){
            //下载
            IMMessageManager.instance().startSaveFile(fileMessage.getTaskId());
        }
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
        if (isMine()) {

            messageImage.setImageResource(R.mipmap.file_icon);
        }

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
        messageImage.setImageResource(R.mipmap.file_icon);
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
