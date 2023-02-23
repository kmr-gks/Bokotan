package com.gukos.bokotan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

//https://oc-technote.com/android/service%E3%81%8B%E3%82%89activity%E3%81%AB%E5%80%A4%E3%82%92%E6%8A%95%E3%81%92%E3%81%9F%E3%82%8A%E7%94%BB%E9%9D%A2%E3%82%92%E6%9B%B4%E6%96%B0%E3%81%97%E3%81%9F%E3%82%8A%E3%81%99%E3%82%8B%E6%96%B9/
public class DrawReceiver extends BroadcastReceiver {
	
	public static Handler handler;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (handler!=null){
			Message message=new Message();
			message.setData(intent.getExtras());
			handler.sendMessage(message);
		}
	}
	
	/**
	 * メイン画面の表示を更新
	 */
	public void registerHandler(Handler locationUpdateHandler) {
		handler = locationUpdateHandler;
	}
}