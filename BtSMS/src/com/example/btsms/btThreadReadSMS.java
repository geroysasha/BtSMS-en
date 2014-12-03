package com.example.btsms;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.widget.TextView;
/**
 * Interface btThreadReadSMS describes 
 * the mechanism for reading sms from 
 * the phone or SIM card memory.
 * 
 * Version 1.0 October 17, 2014
 * Author Karpenko Alexander karpenkoAV@ukr.net
 * The Apache License 2
 */
public class btThreadReadSMS implements Runnable, btInterface {

	private MainActivity context;
	private BluetoothDevice device;
	private int port;
	private Handler btHandler;
	private BtSMS btSMS;
	private TextView textView_bank_memory;

	public btThreadReadSMS(MainActivity mainActivity) {
		// TODO Auto-generated constructor stub
		this.context =  mainActivity;
		this.device = mainActivity.device;
		this.port = mainActivity.RemoteDeviceRfcommPort;
		this.btHandler = mainActivity.btHandler;
		this.btSMS = new BtSMS(this.btHandler);
		this.textView_bank_memory = mainActivity.textView_bank_memory;		
	}

	@Override
	public void run() {
		
		// TODO Auto-generated method stub
		try {
			if(btSMS.btSocketConnect(this.device, this.port)){
				//check the support phone reading sms on BLUETOOTH
				if(!btSMS.SMSsupport()){
					//notify the main thread that the phone does not support reading sms via BLUETOOTH
					this.btHandler.sendEmptyMessage(1); 
				}else{
					if( btSMS.memoryEeprom()){
						//notify the main thread that you need to stop and select the memory bank for reading sms
						this.btHandler.sendEmptyMessage(2);   
						//stop the flow until the user selects the memory bank
						while(this.textView_bank_memory.getText().equals(context.getResources().getString(R.string.textView_bank_memory_text)))
								Thread.sleep(1000);
						if(this.textView_bank_memory.getText().equals(context.getResources().getString(R.string.textView_bank_memory_text_sim)))
							btSMS.readSMS("SM");
						else
							btSMS.readSMS("ME");
					}
				}
				btSMS.btSocketClose();
				//notify the main thread that read sms from the selected memory bank is completed
				btHandler.sendEmptyMessage(STATUS_END_THREAD);					
			}

		} catch (IllegalArgumentException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			btSMS.btSocketClose();
			//notify the main thread an internal error
			btHandler.sendMessage(btHandler.obtainMessage(STATUS_INTERNAL_ERROR,  0, 0, e.toString()));			
		}
	
	}

}
