package com.personal.revenant.oulewaimai;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.apkfuns.logutils.LogUtils;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.DefaultWebClient;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.view.CropImageView;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.master.permissionhelper.PermissionHelper;
import com.nanchen.compresshelper.CompressHelper;
import com.personal.revenant.oulewaimai.bean.MutiltypePhotoResults;
import com.personal.revenant.oulewaimai.bean.PhotoBean;
import com.personal.revenant.oulewaimai.bean.TypeBean;
import com.personal.revenant.oulewaimai.bean.UploadImgBean;
import com.personal.revenant.oulewaimai.bean.WxPayParams;
import com.personal.revenant.oulewaimai.event.JsApi;
import com.personal.revenant.oulewaimai.utils.GlideImageLoader;
import com.personal.revenant.oulewaimai.utils.GsonUtil;
import com.personal.revenant.oulewaimai.utils.LoadDialog;
import com.personal.revenant.oulewaimai.utils.PayResult;
import com.personal.revenant.oulewaimai.utils.Utils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.utils.SocializeUtils;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;
import wendu.dsbridge.DWebView;

import static com.luck.picture.lib.config.PictureConfig.CHOOSE_REQUEST;
import static com.personal.revenant.oulewaimai.utils.Constant.ACTION_UPLOAD_HEADIMG;
import static com.personal.revenant.oulewaimai.utils.Constant.BASEURL;
import static com.personal.revenant.oulewaimai.utils.Constant.WEIXINAPPID;

public class MainActivity extends Activity implements JsApi.JsCallback, UMShareListener {

    private RelativeLayout loadView;
    private SmartRefreshLayout smartRefreshLayout;
    private boolean firstLoad = true;
    protected LoadDialog loadDialog;
    private AgentWeb mAgentWeb;
    private ProgressDialog dialog;
    private DWebView dWebView;
    private String url;
    private Context context;
    private static final int IMAGE_PICKER = 300;
    private Button button;
    private int REQUEST_CODE_SCAN = 111;
    private PermissionHelper permissionHelper;
    private List<LocalMedia> selectList = new ArrayList<>();
    private String userid;
    private String goodinfo;
    private static final int SDK_PAY_FLAG = 1;
    private SHARE_MEDIA shareMedia;
    private String type;

