package com.sdust.im.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.sdust.im.activity.FriendSearchResultActivity;
import com.sdust.im.databse.ImDB;
import com.sdust.im.global.Result;
import com.sdust.im.util.PhotoUtils;

public class ApplicationData {

	private static ApplicationData mInitData;

	private User mUser;	//用户自己
	private boolean mIsReceived;
	private List<User> mFriendList;		//好友列表
	private TranObject mReceivedMessage;
	private Map<Integer, Bitmap> mFriendPhotoMap;

	private Handler messageHandler;	//消息处理
	private Handler chatMessageHandler;	//聊天处理
	private Handler friendListHandler;	//好友列表处理

	private Context mContext;
	private List<User> mFriendSearched;		//搜索好友的结果
	private Bitmap mUserPhoto;		//用户自己的头像
	private List<MessageTabEntity> mMessageEntities;// messageFragment显示的列表
	private Map<Integer, List<ChatEntity>> mChatMessagesMap;		//与不同好友的聊天消息

	private Context mTempContext;
	public boolean isError;

	public Map<Integer, List<ChatEntity>> getChatMessagesMap() {
		return mChatMessagesMap;
	}

	public void setChatMessagesMap(
			Map<Integer, List<ChatEntity>> mChatMessagesMap) {
		this.mChatMessagesMap = mChatMessagesMap;
	}

	public static ApplicationData getInstance() {
		if (mInitData == null) {
			mInitData = new ApplicationData();
		}
		return mInitData;
	}

