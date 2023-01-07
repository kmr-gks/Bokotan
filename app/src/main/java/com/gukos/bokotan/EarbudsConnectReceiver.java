package com.gukos.bokotan;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

public class EarbudsConnectReceiver {
	public IntentFilter ifBluetooth,ifWired;
	public BluetoothEarPhoneReceiver bluetoothEarPhoneReceiver;
	public WiredEarPhoneReceiver wiredEarPhoneReceiver;

	//本当はRunnableじゃなくてFunction<void,void>としたい。
	public EarbudsConnectReceiver(Runnable funcWiredEarPhoneConnected, Runnable funcWiredEarPhoneDisconnected , Runnable funcBluetoothEarPhoneConnected, Runnable funcBluetoothEarPhoneDisconnected){
		bluetoothEarPhoneReceiver=new BluetoothEarPhoneReceiver(funcBluetoothEarPhoneConnected,funcBluetoothEarPhoneDisconnected);
		wiredEarPhoneReceiver=new WiredEarPhoneReceiver(funcWiredEarPhoneConnected,funcWiredEarPhoneDisconnected);

		ifBluetooth=new IntentFilter();
		ifBluetooth.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		ifBluetooth.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		ifWired=new IntentFilter(Intent.ACTION_HEADSET_PLUG);
	}
}

class BluetoothEarPhoneReceiver extends BroadcastReceiver {
	Runnable funcBluetoothEarPhoneConnected,funcBluetoothEarPhoneDisconnected;
	public BluetoothEarPhoneReceiver(Runnable funcBluetoothEarPhoneConnected,Runnable funcBluetoothEarPhoneDisconnected){
		this.funcBluetoothEarPhoneConnected=funcBluetoothEarPhoneConnected;
		this.funcBluetoothEarPhoneDisconnected=funcBluetoothEarPhoneDisconnected;
	}
	@Override
	public void onReceive(Context context, Intent intent){
		switch (intent.getAction()){
			case BluetoothDevice.ACTION_ACL_CONNECTED:{
				funcBluetoothEarPhoneConnected.run();
				break;
			}
			case BluetoothDevice.ACTION_ACL_DISCONNECTED:{
				funcBluetoothEarPhoneDisconnected.run();
				break;
			}
		}
	}
}

class WiredEarPhoneReceiver extends BroadcastReceiver {
	Runnable funcWiredEarPhoneConnected,funcWiredEarPhoneDisconnected;
	//本当はRunnableじゃなくてFunction<void,void>としたい。
	public WiredEarPhoneReceiver(Runnable funcWiredEarPhoneConnected,Runnable funcWiredEarPhoneDisconnected){
		this.funcWiredEarPhoneConnected=funcWiredEarPhoneConnected;
		this.funcWiredEarPhoneDisconnected=funcWiredEarPhoneDisconnected;
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent==null||intent.getAction()!=Intent.ACTION_HEADSET_PLUG){
			return;
		}
		int state=intent.getIntExtra("state", AudioManager.SCO_AUDIO_STATE_ERROR);
		switch (state){
			case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:{
				// 切断された時の処理
				funcWiredEarPhoneDisconnected.run();
				break;
			}
			case AudioManager.SCO_AUDIO_STATE_CONNECTED:{
				// 接続された時の処理
				funcWiredEarPhoneConnected.run();
				break;
			}
			default:{
				// その他の場合の処理
				break;
			}
		}
	}
}
