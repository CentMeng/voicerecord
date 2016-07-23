package com.luoteng.voicerecord.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

/**
 * 文件相关
 * @author mengxc
 *
 */
public class FileUtils {

    /**
     * 检测Sdcard是否存在
     *
     * @return
     */
    public static boolean isExitsSdcard() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }


    /**
     * 表示如果该文件表示在底层文件系统的文件
     * Indicates if this file represents a file on the underlying file system.
     *
     * @param filePath
     * @return
     */
    public static boolean isFileExist(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        File file = new File(filePath);
        return (file.exists() && file.isFile());
    }
    /**
     * 删除文件
     * delete file or directory
     * <ul>
     * <li>if path is null or empty, return true</li>
     * <li>if path not exist, return true</li>
     * <li>if path exist, delete recursion. return true</li>
     * <ul>
     *
     * @param path
     * @return
     */
    public static boolean deleteFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return true;
        }

        File file = new File(path);
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (!file.isDirectory()) {
            return false;
        }
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                f.delete();
            } else if (f.isDirectory()) {
                deleteFile(f.getAbsolutePath());
            }
        }
        return file.delete();
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    /**
     * 获取缓存路径地址
     * @param context
     * @return
     */
    public static String getDiskCachePath(Context context){
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())
            // || !Environment.isExternalStorageRemovable()
                ) {
            try {
                ///sdcard/Android/data/<application package>/cache
                //getExternalFilesDir()方法可以获取到 SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据  files对应设置中的清楚数据
                //通过Context.getExternalCacheDir()方法可以获取到 SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据  cache对应设置中的清楚缓存
                cachePath = context.getExternalCacheDir().getPath();
            } catch (Exception e) {
                //getCacheDir()方法用于获取/data/data/<application package>/cache目录
                //getFilesDir()方法用于获取/data/data/<application package>/files目录
                cachePath = context.getCacheDir().getPath();
            }
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    /**
     * 创建和获取缓存路径 
     *
     * @param uniqueName
     * @return
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath = getDiskCachePath(context);

        File file = new File(cachePath + File.separator + uniqueName);
        file.mkdirs();
        return file;
    }

    /**
     * 获取缓存文件地址
     * @param context
     * @return
     */
    public static String getDiskFilePath(Context context,String type){
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())
            // || !Environment.isExternalStorageRemovable()
                ) {
            try {
                ///sdcard/Android/data/<application package>/cache
                //getExternalFilesDir()方法可以获取到 SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据  files对应设置中的清楚数据
                //通过Context.getExternalCacheDir()方法可以获取到 SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据  cache对应设置中的清楚缓存
                if (TextUtils.isEmpty(type)) {
                    type = Environment.DIRECTORY_PICTURES;
                }
                cachePath = context.getExternalFilesDir(type).getPath();
            } catch (Exception e) {
                //getCacheDir()方法用于获取/data/data/<application package>/cache目录
                //getFilesDir()方法用于获取/data/data/<application package>/files目录
                cachePath = context.getFilesDir().getPath();
            }
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    /**
     * 创建和获取数据保存路径 
     *
     * @param context
     * @param uniqueName 后缀文件夹
     * @param type       文件类型，空则默认
     * @return
     */
    public static File getDiskFileDir(Context context, String uniqueName, String type) {

        String cachePath = getDiskFilePath(context,type);
        File file = new File(cachePath);
        if(!TextUtils.isEmpty(uniqueName)){
            file = new File(cachePath + File.separator + uniqueName);
        }
        file.mkdirs();
        return file;
    }

}
