package com.sdust.im.activity.register;

import java.util.regex.Pattern;

import com.sdust.im.network.NetService;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;

public abstract class RegisterStep {
	protected RegisterActivity mActivity;
	protected Context mContext;
	private View mContentRootView;
	protected onNextActionListener mOnNextActionListener;
	protected NetService mNetService = NetService.getInstance();
	protected boolean mNeedRequest = true;	//表示当前步骤是否需要进行响应的请求操作，若不需要直接跳到下一步

	public RegisterStep(RegisterActivity activity, View contentRootView) {
		mActivity = activity;
		mContext = mActivity;
		mContentRootView = contentRootView;
		//子类具体实现
		initViews();
		initEvents();
	}

	public abstract void initViews();

	public abstract void initEvents();

	public abstract boolean validate();        //判断每一步信息是否有效

	//判断当前步骤是否需要进行操作
	public boolean isNeedRequest(){
		return mNeedRequest;
	}

	public View findViewById(int id) {
		return mContentRootView.findViewById(id);
	}

	public void doPrevious() {

	}

	public void doNext() {

	}

	public void nextAnimation() {

	}

	public void preAnimation() {

	}


	protected void showCustomToast(String text) {
		mActivity.showCustomToast(text);
	}

	protected void putAsyncTask(AsyncTask<Void, Void, Integer> asyncTask) {
		mActivity.putAsyncTask(asyncTask);
	}

	protected void showLoadingDialog(String text) {
		mActivity.showLoadingDialog(text);
	}

	protected void dismissLoadingDialog() {
		mActivity.dismissLoadingDialog();
	}

	protected int getScreenWidth() {
		return mActivity.getScreenWidth();
	}


	public void setOnNextActionListener(onNextActionListener listener) {
		mOnNextActionListener = listener;
	}

	public interface onNextActionListener {
		void next();
	}
}
