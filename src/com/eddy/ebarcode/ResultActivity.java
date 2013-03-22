package com.eddy.ebarcode;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ResultActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		String info = (String) getIntent().getExtras().get("info");
		TextView textView = (TextView) findViewById(R.id.text_Result);
		textView.setText(info);
		
	}

}
