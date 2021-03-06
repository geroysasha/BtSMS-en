package com.example.btsms;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Class BtSbHelper creates a database and table btSMS
 * BtTable with specified fields.
 *
 *Version 1.0 October 17, 2014
 *Author Karpenko Alexander karpenkoAV@ukr.net
 *The Apache License 2
 */
public class BtDbHelper extends SQLiteOpenHelper {

	public BtDbHelper(Context context) {
		 super(context, "btSMS", null, 1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.e(this.toString(), "--- onCreate database ---");
		db.execSQL("create table btTable ("
					+ "id integer primary key autoincrement,"
					//stores the type of the memory bank
					+ "bank text," 
					//stores the message status (read / unread, answered / not answered etc.).
					+ "status text," 
					//stores the date of adoption of SMS
					+ "date text," 
					//stores the number of the sender
					+ "number text, "
					//contents of SMS
					+ "content text," 
					//stores the number of sms
					+ "num text" + ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
