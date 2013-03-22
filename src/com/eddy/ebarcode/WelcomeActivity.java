package com.eddy.ebarcode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class WelcomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		Thread splashThread = new Thread() {
			@Override
			public void run() {
				try {
					int waited = 0;
					while (waited < 1000) {
						sleep(100);
						waited += 100;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					finish();
					Intent i = new Intent();
					i.setClassName("com.eddy.ebarcode", "com.eddy.ebarcode.MainActivity");
					startActivity(i);
				}
			}
		};
		splashThread.start();
	}
}
