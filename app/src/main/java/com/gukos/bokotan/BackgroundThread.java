package com.gukos.bokotan;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public abstract class BackgroundThread {
	protected final HandlerThread handlerThread;
	protected final Handler handler;
	protected final Context context;
	
	public abstract void handleMessage(Message message);
	public abstract void initialize();
	
	public BackgroundThread(Context context,String uniqueName){
		this.context=context;
		handlerThread=new HandlerThread("HT:"+uniqueName);
		handlerThread.start();
		handler=new Handler(handlerThread.getLooper()){
			@Override
			public void handleMessage(Message message){
				this.handleMessage(message);
			}
		};
		handler.post(this::initialize);
	}
	
}