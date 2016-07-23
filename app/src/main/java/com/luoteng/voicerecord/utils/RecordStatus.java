package com.luoteng.voicerecord.utils;

/**
 * @author CentMeng csdn@vip.163.com on 16/7/6.
 * 录音相关状态
 */
public enum RecordStatus implements BaseEnum {

    BEFORE("点击录音,最多可录60\'\'"),

    RECORDING("录音中，再次点击结束录音"),

    PRE_PLAYING("试听中"),

    PLAYING("播放"),

    RECORDED("点击按钮试听录音"),

    STOP_PREPLAYING("点击按钮试听录音"),

    PREPARED("准备完毕");

    RecordStatus(String key) {
        this.key = key;
    }

    private String key;

    @Override
    public String getKey() {
        return key;
    }
}