    private final int REQUEST_UPLOAD_HEADIMG = 10001;// 上传头像
    private String requestUrl;// 请求地址
    private IWXAPI api;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, WEIXINAPPID);
        initWight();
        context = this;
        setContentView(R.layout.activity_main);
        Utils.setImageStatus(this);
        setStatusBg(R.color.colorPrimary);
        url = BASEURL;
        initView();
    }

    private void initView() {
        button = findViewById(R.id.test);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                jsTakeUpload("dheueee");
                third_party_login(null);
            }
        });
        dialog = new ProgressDialog(context);
        loadView = findViewById(R.id.loadView);
        smartRefreshLayout = findViewById(R.id.refreshlayout);
        smartRefreshLayout.setRefreshHeader(new ClassicsHeader(this));
        smartRefreshLayout.setEnableOverScrollDrag(false);
        smartRefreshLayout.setEnableRefresh(false);

        dWebView = new DWebView(this);
        dWebView.addJavascriptObject(new JsApi(this), null);
        //开启浏览器调试
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && BuildConfig.DEBUG) {
            dWebView.setWebContentsDebuggingEnabled(true);
        }

        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                mAgentWeb.getUrlLoader().reload();
            }
        });
        //下拉内容不偏移
        smartRefreshLayout.setEnableHeaderTranslationContent(false);

        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(smartRefreshLayout, new FrameLayout.LayoutParams(-1, -1))
                .closeIndicator()
                .setWebViewClient(webViewClient)
                .setWebView(dWebView)
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.DISALLOW)//不跳转其他应用
                .createAgentWeb()
                .ready()
                .go(url);
    }

    private void initWight() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
        imagePicker.setShowCamera(true);  //显示拍照按钮
        imagePicker.setCrop(true);        //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(false); //是否按矩形区域保存
        imagePicker.setSelectLimit(6);    //选中数量限制
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
        imagePicker.setFocusWidth(800);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000);//保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000);//保存文件的高度。单位像素
        imagePicker.setMultiMode(true);   //允许剪切
    }

    WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (firstLoad) {
                loadView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            smartRefreshLayout.finishRefresh();
            if (firstLoad) {
                firstLoad = false;
                goneAnim(loadView);
            }
        }
    };

    private void goneAnim(final View view) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
        alphaAnimation.setDuration(700);

        view.startAnimation(alphaAnimation);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private long exitTime = 0;

    public void doubleExit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            toast("再按一次退出程序");
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    protected void showDig(String msg, boolean canCancel) {
        if (loadDialog == null) {
            loadDialog = new LoadDialog.Builder(this).loadText(msg).canCancel(canCancel).build();
        } else {
            loadDialog.setText(msg);
        }
        if (!loadDialog.isShowing())
            loadDialog.show();
    }

    protected void dismissDig() {
        if (loadDialog != null && loadDialog.isShowing()) {
            loadDialog.dismiss();
        }
    }

    private void setStatusBg(int resId) {
        ViewGroup contentView = findViewById(android.R.id.content);
        View statusBarView = new View(this);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                Utils.getStatusBarHeight(this));
        statusBarView.setBackgroundResource(resId);
        contentView.addView(statusBarView, lp);
    }

    @Override
    public void jsGetAlipayParams(Object params) {

    }

    @Override
    public void jsTakeUpload(Object params) {
        if (params==null){
            return;
        }
        type="1";
        initPictandVideoone();
    }

    @Override
    public void jsTakeUploads(Object params) {
        initPictandVideoone();
    }

    private void initPictandVideoone() {
        PictureSelector.create(MainActivity.this)
                .openGallery(PictureMimeType.ofImage())
                .imageSpanCount(3)
                .maxSelectNum(1)
                .selectionMode(PictureConfig.MULTIPLE)
                .previewImage(true)
                .previewVideo(false)
                .enableCrop(false)
                .circleDimmedLayer(false)
                .isCamera(false)
                .selectionMedia(selectList)
                .minimumCompressSize(100)
                .isDragFrame(true)
                .forResult(CHOOSE_REQUEST);
    }

    SHARE_MEDIA id;

    @Override
    public void third_party_login(Object params) {
        if (params == null) {
            return;
        }
        LogUtils.d("数据是" + params.toString());
        if (params.toString().equals("1")) {// 微信
            shareMedia = SHARE_MEDIA.WEIXIN;
        } else if (params.toString().equals("2")) {// QQ
            shareMedia = SHARE_MEDIA.QQ;
        }
        UMShareAPI.get(context).getPlatformInfo(this, shareMedia, new UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {
                SocializeUtils.safeShowDialog(dialog);
            }

            @Override
            public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                SocializeUtils.safeCloseDialog(dialog);
                String s = map.get("openid");
                dWebView.callHandler("third_msg", new Object[]{s});
            }

            @Override
            public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                SocializeUtils.safeCloseDialog(dialog);
                Toast.makeText(context, "失败：" + throwable.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("onCancel: ", throwable.getMessage());
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media, int i) {
                SocializeUtils.safeCloseDialog(dialog);
                Toast.makeText(context, "取消了", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void initPictandVideo() {
        PictureSelector.create(MainActivity.this)
                .openGallery(PictureMimeType.ofImage())
                .imageSpanCount(3)
                .maxSelectNum(1)
                .selectionMode(PictureConfig.SINGLE)
                .previewImage(true)
                .previewVideo(false)
                .enableCrop(true)
                .freeStyleCropEnabled(false)
                .circleDimmedLayer(false)
                .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
                .isCamera(true)
                .isZoomAnim(true)
                .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
                .showCropGrid(false)
                .cropWH(600, 600)
                .withAspectRatio(1, 1)
                .selectionMedia(selectList)
                .minimumCompressSize(100)
                .isDragFrame(true)
                .forResult(PictureConfig.CHOOSE_REQUEST);
    }


    @Override
    public void qr_code(Object params) {
        permissionHelper = new PermissionHelper(this, new String[]{Manifest.permission.CAMERA}, 100);
        permissionHelper.request(new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);

                startActivityForResult(intent, REQUEST_CODE_SCAN);
            }

            @Override
            public void onIndividualPermissionGranted(String[] grantedPermission) {
//                 Log.d(TAG, "onIndividualPermissionGranted() called with: grantedPermission = [" + TextUtils.join(",",grantedPermission) + "]");
            }

            @Override
            public void onPermissionDenied() {
//                 Log.d(TAG, "onPermissionDenied() called");
            }

            @Override
            public void onPermissionDeniedBySystem() {
//                 Log.d(TAG, "onPermissionDeniedBySystem() called");
            }
        });

    }

    @Override
    public void comparison(Object params) {
        LogUtils.d("成功是" + params.toString());


        userid = params.toString();

        initPictandVideo();
    }

    @Override
    public void alipay(Object params) {
        zhifubao();
    }

    @Override
    public void Copy(Object params) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        //创建ClipData对象
        ClipData clipData = ClipData.newPlainText("tahome text copy", params.toString());
        //添加ClipData对象到剪切板中
        clipboardManager.setPrimaryClip(clipData);

        toast("已复制内容到剪切板");

    }

    @Override
    public void uploadImg(Object params) {
        LogUtils.d("数据是" + params.toString());
        type = "1";
        if (params != null) {
            userid = params.toString();
        }
        requestUrl = "http://xinlian.nxiapk.top/api/upload/upload";
        PictureSelector.create(MainActivity.this)
                .openGallery(PictureMimeType.ofImage())
                .maxSelectNum(1)
                .selectionMedia(selectList)
                .forResult(CHOOSE_REQUEST);
    }

    @Override
    public void shareUrl(Object params) {
        LogUtils.d("数据是" + params.toString());
        if (params.toString().equals("1")) {// 微信
            shareMedia = SHARE_MEDIA.WEIXIN;
        } else if (params.toString().equals("2")) {// 朋友圈
            shareMedia = SHARE_MEDIA.WEIXIN_CIRCLE;
        } else if (params.toString().equals("3")) {// QQ
            shareMedia = SHARE_MEDIA.QQ;
        }
//        new ShareAction(MainActivity.this).setDisplayList(SHARE_MEDIA.QQ,SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE)
//                .setShareboardclickCallback(new ShareBoardlistener() {
//                    @Override
//                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
        new ShareAction(MainActivity.this).withText("http://www.baidu.com")
                .setPlatform(shareMedia)
                .setCallback(new UMShareListener() {
                    @Override
                    public void onStart(SHARE_MEDIA share_media) {
                        LogUtils.d("分享的是:" + share_media.toString());
                    }

                    @Override
                    public void onResult(SHARE_MEDIA share_media) {
                        LogUtils.d("分享的是:" + share_media.toString());
                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                        LogUtils.d("分享的是:" + share_media.toString());
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media) {
                        LogUtils.d("分享的是:" + share_media.toString());
                    }
                }).share();
//                    }
//                }).open();
    }

    @Override
    public void identity(Object params) {
//        LogUtils.d("数据是" + params.toString());

//        TypeBean info = GsonUtil.parseJsonWithGson(params.toString(), TypeBean.class);
//        type = info.getType();
//
//        if ("2".equals(type)) {
        requestUrl = ACTION_UPLOAD_HEADIMG;
//        } else {
//            requestUrl = "";
//        }

        PictureSelector.create(MainActivity.this)
                .openGallery(PictureMimeType.ofImage())
                .maxSelectNum(1)
                .selectionMedia(selectList)
                .forResult(REQUEST_UPLOAD_HEADIMG);

    }

    @Override
    public void downloadFile(Object params) {
        String downloadurl = params.toString();

        OkGo.<File>get(downloadurl)
                .tag(this)
                .execute(new FileCallback() {
                    @Override
                    public void onSuccess(Response<File> response) {
                        LogUtils.d("文件下载" + response.body());
                        LogUtils.d("文件下载" + response.code());
                        if (response.code() == 200) {
                            toast("文件下载成功");
                        }
                    }
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionHelper != null) {
            permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void compress(List<String> paths) {
        showDig("压缩中...", false);
        final int size = paths.size();
//        final Map<String, File> map = new HashMap<>();
        final List<File> map = new ArrayList<>();

        Luban.with(context).load(paths).ignoreBy(100)
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess(File file) {
                        LogUtils.d("数据是:" + file.getPath() + "---------------->>>>>>");
//                        map.put(file.getName(), file);
                        map.add(file);
                        if (map.size() == size) {
                            //压缩完毕,上传图片
                            showDig("上传中...", false);
                            uploadImages(map);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissDig();
                        toast("图片压缩失败");

                    }
                }).launch();
    }


    private void zhifubao() {
        OkGo.<String>get("http://47.100.160.168:819/index.php/index/index/zfb_pay")
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (response.code() == 200) {
                            LogUtils.d("数据是支付宝" + response.body());
                            goodinfo = response.body().toString();
                            start();
                        }
//
                    }
                });
    }

    private void uploadImages(
//            Map<String, File> map)
            List<File> map) {
        OkGo.<String>post("http://53i532.natappfree.cc/mall/addPhotos")
                .tag(this)
                .params("user_id", "1000000000")
                .params("token", "VVON8nUAEzg7fSsh4p194TBUQtgW-rVfU7xKzpyTWe8=")
                .addFileParams("file", map)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        LogUtils.d("数据是:" + response.body());
                        MutiltypePhotoResults results = GsonUtil.parseJsonWithGson(response.body(), MutiltypePhotoResults.class);
                        LogUtils.d("数据是" + results.getMeta().getMsg());
                        if (results.getMeta().getMsg().equals("OK")) {
                            LogUtils.d("数据是" + results.getMeta().getMsg());
                            dismissDig();
                        }
                    }
                });
    }

    /**
     * 分享
     */
    @Override
    public void takeShare(Object params) {
        if (params.toString().equals("1")) {// 微信
            shareMedia = SHARE_MEDIA.WEIXIN;
        } else if (params.toString().equals("2")) {// 朋友圈
            shareMedia = SHARE_MEDIA.WEIXIN_CIRCLE;
        } else if (params.toString().equals("3")) {// QQ
            shareMedia = SHARE_MEDIA.QQ;
        }

        new ShareAction(MainActivity.this).withText("你好吗")
                .setPlatform(shareMedia)
                .setCallback(new UMShareListener() {
                    @Override
                    public void onStart(SHARE_MEDIA share_media) {
                        LogUtils.d("分享的是:" + share_media.toString());
                    }

                    @Override
                    public void onResult(SHARE_MEDIA share_media) {
                        LogUtils.d("分享的是:" + share_media.toString());
                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                        LogUtils.d("分享的是:" + share_media.toString());
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media) {
                        LogUtils.d("分享的是:" + share_media.toString());
                    }
                }).share();

    }

    /**
     * 复制
     */
    @Override
    public void takeCopy(Object params) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        //创建ClipData对象
        ClipData clipData = ClipData.newPlainText("tahome text copy", params.toString());
        //添加ClipData对象到剪切板中
        clipboardManager.setPrimaryClip(clipData);

        toast("已复制内容到剪切板");

    }

    @Override
    public void takeExit(Object params) {
//        int flag = (int) params;
//        if (flag == 0) {
//            //不能退出,返回上一页
//            if (dWebView != null && dWebView.canGoBack()) {
//                dWebView.goBack();
//            }
//        } else {
//            doubleExit();
//        }
        doubleExit();
    }

    private long mExitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            与上次点击返回键时刻作差
            String substring = dWebView.getUrl().substring(dWebView.getUrl().lastIndexOf("/") + 1, dWebView.getUrl().length());
            if (substring.equals("home") | substring.equals("verbal") | substring.equals("discussion") | substring.equals("mine")) {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    //大于2000ms则认为是误操作，使用Toast进行提示
                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    //并记录下本次点击“返回键”的时刻，以便下次进行判断
                    mExitTime = System.currentTimeMillis();
                } else {
                    //小于2000ms则认为是用户确实希望退出程序-调用System.exit()方法进行退出
                    System.exit(0);
                }
                return true;
            } else {
                if (dWebView != null && dWebView.canGoBack()) {
                    dWebView.goBack();
                    return true;
                }
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onStart(SHARE_MEDIA share_media) {
    }

    @Override
    public void onResult(SHARE_MEDIA share_media) {
        toast("分享成功");
    }

    @Override
    public void onError(SHARE_MEDIA share_media, Throwable throwable) {
        toast("分享失败");
    }

    @Override
    public void onCancel(SHARE_MEDIA share_media) {
    }

    @Override
    protected void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();

    }

    @Override
    protected void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        mAgentWeb.getWebLifeCycle().onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == IMAGE_PICKER) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                List<String> paths = new ArrayList<>();
                LogUtils.d("数据是" + images.size());
                for (ImageItem datae : images) {
                    String headImage = datae.path;
                    paths.add(headImage);
//                    String imagename = headImage.substring(headImage.lastIndexOf("/") + 1);
                    LogUtils.d("数据是:" + headImage);
//                    testokGO();
                }
                if (paths.size() == 1) {
                    LogUtils.d("数据是" + paths);
//                    uploadAvatar(paths.get(0));
//                    File file = new File(path);
//                    File newfile = CompressHelper.getDefault(context).compressToFile(file);
//                    dWebView.callHandler("getImg", new Object[]{content});
                } else if (paths.size() > 1) {
                    LogUtils.d("数据是多选" + paths);
                    compress(paths);
                }


            } else {
                Toast.makeText(this, "没有数据", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                LogUtils.d("扫一扫：" + content);
                if (!content.isEmpty()) {
                    dWebView.callHandler("qr_code_callback", new Object[]{content});
                }
//                int friendsid=Integer.valueOf(content.substring(0,content.indexOf("<")));
//                LogUtils.d("扫一扫"+friendsid);
//                int grid=Integer.valueOf(content.substring(content.indexOf("<")+1,content.lastIndexOf("<")));
//                LogUtils.d("扫一扫"+grid);
//                String reamrks=content.substring(content.lastIndexOf("<")+1,content.length());
//                LogUtils.d("扫一扫"+reamrks);
//                addfriends(friendsid,grid,reamrks);
            }
        } else if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_UPLOAD_HEADIMG:
                    // 图片、视频、音频选择结果回调
                    selectList = PictureSelector.obtainMultipleResult(data);
                    LogUtils.d("选择图片是" + selectList.get(0).getPath());
                    if (selectList.size() > 0) {
                        for (LocalMedia media : selectList) {
                            LogUtils.d("裁剪的数据是" + media.getCutPath());
                            LogUtils.d("未裁剪的数据是" + media.getPath());
                            if (media.isCut()) {
                                uploadAvatar(media.getCutPath());
                            } else {
                                uploadAvatar(media.getPath());
                            }
                        }
                    }
                    break;
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片、视频、音频选择结果回调
                    selectList = PictureSelector.obtainMultipleResult(data);
                    LogUtils.d("选择图片是" + selectList.get(0).getPath());
                    // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
                    if (selectList.size() > 0) {
                        for (LocalMedia media : selectList) {
                            Log.i("图片-----》", media.getPath());
                            if (media.isCut()) {
                                uploadAvatar(media.getCutPath());
                            } else {
                                uploadAvatar(media.getPath());
                            }
                        }
                    }
                    break;
            }
        }
    }

    private void testokGO() {
        OkGo.<String>post("http://39.105.148.182/qingniaozhongchou/wdt_showgoodsdetail.do")
                .params("goodsid", 13)
                .params("userid", 0)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
//                        TestGoodsBean s = GsonUtil.parseJsonWithGson(response.body(), GoodsDetails.class);
//                        LogUtils.d("数据是:" + s.getMsg() + s.getGoods());
                    }
                });
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        LogUtils.d("支付成功");
                        dWebView.callHandler("alipayMsg", new Object[]{"1"});
