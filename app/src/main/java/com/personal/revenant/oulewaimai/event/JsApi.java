package com.personal.revenant.oulewaimai.event;

import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * Created by Administrator on 2018/8/23.
 */

public class JsApi {
    private JsCallback jsCallback;

    public JsApi(JsCallback callback) {
        this.jsCallback = callback;
    }


    @JavascriptInterface
    public void toPay(Object params) {
        Log.d("---->>>>", params.toString());
        jsCallback.jsGetAlipayParams(params);
    }

    @JavascriptInterface
    public void uploadHeadImg(Object params) {
        Log.d("111", "1111111111");
        jsCallback.uploadHeadImg(params);
    }

    @JavascriptInterface
    public void uploadPhotos(Object params) {
        Log.d("---->>>>", params.toString());
        jsCallback.jsTakeUploads(params);
    }

    /**
     * 分享
     */
    @JavascriptInterface
    public void share(Object params) {
        Log.d("---->>>>", params.toString());
        jsCallback.takeShare(params);
    }

    /**
     * 第三方登录
     */
    @JavascriptInterface
    public void third_party_login(Object params) {
        Log.d("---->>>>", params.toString());
        jsCallback.third_party_login(params);
    }

    @JavascriptInterface
    public void copyordercode(Object params) {
        Log.d("---->>>>", params.toString());
        jsCallback.takeShare(params);
    }


    @JavascriptInterface
    public void canExit(Object params) {
        Log.d("jsApi: params ---->>>>", params.toString());
        jsCallback.takeExit(params);
    }

    @JavascriptInterface
    public void qr_code(Object params) {
        Log.d("jsApi: params ---->>>>", params.toString());
        jsCallback.qr_code(params);
    }

    @JavascriptInterface
    public void comparison(Object params) {
        Log.d("jsApi: params ---->>>>", params.toString());
        jsCallback.comparison(params);
    }


    @JavascriptInterface
    public void zfb_pay(Object params) {
        Log.d("jsApi: params ---->>>>", params.toString());
        jsCallback.zfb_pay(params);
    }

    @JavascriptInterface
    public void alipay(Object params) {
        Log.d("jsApi: params ---->>>>", params.toString());
        jsCallback.alipay(params);
    }

    @JavascriptInterface
    public void wx_pay(Object params) {
        Log.d("jsApi: params ---->>>>", params.toString());
        jsCallback.wx_pay(params);
    }

    @JavascriptInterface
    public void Copy(Object params) {
        Log.d("jsApi: params ---->>>>", params.toString());
        jsCallback.Copy(params);
    }

    @JavascriptInterface
    public void uploadImg(Object params) {
        Log.d("jsApi: params ---->>>>", params.toString());
        jsCallback.uploadImg(params);
    }

    @JavascriptInterface
    public void shareUrl(Object params) {
        Log.d("jsApi: params ---->>>>", params.toString());
        jsCallback.shareUrl(params);
    }

    @JavascriptInterface
    public void identity(Object params) {
        Log.d("jsApi: params ---->>>>", params.toString());
        jsCallback.identity(params);
    }

    @JavascriptInterface
    public void downloadFile(Object params) {
        Log.d("jsApi: params ---->>>>", params.toString());
        jsCallback.downloadFile(params);
    }

    @JavascriptInterface
    public void jsTakeUpload(Object params) {
        Log.d("jsApi: params ---->>>>", params.toString());
        jsCallback.jsTakeUpload(params);
    }

    @JavascriptInterface
    public void saveImg(Object params) {
        Log.d("jsApi: params ---->>>>", params.toString());
        jsCallback.saveImg(params);
    }

    public interface JsCallback {
        /**
         * 调用支付
         **/
        void jsGetAlipayParams(Object params);

        /**
         * 上传头像
         **/
        void jsTakeUpload(Object params);

        /**
         * 多图上传
         **/
        void jsTakeUploads(Object params);

        /**
         * 分享
         **/
        void takeShare(Object params);

        /**
         * 拷贝到剪切板
         **/
        void takeCopy(Object params);

        /**
         * 是否可以退出
         **/
        void takeExit(Object params);

        void third_party_login(Object params);

        void qr_code(Object params);

        void comparison(Object params);

        void zfb_pay(Object params);

        void alipay(Object params);

        void Copy(Object params);

        void uploadImg(Object params);

        void shareUrl(Object params);

        void identity(Object params);

        void downloadFile(Object params);

        void saveImg(Object params);

        void wx_pay(Object params);

        void uploadHeadImg(Object params);
    }
}
