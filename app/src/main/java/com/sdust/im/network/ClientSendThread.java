package com.sdust.im.network;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.sdust.im.bean.TranObject;

public class ClientSendThread {
	private Socket mSocket = null;
	private ObjectOutputStream oos = null;
	public ClientSendThread(Socket socket) {
		this.mSocket = socket;
		try {
			oos = new ObjectOutputStream(mSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	//发送请求
	public void sendMessage(final TranObject t) throws IOException{
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					send(t);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void send(TranObject t)throws IOException{
		oos.writeObject(t);
		oos.flush();
	}
}
