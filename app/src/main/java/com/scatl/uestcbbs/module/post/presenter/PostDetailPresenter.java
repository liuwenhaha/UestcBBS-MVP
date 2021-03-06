package com.scatl.uestcbbs.module.post.presenter;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.scatl.uestcbbs.R;
import com.scatl.uestcbbs.api.ApiConstant;
import com.scatl.uestcbbs.base.BasePresenter;
import com.scatl.uestcbbs.custom.imageview.CircleImageView;
import com.scatl.uestcbbs.custom.postview.ContentView;
import com.scatl.uestcbbs.entity.ContentViewBean;
import com.scatl.uestcbbs.entity.FavoritePostResultBean;
import com.scatl.uestcbbs.entity.PostDetailBean;
import com.scatl.uestcbbs.entity.SupportResultBean;
import com.scatl.uestcbbs.entity.VoteResultBean;
import com.scatl.uestcbbs.helper.ExceptionHelper;
import com.scatl.uestcbbs.helper.glidehelper.GlideLoader4Common;
import com.scatl.uestcbbs.helper.rxhelper.Observer;
import com.scatl.uestcbbs.helper.rxhelper.SubscriptionManager;
import com.scatl.uestcbbs.module.post.model.PostModel;
import com.scatl.uestcbbs.module.post.view.PostDetailActivity;
import com.scatl.uestcbbs.module.post.view.PostDetailView;
import com.scatl.uestcbbs.module.user.view.UserDetailActivity;
import com.scatl.uestcbbs.module.webview.view.WebViewActivity;
import com.scatl.uestcbbs.util.Constant;
import com.scatl.uestcbbs.util.JsonUtil;
import com.scatl.uestcbbs.util.SharePrefUtil;
import com.scatl.uestcbbs.util.TimeUtil;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.disposables.Disposable;
import retrofit2.HttpException;

/**
 * author: sca_tl
 * description:
 * date: 2020/1/24 14:33
 */
public class PostDetailPresenter extends BasePresenter<PostDetailView> {

    private PostModel postModel = new PostModel();

