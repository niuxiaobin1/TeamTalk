package com.mogujie.tt.config;



/**
 * TUIKit的通用配置，比如可以设置日志打印、音视频录制时长等
 */
public class GeneralConfig {
    public  static String INSTITUTION_NUMBER = "";
    public  static String API_KEY = "";
    public final static String CUSTOM_ACCOUNT = "account";
    public final static String CUSTOM_PHONE = "phone";
    public final static String FALG_NCAHT_ADD_FRIEND = "Nchat_add";//
    public final static String FALG_NCAHT_PAY_CODE = "Nchat_pay";
    public final static String FALG_NCAHT_CODEPAY_START = "npay://";
    public final static int DEFAULT_AUDIO_RECORD_MAX_TIME = 60;
    public final static int DEFAULT_VIDEO_RECORD_MAX_TIME = 15;
    private static final String TAG = GeneralConfig.class.getSimpleName();
    private String appCacheDir;
    private int audioRecordMaxTime = DEFAULT_AUDIO_RECORD_MAX_TIME;
    private int videoRecordMaxTime = DEFAULT_VIDEO_RECORD_MAX_TIME;

    private boolean enableLogPrint = true;
    private boolean showRead = false;

    /**
     * 获取是否打印日志
     *
     * @return
     */
    public boolean isLogPrint() {
        return enableLogPrint;
    }

    /**
     * 设置是否打印日志
     *
     * @param enableLogPrint
     */
    public void enableLogPrint(boolean enableLogPrint) {
        this.enableLogPrint = enableLogPrint;
    }


    /**
     * 获取TUIKit缓存路径
     *
     * @return
     */
    public String getAppCacheDir() {
        return appCacheDir;
    }

    /**
     * 设置TUIKit缓存路径
     *
     * @param appCacheDir
     * @return
     */
    public GeneralConfig setAppCacheDir(String appCacheDir) {
        this.appCacheDir = appCacheDir;
        return this;
    }

    /**
     * 获取录音最大时长
     *
     * @return
     */
    public int getAudioRecordMaxTime() {
        return audioRecordMaxTime;
    }

    /**
     * 录音最大时长
     *
     * @param audioRecordMaxTime
     * @return
     */
    public GeneralConfig setAudioRecordMaxTime(int audioRecordMaxTime) {
        this.audioRecordMaxTime = audioRecordMaxTime;
        return this;
    }

    /**
     * 获取录像最大时长
     *
     * @return
     */
    public int getVideoRecordMaxTime() {
        return videoRecordMaxTime;
    }

    /**
     * 摄像最大时长
     *
     * @param videoRecordMaxTime
     * @return
     */
    public GeneralConfig setVideoRecordMaxTime(int videoRecordMaxTime) {
        this.videoRecordMaxTime = videoRecordMaxTime;
        return this;
    }

    /**
     * 对方已读的 view 是否展示
     *
     * @return
     */
    public boolean isShowRead() {
        return showRead;
    }

    /**
     * 设置对方已读的 view 是否展示
     *
     * @return
     */
    public void setShowRead(boolean showRead) {
        this.showRead = showRead;
    }

}
