package com.sdust.im.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;

import com.sdust.im.activity.SearchFriendActivity;
import com.sdust.im.activity.register.StepAccount;
import com.sdust.im.activity.register.StepPhoto;
import com.sdust.im.bean.ApplicationData;
import com.sdust.im.bean.TranObject;

import android.content.Context;

//客户端响应服务端消息的Thread
public class ClientListenThread extends Thread {
	private Socket mSocket = null;
	private Context mContext = null;
	private ObjectInputStream mOis;    //对象输入流

	private boolean isStart = true;

	public ClientListenThread(Context context, Socket socket) {
		this.mContext = context;
		this.mSocket = socket;
		try {
			mOis = new ObjectInputStream(mSocket.getInputStream());
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setSocket(Socket socket) {
		this.mSocket = socket;
	}

	@Override
	public void run() {
		try {
			isStart = true;
			while (isStart) {
				TranObject mReceived = null;
				if (!mSocket.isConnected()){
					System.out.println("连接已断开，出现异常情况");
				}
				mReceived = (TranObject) mOis.readObject();
				System.out.println("接受成功");
				System.out.println(mReceived.getTranType());
				switch (mReceived.getTranType()) {
					case REGISTER_ACCOUNT:
						StepAccount.setRegisterInfo(mReceived, true);
						System.out.println("验证账号有效，可注册");
						break;
					case REGISTER:
						StepPhoto.setRegisterInfo(mReceived, true);
						System.out.println("注册账号成功");
						break;
					case LOGIN:
						ApplicationData.getInstance().loginMessageArrived(mReceived);
						System.out.println("登陆账号成功");
						break;
					case SEARCH_FRIEND:
						System.out.println("收到朋友查找结果");
						SearchFriendActivity.messageArrived(mReceived);
						break;
					case FRIEND_REQUEST:
						ApplicationData.getInstance().friendRequestArrived(mReceived);
						System.out.println("收到添加好友请求");
						break;
					case MESSAGE:
						ApplicationData.getInstance().messageArrived(mReceived);
						System.out.println("聊天消息到达");
						break;
					default:
						break;
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public void close() {
		isStart = false;
	}
}
