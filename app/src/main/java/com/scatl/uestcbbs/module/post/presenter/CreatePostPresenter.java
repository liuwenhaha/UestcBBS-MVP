package com.scatl.uestcbbs.module.post.presenter;

import android.content.Context;
import android.text.TextUtils;

import androidx.fragment.app.FragmentActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.scatl.uestcbbs.api.ApiConstant;
import com.scatl.uestcbbs.base.BasePresenter;
import com.scatl.uestcbbs.callback.OnPermission;
import com.scatl.uestcbbs.custom.posteditor.ContentEditor;
import com.scatl.uestcbbs.entity.SendPostBean;
import com.scatl.uestcbbs.entity.UploadResultBean;
import com.scatl.uestcbbs.helper.ExceptionHelper;
import com.scatl.uestcbbs.helper.rxhelper.Observer;
import com.scatl.uestcbbs.helper.rxhelper.SubscriptionManager;
import com.scatl.uestcbbs.module.post.model.PostModel;
import com.scatl.uestcbbs.module.post.view.CreatePostView;
import com.scatl.uestcbbs.util.CommonUtil;
import com.scatl.uestcbbs.util.Constant;
import com.scatl.uestcbbs.util.SharePrefUtil;
import com.scatl.uestcbbs.util.TimeUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * author: sca_tl
 * description:
 * date: 2020/2/9 13:52
 */
public class CreatePostPresenter extends BasePresenter<CreatePostView> {

    PostModel postModel = new PostModel();

