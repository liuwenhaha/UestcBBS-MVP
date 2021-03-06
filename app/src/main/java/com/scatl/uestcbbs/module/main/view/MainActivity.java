package com.scatl.uestcbbs.module.main.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager2.widget.ViewPager2;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jaeger.library.StatusBarUtil;
import com.scatl.uestcbbs.R;
import com.scatl.uestcbbs.base.BaseActivity;
import com.scatl.uestcbbs.base.BaseEvent;
import com.scatl.uestcbbs.base.BasePresenter;
import com.scatl.uestcbbs.entity.UpdateBean;
import com.scatl.uestcbbs.module.main.adapter.ViewPagerAdapter;
import com.scatl.uestcbbs.module.main.presenter.MainPresenter;
import com.scatl.uestcbbs.module.post.view.CreatePostActivity;
import com.scatl.uestcbbs.module.update.view.UpdateFragment;
import com.scatl.uestcbbs.services.heartmsg.view.HeartMsgService;
import com.scatl.uestcbbs.util.CommonUtil;
import com.scatl.uestcbbs.util.Constant;
import com.scatl.uestcbbs.util.ServiceUtil;
import com.scatl.uestcbbs.util.SharePrefUtil;
import com.scatl.uestcbbs.util.TimeUtil;
import com.scatl.uestcbbs.util.ToastUtil;

import org.greenrobot.eventbus.EventBus;

public class MainActivity extends BaseActivity implements MainView{

    private ViewPager2 mainViewpager;
    private AHBottomNavigation ahBottomNavigation;
    private FloatingActionButton floatingActionButton;

    private MainPresenter mainPresenter;

    private int selected;

    @Override
    protected void getIntent(Intent intent) {
        super.getIntent(intent);
        selected = intent.getIntExtra("selected", 0);
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected void findView() {
        mainViewpager = findViewById(R.id.main_viewpager);
        ahBottomNavigation = findViewById(R.id.main_bottom_navigation_bar);
        floatingActionButton = findViewById(R.id.main_create_new_post_btn);

    }

    @Override
    protected void initView() {
        mainPresenter = (MainPresenter) presenter;

        floatingActionButton.setOnClickListener(this);

        if (SharePrefUtil.isNightMode(this)) {
            ahBottomNavigation.setDefaultBackgroundColor(getColor(R.color.background_dark));
        }

        ahBottomNavigation.manageFloatingActionButtonBehavior(floatingActionButton);
        ahBottomNavigation.setNotificationBackgroundColor(getColor(R.color.colorPrimary));
        ahBottomNavigation.setAccentColor(getColor(R.color.colorPrimary));
        ahBottomNavigation.addItem(new AHBottomNavigationItem("首页", R.drawable.ic_home));
        ahBottomNavigation.addItem(new AHBottomNavigationItem("板块", R.drawable.ic_boardlist));
        ahBottomNavigation.addItem(new AHBottomNavigationItem("通知", R.drawable.ic_notification));
        ahBottomNavigation.addItem(new AHBottomNavigationItem("我的", R.drawable.ic_mine));

        mainViewpager.setAdapter(new ViewPagerAdapter(this));
        mainViewpager.setUserInputEnabled(false);
        mainViewpager.setOffscreenPageLimit(3);
        mainViewpager.setCurrentItem(selected, false);
        ahBottomNavigation.setCurrentItem(selected, false);
        floatingActionButton.setVisibility(selected == 0 ? View.VISIBLE : View.GONE);

        mainPresenter.startService(this);
        mainPresenter.getUpdate();
    }

    @Override
    protected BasePresenter initPresenter() {
        return new MainPresenter();
    }

    @Override
    protected void onClickListener(View view) {
        if (view.getId() == R.id.main_create_new_post_btn) {
            startActivity(new Intent(this, CreatePostActivity.class));
        }
    }

    private long t = 0;
    @Override
    protected void setOnItemClickListener() {
        ahBottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {
            floatingActionButton.setVisibility(position == 0 ? View.VISIBLE : View.GONE);

            //双击
            if (wasSelected && System.currentTimeMillis() - t < 300) {
                //刷新首页
                EventBus.getDefault().post(new BaseEvent<>(BaseEvent.EventCode.HOME_REFRESH));
                return true;
            } else {
                t = System.currentTimeMillis();
                mainViewpager.setCurrentItem(position, false);
            }
            return true;
        });

        mainViewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                ahBottomNavigation.setCurrentItem(position, false);
            }
        });

    }

    @Override
    public void getUpdateSuccess(UpdateBean updateBean) {
        if (updateBean.versionCode > CommonUtil.getVersionCode(this)) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constant.IntentKey.DATA, updateBean);
            UpdateFragment.getInstance(bundle)
                    .show(getSupportFragmentManager(), TimeUtil.getStringMs());
        }
    }

    @Override
    public void getUpdateFail(String msg) { }

    //    /**
//     * author: sca_tl
//     * description: 开启消息提醒服务
//     */
//    private void startHeartMsgService() {
//        if (SharePrefUtil.isLogin(this)) {
//            if (! ServiceUtil.isServiceRunning(this, HeartMsgService.serviceName)) {
//                Intent intent = new Intent(this, HeartMsgService.class);
//                startService(intent);
//            }
//        }
//    }

    @Override
    protected boolean registerEventBus() {
        return true;
    }

    @Override
    public void receiveEventBusMsg(BaseEvent baseEvent) {
        if (baseEvent.eventCode == BaseEvent.EventCode.NIGHT_MODE_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            finish();
            Intent intent = new Intent( MainActivity.this, MainActivity.class);
            intent.putExtra("selected", 3);
            startActivity(intent);
            overridePendingTransition(R.anim.switch_night_mode_fade_in, R.anim.switch_night_mode_fade_out);
        }

        if (baseEvent.eventCode == BaseEvent.EventCode.NIGHT_MODE_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            finish();
            Intent intent = new Intent( MainActivity.this, MainActivity.class);
            intent.putExtra("selected", 3);
            startActivity(intent);
            overridePendingTransition(R.anim.switch_night_mode_fade_in, R.anim.switch_night_mode_fade_out);
        }

        if (baseEvent.eventCode == BaseEvent.EventCode.SET_MSG_COUNT) {
            int msg_count = HeartMsgService.at_me_msg_count +
                    HeartMsgService.private_me_msg_count +
                    HeartMsgService.reply_me_msg_count;
            if (msg_count != 0) {
                ahBottomNavigation.setNotification(msg_count + "", 2);
            } else {
                ahBottomNavigation.setNotification("", 2);
            }
        }
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTranslucentForImageViewInFragment(MainActivity.this, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, HeartMsgService.class));
    }
}
