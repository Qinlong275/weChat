package com.sdust.im.fragment;

import com.sdust.im.R;
import com.sdust.im.activity.ContentActivity;
import com.sdust.im.activity.MainActivity;
import com.sdust.im.adapter.TitleAdapter;
import com.sdust.im.bean.News;
import com.sdust.im.bean.NewsList;
import com.sdust.im.bean.Title;
import com.sdust.im.network.HttpUtil;
import com.sdust.im.util.Utility;
import com.sdust.im.view.BannerLayout;
import com.sdust.im.view.TitleBarView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class NewsFragment extends Fragment {

	private Context mContext;
	private View mBaseView;
	private TitleBarView mTitleBarView;
	private List<Title> titleList = new ArrayList<Title>();
	private ListView listView;
	private TitleAdapter adapter;
	private SwipeRefreshLayout refreshLayout;
	private BannerLayout mBannerLayout;

	private String tempUrl1;        //用于顶部Banner
	private String tempUrl2;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestNew();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		mContext = getActivity();
		mBaseView = inflater.inflate(R.layout.fragment_nearby, null);
		initView();
		initEvent();
		return mBaseView;
	}

	private void initView() {
		mTitleBarView = (TitleBarView) mBaseView.findViewById(R.id.title_bar);
		refreshLayout = (SwipeRefreshLayout) mBaseView.findViewById(R.id.swipe_layout);
		refreshLayout.setColorSchemeColors(getResources().getColor(R.color.blue));
		listView = (ListView) mBaseView.findViewById(R.id.list_view);
		mBannerLayout = (BannerLayout) mBaseView.findViewById(R.id.banner);
	}

	private void initEvent() {
		mTitleBarView.setCommonTitle(View.VISIBLE, View.VISIBLE, View.GONE);
		mTitleBarView.setTitleText("体育新闻");
		mTitleBarView.setBtnLeftOnclickListener((MainActivity) mContext);
		adapter = new TitleAdapter(this.getActivity(), R.layout.news_list_view_item, titleList);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			Intent intent = new Intent(NewsFragment.this.getActivity(), ContentActivity.class);

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Title title = titleList.get(position);
				intent.putExtra("title", "体育新闻");
				intent.putExtra("uri", title.getUri());
				startActivity(intent);
			}
		});
		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refreshLayout.setRefreshing(true);
				requestNew();
				System.out.println("qinlongtest requestnews");
			}
		});

		mBannerLayout.setOnBannerItemClickListener(new BannerLayout.OnBannerItemClickListener() {
			Intent intent = new Intent(NewsFragment.this.getActivity(), ContentActivity.class);

			@Override
			public void onItemClick(int position) {
				//动态添加的时候要根据position获得参数绑定点击事件
				String url;
				if (position == 0) {
					url = tempUrl1;
				} else {
					url = tempUrl2;
				}
				intent.putExtra("title", "体育新闻");
				intent.putExtra("uri", url);
				startActivity(intent);
			}
		});
	}

	/**
	 * 请求处理数据
	 */
	public void requestNew() {

		String address = response();
		HttpUtil.sendOkHttpRequest(address, new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(NewsFragment.this.getActivity(), "新闻加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				final String responseText = response.body().string();
				final NewsList newlist = Utility.parseJsonWithGson(responseText);
				final int code = newlist.code;
				if (code == 200) {
					titleList.clear();
					for (News news : newlist.newsList) {
						Title title = new Title(news.title, news.description, news.picUrl, news.url);
						titleList.add(title);
					}

					if (getActivity() != null) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								tempUrl1 = newlist.newsList.get(newlist.newsList.size() - 2).url;
								tempUrl2 = newlist.newsList.get(newlist.newsList.size() - 1).url;
								mBannerLayout.initView(newlist.newsList.get(newlist.newsList.size() - 2).picUrl,
										newlist.newsList.get(newlist.newsList.size() - 1).picUrl);
								adapter.notifyDataSetChanged();
								listView.setSelection(0);
								refreshLayout.setRefreshing(false);
							}

							;
						});
					}
				} else {
					if (getActivity() != null) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(NewsFragment.this.getActivity(), "数据错误返回,请稍后重试", Toast.LENGTH_SHORT).show();
								refreshLayout.setRefreshing(false);
							}
						});
					}
				}
			}
		});


	}

	//暂时只支持体育新闻
	private String response() {
		String address = "https://api.tianapi.com/social/?key=ed1b4a2b862c9f56629a1f91b2c903cc&num=15&rand=1";
		address = address.replaceAll("social", "tiyu");
		return address;
	}
}

