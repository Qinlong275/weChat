package com.sdust.im.activity.register;

import java.util.Date;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sdust.im.R;
import com.sdust.im.action.UserAction;
import com.sdust.im.bean.TranObject;
import com.sdust.im.bean.User;
import com.sdust.im.global.Result;
import com.sdust.im.util.PhotoUtils;

import de.hdodenhof.circleimageview.CircleImageView;

public class StepPhoto extends RegisterStep implements OnClickListener {

	private ImageView mIvUserPhoto;        //选择的头像

	private CircleImageView addButton;
	private CircleImageView takePhoto;
	private CircleImageView openAlbum;
	private boolean start = true;        //标志底部制作照片按钮的状态

	private String mTakePicturePath;
	private Bitmap mUserPhoto;
	private String mAccount;
	private String mPassword;
	private Date mBirthday;
	private String mName;
	private int mGender;
	private static TranObject mReceivedInfo = null;
	private static boolean mIsReceived = false;
	private boolean errorOccur;

	public StepPhoto(RegisterActivity activity, View contentRootView) {
		super(activity, contentRootView);
		mNeedRequest = false;
	}

	//设置最后制作完成的用户头像
	public void setUserPhoto(Bitmap bitmap) {
		if (bitmap != null) {
			mUserPhoto = bitmap;
			mIvUserPhoto.setImageBitmap(mUserPhoto);
			return;
		}
		showCustomToast("未获取到图片");
		mUserPhoto = null;
		mIvUserPhoto.setImageResource(R.drawable.ic_common_def_header);
	}

	public String getTakePicturePath() {
		return mTakePicturePath;
	}

	@Override
	public void initViews() {
		mIvUserPhoto = (ImageView) findViewById(R.id.reg_photo_iv_userphoto);
		addButton = (CircleImageView) findViewById(R.id.icon_add);
		takePhoto = (CircleImageView) findViewById(R.id.icon_take_photo);
		openAlbum = (CircleImageView) findViewById(R.id.icon_album);
	}

	@Override
	public void initEvents() {
		openAlbum.setOnClickListener(this);
		takePhoto.setOnClickListener(this);
		addButton.setOnClickListener(this);
	}

	@Override
	public boolean validate() {
		if (mUserPhoto == null) {
			showCustomToast("请添加头像");
			return false;
		}
		return true;
	}

	//展开工具栏
	private void open() {
		start = false;
		ObjectAnimator translationLeft = new ObjectAnimator().ofFloat(takePhoto, "translationX", 0, -220f);
		translationLeft.setDuration(500);
		translationLeft.start();
		ObjectAnimator translationRight = new ObjectAnimator().ofFloat(openAlbum, "translationX", 0, 220f);
		translationRight.setDuration(500);
		translationRight.start();
		ObjectAnimator re = ObjectAnimator.ofFloat(addButton, "rotation", 0f, 90f);
		AnimatorSet animatorSetsuofang = new AnimatorSet();//组合动画
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(addButton, "scaleX", 1, 0.8f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(addButton, "scaleY", 1, 0.8f);
		animatorSetsuofang.setDuration(500);
		animatorSetsuofang.play(scaleX).with(scaleY).with(re);
		animatorSetsuofang.start();
	}

	//合上工具栏
	private void close() {
		start = true;
		ObjectAnimator translationLeft = new ObjectAnimator().ofFloat(takePhoto, "translationX", -220, 0f);
		translationLeft.setDuration(500);
		translationLeft.start();
		ObjectAnimator translationRight = new ObjectAnimator().ofFloat(openAlbum, "translationX", 220, 0f);
		translationRight.setDuration(500);
		translationRight.start();
		ObjectAnimator re = ObjectAnimator.ofFloat(addButton, "rotation", 90f, 0f);
		AnimatorSet animatorSetsuofang = new AnimatorSet();//组合动画
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(addButton, "scaleX", 0.8f, 1f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(addButton, "scaleY", 0.8f, 1f);
		animatorSetsuofang.setDuration(500);
		animatorSetsuofang.play(scaleX).with(scaleY).with(re);
		animatorSetsuofang.start();
	}

	@Override
	public void doNext() {
		putAsyncTask(new AsyncTask<Void, Void, Integer>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				showLoadingDialog("请稍后,正在提交...");
			}

			@Override
			protected Integer doInBackground(Void... params) {
				try {
					mIsReceived = false;
					errorOccur = false;
					mNetService.setupConnection();
					if (!mNetService.isConnected()) {
						return 0;
					} else {
						byte[] photoByte = PhotoUtils.getBytes(mUserPhoto);
						User user = new User(mAccount, mName, mPassword,
								mBirthday, mGender, photoByte);
						UserAction.register(user);
						new Thread(new Runnable() {
							@Override
							public void run() {
								Looper.prepare();
								new Handler().postDelayed(new Runnable() {
									@Override
									public void run() {
										if (!mIsReceived) {
											System.out.println("登陆遇到问题");
											mIsReceived = true;
											errorOccur = true;
										}
									}
								}, 3500);
								Looper.loop();
							}
						}).start();
						while (!mIsReceived) {
							System.out.println("");
						}// 如果没收到的话就会一直阻塞;
						mNetService.closeConnection();
						if (errorOccur) {
							errorOccur = false;
							return 0;
						}
						if (mReceivedInfo.getResult() == Result.REGISTER_SUCCESS)
							return 1;
						else
							return 2;
					}
				} catch (Exception e) {
					Log.d("regester", "注册异常");

				}
				return 0;

			}

			@Override
			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);
				dismissLoadingDialog();
				if (result == 0) {
					showCustomToast("服务器异常");

				} else {
					if (result == 1) {
						showCustomToast("注册成功");
						User.saveUserName = mAccount;
						User.saveUserPass = mPassword;
						saveUserInfo(mAccount, mPassword);
						mActivity.finish();
					} else if (result == 2) {
						showCustomToast("注册失败");
					}
				}
			}

		});
	}

	private void saveUserInfo(String account, String password) {
		SharedPreferences userSettings = mActivity.getApplicationContext().getSharedPreferences("user", 0);
		SharedPreferences.Editor editor = userSettings.edit();
		editor.putString("account", account);
		editor.putString("password", password);
		editor.commit();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.icon_add:
				if (start) {
					open();
				} else {
					close();
				}
				break;
			case R.id.icon_album:
				PhotoUtils.selectPhoto(mActivity);
				break;
			case R.id.icon_take_photo:
				mTakePicturePath = PhotoUtils.takePicture(mActivity);
				break;
		}
	}

	public Bitmap getPhoto() {
		return mUserPhoto;
	}

	public void setAccount(String account) {
		this.mAccount = account;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public void setGender(int gender) {
		this.mGender = gender;
	}

	public void setBirthday(Date birthday) {
		this.mBirthday = birthday;
	}

	public void setPassword(String password) {
		this.mPassword = password;
	}

	public static void setRegisterInfo(TranObject object, boolean isReceived) {

		mReceivedInfo = object;
		mIsReceived = true;
	}

}
