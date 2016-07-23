package com.luoteng.voicerecord.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.util.UUID;

/**
 * @author CentMeng csdn@vip.163.com on 16/7/8.
 */
public class MediaUtils {

    public static MediaEntity getMediaEntity(Context context,String entityId, String userId, String url, int length){
        //此方法是找到本地是否存在音频文件
        MediaEntity mediaEntity = DbUtils.getMediaEntity(url);
        if(mediaEntity != null && !TextUtils.isEmpty(mediaEntity.getUrl())){
            if(TextUtils.isEmpty(mediaEntity.getEntityId())){
                mediaEntity.setEntityId(entityId);
                DbUtils.updateMediaEntity(mediaEntity);
            }
            if(mediaEntity.getTransDownloadStatus() == DownloadStatus.LOADING) {
                mediaEntity.setTransDownloadStatus(DownloadStatus.PREPARE);
                DbUtils.updateMediaEntity(mediaEntity);
            }
            if(TextUtils.isEmpty(mediaEntity.getPath())){
                mediaEntity.setPath(FileUtils.getDiskFilePath(context, Environment.DIRECTORY_ALARMS)+ File.separator+ UUID.randomUUID()+".mp3");
                DbUtils.updateMediaEntity(mediaEntity);
            }
            return mediaEntity;
        }
        mediaEntity = new MediaEntity();
        mediaEntity.setUrl(url);
        mediaEntity.setEntityId(entityId);
        mediaEntity.setLength(length);
        mediaEntity.setUserId(userId);
        mediaEntity.setStatus(RecordStatus.RECORDED);
        mediaEntity.setTransDownloadStatus(DownloadStatus.PREPARE);
        //存储到本地
        mediaEntity.setPath(FileUtils.getDiskFilePath(context, Environment.DIRECTORY_ALARMS)+ File.separator+ UUID.randomUUID()+".mp3");
        return mediaEntity;
    }
}
