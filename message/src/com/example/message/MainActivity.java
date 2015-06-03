package com.example.message;

import org.apache.log4j.BasicConfigurator;
import org.red5.server.api.so.IClientSharedObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;

import com.flazr.rtmp.client.ClientOptions;

public class MainActivity extends Activity {

	//private Object[] args={"Test123"};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		BasicConfigurator.configure();
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        ClientOptions op=new ClientOptions();
        op.setHost("10.129.200.81");
        op.setAppName("HariPanTest3");
		op.setStreamName("Test123");
		
		BigBlueButtonClient client=new BigBlueButtonClient();
		client.connectBigBlueButton(op);
		//client.setMyUserId("Test123");
		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