    public void sendPost(ContentEditor contentEditor,
                         int boardId,
                         int filterId,
                         String title,
                         List<String> imgUrls,
                         List<Integer> imgIds,
                         List<String> pollOptions,
                         int pollChoices,
                         int pollExp,
                         boolean pollVisible,
                         boolean pollShowVoters,
                         Context context) {

        JSONObject json = new JSONObject();

        if (pollOptions != null && pollOptions.size() != 0) {
            JSONObject pollInfo = new JSONObject();
            pollInfo.put("expiration", pollExp);
            pollInfo.put("maxChoices", pollChoices);
            pollInfo.put("visibleAfterVote", pollVisible);
            pollInfo.put("showVoters", pollShowVoters);

            JSONArray op = new JSONArray();
            op.addAll(pollOptions);
            pollInfo.put("options", op);

            json.put("poll", pollInfo);
        }

        json.put("fid", boardId);
        json.put("typeId", filterId);
        json.put("title", title);
        json.put("isQuote", 0);
        json.put("aid", imgIds.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(" ", ""));

        JSONArray jsonArray = new JSONArray();
        int img_index = 0;
        for (int i = 0; i < contentEditor.buildEditorData().size(); i ++) {
            JSONObject jsonObject = new JSONObject();
            if (contentEditor.buildEditorData().get(i).content_type == 0) {
                jsonObject.put("type", 0);
                jsonObject.put("infor", contentEditor.buildEditorData().get(i).inputStr);
                jsonArray.add(jsonObject);
            }

            if (contentEditor.buildEditorData().get(i).content_type == 1) {
                jsonObject.put("type", 1);
                jsonObject.put("infor", imgUrls.get(img_index));
                jsonArray.add(jsonObject);
                img_index = img_index + 1;
            }
        }

        json.put("content", jsonArray.toJSONString());

        JSONObject body = new JSONObject();
        body.put("json", json);

        JSONObject json_ = new JSONObject();
        json_.put("body", body);

        postModel.setPost("new",
                json_.toJSONString(),
                SharePrefUtil.getToken(context),
                SharePrefUtil.getSecret(context),
                new Observer<SendPostBean>() {
                    @Override
                    public void OnSuccess(SendPostBean sendPostBean) {
                        if (sendPostBean.rs == ApiConstant.Code.SUCCESS_CODE) {
                            view.onSendPostSuccess(sendPostBean);
                        }
                        if (sendPostBean.rs == ApiConstant.Code.ERROR_CODE) {
                            view.onSendPostError(sendPostBean.head.errInfo);
                        }
                    }

                    @Override
                    public void onError(ExceptionHelper.ResponseThrowable e) {
                        view.onSendPostError(e.message);
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
     * description: 压缩图片
     */
    public void compressImage(Context context, List<String> files) {
        List<File> successFile = new ArrayList<>();
        Luban
                .with(context)
                .load(files)
                .ignoreBy(1)
                .setTargetDir(context.getExternalFilesDir(Constant.AppPath.TEMP_PATH).getAbsolutePath())
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() { }

                    @Override
                    public void onSuccess(File file) {
                        successFile.add(file);
                        if (successFile.size() == files.size()) {
                            view.onCompressImageSuccess(successFile);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.onCompressImageFail(e.getMessage());
                    }
                })
                .launch();
    }

    /**
     * author: sca_tl
     * description: 上传图片
     * FIXME 使用retrofit上传图片返回的结果没有图片信息。先使用这种方法，有时间再研究一下
     */
    public void upload(List<File> files,
                       String module,
                       String type,
                       Context context) {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20000L, TimeUnit.MILLISECONDS)
                .readTimeout(20000L, TimeUnit.MILLISECONDS)
                .writeTimeout(20000L, TimeUnit.MILLISECONDS)
                .build();
        OkHttpUtils.initClient(okHttpClient);

        Map<String, String> map = new HashMap<>();
        map.put("module", module);
        map.put("type", type);
        map.put("accessToken", SharePrefUtil.getToken(context));
        map.put("accessSecret", SharePrefUtil.getSecret(context));

        PostFormBuilder postFormBuilder = OkHttpUtils.post();
        for (int i = 0; i < files.size(); i ++) {
            postFormBuilder.addFile("uploadFile[]", files.get(i).getName(), files.get(i));
        }

        postFormBuilder
                .url(ApiConstant.BBS_BASE_URL + ApiConstant.SendMessage.UPLOAD_IMG)
                .params(map)
                .addHeader("content-type","multipart/form-data")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        view.onUploadError(e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        if (JSONObject.isValidObject(response)) {
                            UploadResultBean uploadResultBean = JSON.toJavaObject(JSONObject.parseObject(response), UploadResultBean.class);
                            if (uploadResultBean.rs == ApiConstant.Code.SUCCESS_CODE) {
                                view.onUploadSuccess(uploadResultBean);
                            }

                            if (uploadResultBean.rs == ApiConstant.Code.ERROR_CODE) {
                                view.onUploadError(uploadResultBean.head.errInfo);
                            }
                        }
                    }
                });


//        postModel.upload(files,module, type,
//                SharePrefUtil.getToken(context),
//                SharePrefUtil.getSecret(context), new Observer<UploadResultBean>() {
//            @Override
//            public void OnSuccess(UploadResultBean uploadBean) {
//                if (uploadBean.rs == ApiConstant.Code.SUCCESS_CODE) {
//                    view.onUploadSuccess(uploadBean);
//                }
//                if (uploadBean.rs == ApiConstant.Code.ERROR_CODE) {
//                    view.onUploadError(uploadBean.head.errInfo);
//                }
//            }
//
//            @Override
//            public void onError(ExceptionHelper.ResponseThrowable e) {
//                view.onUploadError(e.message);
//            }
//
//            @Override
//            public void OnCompleted() {
//
//            }
//
//            @Override
//            public void OnDisposable(Disposable d) {
//                SubscriptionManager.getInstance().add(d);
//            }
//        });
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

//    /**
//     * author: sca_tl
//     * description: 退出编辑警告
//     */
//    public void checkBeforeExit(Context context, boolean empty) {
//        if (!empty) {
//            final AlertDialog dialog = new AlertDialog.Builder(context)
//                    .setNegativeButton("确认退出", null)
//                    .setPositiveButton("继续编辑", null )
//                    .setTitle("退出编辑")
//                    .setMessage("确认退出编辑吗？你将丢失已经编辑的内容")
//                    .create();
//            dialog.setOnShowListener(dialogInterface -> {
//                Button n = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
//                n.setOnClickListener(v -> {
//                    dialog.dismiss();
//                    view.onExit();
//                });
//            });
//            dialog.show();
//        } else {
//            view.onExit();
//        }
//    }

}
