package com.personal.revenant.oulewaimai.App;

import android.app.Application;

import com.lzy.okgo.OkGo;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.commonsdk.BuildConfig;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;

import static com.personal.revenant.oulewaimai.utils.Constant.QQAPPID;
import static com.personal.revenant.oulewaimai.utils.Constant.QQSECRET;
import static com.personal.revenant.oulewaimai.utils.Constant.WXAPP_ID;
import static com.personal.revenant.oulewaimai.utils.Constant.WX_SECRET;

/**
 * Created by Administrator on 2018/8/23.
 */

public class App extends Application {
    public static IWXAPI mWxApi;

    @Override
    public void onCreate() {
        super.onCreate();
        OkGo.getInstance().init(this);
        if (BuildConfig.DEBUG) {
            UMConfigure.setLogEnabled(true);
        }
        UMConfigure.init(this, "5bfb4f43f1f55628fd00005c", "Umeng", UMConfigure.DEVICE_TYPE_PHONE,
                "");
        PlatformConfig.setWeixin(WXAPP_ID, WX_SECRET);
        PlatformConfig.setQQZone(QQAPPID, QQSECRET);

        mWxApi = WXAPIFactory.createWXAPI(this, WXAPP_ID, false);
        // 将该app注册到微信
        mWxApi.registerApp(WXAPP_ID);
        // 添加微信平台
    }
}
