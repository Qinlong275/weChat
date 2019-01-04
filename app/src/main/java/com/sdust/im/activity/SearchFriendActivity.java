package com.sdust.im.activity;

import java.io.IOException;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.sdust.im.BaseActivity;
import com.sdust.im.R;
import com.sdust.im.action.UserAction;
import com.sdust.im.bean.ApplicationData;
import com.sdust.im.bean.TranObject;
import com.sdust.im.bean.User;
import com.sdust.im.util.VerifyUtils;
import com.sdust.im.view.TitleBarView;

public class SearchFriendActivity extends BaseActivity implements
		OnClickListener {

	private TitleBarView mTitleBarView;
	private EditText mSearchEtName;
	private Button mBtnSearchByName;

	private static boolean mIsReceived;
	private boolean flag = false;
	public boolean isError;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_searchfriend);

		initViews();
		initEvents();
	}

	@Override
	protected void initViews() {
		mTitleBarView = (TitleBarView) findViewById(R.id.title_bar);
		mTitleBarView.setCommonTitle(View.GONE, View.VISIBLE, View.GONE);
		mTitleBarView.setTitleText("查找朋友");
		mSearchEtName = (EditText) findViewById(R.id.search_friend_by_name_edit_name);
		mBtnSearchByName = (Button) findViewById(R.id.search_friend_by_name_btn_search);

	}

	@Override
	protected void initEvents() {
		// TODO Auto-generated method stub
		mIsReceived = false;
		mBtnSearchByName.setOnClickListener(this);
		ApplicationData.getInstance().saveContext(this);

	}

	public static void messageArrived(TranObject mReceived) {
		ArrayList<User> list = (ArrayList<User>) mReceived.getObject();
		ApplicationData.getInstance().setFriendSearched(list);
		mIsReceived = true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.search_friend_by_name_btn_search:
				flag = false;
				String searchName = mSearchEtName.getText().toString();
				if (searchName.equals("")) {
					showCustomToast("请填写账号");
					mSearchEtName.requestFocus();
				} else if (!VerifyUtils.matchAccount(searchName)) {
					showCustomToast("账号格式错误");
					mSearchEtName.requestFocus();
				} else {
					try {
						isError = false;
						showLoadingDialog("正在查找...");
						UserAction.searchFriend("0" + " " + searchName);
						new Thread(new Runnable() {
							@Override
							public void run() {
								Looper.prepare();
								new Handler().postDelayed(new Runnable() {
									@Override
									public void run() {
										if (!mIsReceived){
											showCustomToast("查找失敗稍后重试");
										}else {
											System.out.println("成功返回");
										}
										dismissLoadingDialog();
										return;
									}
								}, 3500);
								Looper.loop();
							}
						}).start();

					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				break;
			default:
				break;
		}
	}

}
