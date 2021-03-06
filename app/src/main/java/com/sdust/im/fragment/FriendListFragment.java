package com.sdust.im.fragment;

import java.util.List;

import com.sdust.im.R;
import com.sdust.im.activity.ChatActivity;
import com.sdust.im.activity.MainActivity;
import com.sdust.im.activity.SearchFriendActivity;
import com.sdust.im.adapter.FriendListAdapter;
import com.sdust.im.bean.ApplicationData;
import com.sdust.im.bean.User;
import com.sdust.im.view.TitleBarView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class FriendListFragment extends Fragment {
	private Context mContext;
	private View mBaseView;
	private View mEmptyView;
	private TitleBarView mTitleBarView;
	private ListView mFriendListView;
	private List<User> mFriendList;
	private Handler handler;		//处理新的好友列表变化消息
	private FriendListAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContext = getActivity();
		mBaseView = inflater.inflate(R.layout.fragment_friendlist, null);
		initView();
		init();
		return mBaseView;
	}

	private void initView() {
		mTitleBarView = (TitleBarView) mBaseView.findViewById(R.id.title_bar);
		mTitleBarView.setBackgroundResource(R.color.common_title);
		mFriendListView = (ListView)mBaseView.findViewById(R.id.friend_list_listview);
		mEmptyView = (View) mBaseView.findViewById(R.id.empty_layout);
	}

	private void init() {
		mFriendList = ApplicationData.getInstance().getFriendList();
		adapter = new FriendListAdapter(mContext, mFriendList);
		mFriendListView.setAdapter(adapter);
		changeEmptyStatus();
		handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					adapter.notifyDataSetChanged();
					mFriendListView.setSelection(mFriendList.size());
					changeEmptyStatus();
					break;
				default:
					break;
				}
			}
		};
		ApplicationData.getInstance().setfriendListHandler(handler);
		mTitleBarView.setCommonTitle(View.VISIBLE, View.VISIBLE, View.VISIBLE);
		mTitleBarView.setTitleText("好友");
		mTitleBarView.setBtnRight(R.drawable.qq_constact);
		mTitleBarView.setBtnLeftOnclickListener((MainActivity)mContext);
		
		mFriendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				User friend = mFriendList.get(position);
				Intent intent = new Intent(mContext,ChatActivity.class);
				intent.putExtra("friendName", friend.getUserName());
				intent.putExtra("friendId", friend.getId());
				startActivity(intent);
			}
		});
		mTitleBarView.setBtnRightOnclickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext,SearchFriendActivity.class);
				startActivity(intent);
			}
		});
		
	}

	private void changeEmptyStatus(){
		if (mFriendList.size() < 1){
			mEmptyView.setVisibility(View.VISIBLE);
		}else {
			mEmptyView.setVisibility(View.GONE);
		}
	}
}
