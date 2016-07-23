package com.luoteng.voicerecord.utils;

import android.text.TextUtils;
import android.util.Log;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * @author CentMeng csdn@vip.163.com on 16/1/7.
 * 数据库操作
 */
public class DbUtils {

    /**
     * 保存语音信息
     *
     * @param entity
     */
    public static void updateMediaEntity(MediaEntity entity) {
        if (entity != null) {
            if (TextUtils.isEmpty(entity.getUrl()) || entity.updateAll("url = ?", entity.getUrl()) <= 0) {
                entity.save();
                Log.e("数据输出", "语音信息更新不存在,添加");
            } else {
                Log.e("数据输出",  "语音信息更新存在,更新");
            }
        }
    }


    /**
     * 获取语音信息
     *
     * @param url 网络路径
     * @return
     */
    public static MediaEntity getMediaEntity(String url) {
        if(!TextUtils.isEmpty(url)){
            List<MediaEntity> medias = DataSupport.where("url = ?", url).find(MediaEntity.class);
            if (medias !=null && medias.size()>0) {
                Log.e("数据输出",  "获取语音信息");
                return medias.get(0);
            }
        }
        Log.e("数据输出", "获取语音信息为空");
        return null;
    }


    /**
     * 获取语音信息
     *
     * @param url 网络路径
     * @return
     */
    public static String getLocalPath(String url) {
        MediaEntity media = getMediaEntity(url);
        if (media != null) {
            return media.getPath();
        }
        return null;
    }


}
