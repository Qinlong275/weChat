package com.sdust.im.view;

import com.sdust.im.R;
import com.sdust.im.util.SystemMethod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TitleBarView extends RelativeLayout {

	private static final String TAG = "TitleBarView";
	private Context mContext;
	private ImageView btnLeft;
	private ImageView btnRight;
	private TextView tv_center;

	public TitleBarView(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	public TitleBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}

	private void initView() {
		LayoutInflater.from(mContext).inflate(R.layout.common_title_bar, this);
		btnLeft = (ImageView) findViewById(R.id.title_btn_left);
		btnRight = (ImageView) findViewById(R.id.title_btn_right);
		tv_center = (TextView) findViewById(R.id.title_txt);
	}

	public void setCommonTitle(int LeftVisibility, int centerVisibility, int rightVisibility) {
		btnLeft.setVisibility(LeftVisibility);
		btnRight.setVisibility(rightVisibility);
		tv_center.setVisibility(centerVisibility);

	}


	public void setBtnRight(int icon) {
		btnRight.setImageResource(icon);
	}


	public void setTitleText(String txtRes) {
		tv_center.setText(txtRes);
	}

	public void setBtnLeftOnclickListener(OnClickListener listener) {
		btnLeft.setOnClickListener(listener);
	}

	public void setBtnRightOnclickListener(OnClickListener listener) {
		btnRight.setOnClickListener(listener);
	}


}
