package com.luoteng.voicerecord;

import org.litepal.LitePalApplication;

/**
 * @author CentMeng csdn@vip.163.com on 15/9/8.
 */
public class GlobalPhone extends LitePalApplication {

    private static GlobalPhone instance;


    public static GlobalPhone getInstance() {
        if (instance == null) {
            instance = new GlobalPhone();
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }


}
