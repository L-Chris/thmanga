package com.haleydu.cimoc.core;

//import com.alibaba.fastjson.JSONObject;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.haleydu.cimoc.App;
import com.haleydu.cimoc.R;
import com.king.app.dialog.AppDialog;
import com.king.app.updater.AppUpdater;
import com.king.app.updater.http.OkHttpManager;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

//import com.azhon.appupdate.config.UpdateConfiguration;
//import com.azhon.appupdate.manager.DownloadManager;

/**
 * Created by Hiroshi on 2016/8/24.
 */
public class Update {

    private static final String UPDATE_URL = "https://api.github.com/repos/Haleydu/cimoc/releases/latest";
    private static final String UPDATE_URL_GITHUB = "https://raw.githubusercontent.com/Haleydu/update/master/Update.json";
    private static final String UPDATE_URL_GITEE = "https://gitee.com/Haleydu/update/raw/master/Update.json";
    private static final String SERVER_FILENAME = "tag_name";
    private AppUpdater mAppUpdater;
//    private static final String LIST = "list";

    public static Observable<String> check() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                OkHttpClient client = App.getHttpClient();
                Request request = new Request.Builder().url(UPDATE_URL).build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String json = response.body().string();
//                        JSONObject object = new JSONObject(json).getJSONArray(LIST).getJSONObject(0);
                        String version = new JSONObject(json).getString(SERVER_FILENAME);
                        subscriber.onNext(version);
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (response != null) {
                        response.close();
                    }
                }
                subscriber.onError(new Exception());
            }
        }).subscribeOn(Schedulers.io());
    }


//    @SuppressLint("DefaultLocale")
//    public static boolean update(Context context) {
//        try {
////                DownloadManager.getInstance().release();
//            JSONObject updateObject = JSON.parseObject(Update.getUpdateJson());
//            JSONObject updateAssetsObject = updateObject.getJSONArray(ASSETS).getJSONObject(0);
//
//            UpdateConfiguration configuration = new UpdateConfiguration()
//                    //??????????????????
//                    .setEnableLog(true)
//                    //????????????????????????
//                    //.setHttpManager()
//                    //????????????????????????????????????
//                    .setJumpInstallPage(true)
//                    //??????????????????????????? (??????????????????demo???????????????)
//                    .setDialogImage(R.drawable.ic_dialog_download_top_3)
//                    //?????????????????????
//                    .setDialogButtonColor(Color.parseColor("#39c1e9"))
//                    //???????????????????????????
//                    .setDialogButtonTextColor(Color.WHITE)
//                    //??????????????????
//                    .setBreakpointDownload(true)
//                    //?????????????????????????????????
//                    .setShowNotification(true)
//                    //??????????????????
//                    .setForcedUpgrade(false);
//
//            DownloadManager manager = DownloadManager.getInstance(context);
//            manager.setApkName("Comic." + updateObject.getString(NAME) + ".release.apk")
//                    .setApkUrl(updateAssetsObject.getString("browser_download_url"))
//                    .setDownloadPath(Environment.getExternalStorageDirectory() + "/Download")
//                    .setApkDescription(updateObject.getString("body"))
//                    .setSmallIcon(R.mipmap.ic_launcher_blue_foreground)
//                    .setShowNewerToast(true)
//                    .setConfiguration(configuration)
//                    .setApkVersionCode(2)
//                    .setApkVersionName(updateObject.getString(TAG_NAME).substring(1));
//
//            if (App.getUpdateCurrentUrl().equals(Constants.UPDATE_GITEE_URL)) {
//                manager.download();
//            } else {
//                manager.setApkSize(String.format("%.2f", updateAssetsObject.getDouble("size") / (1024 * 1024)))
//                        .download();
//            }
//
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public static String getUpdateJson() {
//        return updateJson;
//    }

    public static Observable<String> checkGitee() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                OkHttpClient client = App.getHttpClient();
                Request request = new Request.Builder().url(UPDATE_URL_GITEE).build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String json = response.body().string();
                        subscriber.onNext(json);
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (response != null) {
                        response.close();
                    }
                }
                subscriber.onError(new Exception());
            }
        }).subscribeOn(Schedulers.io());
    }

    public void startUpdate(String versionName, String content, String mUrl, int versionCode, String md5) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update, null);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.main_start_update);
        tvTitle.append(versionName);
        TextView tvContent = view.findViewById(R.id.tvContent);
        tvContent.setText(content);

        Button btnCancel = view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppDialog.INSTANCE.dismissDialog();
            }
        });
        Button btnOK = view.findViewById(R.id.btnOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAppUpdater = new AppUpdater.Builder()
//                        .setPath(Environment.getExternalStorageDirectory() + "/.AppUpdater")//????????????Android Q??????Environment.getExternalStorageDirectory()?????????
//                        .setPath(getExternalFilesDir(Constants.DEFAULT_DIR).getAbsolutePath())//????????????????????????????????????
                        .setApkMD5(md5)//??????MD5?????????????????????APK???MD5??????MD5??????????????????????????????????????????????????????MD5???????????????
                        .serUrl(mUrl)
                        .setVersionCode(versionCode)//??????versionCode???????????????versionCode??????????????????versionCode?????????apk???????????????,?????????????????????,????????????MD5???????????????
                        .setVibrate(true)  //??????
                        .setFilename("Cimoc_"+versionName+".apk")
                        .build(getContext());

                mAppUpdater.setHttpManager(OkHttpManager.getInstance()).start();
                AppDialog.INSTANCE.dismissDialog();
            }
        });

        AppDialog.INSTANCE.showDialog(getContext(), view);
    }

    public Context getContext() {
        return App.getActivity();
    }
}
