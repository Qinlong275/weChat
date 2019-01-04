package com.sdust.im.activity;

import com.sdust.im.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

//欢迎界面
public class WelcomeActivity extends Activity {
	private Context mContext;
	private TextView mTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		mContext = this;
		findView();
		init();
	}

	private void findView() {
		mTextView = (TextView) findViewById(R.id.iv_welcome);
	}

	private void init() {
		mTextView.postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(mContext, LoginActivity.class);
				startActivity(intent);
				finish();
			}
		},2000);
		
	}
}
