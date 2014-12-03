package com.example.btsms;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
/**
 * Interface btThreadContacts describes 
 * the mechanism of reading the phonebook entry.
 * 
 * Version 1.0 October 17, 2014
 * Author Karpenko Alexander karpenkoAV@ukr.net
 * The Apache License 2
 */
public class btThreadContacts implements Runnable, btInterface {
	
	private MainActivity context;
	private BtSMS btSMS;
	private BluetoothDevice device;
	private int port;
	private Handler btHandler;

	public btThreadContacts(MainActivity mainActivity) {
		// TODO Auto-generated constructor stub
		this.context = mainActivity;
		this.device = mainActivity.device;
		this.port = mainActivity.RemoteDeviceRfcommPort;
		this.btHandler = mainActivity.btHandler;		
		this.btSMS = new BtSMS(this.btHandler);
	}
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(btSMS.btSocketConnect(this.device, this.port)){
			//If at the time of reading the phone book there is an error
			if(!btSMS.contacts())
				btHandler.sendMessage(btHandler.obtainMessage(STATUS_INTERNAL_ERROR,  
															  0, 
															  0, 
															  this.context.getResources().getString(R.string.ErrorBtReadPhoneBooks)));
				btSMS.btSocketClose();
		}
	}
}
