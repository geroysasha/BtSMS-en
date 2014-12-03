package com.example.btsms;


import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Class MainActivity determining the contents of Activity,
 * specifies handlers control registers
 * broadcast receivers of system messages
 * local BLUETOOTH adapter, defines the processing
 * messages from the data flow.  
 * 
 * Version 1.0 October 17, 2014
 * Author Karpenko Alexander karpenkoAV@ukr.net
 * The Apache License 2
 */
public class MainActivity extends Activity implements OnClickListener, btInterface{
	
	//to work with a local adapter
	BluetoothAdapter LocalAdapter;
	//to work with a remote devices
	BluetoothDevice device;
	//port of the remote device providing the service 0x1101
	int RemoteDeviceRfcommPort;
	TabHost  tabHost;
	//displays the name and MAC adpes remote device
	TextView deviceField; 
	//displays the active memory bank phone
	TextView textView_bank_memory;
	//to create and connect SQLite database
	BtDbHelper btDbHelper;
	//for operation with the database
	SQLiteDatabase db;
	//to add records to the database
	ContentValues btContentValues;
	//for working with dialog boxes
	BtMessage btMessage;
	//receiving messages from the threads
	Handler btHandler;
	//contains the names of devices visibles local BLUETOOTH adapter
	ArrayAdapter<String> adapter;
	//contains text sms phone  is connected to the tablet PC
	ListView ListView_inbox;
	//field enter the phone number of the recipient sms
	EditText number_phone;
	//contains text sms
	EditText text_message;
	//contains  хранит массив ID смс в базу данных
	String[] ListView_inbox_sms_id;
	//stores an array of ID sms database
	String[] remote_device_sms_id;
	//Receivers of broadcast messages from local BLUETOOTH adapter
	BroadcastReceiver StatusAdapter;
	BroadcastReceiver DiscoveryResult;
	//message display that the phone does not support reading sms
	TextView phoneIsNotSupported;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //creating a database storing SMS
        btDbHelper = new BtDbHelper(this);
        btContentValues = new ContentValues();
        //dialog boxes
    	btMessage = new BtMessage();
    	//set context for dialog boxes
    	btMessage.setBtMessageContext(this);
        
        LocalAdapter = BluetoothAdapter.getDefaultAdapter();

        //find TabHost
        tabHost = (TabHost) this.findViewById(android.R.id.tabhost);
        tabHost.setup();
        
        TabHost.TabSpec tabSpec;
        
        tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setIndicator("Create SMS");
        tabSpec.setContent(tabContentFactory);
        tabHost.addTab(tabSpec);
        
        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setIndicator("InBox", getResources().getDrawable(R.drawable.ic_contacts));
        tabSpec.setContent(tabContentFactory);        
        tabHost.addTab(tabSpec);
        
        tabHost.setCurrentTabByTag("tag2");
        
        //find TextView
        phoneIsNotSupported = (TextView) tabHost.findViewById(R.id.phoneIsNotSupported);
        textView_bank_memory = (TextView) tabHost.findViewById(R.id.textView_bank_memory);
        final TextView textView_number_characters = (TextView) tabHost.findViewById(R.id.textView_number_characters);

        
        //find EditText
        number_phone = (EditText) tabHost.findViewById(R.id.editText_number_phone);
        text_message = (EditText) tabHost.findViewById(R.id.editText_message);
        
