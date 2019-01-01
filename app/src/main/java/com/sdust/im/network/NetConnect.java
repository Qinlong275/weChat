package com.sdust.im.network;

import java.io.IOException;
import java.net.*;

import android.util.Log;

public class NetConnect {

	private Socket mClientSocket = null;
	public static String SERVER_IP = "47.106.182.240";
	public static int SERVER_PORT = 8399;
	private boolean mIsConnected = false;

	public NetConnect() {
	}

	public void startConnect() {
		try {
			mClientSocket = new Socket();
			Log.d("qin Network", NetConnect.SERVER_IP + "....." + NetConnect.SERVER_PORT);
			mClientSocket.connect(
					new InetSocketAddress(NetConnect.SERVER_IP, NetConnect.SERVER_PORT), 3000);
			Log.d("Network", "服务器连接成功");
			if (mClientSocket.isConnected()) {
				mIsConnected = true;
			} else {
				mIsConnected = false;
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Log.d("Network", "服务器地址无法解析");
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("Network", "Socket io异常");
		}
	}

	public boolean getIsConnected() {
		return mIsConnected;
	}

	public Socket getSocket() {
		return mClientSocket;
	}

}
