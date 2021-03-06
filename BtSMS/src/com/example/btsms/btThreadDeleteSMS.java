package com.example.btsms;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.widget.TextView;
/**
 * Interface btThreadDeleteSMS  describe 
 * removal mechanism sms from phone 
 * memory and database.
 * 
 * Version 1.0 October 17, 2014
 * Author Karpenko Alexander karpenkoAV@ukr.net
 * The Apache License 2
 */
public class btThreadDeleteSMS implements Runnable, btInterface {

	private MainActivity context;
	private BluetoothDevice device;
	private int port;
	private Handler btHandler;
	private TextView textView_bank_memory;
	private String[] checked;
	private String[] remote_device_sms_id;
	private BtSMS btSMS;

	public btThreadDeleteSMS(MainActivity mainActivity,
			String[] checked) {
		// TODO Auto-generated constructor stub
		this.context = mainActivity;
		this.device = mainActivity.device;
		this.port = mainActivity.RemoteDeviceRfcommPort;
		this.btHandler = mainActivity.btHandler;
		this.textView_bank_memory = mainActivity.textView_bank_memory;	
		this.checked = checked;
		this.remote_device_sms_id = mainActivity.remote_device_sms_id;
		this.btSMS = new BtSMS(this.btHandler);		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(btSMS.btSocketConnect(this.device, this.port)){
			if(this.textView_bank_memory.getText().equals(this.context.getResources().getString(R.string.textView_bank_memory_text_sim)))
				btSMS.deleteSMS("SM", checked, remote_device_sms_id);
			else
				btSMS.deleteSMS("ME", checked, remote_device_sms_id);
		}
		btSMS.btSocketClose();
		//notify the main thread that removing an SMS with the selected memory bank is completed
		btHandler.sendEmptyMessage(STATUS_END_THREAD);			
	}

}
