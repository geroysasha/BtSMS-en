package com.example.btsms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 *BtMessage class contains methods work
 *with dialogs, and indicators of progress.
 *
 *Version 1.0 October 17, 2014
 *Author Karpenko Alexander karpenkoAV@ukr.net
 *The Apache License 2
 */
public class BtMessage implements btInterface {
	

	private int itemSelect = -1;
	private int RemoteDeviceRfcommPort = -1;
	private Context context;
	private ProgressDialog DiscoveryProgress;
	private ProgressDialog ReadProgress;
	private AlertDialog.Builder builder;
	private String alertDialogID = "";
	private Handler btHandler;
	
	//returns Rfcomm port connected to the tablet pc
	public int getRemoteDeviceRfcommPort(){
		return RemoteDeviceRfcommPort;
	}
	
	//set context
	public void setBtMessageContext(Context mainContext){
		context = mainContext;
	};
	
	//generates dialogues and indicators of progress with the main parameters
	public void createDialog(final Handler handler, 
			final TextView textView_bank_memory){
        DiscoveryProgress= new ProgressDialog(context);
        ReadProgress= new ProgressDialog(context);
        DiscoveryProgress.setCancelable(false);
        ReadProgress.setCancelable(false);      
        btHandler = handler;
        builder = new AlertDialog.Builder(context);
        builder.setCancelable(false)
		.setPositiveButton(R.string.alertDialogBtnConnOk, 
				new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//if the handler is invoked dialog "selection of a memory bank"
				if(itemSelect != -1){
					//notify the main thread that memory bank selected
					if(itemSelect == 0)
						textView_bank_memory.setText(R.string.textView_bank_memory_text_sim); 
					else
						textView_bank_memory.setText(R.string.textView_bank_memory_text_phone); 				
				}
				switch(alertDialogID) {
				case "AlertDialogDelete":
					btHandler.sendEmptyMessage(STATUS_LIST_VIEW_ITEM_DELETE);					
					break;
				case "AlertDialogConnectionDevice":
					break;
				};
				alertDialogID = "";						
			}
		});
	};

	//dialog box reports an error
	public void AlertDialogError(String title, String error){
		builder.setTitle(title)
		.setMessage(error)	
		.setNegativeButton(null, null);
		builder.create().show();
	};	

	//dialog box, confirm removal sms
	public void AlertDialogDelete(String title, String error){
		alertDialogID = "AlertDialogDelete";
		builder.setTitle(title)
		.setMessage(error)
		.setNegativeButton(R.string.alertDialogClose, 
				new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		builder.create().show();
	};	
	
	/* the dialog box select the phone number from the list of detected local
	adapter bluetooth */
	public void AlertDialogConnectionDevice(final String[] arrDiscoveryDevice, 
			final TextView deviceField ){

		alertDialogID = "AlertDialogConnectionDevice";
		builder.setTitle(R.string.alertDialogBtnConnectionTitle)
		.setMessage(null)
		.setNegativeButton(R.string.alertDialogClose, 
				new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				deviceField.setText(R.string.textDeviceFieldDefaul);
				dialog.cancel();
			}
		})		
		.setSingleChoiceItems(arrDiscoveryDevice, -1, 
				new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//remembers the selected device
				deviceField.setText(arrDiscoveryDevice[which]);
				RemoteDeviceRfcommPort = PortRequest(arrDiscoveryDevice[which]);
				//Log.e(this.toString(), "port=" + String.valueOf(RemoteDeviceRfcommPort));
				//Toast.makeText(context, String.valueOf(which), Toast.LENGTH_SHORT).show();
			}
		});
		builder.create().show();
	};	
	
	//dialog to select the memory bank from which read sms
	public void AlertDialogSelectBankMemory(){
		Log.e(this.toString(),"AlertDialogSelectBankMemory");
		String[] arrBankMemory = {context.getResources().getString(R.string.textView_bank_memory_text_sim)
									, context.getResources().getString(R.string.textView_bank_memory_text_phone)};
		builder.setTitle(R.string.alertDialogSelectBankMemory)
		.setMessage(null)
		.setNegativeButton(null, null)
		.setSingleChoiceItems(arrBankMemory, -1, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				itemSelect = which;
				Toast.makeText(context, String.valueOf(which), Toast.LENGTH_SHORT).show();
			}
		});
		builder.create().show();
	};	
	
	//dialog to select a contact from the list
	public void AlertDialogSelectContacts(final String[] arrDiscoveryDevice, 
			final EditText number_phone ){

		builder.setTitle(R.string.alertDialogBtnConnectionTitle)
		.setMessage(null)
		.setNegativeButton(R.string.alertDialogClose, 
				new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				number_phone.setText("");
				dialog.cancel();
			}
		})		
		.setSingleChoiceItems(arrDiscoveryDevice, -1, 
				new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//remember selected contact
				String[] arrTmp = arrDiscoveryDevice[which].split(",");
				number_phone.setText(arrTmp[1] + " " + arrTmp[2]);
			}
		});
		builder.create().show();
	};	
	
	//progress reading sms
	public void ProgressDialogReadMessage(int max){
		ReadProgress.setTitle("Read message");
		ReadProgress.setMessage("Message");
		ReadProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		ReadProgress.setMax(max);
		ReadProgress.show();
	}

	//increment progress reading sms
	public void setProgressDialogReadMessage(){
	      if (ReadProgress.getProgress() < ReadProgress.getMax() - 1) {
	    	  ReadProgress.incrementProgressBy(1);
	      } else {
	    	  ReadProgress.setMax(0);
	    	  ReadProgress.dismiss();
	      }
	}	
	
	//progress waiting
	public void ProgressDialogWait(){
		DiscoveryProgress.setTitle(context.getResources().getString(R.string.ProgressDialogWaitTitle));
		DiscoveryProgress.show();
	}
	
	//progress waiting scanning devices
	public void ProgressDialogDiscoveryDevice(final BluetoothAdapter LocalAdapter){

		DiscoveryProgress.setTitle(R.string.ProgressDialogDiscoveryDeviceTitle);
		DiscoveryProgress.setButton(Dialog.BUTTON_POSITIVE
									, context.getResources().getString(R.string.ProgressDialogDiscoveryDeviceCancel) 
									, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				LocalAdapter.cancelDiscovery();
				DiscoveryProgress.dismiss();
			}
		});
		DiscoveryProgress.show();
		if(!LocalAdapter.isDiscovering())
			LocalAdapter.startDiscovery();
	};
	
	//close progress
	public void ProgressDialogClose(){
		DiscoveryProgress.dismiss();
	}
	
	//returns Rfcomm port of the remote device
	private int PortRequest(String deviceField){
		Log.e(this.toString(), "PortRequest() event");
		Runtime runtime = Runtime.getRuntime();
		Process ls = null;
    	String[] macRemoteDevice = deviceField.split("    ");  	
		try {
			//ls = runtime.exec("sdptool browse " + macRemoteDevice[1]);
			ls = runtime.exec("sdptool search 0x1101 ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InputStream in = ls.getInputStream();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		String line;
		int i = 0;
		try {
			while ((line = reader.readLine()) != null) {
				
				if (line.indexOf("Searching for 0x1101 on") != -1 || i == 1){
					if (line.indexOf(macRemoteDevice[1]) != -1 || i == 1){
						if(line.indexOf("Channel:") != -1){
							String[] arrInfo = line.split(" ");
							int portRfcomm = Integer.parseInt(arrInfo[arrInfo.length - 1]) ;
							if (portRfcomm < 10){			
								return portRfcomm;
							}							
						}
						else if(i == 0)
							i = 1;
					}
					else
						i = 0;
				}
			}
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ls.destroy();
			return -1;
		};
		ls.destroy();
		return -1;	
	};	
	
}
