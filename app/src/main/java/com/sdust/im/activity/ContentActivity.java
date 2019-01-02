package com.sdust.im.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sdust.im.R;
import com.sdust.im.view.TitleBarView;

public class ContentActivity extends AppCompatActivity {

	private WebView webView;
	private TitleBarView mTitleBarView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_content);
		initEvent();
	}

	private void initEvent(){
		mTitleBarView=(TitleBarView) findViewById(R.id.title_bar);
		mTitleBarView.setTitleText("体育新闻");
		webView = (WebView)findViewById(R.id.web_view);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient());

		String uri = getIntent().getStringExtra("uri");
		webView.loadUrl(uri);
	}
}
