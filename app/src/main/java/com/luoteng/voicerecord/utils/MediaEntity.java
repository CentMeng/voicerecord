package com.luoteng.voicerecord.utils;

import android.text.TextUtils;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * @author CentMeng csdn@vip.163.com on 16/7/7.
 * 语音播放相关
 * downloadStatus 自己回答的存入到数据库要设置成loaded状态
 * path 是更具本地有无存在文件来做的判断，网络获取后，设置path为去掉前面前缀后的文件名，加bending路径
 */
public class MediaEntity extends DataSupport implements Serializable {

    private String userId;

    private String entityId;

    /**
     * 时长
     */
    private int length;

    /**
     * 网络路径地址
     */
    private String url;

    /**
     * 本地路径地址
     */
    private String path;

    protected RecordStatus status;

    private String downloadStatus;

    public void setDownloadStatus(String downloadStatus) {
        this.downloadStatus = downloadStatus;
    }
    public String getDownloadStatus() {
        if(TextUtils.isEmpty(downloadStatus)){
            return DownloadStatus.PREPARE.name();
        }
        return downloadStatus;
    }


    public DownloadStatus getTransDownloadStatus() {
        if(TextUtils.isEmpty(downloadStatus)){
            return DownloadStatus.PREPARE;
        }
        return DownloadStatus.valueOf(downloadStatus);
    }

    public void setTransDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus = downloadStatus.name();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getUrl() {
        if(TextUtils.isEmpty(url)){
            return "";
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        if(!TextUtils.isEmpty(path) && FileUtils.isFileExist(path)){
            return path;
        }
        return "";
    }

    public String getAbsolutePath(){
        if(!TextUtils.isEmpty(path)){
            return path;
        }
        return "";
    }

    public void setPath(String path) {
        this.path = path;
    }

    public RecordStatus getStatus() {
        return status;
    }

    public void setStatus(RecordStatus status) {
        this.status = status;
    }


}