    public void getPostDetail(int page,
                              int pageSize,
                              int order,
                              int topicId,
                              int authorId,
                              Context context) {
        postModel.getPostDetail(page, pageSize, order, topicId, authorId,
                SharePrefUtil.getToken(context),
                SharePrefUtil.getSecret(context),
                new Observer<PostDetailBean>() {
                    @Override
                    public void OnSuccess(PostDetailBean postDetailBean) {
                        if (postDetailBean.rs == ApiConstant.Code.SUCCESS_CODE) {
                            view.onGetPostDetailSuccess(postDetailBean);
                        }

                        if (postDetailBean.rs == ApiConstant.Code.ERROR_CODE) {
                            view.onGetPostDetailError(postDetailBean.head.errInfo);
                        }
                    }

                    @Override
                    public void onError(ExceptionHelper.ResponseThrowable e) {
                        view.onGetPostDetailError(e.message);
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

    public void favorite(String idType,
                         String action,
                         int id,
                         Context context) {
        postModel.favorite(idType, action, id,
                SharePrefUtil.getToken(context),
                SharePrefUtil.getSecret(context),
                new Observer<FavoritePostResultBean>() {
                    @Override
                    public void OnSuccess(FavoritePostResultBean favoritePostResultBean) {
                        if (favoritePostResultBean.rs == ApiConstant.Code.SUCCESS_CODE) {
                            view.onFavoritePostSuccess(favoritePostResultBean);
                        }

                        if (favoritePostResultBean.rs == ApiConstant.Code.ERROR_CODE) {
                            view.onFavoritePostError(favoritePostResultBean.head.errInfo);
                        }
                    }

                    @Override
                    public void onError(ExceptionHelper.ResponseThrowable e) {
                        view.onGetPostDetailError(e.message);
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


    public void support(int tid,
                        int pid,
                        String type,
                        Context context) {
        postModel.support(tid, pid, type,
                SharePrefUtil.getToken(context),
                SharePrefUtil.getSecret(context),
                new Observer<SupportResultBean>() {
                    @Override
                    public void OnSuccess(SupportResultBean supportResultBean) {
                        if (supportResultBean.rs == ApiConstant.Code.SUCCESS_CODE) {
                            view.onSupportSuccess(supportResultBean);
                        }

                        if (supportResultBean.rs == ApiConstant.Code.ERROR_CODE) {
                            view.onSupportError(supportResultBean.head.errInfo);
                        }
                    }

                    @Override
                    public void onError(ExceptionHelper.ResponseThrowable e) {
                        view.onGetPostDetailError(e.message);
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

    public void vote(int tid,
                     int boardId,
                     int max,
                     List<Integer> options,
                     Context context) {

        if (options.size() == 0) {
            view.onVoteError("至少选择1项");
        } else if (options.size() > max) {
            view.onVoteError("至多选择" + max + "项");
        } else {

            postModel.vote(tid, boardId, options.toString().replace("[", "").replace("]", ""),
                    SharePrefUtil.getToken(context),
                    SharePrefUtil.getSecret(context),
                    new Observer<VoteResultBean>() {
                        @Override
                        public void OnSuccess(VoteResultBean voteResultBean) {
                            if (voteResultBean.rs == ApiConstant.Code.SUCCESS_CODE) {
                                view.onVoteSuccess(voteResultBean);
                            }

                            if (voteResultBean.rs == ApiConstant.Code.ERROR_CODE) {
                                view.onVoteError(voteResultBean.head.errInfo);
                            }
                        }

                        @Override
                        public void onError(ExceptionHelper.ResponseThrowable e) {
                            view.onVoteError(e.message);
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


    }

    /**
     * author: sca_tl
     * description: 展示帖子基本信息（除去评论）
     */
    public void setBasicData(Activity activity, View basicView, PostDetailBean postDetailBean) {

        CircleImageView userAvatar = basicView.findViewById(R.id.post_detail_item_content_view_author_avatar);
        TextView postTitle = basicView.findViewById(R.id.post_detail_item_content_view_title);
        TextView userName = basicView.findViewById(R.id.post_detail_item_content_view_author_name);
        TextView userLevel = basicView.findViewById(R.id.post_detail_item_content_view_author_level);
        TextView time = basicView.findViewById(R.id.post_detail_item_content_view_time);
        ContentView contentView = basicView.findViewById(R.id.post_detail_item_content_view_content);

        contentView.setContentData(JsonUtil.modelListA2B(postDetailBean.topic.content, ContentViewBean.class, postDetailBean.topic.content.size()));

        postTitle.setText(postDetailBean.topic.title);
        userName.setText(postDetailBean.topic.user_nick_name);

        if (! activity.isFinishing()) {
            Glide.with(activity).load(postDetailBean.topic.icon).into(userAvatar);
        }

        time.setText(TimeUtil.formatTime(postDetailBean.topic.create_date, R.string.post_time1, activity
        ));

        if (!TextUtils.isEmpty(postDetailBean.topic.userTitle)) {
            Matcher matcher = Pattern.compile("(.*?)\\((Lv\\..*)\\)").matcher(postDetailBean.topic.userTitle);
            if (matcher.find()) {
                String level = matcher.group(2);
                userLevel.setText(level);

            } else {
                userLevel.setText(postDetailBean.topic.userTitle);
            }
            userLevel.setBackgroundResource(R.drawable.shape_post_detail_user_level);
        } else {
            userLevel.setText(postDetailBean.topic.user_nick_name);
            userLevel.setBackgroundResource(R.drawable.shape_post_detail_user_level);
        }

        //若是投票帖
        if (postDetailBean.topic.vote == 1) {
            contentView.setVoteBean(postDetailBean.topic.poll_info);
        }
    }

    /**
     * author: sca_tl
     * description: 展示表达意见的用户
     */
    public void setZanView(Context context, View zanView, PostDetailBean postDetailBean) {
        TagFlowLayout zanFlowLayout = zanView.findViewById(R.id.post_detail_item_zanlist_view_taglayout);
        TextView zanViewTitle = zanView.findViewById(R.id.post_detail_item_zanlist_view_title);
        if (postDetailBean.topic.zanList == null || postDetailBean.topic.zanList.size() == 0) {
            zanView.setVisibility(View.GONE);
        } else {
            zanView.setVisibility(View.VISIBLE);
            zanViewTitle.setText("•支持或反对(" + postDetailBean.topic.zanList.size() + ")•");

            zanFlowLayout.setAdapter(new TagAdapter<PostDetailBean.TopicBean.ZanListBean>(postDetailBean.topic.zanList) {
                @Override
                public View getView(FlowLayout parent, int position, PostDetailBean.TopicBean.ZanListBean o) {
                    View view = LayoutInflater.from(context)
                            .inflate(R.layout.item_post_detail_item_zanlist_view, new LinearLayout(context));
                    CircleImageView imageView = view.findViewById(R.id.item_post_detail_item_zanlist_view_avatar);
                    TextView textView = view.findViewById(R.id.item_post_detail_item_zanlist_view_name);
                    GlideLoader4Common.simpleLoad(context, context.getString(R.string.icon_url, Integer.valueOf(o.recommenduid)), imageView);
                    textView.setText(o.username);
                    return view;
                }
            });
            zanFlowLayout.setOnTagClickListener((view, position, parent) -> {
                Intent intent = new Intent(context, UserDetailActivity.class);
                intent.putExtra(Constant.IntentKey.USER_ID, Integer.valueOf(postDetailBean.topic.zanList.get(position).recommenduid));
                context.startActivity(intent);
                return true;
            });

        }
    }

    /**
     * author: sca_tl
     * description: 展示评分用户
     */
    public void setRateData(Context context, View rateView, PostDetailBean postDetailBean) {
        TextView rateViewTitle = rateView.findViewById(R.id.post_detail_item_rate_view_title);
        TextView shuidiNum = rateView.findViewById(R.id.post_detail_item_rate_view_shuidi_num);
        LinearLayout shuidiLayout = rateView.findViewById(R.id.post_detail_item_rate_view_shuidi_layout);
        TextView weiwangNum = rateView.findViewById(R.id.post_detail_item_rate_view_weiwang_num);
        LinearLayout weiwangLayout = rateView.findViewById(R.id.post_detail_item_rate_view_weiwang_layout);
        LinearLayout more = rateView.findViewById(R.id.post_detail_rate_view_more);
        TagFlowLayout zanFlowLayout = rateView.findViewById(R.id.post_detail_item_rate_view_taglayout);

        if (postDetailBean.topic.reward == null) {
            rateView.setVisibility(View.GONE);
        } else {
            rateView.setVisibility(View.VISIBLE);
            rateViewTitle.setText("•评分(" + postDetailBean.topic.reward.userNumber + ")•");

            for (int i = 0; i < postDetailBean.topic.reward.score.size(); i ++) {

                if (postDetailBean.topic.reward.score.get(i).info.equals("水滴")) {
                    shuidiLayout.setVisibility(View.VISIBLE);
                    shuidiNum.setText(postDetailBean.topic.reward.score.get(i).value >= 0 ?
                            " +" + postDetailBean.topic.reward.score.get(i).value : " "+postDetailBean.topic.reward.score.get(i).value);
                }
                if (postDetailBean.topic.reward.score.get(i).info.equals("威望")) {
                    weiwangLayout.setVisibility(View.VISIBLE);
                    weiwangNum.setText(postDetailBean.topic.reward.score.get(i).value >= 0 ?
                            " +" + postDetailBean.topic.reward.score.get(i).value : " "+postDetailBean.topic.reward.score.get(i).value);
                }
            }

            more.setOnClickListener(v -> {
                Intent intent = new Intent(context, WebViewActivity.class);
                intent.putExtra(Constant.IntentKey.URL, postDetailBean.topic.reward.showAllUrl);
                context.startActivity(intent);
            });

            zanFlowLayout.setAdapter(new TagAdapter<PostDetailBean.TopicBean.RewardBean.UserListBean>(postDetailBean.topic.reward.userList) {
                @Override
                public View getView(FlowLayout parent, int position, PostDetailBean.TopicBean.RewardBean.UserListBean o) {
                    View view = LayoutInflater.from(context)
                            .inflate(R.layout.item_post_detail_item_zanlist_view, new LinearLayout(context));
                    CircleImageView imageView = view.findViewById(R.id.item_post_detail_item_zanlist_view_avatar);
                    TextView textView = view.findViewById(R.id.item_post_detail_item_zanlist_view_name);
                    GlideLoader4Common.simpleLoad(context, o.userIcon, imageView);
                    textView.setText(o.userName);
                    return view;
                }
            });
            zanFlowLayout.setOnTagClickListener((view, position, parent) -> {
                Intent intent = new Intent(context, UserDetailActivity.class);
                intent.putExtra(Constant.IntentKey.USER_ID, postDetailBean.topic.reward.userList.get(position).uid);
                context.startActivity(intent);
                return true;
            });

        }
    }
}
