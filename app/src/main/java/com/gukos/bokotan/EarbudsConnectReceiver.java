package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import java.util.Objects;

public class EarbudsConnectReceiver {

	//本当はRunnableじゃなくてFunction<void,void>としたい。
	public EarbudsConnectReceiver(Context context, Runnable funcWiredEarPhoneConnected, Runnable funcWiredEarPhoneDisconnected, Runnable funcBluetoothEarPhoneConnected, Runnable funcBluetoothEarPhoneDisconnected) {
		try {
			BluetoothEarPhoneReceiver bluetoothEarPhoneReceiver = new BluetoothEarPhoneReceiver(funcBluetoothEarPhoneConnected, funcBluetoothEarPhoneDisconnected);
			WiredEarPhoneReceiver wiredEarPhoneReceiver = new WiredEarPhoneReceiver(funcWiredEarPhoneConnected, funcWiredEarPhoneDisconnected);

			IntentFilter intentFilterBluetooth = new IntentFilter();
			intentFilterBluetooth.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
			intentFilterBluetooth.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
			intentFilterBluetooth.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);

			IntentFilter intentFilterWired = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
			context.registerReceiver(bluetoothEarPhoneReceiver, intentFilterBluetooth);
			context.registerReceiver(wiredEarPhoneReceiver, intentFilterWired);
		} catch (Exception e) {
			showException(e);
		}
	}
}

class BluetoothEarPhoneReceiver extends BroadcastReceiver {
	private Runnable funcBluetoothEarPhoneConnected, funcBluetoothEarPhoneDisconnected;
	private boolean isConnected = true;

	public BluetoothEarPhoneReceiver(Runnable funcBluetoothEarPhoneConnected, Runnable funcBluetoothEarPhoneDisconnected) {
		try {
			this.funcBluetoothEarPhoneConnected = funcBluetoothEarPhoneConnected;
			this.funcBluetoothEarPhoneDisconnected = funcBluetoothEarPhoneDisconnected;
		} catch (Exception e) {
			showException(e);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			switch (intent.getAction()) {
				case BluetoothDevice.ACTION_ACL_CONNECTED: {
					isConnected = true;
					break;
				}
				case BluetoothDevice.ACTION_ACL_DISCONNECTED: {
					isConnected = false;
					break;
				}
				case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED: {
					if (isConnected) {
						funcBluetoothEarPhoneConnected.run();
					} else {
						funcBluetoothEarPhoneDisconnected.run();
					}
				}
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}
}

class WiredEarPhoneReceiver extends BroadcastReceiver {
	Runnable funcWiredEarPhoneConnected, funcWiredEarPhoneDisconnected;

	//本当はRunnableじゃなくてFunction<void,void>としたい。
	public WiredEarPhoneReceiver(Runnable funcWiredEarPhoneConnected, Runnable funcWiredEarPhoneDisconnected) {
		try {
			this.funcWiredEarPhoneConnected = funcWiredEarPhoneConnected;
			this.funcWiredEarPhoneDisconnected = funcWiredEarPhoneDisconnected;
		} catch (Exception e) {
			showException(e);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			if (intent == null || !Objects.equals(intent.getAction(), Intent.ACTION_HEADSET_PLUG)) {
				return;
			}
			int state = intent.getIntExtra("state", AudioManager.SCO_AUDIO_STATE_ERROR);
			switch (state) {
				case AudioManager.SCO_AUDIO_STATE_DISCONNECTED: {
					// 切断された時の処理
					funcWiredEarPhoneDisconnected.run();
					break;
				}
				case AudioManager.SCO_AUDIO_STATE_CONNECTED: {
					// 接続された時の処理
					funcWiredEarPhoneConnected.run();
					break;
				}
				default: {
					// その他の場合の処理
					break;
				}
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}
}