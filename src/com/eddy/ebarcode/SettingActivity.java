package com.eddy.ebarcode;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class SettingActivity extends Activity {
	private Spinner picSpinner;
	private ArrayAdapter<String> spinnerAdapter;
	private EditText text_takePicPeriod;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		picSpinner = (Spinner) findViewById(R.id.picSizeSel);
		int[] orgPicSize = getIntent().getExtras().getIntArray(AppSetting.PictureSize);
		int takePicPeriod = getIntent().getExtras().getInt(AppSetting.TakePicPeriod);
		int selectItemPosition = 0;
		ArrayList<String> supportedPicSizes = getIntent().getStringArrayListExtra("supportSizes");
		String[] m = new String[supportedPicSizes.size()];
		for(int i = 0; i < supportedPicSizes.size(); i++) {
			String s = supportedPicSizes.get(i);
			supportedPicSizes.toArray(m);
			if((orgPicSize[0] + "X" + orgPicSize[1]).equals(s)) {
				selectItemPosition = i;
			}
		}
		spinnerAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,m);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  
        //将adapter 添加到spinner中  
		picSpinner.setAdapter(spinnerAdapter);  
		picSpinner.setSelection(selectItemPosition);
		
		text_takePicPeriod = (EditText) findViewById(R.id.text_takePicPeriod);
		text_takePicPeriod.setText(takePicPeriod+"");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.setting, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.btn_settings_OK:
			Intent output = new Intent();
			String size = spinnerAdapter.getItem(picSpinner.getSelectedItemPosition());
			int takePicPeriod = 3;
			String ss = text_takePicPeriod.getText().toString();
			if(!ss.trim().equals("")) {
				takePicPeriod = Integer.parseInt(ss);
			}
			
			output.putExtra(AppSetting.PictureSize, size);
			output.putExtra(AppSetting.TakePicPeriod, takePicPeriod);
			setResult(RESULT_OK, output);
			finish();
			break;
		case R.id.btn_settings_Cancel:
			finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
