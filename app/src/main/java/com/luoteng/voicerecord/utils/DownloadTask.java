package com.luoteng.voicerecord.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 下载文件采用多个线程下载一个文件的方式
 * @author CentMeng csdn@vip.163.com on 16/7/8.
 */
public class DownloadTask extends Thread {
    private String downloadUrl;// 下载链接地址
    private int threadNum;// 开启的线程数
    private String filePath;// 保存文件路径地址
    private int blockSize;// 每一个线程的下载量
    private DownloadListener downloadListener;
    private Context context;

    public DownloadTask(String downloadUrl, int threadNum, String fileptah, DownloadListener downloadListener, Context context) {
        this.downloadUrl = downloadUrl;
        this.threadNum = threadNum;
        this.filePath = fileptah;
        this.downloadListener = downloadListener;
        this.context = context;
    }

    @Override
    public void run() {

        if(!MemorySpaceUtils.hasEnoughMemory(FileUtils.getDiskFilePath(context, Environment.DIRECTORY_ALARMS),1024)){
            FileUtils.deleteDir(FileUtils.getDiskFileDir(context,"",Environment.DIRECTORY_ALARMS));
            if(!MemorySpaceUtils.hasEnoughMemory(FileUtils.getDiskFilePath(context, Environment.DIRECTORY_ALARMS),1024)){
                Toast.makeText(context,"存储空间不足，请清理下空间",Toast.LENGTH_SHORT).show();
                return;
            }
        }


        FileDownloadThread[] threads = new FileDownloadThread[threadNum];
        try {
            URL url = new URL(downloadUrl);
            Log.e("下载数据", "download file http path:" + downloadUrl);
            URLConnection conn = url.openConnection();
            // 读取下载文件总大小
            int fileSize = conn.getContentLength();
            if (fileSize <= 0) {
                System.out.println("读取文件失败");
                downloadListener.downloadedFail(downloadUrl, filePath);
                return;
            }
            // 设置ProgressBar最大的长度为文件Size
//            mProgressbar.setMax(fileSize);

            // 计算每条线程下载的数据长度
            blockSize = (fileSize % threadNum) == 0 ? fileSize / threadNum
                    : fileSize / threadNum + 1;

            Log.e("下载数据", "fileSize:" + fileSize + "  blockSize:"+blockSize);

            File file = new File(filePath);

            for (int i = 0; i < threads.length; i++) {
                // 启动线程，分别下载每个线程需要下载的部分
                threads[i] = new FileDownloadThread(url, file, blockSize,
                        (i + 1));
                threads[i].setName("Thread:" + i);
                threads[i].start();
            }

            boolean isfinished = false;
            int downloadedAllSize = 0;
            while (!isfinished) {
                isfinished = true;
                // 当前所有线程下载总量
                downloadedAllSize = 0;
                for (int i = 0; i < threads.length; i++) {
                    downloadedAllSize += threads[i].getDownloadLength();
                    if (!threads[i].isCompleted()) {
                        isfinished = false;
                    }
                }
                if(isfinished){
                    downloadListener.downloadedFile(downloadUrl,filePath);
                }else{
                    downloadListener.downloadingFile(downloadedAllSize);
                }

                // 通知handler去更新视图组件
//                Message msg = new Message();
//                msg.getData().putInt("size", downloadedAllSize);
//                mHandler.sendMessage(msg);
                // Log.d(TAG, "current downloadSize:" + downloadedAllSize);
                Thread.sleep(1000);// 休息1秒后再读取下载进度
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            downloadListener.downloadedFail(downloadUrl,filePath);
        } catch (IOException e) {
            e.printStackTrace();
            downloadListener.downloadedFail(downloadUrl,filePath);
        } catch (InterruptedException e) {
            e.printStackTrace();
            downloadListener.downloadedFail(downloadUrl,filePath);
        }

    }

    /**
     * 操作需要在主线程进行
     */
    public interface DownloadListener{

        /**
         * 下载中
         */
        public void downloadingFile(int process);

        /**
         * 下载完成
         */
        public void downloadedFile(String url, String path);

        /**
         * 下载失败
         */
        public void downloadedFail(String url, String path);
    }

}