//                        finish();
//                        Intent jump_gouwuche = new Intent(GoToBuyActivity.this, MainActivity.class);
////                finish();
//                        jump_gouwuche.putExtra("jump_gouwuche", 0);
//                        startActivity(jump_gouwuche);
//                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
//                        Toast.makeText(GoToBuyActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
//                        Toast.makeText(GoToBuyActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                        LogUtils.d("支付失败");
                    }
                    break;
                }
//                case SDK_AUTH_FLAG: {
//                    @SuppressWarnings("unchecked")
//                    AuthResult authResult = new AuthResult((Map<String, String>) msg.obj, true);
//                    String resultStatus = authResult.getResultStatus();
//
//                    // 判断resultStatus 为“9000”且result_code
//                    // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
//                    if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.getResultCode(), "200")) {
//                        // 获取alipay_open_id，调支付时作为参数extern_token 的value
//                        // 传入，则支付账户为该授权账户
//                        Toast.makeText(PayDemoActivity.this,
//                                "授权成功\n" + String.format("authCode:%s", authResult.getAuthCode()), Toast.LENGTH_SHORT)
//                                .show();
//                    } else {
//                        // 其他状态值则为授权失败
//                        Toast.makeText(PayDemoActivity.this,
//                                "授权失败" + String.format("authCode:%s", authResult.getAuthCode()), Toast.LENGTH_SHORT).show();
//
//                    }
//                    break;
//                }
                default:
                    break;
            }
        }
    };

    private void start() {
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                Log.d("AAAAAAAAAAAA", goodinfo);
                PayTask alipay = new PayTask(MainActivity.this);
                Map<String, String> result = alipay.payV2(goodinfo, true);
                Log.i("msp", result.toString());
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    private void uploadAvatar(String path) {
        LogUtils.d("数据是" + path);
        File file = new File(path);
        File newfile = CompressHelper.getDefault(context).compressToFile(file);
        if (!newfile.exists()) {
            toast("图片不存在");
            return;
        }
        if ("1".equals(type)) {
            OkGo.<String>post(requestUrl)
                    .tag(this)
                    .isMultipart(true)
                    .params("file", newfile)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            if (response.code() == 200) {
                                String resultBody = response.body();
                                PhotoBean photoBean = GsonUtil.parseJsonWithGson(resultBody, PhotoBean.class);
                                if (photoBean != null) {
                                    if ("1".equals(photoBean.getCode()+"")&&photoBean.getMsg().equals("上传成功")) {
                                        OkGo.<String>post("http://xinlian.nxiapk.top/api/xuser/upload_qualification")
                                                .tag(this)
                                                .isMultipart(true)
                                                .params("url",photoBean.getUrl())
                                                .params("user_id",userid)
                                                .execute(new StringCallback() {
                                                    @Override
                                                    public void onSuccess(Response<String> response) {
                                                        String resultBody = response.body();
                                                        TypeBean photoBean = GsonUtil.parseJsonWithGson(resultBody, TypeBean.class);
                                                        if ("1".equals(photoBean.getCode()+"")){
                                                            toast(photoBean.getMsg());
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            LogUtils.d("失败是是" + response.message());
                        }
                    });
        } else {
            OkGo.<String>post(requestUrl)
                    .tag(this)
                    .isMultipart(true)
                    .params("icon_image", newfile)
                    .params("id", userid)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            if (response.code() == 200) {
                                selectList = new ArrayList<>();
                                String resultBody = response.body();
                                UploadImgBean photoBean = GsonUtil.parseJsonWithGson(resultBody, UploadImgBean.class);
                                if (photoBean != null && photoBean.getData() != null) {
                                    if ("1".equals(photoBean.getCode()+"")) {
                                        String resultData = photoBean.getData().getUrl();
                                        if (!TextUtils.isEmpty(resultData)) {
                                            dWebView.callHandler("addHeadImgCallback", new Object[]{resultData});
                                        } else {
                                            toast("上传失败");
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            LogUtils.d("失败是是" + response.message());
                        }
                    });
        }
    }

    private final int HANDLER_REFRESH_HEADIMG = 112;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_REFRESH_HEADIMG:
                    dWebView.callHandler("addCardImgCallback", new Object[]{"123123"});
//                    dWebView.callHandler("addCardImgCallback", new Object[]{resultStr});
                    break;
            }
        }
    };

    /**
     * 下载图片
     */
    @Override
    public void saveImg(Object params) {
        String imgUrl = params.toString();
        OkGo.<File>get(imgUrl)
                .tag(this)
                .execute(new FileCallback(FILE_DIR, Utils.getFileName(imgUrl)) {
                    @Override
                    public void onSuccess(Response<File> response) {
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.fromFile(new File(response.body().getPath()))));
                        toast("已保存到本地");
                    }

                    @Override
                    public void onError(Response<File> response) {
                        super.onError(response);
                        toast("保存出错");
                    }
                });
    }

    String FILE_DIR = Environment.getExternalStorageDirectory().getPath() + "/wlgj/picture/";

    public void saveImg1(Object params) {
        String fileUrl = params.toString();
        String destFileName = String.valueOf(System.currentTimeMillis());
        String mDestFileName = fileUrl.substring(fileUrl.lastIndexOf("."), fileUrl.length());
        OkGo.<File>get(fileUrl).tag(this).execute(new FileCallback(FILE_DIR, destFileName + mDestFileName) { //文件下载时指定下载的路径以及下载的文件的名称
            @Override
            public void onStart(Request<File, ? extends Request> request) {
                super.onStart(request);
                LogUtils.e("开始下载文件" + "DDDDD");
            }

            @Override
            public void onSuccess(com.lzy.okgo.model.Response<File> response) {
                LogUtils.e("下载文件成功" + "DDDDD" + response.body().length());
                String mBasePath = response.body().getAbsolutePath();
                File f_path = new File(mBasePath);
                if (f_path != null && f_path.exists() && f_path.isFile()) {
                    try {
                        saveBitmap(MainActivity.this, mBasePath, f_path.getName());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                LogUtils.e("下载文件完成" + "DDDDD");
            }

            @Override
            public void onError(com.lzy.okgo.model.Response<File> response) {
                super.onError(response);
                LogUtils.e("下载文件出错" + "DDDDD" + response.message());
            }

            @Override
            public void downloadProgress(Progress progress) {
                super.downloadProgress(progress);
                float dLProgress = progress.fraction;
                LogUtils.e("文件下载的进度" + "DDDDD" + dLProgress);
            }
        });
    }

    private void saveBitmap(Context context, String imagePath, String imagename) throws IOException {
        try {
            ContentResolver cr = context.getContentResolver();
            String url = MediaStore.Images.Media.insertImage(cr, imagePath, imagename, "");
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(new File(url));
            intent.setData(uri);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void zfb_pay(Object params) {
        LogUtils.d("数据是" + params.toString());
        goodinfo = params.toString();
        start();
    }

    @Override
    public void wx_pay(Object params) {
        if (params != null) {
            WxPayParams wxPayParams = GsonUtil.parseJsonWithGson(params.toString(), WxPayParams.class);
            wxPay(wxPayParams);
        }
    }

    @Override
    public void uploadHeadImg(Object params) {
        LogUtils.d("数据是" + params.toString());
        if (params != null) {
            userid = params.toString();
        }
        type = "2";
        requestUrl = "http://xinlian.nxiapk.top/api/xuser/editrecommendpost";
        initPictandVideo();
    }

    /**
     * 微信支付
     *
     * @param params
     */
    private void wxPay(WxPayParams params) {
        PayReq req = new PayReq();
        req.appId = params.getAppId();
        req.partnerId = params.getPartnerId();
        req.prepayId = params.getPrepayId();
        req.packageValue = params.getPackages();
        req.nonceStr = params.getNonceStr();
        req.timeStamp = params.getTimeStamp();
        req.sign = params.getSign();
        api.sendReq(req);
    }

}
