package com.scatl.uestcbbs.module.home.presenter;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.scatl.uestcbbs.api.ApiConstant;
import com.scatl.uestcbbs.base.BasePresenter;
import com.scatl.uestcbbs.callback.OnPermission;
import com.scatl.uestcbbs.entity.BingPicBean;
import com.scatl.uestcbbs.entity.HotPostBean;
import com.scatl.uestcbbs.entity.SimplePostListBean;
import com.scatl.uestcbbs.helper.ExceptionHelper;
import com.scatl.uestcbbs.helper.rxhelper.Observer;
import com.scatl.uestcbbs.helper.rxhelper.SubscriptionManager;
import com.scatl.uestcbbs.module.home.model.HomeModel;
import com.scatl.uestcbbs.module.home.view.HomeFragment;
import com.scatl.uestcbbs.module.home.view.HomeView;
import com.scatl.uestcbbs.util.CommonUtil;
import com.scatl.uestcbbs.util.SharePrefUtil;

import io.reactivex.disposables.Disposable;

public class HomePresenter extends BasePresenter<HomeView> {

    private HomeModel homeModel = new HomeModel();

    public void getBannerData() {
        homeModel.getBannerData(new Observer<BingPicBean>() {
            @Override
            public void OnSuccess(BingPicBean bingPicBean) {
                view.getBannerDataSuccess(bingPicBean);
            }

            @Override
            public void onError(ExceptionHelper.ResponseThrowable e) {

            }

            @Override
            public void OnCompleted() {

            }

            @Override
            public void OnDisposable(Disposable d) {
                SubscriptionManager.getInstance().add(d);
            }
        });
    }

    public void getSimplePostList(int page, int pageSize, String sortby, Context context){
        homeModel.getSimplePostList(page, pageSize, 0, sortby,
                SharePrefUtil.getToken(context),
                SharePrefUtil.getSecret(context), new Observer<SimplePostListBean>() {
                    @Override
                    public void OnSuccess(SimplePostListBean simplePostListBean) {
                        if (simplePostListBean.rs == ApiConstant.Code.SUCCESS_CODE) {
                            view.getSimplePostDataSuccess(simplePostListBean);
                        }
                        if (simplePostListBean.rs == ApiConstant.Code.ERROR_CODE) {
                            view.getSimplePostDataError(simplePostListBean.head.errInfo);
                        }
                    }

                    @Override
                    public void onError(ExceptionHelper.ResponseThrowable e) {
                        view.getSimplePostDataError(e.message);
                    }

                    @Override
                    public void OnCompleted() {

                    }

                    @Override
                    public void OnDisposable(Disposable d) {
                        SubscriptionManager.getInstance().add(d);
                    }
                });
    }

    /**
     * author: sca_tl
     * description: 请求权限
     */
    public void requestPermission(FragmentActivity activity, final int action, String... permissions) {
        CommonUtil.requestPermission(activity, new OnPermission() {
            @Override
            public void onGranted() {
                view.onPermissionGranted(action);
            }

            @Override
            public void onRefusedWithNoMoreRequest() {
                view.onPermissionRefusedWithNoMoreRequest();
            }

            @Override
            public void onRefused() {
                view.onPermissionRefused();
            }
        }, permissions);
    }

    public void downDailyPicConfirm(FragmentActivity context) {
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setPositiveButton("下载", null)
                .setNegativeButton("取消", null )
                .setTitle("下载图片")
                .setMessage("确认要下载该图片吗？图片资源来自：https://cn.bing.com/")
                .create();
        dialog.setOnShowListener(dialogInterface -> {
            Button p = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            p.setOnClickListener(v -> {
                requestPermission(context,
                        HomeFragment.DOWNLOAD_PIC, Manifest.permission.READ_EXTERNAL_STORAGE);
                dialog.dismiss();

            });
        });
        dialog.show();
    }

    public void downDailyPic(Context context, String imgUrl, String imgCopyRight) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imgUrl));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, imgCopyRight.replace("/", "_") + ".jpg");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE
                | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        if (downloadManager != null) downloadManager.enqueue(request);
    }


}