        //handler changes in the SMS editor text_message
        text_message.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub		
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub					
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				String text = text_message.getText().toString();
				int len = text.length();
				textView_number_characters.setText(String.valueOf(152 - len));				
			}});        
        
        //find ListView
        ListView_inbox = (ListView) tabHost.findViewById(R.id.listView_inbox);
        ListView_inbox.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); 
	
        //find Spinner
        final Spinner spinner = (Spinner) tabHost.findViewById(R.id.spinner_set_mode);
        spinner.setSelection(0);

        //handler changes of Spinner
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
				Toast.makeText(getApplicationContext(), "Position = " + position, Toast.LENGTH_SHORT).show();
	    		//connect database
	    		db = btDbHelper.getWritableDatabase();
	    		//contains the status SMS of which are selected from the database
	    		String status = "";
				switch(position){
            	case 0:
            		//received unread
            		status = "REC UNREAD";
            		break;
            	case 1:
            		//received read
            		status = "REC READ";
            		break;
            	case 2:
            		//unsent
            		status = "STO UNSENT";
            		break;
            	case 3:
            		//sent
            		status = "STO SENT";
            		break; 					
				}
				//to select from the database according to the status sms
				readDB(status,ListView_inbox);
				db.close();
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});   
        
        //find button

        ImageButton ButtonNewConnection = (ImageButton) findViewById(R.id.ButtonNewConnection);
        ImageButton ButtonContacts = (ImageButton) tabHost.findViewById(R.id.ButtonContacts);
        Button ButtonSend = (Button) tabHost.findViewById(R.id.buttonSend);
        ImageButton ButtonReload = (ImageButton) tabHost.findViewById(R.id.ButtonReload);
        Button ButtonReply = (Button) tabHost.findViewById(R.id.buttonReply);
        Button buttonDelete = (Button) tabHost.findViewById(R.id.buttonDelete);
        
        //declare handlers button
        ButtonNewConnection.setOnClickListener(this);
        ButtonContacts.setOnClickListener(this);
        ButtonSend.setOnClickListener(this);
        ButtonReload.setOnClickListener(this);
        ButtonReply.setOnClickListener(this);
        buttonDelete.setOnClickListener(this);
        
        //register the receiver of messages from local BLUETOOTH adapter
        btCreateRegisterReceiver();
        
        //message handler of data flow
        btHandler = new Handler(){      	

			public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                //signal the maximum number of SMS to your phone
                case STATUS_MAX_SMS: 
                	btMessage.ProgressDialogClose();
                	btMessage.ProgressDialogReadMessage(msg.arg1);
                	break;
                //signal that the phone does not support reading via BLUETOOTH
                case STATUS_PHONE_IS_NOT_SUPPORTED:
                	btMessage.ProgressDialogClose();
                	ListView_inbox.setVisibility(0);
                	phoneIsNotSupported.setVisibility(1);
                	break;
                //selection signal memory bank
                case STATUS_SELECT_BAMK_MEMORY:
                	btMessage.AlertDialogSelectBankMemory();
                	break;
                //signal increment horizontal progress
                case STATUS_SET_PROGRESS_SMS:
                	//if the message from the flow reading sms
                	if(msg.arg2 == 0){
	                	String[] messages = new String[5];
						if(MainActivity.this.textView_bank_memory.getText().equals(MainActivity.this.getResources().getString(R.string.textView_bank_memory_text_sim)))
							messages = (String[]) msg.obj;
	                	else
	                		messages = (String[]) msg.obj;
						if (messages != null){
		            		btContentValues.put("bank", messages[0]);
		            		btContentValues.put("status", messages[1]);
		            		btContentValues.put("date", messages[2]); 
		            		btContentValues.put("number", messages[3]);
		            		btContentValues.put("content", messages[4]);
		            		btContentValues.put("num", messages[5]);
		            	    //Insert a record and get her ID
		            	    db.insert("btTable", null, btContentValues);  
	                	}
			  //if the message from the stream which removes sms	
                	} else{
                		//removed from the database sms
                		//Log.e(this.toString(), "id = " + ListView_inbox_sms_id[Integer.valueOf((String) msg.obj)] );
                		db.delete("btTable", 
                				  "id = " + ListView_inbox_sms_id[Integer.valueOf((String) msg.obj)],
                				  null);
                	}
                	//increment progress
                	btMessage.setProgressDialogReadMessage();
                	break;
                //signal the end of the thread
                case STATUS_END_THREAD:
                	String status = "";
                	switch(spinner.getSelectedItemPosition()){
                	case 0:
                		status = "REC UNREAD";
                		break;
                	case 1:
                		status = "REC READ";
                		break;
                	case 2:
                		status = "STO UNSENT";
                		break;
                	case 3:
                		status = "STO SENT";
                		break;                		
                	}
                	//read database
                	readDB(status,ListView_inbox);
                	db.close();
                	break;
                //signal delete the selected  from the list of SMS
                case STATUS_LIST_VIEW_ITEM_DELETE:
                	//processes the message from the btMessage.AlertDialogDelete
                	///////////////////////////////////////////
    	    		//connect database
    	    		db = btDbHelper.getWritableDatabase();
    	    		
    	    		//show progressDialog
                	btMessage.ProgressDialogWait();
    	    		
    		    	String[] macremoteDeviceInfo = ((String) deviceField.getText()).split("    ");  
    		    	device = LocalAdapter.getRemoteDevice(macremoteDeviceInfo[1]);	
    	    		Runnable btRunnable = new btThreadDeleteSMS(MainActivity.this, ListView_inbox_checked());
    			//thread data exchange BLUETOOTH
    	    		Thread btTh = new Thread(btRunnable);	
    				btTh.start();   
                	break;
                //completion signal read contacts list
                case STATUS_END_READ_CONTACT:
                	btMessage.ProgressDialogClose();
                	btMessage.AlertDialogSelectContacts((String[]) msg.obj, number_phone);
                	break;
                //completion signal sending sms
                case STATUS_END_THREAD_SEND_SMS:
                	btMessage.ProgressDialogClose();
                	if(msg.arg1 == 1)
                		btMessage.AlertDialogError(MainActivity.this.getResources().getString(R.string.alertDialogStatusSendSmsTitle)
                								    , MainActivity.this.getResources().getString(R.string.alertDialogStatusSendSmsOk));
                	else
                		btMessage.AlertDialogError(MainActivity.this.getResources().getString(R.string.alertDialogStatusSendSmsTitle)
								   					, MainActivity.this.getResources().getString(R.string.alertDialogStatusSendSmsErr));
                	break;
                //signal internal error
                case STATUS_INTERNAL_ERROR:
                	btMessage.ProgressDialogClose();
                	btMessage.AlertDialogError(MainActivity.this.getResources().getString(R.string.alertDialogBtError)
                							   , (String) msg.obj);
                	break;
                }     		
        	};
        };
        
    	//creat dialog box
    	btMessage.createDialog(btHandler, textView_bank_memory);
    	//default text
    	deviceField.setText(R.string.textDeviceFieldDefaul);
    }
    
    	//reading from the database and display information in ListView_inbox
	void readDB(String status,ListView ListView_inbox){
		
	//contains selected from the database sms
    	ArrayList<String> arrayMessage = new ArrayList<String>();
    	
    	Cursor c;
		if(textView_bank_memory.getText().equals(MainActivity.this.getResources().getString(R.string.textView_bank_memory_text_sim)))
            c = db.query("btTable"
            				, null 
            				, "bank = \"SM\" AND status = \""+ status +"\"" 
            				, null 
            				, null
            				, null 
            				, null);
		else
            c = db.query("btTable" 
            				, null
            				, "bank = \"ME\" AND status = \"" + status + "\""
            				, null 
            				, null
            				, null
            				, null);

		ListView_inbox_sms_id = new String[c.getCount()];
		remote_device_sms_id = new String[c.getCount()];
        if (c.moveToFirst()) {

          int idColIndex = c.getColumnIndex("id");
          //int bankColIndex = c.getColumnIndex("bank");
          //int statusColIndex = c.getColumnIndex("status");
          //int dateColIndex = c.getColumnIndex("date");
          //int numberColIndex = c.getColumnIndex("number");
          int contentColIndex = c.getColumnIndex("content");
          int numColIndex = c.getColumnIndex("num");
          int i = 0;
          do {
        	  ListView_inbox_sms_id[i] = c.getString(idColIndex);
        	  remote_device_sms_id[i] = c.getString(numColIndex);
        	  arrayMessage.add(c.getString(contentColIndex)); 
        	  i++;
          } while (c.moveToNext());  
          
  		adapter = new ArrayAdapter<String>(MainActivity.this 
  											, android.R.layout.simple_list_item_single_choice 
  											, arrayMessage.toArray(new String[0]));
  		ListView_inbox.setAdapter(adapter);  
        }else{
        	ListView_inbox.setAdapter(null);; 
        	Log.e(this.toString(), "0 rows");
        }
        c.close();		
	}
    
	//handler ListView_inbox, which searches selected item in the list
	String[] ListView_inbox_checked()
	{
		ArrayList<String> listViewChecked = new ArrayList<String>();
	    SparseBooleanArray sbArray = ListView_inbox.getCheckedItemPositions();
	    //String[] arr;
	    for (int i = 0; i < sbArray.size(); i++) {
	      int key = sbArray.keyAt(i);
	      if (sbArray.get(key))
	    	  listViewChecked.add(String.valueOf(key));
	    }	
	    return listViewChecked.toArray(new String[0]);
	}
	
    //defines the content of the active tab TabHost
    TabHost.TabContentFactory tabContentFactory = new TabHost.TabContentFactory() {
		
		@Override
		public View createTabContent(String tag) {
			switch(tag){
			case "tag1":
				return getLayoutInflater().inflate(R.layout.create_sms, null);
			case "tag2":
				return getLayoutInflater().inflate(R.layout.inbox, null);
			};		
			// TODO Auto-generated method stub
			return null;
		}
	};

	void btCreateRegisterReceiver(){
		
		//keep track of end of the scan
		StatusAdapter = null;
		//keep track of remote devices that are available
		DiscoveryResult = null;
		
		deviceField = (TextView) findViewById(R.id.textView_device);
		final ArrayList<String> remoteDeviceArray = new ArrayList<String>();
		//triggered at the end of the scan BLUETOOTH
		StatusAdapter = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				//disable the receivers of messages from the local Bluetooth adapter
				btUnRegisterReceiver();
				//transform into an array of strings
				String[] StringRemoteDevice = remoteDeviceArray.toArray(new String[0]);
				remoteDeviceArray.clear();
				btMessage.ProgressDialogClose();
				btMessage.AlertDialogConnectionDevice(StringRemoteDevice, deviceField);
			}};
		//handles each BLUETOOTH device
		DiscoveryResult = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Toast.makeText(context, device.getName() + " " + device.getAddress(), Toast.LENGTH_SHORT).show();
				remoteDeviceArray.add(device.getName() + "    " + device.getAddress());			
			}};		
	};
	
	void btRegisterReceiver(){
		//register the receivers
		registerReceiver(StatusAdapter, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		registerReceiver(DiscoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));	
	}
	
	void btUnRegisterReceiver(){
		//disable receivers
		unregisterReceiver(StatusAdapter);
		unregisterReceiver(DiscoveryResult);
	}
	
	@Override
	public void onClick(View v) {

	
		// TODO Auto-generated method stub
		String[] checked;
		Runnable btRunnable;
		Thread btTh;		
		
    	RemoteDeviceRfcommPort = btMessage.getRemoteDeviceRfcommPort();
		switch(v.getId()){
		//new connection
		case R.id.ButtonNewConnection:
			//if already connect the phone that does not support reading sms
			if(phoneIsNotSupported.getVisibility() == View.VISIBLE)
				phoneIsNotSupported.setVisibility(View.INVISIBLE);
			if(LocalAdapter==null){
				btMessage.AlertDialogError(this.getResources().getString(R.string.alertDialogBtError)
										   , this.getResources().getString(R.string.BtErrorAdapterNotSupport) );
			}else{
				//register the receivers of messages from the local Bluetooth adapter
				btRegisterReceiver();
				if(LocalAdapter.isEnabled()){
					if(!LocalAdapter.isDiscovering()){
						btMessage.ProgressDialogDiscoveryDevice(LocalAdapter);
					}
				}else{
					Intent enableBtIntent = new Intent(LocalAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, 1);
				}
			}	
			break;
		//get list of contacts
		case R.id.ButtonContacts:
			if(!((String) deviceField.getText()).equals("No device")){
				btMessage.ProgressDialogWait();			
	    		btRunnable = new btThreadContacts(MainActivity.this);
			//start data exchange BLUETOOTH
	    		btTh = new Thread(btRunnable);
				btTh.start();			
			}else
				btMessage.AlertDialogError(this.getResources().getString(R.string.alertDialogBtError)
										  , this.getResources().getString(R.string.alertDialogBtnContactsErr));
			break;	
		//send sms
		case R.id.buttonSend:
			if(!number_phone.getText().toString().equals("")  && !((String) deviceField.getText()).equals("")){
	    		//show progressDialog
	    		btMessage.ProgressDialogWait();
	    		
		    	String[] macRemoteDeviceInfo = ((String) deviceField.getText()).split("    ");  
		    	device = LocalAdapter.getRemoteDevice(macRemoteDeviceInfo[1]);	
		    	String[] number_phone_split = number_phone.getText().toString().split(" ");
	    		btRunnable = new btThreadSendSMS(MainActivity.this
							    				, number_phone_split[0]
							    				, (String) text_message.getText().toString());
			//start data exchange BLUETOOTH
	    		btTh = new Thread(btRunnable);	
				btTh.start(); 	
			}else
				btMessage.AlertDialogError(this.getResources().getString(R.string.alertDialogBtError)
										   , this.getResources().getString(R.string.alertDialogBtnSendErr));
			break;	
		//reload the SMS messages from the phone memory bank
		case R.id.ButtonReload:
	    	if (((String) deviceField.getText()).equals("No device"))
	    		btMessage.AlertDialogError(this.getResources().getString(R.string.alertDialogBtError)
	    									, this.getResources().getString( R.string.ErrorBtRemoteNoSelected));
	    	else{
				
	    		//connect database
	    		db = btDbHelper.getWritableDatabase();
	    		
	    		// if the program has already been used
	    		if(!this.textView_bank_memory.getText().equals(this.getResources().getString(R.string.textView_bank_memory_text))){
		    		//clean up the old records in the database by the condition
					if(this.textView_bank_memory.getText().equals(this.getResources().getString(R.string.textView_bank_memory_text_sim)))
			    		//check whether there is a record
			    		if (db.query("btTable"
			    				, null, "bank = \"SM\""
			    				, null
			    				, null
			    				, null
			    				, null).getCount() != 0) {
			    			db.delete("btTable", "bank = \"SM\"", null);
			    		}else
			    		//check whether there is a record
			    		if (db.query("btTable"
			    				, null
			    				, "bank = \"ME\""
			    				, null
			    				, null
			    				, null
			    				, null).getCount() != 0) {
			    			db.delete("btTable", "bank = \"ME\"", null);
			    		}	    			
	    		}else{
	    			Cursor c = db.query("btTable"
	    								, null
	    								, null
	    								, null
	    								, null
	    								, null
	    								, null);
	    			if(c.getColumnCount() != 0)
	    				db.delete("btTable", null, null);
	    		}

				//set the default value
				textView_bank_memory.setText(this.getResources().getString(R.string.textView_bank_memory_text));	    		
	    		//show progressDialog
	    		btMessage.ProgressDialogWait();
	    		 
	    		String[] macRemoteDeviceInfo = ((String) deviceField.getText()).split("    ");  
		    	device = LocalAdapter.getRemoteDevice(macRemoteDeviceInfo[1]);	
	    		btRunnable = new btThreadReadSMS(this);
			//start data exchange BLUETOOTH
	    		btTh = new Thread(btRunnable);
				btTh.start();
	    	}
			break;
		//answer selected from the list of SMS
		case R.id.buttonReply:
			checked = ListView_inbox_checked();
			if(checked.length > 1 || checked.length == 0)
				btMessage.AlertDialogError(this.getResources().getString(R.string.alertDialogBtError)
										   , this.getResources().getString(R.string.alertDialogBtnReplyMessage));
			else if (checked.length == 1){
				//method ListView_inbox_checked () returns only one selected item
				String[] item = ListView_inbox_checked();
				String telephone = "";
				tabHost.setCurrentTabByTag("tag1");
				//checks that the message recipient has contact
				if(adapter.getItem(Integer.parseInt(item[0])).charAt(1) == '+'){
					for(int i = 1; i < 14; i++){
						telephone += adapter.getItem(Integer.parseInt(item[0])).charAt(i);
					}
					number_phone.setText(telephone);
   				
	        		Log.e(this.toString(), telephone);
				}else{
					btMessage.AlertDialogError(this.getResources().getString(R.string.alertDialogBtError)
											   , this.getResources().getString(R.string.alertDialogBtnReplyErr));
					number_phone.setText("");
				}					
			}
		
			break;
		//delete the selected from the list of SMS
		case R.id.buttonDelete:
			checked = ListView_inbox_checked();
			if(checked.length == 0)
				btMessage.AlertDialogError(this.getResources().getString(R.string.alertDialogBtError)
											, this.getResources().getString(R.string.alertDialogBtnDelete));	
			else
				btMessage.AlertDialogDelete(this.getResources().getString(R.string.alertDialogBtnDeleteTitle)
											, this.getResources().getString(R.string.alertDialogBtnDeleteMessage));
			break;		
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode == RESULT_OK) {
	        switch (requestCode) {
	        case 1:
	        	btMessage.ProgressDialogDiscoveryDevice(LocalAdapter);
	          break;
	        }
	    }else {
	        btMessage.AlertDialogError(this.getResources().getString(R.string.alertDialogBtError)
	        							, this.getResources().getString( R.string.ErrorBtAdapterEnableRequest));
	    }
	}
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
     
}
