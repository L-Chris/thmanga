package com.haleydu.cimoc.presenter;

import android.util.Log;

import com.haleydu.cimoc.App;
import com.haleydu.cimoc.core.Update;
import com.haleydu.cimoc.helper.UpdateHelper;
import com.haleydu.cimoc.manager.ComicManager;
import com.haleydu.cimoc.model.Comic;
import com.haleydu.cimoc.model.MiniComic;
import com.haleydu.cimoc.model.Source;
import com.haleydu.cimoc.rx.RxEvent;
import com.haleydu.cimoc.ui.view.MainView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public class MainPresenter extends BasePresenter<MainView> {

    private ComicManager mComicManager;
    private static final String APP_VERSION_NAME = "versionName";
    private static final String APP_VERSION_CODE = "versionCode";
    private static final String APP_CONTENT = "content";
    private static final String APP_MD5 = "md5";
    private static final String APP_URL= "url";

    private static final String SOURCE_URL = "https://gitee.com/Haleydu/update/raw/master/sourceBaseUrl.json";

    @Override
    protected void onViewAttach() {
        mComicManager = ComicManager.getInstance(mBaseView);
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_COMIC_READ, rxEvent -> {
            MiniComic comic = (MiniComic) rxEvent.getData();
            mBaseView.onLastChange(comic.getId(), comic.getSource(), comic.getCid(),
                    comic.getTitle(), comic.getCover());
        });
    }

    public boolean checkLocal(long id) {
        Comic comic = mComicManager.load(id);
        return comic != null && comic.getLocal();
    }

    public void loadLast() {
        mCompositeSubscription.add(mComicManager.loadLast()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comic -> {
                    if (comic != null) {
                        mBaseView.onLastLoadSuccess(comic.getId(), comic.getSource(), comic.getCid(), comic.getTitle(), comic.getCover());
                    }
                }, throwable -> mBaseView.onLastLoadFail()));
    }

    public void checkUpdate(final String version) {
        mCompositeSubscription.add(Update.check()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    if (-1 == version.indexOf(s) && -1 == version.indexOf("t")) {
                        mBaseView.onUpdateReady();
                    }
                }, throwable -> {
                }));
    }

    public void checkGiteeUpdate(final int appVersionCode) {
        mCompositeSubscription.add(Update.checkGitee()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(json -> {
                    try {
                        String versionName = new JSONObject(json).getString(APP_VERSION_NAME);
                        String versionCodeString = new JSONObject(json).getString(APP_VERSION_CODE);
                        int ServerAppVersionCode = Integer.parseInt(versionCodeString);
                        String content = new JSONObject(json).getString(APP_CONTENT);
                        String md5 = new JSONObject(json).getString(APP_MD5);
                        String url = new JSONObject(json).getString(APP_URL);
                        if (appVersionCode < ServerAppVersionCode) {
                            mBaseView.onUpdateReady(versionName,content,url,ServerAppVersionCode,md5);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                    }
                }));
    }


    public void
    getSourceBaseUrl() {
        mCompositeSubscription.add(
                Observable.create((Observable.OnSubscribe<String>) subscriber -> {
                    OkHttpClient client = App.getHttpClient();
                    Request request = new Request.Builder().url(SOURCE_URL).build();
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
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(jsonStr -> {
                            try {
                                JSONObject json = new JSONObject(jsonStr);
                                App app = (App)App.getAppContext();
                                List<Source> list = new ArrayList<>();

                                Iterator<String> keys = json.keys();

                                while(keys.hasNext()) {
                                    String key = keys.next();
                                    Object item = json.get(key);
                                    if (item instanceof JSONObject) {
                                        Log.d("test", item.toString());
                                    } else if (item instanceof String) {

                                    }
                                }

                                UpdateHelper.update(App.getPreferenceManager(), app.getDaoSession(), list);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }, throwable -> {
                        }));
    }
}
