package com.sdust.im.activity.register;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.MediaColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.sdust.im.BaseActivity;
import com.sdust.im.BaseDialog;
import com.sdust.im.R;
import com.sdust.im.activity.register.RegisterStep.onNextActionListener;
import com.sdust.im.util.PhotoUtils;
import com.sdust.im.view.HeaderLayout;
import com.sdust.im.view.HeaderLayout.HeaderStyle;

public class RegisterActivity extends BaseActivity implements OnClickListener,
		onNextActionListener {

	private HeaderLayout mHeaderLayout;
	private ViewFlipper mVfFlipper;    //中间滑动控件

	private Button mBtnPrevious;
	private Button mBtnNext;

	private BaseDialog mBackDialog;

	private RegisterStep mCurrentStep;        //当前步骤的抽象类

	private StepAccount mStepAccount;
	private StepSetPassword mStepSetPassword;
	private StepBaseInfo mStepBaseInfo;
	private StepBirthday mStepBirthday;
	private StepPhoto mStepPhoto;

	private int mCurrentStepIndex = 1;    //当前所处的注册步骤

	private static final String[] permissionsArray = new String[]{
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.INTERNET, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_WIFI_STATE,
			Manifest.permission.INTERNET, Manifest.permission
			.CAMERA
	};
	//还需申请的权限列表
	private List<String> permissionsList = new ArrayList<String>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		initViews();
		mCurrentStep = initStep();
		initEvents();
		initBackDialog();
		initPermission();
	}

	//申请App所需要的权限
	private void initPermission() {
		for (String permission : permissionsArray) {
			if (ContextCompat.checkSelfPermission(RegisterActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
				permissionsList.add(permission);
			}
		}
		if (permissionsList.size() > 0) {
			ActivityCompat.requestPermissions(RegisterActivity.this, permissionsList.toArray(new String[permissionsList.size()]), 1);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case 1:
				for (int i = 0; i < permissions.length; i++) {
					if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

					} else {
						Toast.makeText(RegisterActivity.this, "权限被拒绝： " + permissions[i], Toast.LENGTH_SHORT).show();
					}
				}
				break;
			default:
		}
	}


	@Override
	protected void onDestroy() {
		PhotoUtils.deleteImageFile();
		super.onDestroy();
	}

	@Override
	protected void initViews() {
		mHeaderLayout = (HeaderLayout) findViewById(R.id.reg_header);
		mHeaderLayout.init(HeaderStyle.TITLE_RIGHT_TEXT);
		mVfFlipper = (ViewFlipper) findViewById(R.id.reg_vf_viewflipper);
		mVfFlipper.setDisplayedChild(0);
		mBtnPrevious = (Button) findViewById(R.id.reg_btn_previous);
		mBtnNext = (Button) findViewById(R.id.reg_btn_next);
	}

	@Override
	protected void initEvents() {
		mCurrentStep.setOnNextActionListener(this);
		mBtnPrevious.setOnClickListener(this);
		mBtnNext.setOnClickListener(this);
	}

	@Override
	public void onBackPressed() {
		if (mCurrentStepIndex <= 1) {
			mBackDialog.show();
		} else {
			doPrevious();
		}
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
			case R.id.reg_btn_previous:
				if (mCurrentStepIndex <= 1) {
					mBackDialog.show();
				} else {
					doPrevious();
				}
				break;

			case R.id.reg_btn_next:
				if (mCurrentStepIndex < 5) {
					doNext();
				} else {
					//最后一步相册部分，直接请求
					if (mCurrentStep.validate()) {
						mCurrentStep.doNext();
					}
				}
				break;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case PhotoUtils.INTENT_REQUEST_CODE_ALBUM:
				if (data == null) {
					return;
				}
				if (resultCode == RESULT_OK) {
					if (data.getData() == null) {
						return;
					}
					Uri uri = data.getData();
					String[] proj = {MediaColumns.DATA};
					Cursor cursor = managedQuery(uri, proj, null, null, null);
					if (cursor != null) {
						int column_index = cursor
								.getColumnIndexOrThrow(MediaColumns.DATA);
						if (cursor.getCount() > 0 && cursor.moveToFirst()) {
							String path = cursor.getString(column_index);
							Bitmap bitmap = BitmapFactory.decodeFile(path);
							if (PhotoUtils.bitmapIsLarge(bitmap)) {
								//裁剪照片
								PhotoUtils.cropPhoto(this, this, path);
							} else {
								mStepPhoto.setUserPhoto(PhotoUtils.compressImage(bitmap));
							}
						}
					}
				}
				break;

			case PhotoUtils.INTENT_REQUEST_CODE_CAMERA:
				if (resultCode == RESULT_OK) {
					String path = mStepPhoto.getTakePicturePath();
					Bitmap bitmap = BitmapFactory.decodeFile(path);
					if (PhotoUtils.bitmapIsLarge(bitmap)) {
						PhotoUtils.cropPhoto(this, this, path);
					} else {
						//mStepPhoto.setUserPhoto(bitmap);
						mStepPhoto.setUserPhoto(PhotoUtils.compressImage(bitmap));
					}
				}
				break;

			case PhotoUtils.INTENT_REQUEST_CODE_CROP:
				if (resultCode == RESULT_OK) {
					String path = data.getStringExtra("path");
					if (path != null) {
						Bitmap bitmap = BitmapFactory.decodeFile(path);
						if (bitmap != null) {
							//mStepPhoto.setUserPhoto(bitmap);
							mStepPhoto.setUserPhoto(PhotoUtils.compressImage(bitmap));
						}
					}
				}
				break;
		}
	}

	//进入注册的下一阶段
	@Override
	public void next() {
		mCurrentStepIndex++;
		mCurrentStep = initStep();
		mCurrentStep.setOnNextActionListener(this);
		mVfFlipper.setInAnimation(this, R.anim.push_left_in);
		mVfFlipper.setOutAnimation(this, R.anim.push_left_out);
		mVfFlipper.showNext();
	}

	private RegisterStep initStep() {
		switch (mCurrentStepIndex) {
			case 1:
				if (mStepAccount == null) {
					mStepAccount = new StepAccount(this, mVfFlipper.getChildAt(0));
				}
				mHeaderLayout.setTitleRightText("申请账号", null, "1/5");
				mBtnPrevious.setText("返    回");
				mBtnNext.setText("下一步");
				return mStepAccount;

			case 2:
				if (mStepSetPassword == null) {
					mStepSetPassword = new StepSetPassword(this,
							mVfFlipper.getChildAt(1));
				}
				mHeaderLayout.setTitleRightText("设置密码", null, "2/5");
				mBtnPrevious.setText("上一步");
				mBtnNext.setText("下一步");
				return mStepSetPassword;

			case 3:
				if (mStepBaseInfo == null) {
					mStepBaseInfo = new StepBaseInfo(this, mVfFlipper.getChildAt(2));
				}
				mHeaderLayout.setTitleRightText("基本资料", null, "3/5");
				mBtnPrevious.setText("上一步");
				mBtnNext.setText("下一步");
				return mStepBaseInfo;

			case 4:
				if (mStepBirthday == null) {
					mStepBirthday = new StepBirthday(this, mVfFlipper.getChildAt(3));
				}
				mHeaderLayout.setTitleRightText("您的生日", null, "4/5");
				mBtnPrevious.setText("上一步");
				mBtnNext.setText("下一步");
				return mStepBirthday;

			case 5:
				if (mStepPhoto == null) {
					mStepPhoto = new StepPhoto(this, mVfFlipper.getChildAt(4));
				}
				mHeaderLayout.setTitleRightText("潇洒的头像", null, "5/5");
				mBtnPrevious.setText("上一步");
				mBtnNext.setText("注    册");
				//注册的最后一步汇总所有信息
				mStepPhoto.setAccount(getAccount());
				mStepPhoto.setBirthday(getBirthday());
				mStepPhoto.setName(getName());
				mStepPhoto.setGender(getGender());
				mStepPhoto.setPassword(getPassword());


				return mStepPhoto;
		}
		return null;
	}

	private void doPrevious() {
		mCurrentStepIndex--;
		mCurrentStep = initStep();
		mCurrentStep.setOnNextActionListener(this);
		//中间View滑动的动画
		mVfFlipper.setInAnimation(this, R.anim.push_right_in);
		mVfFlipper.setOutAnimation(this, R.anim.push_right_out);
		mVfFlipper.showPrevious();
	}

	private void doNext() {
		//若某一布的信息不符合要求，每一步的Step类中自行处理，出错便不会进行到下一步
		if (mCurrentStep.validate()) {
			if (mCurrentStep.isNeedRequest()) {
				mCurrentStep.doNext();
			} else {
				next();
			}
		}
	}

	private void initBackDialog() {
		mBackDialog = BaseDialog.getDialog(RegisterActivity.this, "提示",
				"确认要放弃注册么?", "确认", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				}, "取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		mBackDialog.setButton1Background(R.drawable.btn_default_popsubmit);

	}

	@Override
	protected void putAsyncTask(AsyncTask<Void, Void, Integer> asyncTask) {
		super.putAsyncTask(asyncTask);
	}

	@Override
	protected void showCustomToast(String text) {
		super.showCustomToast(text);
	}

	@Override
	protected void showLoadingDialog(String text) {
		super.showLoadingDialog(text);
	}

	@Override
	protected void dismissLoadingDialog() {
		super.dismissLoadingDialog();
	}

	protected int getScreenWidth() {
		return mScreenWidth;
	}


	//获取每一步的注册用户信息
	public String getAccount() {
		if (mStepAccount != null) {
			return mStepAccount.getAccount();
		}
		return "";
	}

	public String getPassword() {
		if (mStepSetPassword != null) {
			return mStepSetPassword.getPassword();
		}
		return "";
	}

	public String getName() {
		if (mStepBaseInfo != null) {
			return mStepBaseInfo.getName();
		}
		return "";
	}

	public int getGender() {
		if (mStepBaseInfo != null) {
			return mStepBaseInfo.getGender();
		}
		return 0;
	}

	public Date getBirthday() {
		if (mStepBirthday != null) {
			return mStepBirthday.getBirthday();
		}
		return null;
	}

}