	public void initData(Context comtext) {
		System.out.println("qintest initdata");
		mContext = comtext;
		mIsReceived = false;
		mFriendList = null;
		mUser = null;
		mReceivedMessage = null;
	}
	public void start() {
		System.out.println("qintest start login");
		new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						if (!mIsReceived){
							System.out.println("qintest 登陆遇到问题");
							mIsReceived = true;
							isError = true;
						}else {
							System.out.println("登陆成功返回");
						}
						return;
					}
				}, 3500);
				Looper.loop();
			}
		}).start();

		while (!(mIsReceived)){
			System.out.println("");
		}
		System.out.println("qintest 登陆fanhui");
	}

	//登陆成功，获取相关消息数据
	public void loginMessageArrived(Object tranObject) {

		mReceivedMessage = (TranObject) tranObject;
		Result loginResult = mReceivedMessage.getResult();
		if (loginResult == Result.LOGIN_SUCCESS) {
			mUser = (User) mReceivedMessage.getObject();
			mFriendList = mUser.getFriendList();// 根据从服务器得到的信息，设置好友是否在线
			//用户头像
			mUserPhoto = PhotoUtils.getBitmap(mUser.getPhoto());
			List<User> friendListLocal = ImDB.getInstance(mContext)
					.getAllFriend();
			mFriendPhotoMap = new HashMap<Integer, Bitmap>();
			for (int i = 0; i < friendListLocal.size(); i++) {
				User friend = friendListLocal.get(i);
				Bitmap photo = PhotoUtils.getBitmap(friend.getPhoto());
				mFriendPhotoMap.put(friend.getId(), photo);
			}
			//登陆成功时获取消息缓存
			mMessageEntities = ImDB.getInstance(mContext).getAllMessage();
		} else {

			mUser = null;
			mFriendList = null;
		}
		mChatMessagesMap = new HashMap<Integer, List<ChatEntity>>();
		System.out.println("qintest loginMessageArrived");
		mIsReceived = true;
	}

	//好友请求
	public void friendRequestArrived(TranObject mReceivedRequest) {
		MessageTabEntity messageEntity = new MessageTabEntity();
		if (mReceivedRequest.getResult() == Result.MAKE_FRIEND_REQUEST) {
			messageEntity.setMessageType(MessageTabEntity.MAKE_FRIEND_REQUEST);
			messageEntity.setContent("希望加你为好友");
		} else if (mReceivedRequest.getResult() == Result.FRIEND_REQUEST_RESPONSE_ACCEPT) {
			messageEntity
					.setMessageType(MessageTabEntity.MAKE_FRIEND_RESPONSE_ACCEPT);
			messageEntity.setContent("接受了你的好友请求");
			User newFriend = (User) mReceivedRequest.getObject();
			if (!mFriendList.contains(newFriend)) {
				mFriendList.add(newFriend);
			}
			
			mFriendPhotoMap.put(newFriend.getId(), PhotoUtils.getBitmap(newFriend.getPhoto()));
			if (friendListHandler != null) {
				Message message = new Message();
				message.what = 1;
				friendListHandler.sendMessage(message);
			}
			//缓存好友信息
			ImDB.getInstance(mContext).saveFriend(newFriend);
		} else {
			messageEntity
					.setMessageType(MessageTabEntity.MAKE_FRIEND_RESPONSE_REJECT);
			messageEntity.setContent("拒绝了你的好友请求");
		}
		messageEntity.setName(mReceivedRequest.getSendName());
		messageEntity.setSendTime(mReceivedRequest.getSendTime());
		messageEntity.setSenderId(mReceivedRequest.getSendId());
		messageEntity.setUnReadCount(1);
		ImDB.getInstance(mContext).saveMessage(messageEntity);
		mMessageEntities.add(messageEntity);
		//通知消息列表
		if (messageHandler != null) {
			Message message = new Message();
			message.what = 1;
			messageHandler.sendMessage(message);
		}
	}

	//聊天消息到达
	public void messageArrived(TranObject tran) {
		ChatEntity chat = (ChatEntity) tran.getObject();
		int senderId = chat.getSenderId();
		System.out.println("senderId" + senderId);
		boolean hasMessageTab = false;
		for (int i = 0; i < mMessageEntities.size(); i++) {
			MessageTabEntity messageTab = mMessageEntities.get(i);
			if (messageTab.getSenderId() == senderId
					&& messageTab.getMessageType() == MessageTabEntity.FRIEND_MESSAGE) {
				messageTab.setUnReadCount(messageTab.getUnReadCount() + 1);
				messageTab.setContent(chat.getContent());
				messageTab.setSendTime(chat.getSendTime());
				ImDB.getInstance(mContext).updateMessages(messageTab);
				hasMessageTab = true;
			}
		}
		//新消息
		if (!hasMessageTab) {
			MessageTabEntity messageTab = new MessageTabEntity();
			messageTab.setContent(chat.getContent());
			messageTab.setMessageType(MessageTabEntity.FRIEND_MESSAGE);
			messageTab.setName(tran.getSendName());
			messageTab.setSenderId(senderId);
			messageTab.setSendTime(chat.getSendTime());
			messageTab.setUnReadCount(1);
			mMessageEntities.add(messageTab);
			ImDB.getInstance(mContext).saveMessage(messageTab);
		}
		chat.setMessageType(ChatEntity.RECEIVE);
		List<ChatEntity> chatList = mChatMessagesMap.get(chat.getSenderId());
		if (chatList == null) {
			chatList = ImDB.getInstance(mContext).getChatMessage(
					chat.getSenderId());
			getChatMessagesMap().put(chat.getSenderId(), chatList);
		}
		chatList.add(chat);
		//缓存消息
		ImDB.getInstance(mContext).saveChatMessage(chat);
		if (messageHandler != null) {
			Message message = new Message();
			message.what = 1;
			messageHandler.sendMessage(message);
		}
		if (chatMessageHandler != null) {
			Message message = new Message();
			message.what = 1;
			chatMessageHandler.sendMessage(message);
		}
	}



	public Bitmap getUserPhoto() {
		return mUserPhoto;
	}

	public void setUserPhoto(Bitmap mUserPhoto) {
		this.mUserPhoto = mUserPhoto;
	}

	public List<MessageTabEntity> getMessageEntities() {
		return mMessageEntities;
	}

	public void setMessageEntities(List<MessageTabEntity> mMessageEntities) {
		this.mMessageEntities = mMessageEntities;
	}

	public void setMessageHandler(Handler handler) {
		this.messageHandler = handler;
	}

	public void setChatHandler(Handler handler) {
		this.chatMessageHandler = handler;
	}

	public void setfriendListHandler(Handler handler) {
		this.friendListHandler = handler;
	}

	public Map<Integer, Bitmap> getFriendPhotoMap() {
		return mFriendPhotoMap;
	}

	public void setFriendPhotoList(Map<Integer, Bitmap> mFriendPhotoMap) {
		this.mFriendPhotoMap = mFriendPhotoMap;
	}

	public User getUserInfo() {
		return mUser;
	}

	public List<User> getFriendList() {
		return mFriendList;
	}

	public TranObject getReceivedMessage() {
		return mReceivedMessage;
	}

	public void setReceivedMessage(TranObject mReceivedMessage) {
		this.mReceivedMessage = mReceivedMessage;
	}

	public List<User> getFriendSearched() {
		return mFriendSearched;
	}

	//搜索好友回调
	public void setFriendSearched(List<User> mFriendSearched) {
		System.out.println("setFriendSearched : " + mFriendSearched.size());
		this.mFriendSearched = mFriendSearched;
		Intent intent = new Intent(mTempContext, FriendSearchResultActivity.class);
		mTempContext.startActivity(intent);
	}

	public void saveContext(Context context){
		mTempContext = context;
	}
}
