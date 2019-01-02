package com.sdust.im.activity;

import com.sdust.im.R;
import com.sdust.im.bean.ApplicationData;
import com.sdust.im.databse.ImDB;
import com.sdust.im.fragment.FriendListFragment;
import com.sdust.im.fragment.MessageFragment;


import com.sdust.im.fragment.NewsFragment;

import com.sdust.im.network.NetService;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

public class MainActivity extends FragmentActivity {

	protected static final String TAG = "MainActivity";
	private Context mContext;
	private ImageButton mNews, mConstact, mDeynaimic, mSetting;
	private View mPopView;
	private View currentButton;

	private TextView app_cancle;
	private TextView app_exit;
	private TextView app_change;

	private PopupWindow mPopupWindow;
	private LinearLayout buttomBarGroup;
	private NavigationView navigationView;

	MessageFragment messageFragment;
	FriendListFragment constactFatherFragment;
	NewsFragment mNewsFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		initView();
		initEvent();
	}

	private void initView() {
		mPopView = LayoutInflater.from(mContext).inflate(R.layout.app_exit, null);
		buttomBarGroup = (LinearLayout) findViewById(R.id.buttom_bar_group);
		mNews = (ImageButton) findViewById(R.id.buttom_news);
		mConstact = (ImageButton) findViewById(R.id.buttom_constact);
		mDeynaimic = (ImageButton) findViewById(R.id.buttom_deynaimic);

		app_cancle = (TextView) mPopView.findViewById(R.id.app_cancle);
		app_change = (TextView) mPopView.findViewById(R.id.app_change_user);
		app_exit = (TextView) mPopView.findViewById(R.id.app_exit);

		navigationView = (NavigationView)findViewById(R.id.nav_view);
		((ImageView)(navigationView.getHeaderView(0).findViewById(R.id.icon_image))).setImageBitmap(ApplicationData.getInstance().getUserPhoto());
	}

	private void initEvent() {
		mNews.setOnClickListener(newsOnClickListener);
		mConstact.setOnClickListener(constactOnClickListener);
		mDeynaimic.setOnClickListener(deynaimicOnClickListener);

		mConstact.performClick();

		mPopupWindow = new PopupWindow(mPopView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);

		app_cancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPopupWindow.dismiss();
			}
		});

		//注销
		app_change.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, LoginActivity.class);
				startActivity(intent);
				((Activity) mContext).overridePendingTransition(R.anim.activity_up, R.anim.fade_out);
				NetService.getInstance().closeConnection();
				ImDB.getInstance(mContext).resetDb();
				finish();
			}
		});

		app_exit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				NetService.getInstance().closeConnection();
				finish();
			}
		});
	}

	private OnClickListener newsOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			if (messageFragment == null) {
				messageFragment = new MessageFragment();
			}
			ft.replace(R.id.fl_content, messageFragment, MainActivity.TAG);
			ft.commit();
			setButton(v);
		}
	};

	private OnClickListener constactOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			if (constactFatherFragment == null) {
				constactFatherFragment = new FriendListFragment();
			}
			ft.replace(R.id.fl_content, constactFatherFragment, MainActivity.TAG);
			ft.commit();
			setButton(v);

		}
	};

	private OnClickListener deynaimicOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			if (mNewsFragment == null) {
				mNewsFragment = new NewsFragment();
			}
			ft.replace(R.id.fl_content, mNewsFragment, MainActivity.TAG);
			ft.commit();
			setButton(v);
		}
	};

	//改变底部按钮状态
	private void setButton(View v) {
		if (currentButton != null && currentButton.getId() != v.getId()) {
			currentButton.setEnabled(true);
		}
		v.setEnabled(false);
		currentButton = v;
	}

	//长按手机自带底部菜单项
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#b0000000")));
			mPopupWindow.showAtLocation(buttomBarGroup, Gravity.BOTTOM, 0, 0);
			mPopupWindow.setAnimationStyle(R.style.app_pop);
			mPopupWindow.setOutsideTouchable(true);
			mPopupWindow.setFocusable(true);
			mPopupWindow.update();
		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	protected void onDestroy() {
		System.out.println("Main activity destroy");
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		NetService.getInstance().closeConnection();
		super.onBackPressed();
	}
}
