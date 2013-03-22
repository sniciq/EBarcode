package com.eddy.ebarcode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {
	private CameraView cameraView;
	private AppSetting appSetting;
	private static final int requestCode_setting = 1;
	public static final int requestCode_barcode = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		getActionBar().hide();
//		cameraView = new CameraView(this);
//		cameraView.setOnTouchListener(new OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				if(getActionBar().isShowing()) {
//					getActionBar().hide();
//				}
//				else {
//					getActionBar().show();
//				}
//				return false;
//			}
//		});
		
//		setContentView(cameraView);
		setContentView(R.layout.activity_main);
		appSetting = new AppSetting(this);
		appSetting.init();
		cameraView = (CameraView) findViewById(R.id.cameraView);
		cameraView.setConfigPicSize(appSetting.getConfigPictureSize());
		cameraView.setTakePicPeriod(appSetting.getTakePicPeriod());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.main, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(MainActivity.this, SettingActivity.class);
			intent.putStringArrayListExtra("supportSizes", cameraView.getSupportedPicSizes());
			intent.putExtra(AppSetting.PictureSize, appSetting.getConfigPictureSize());
			intent.putExtra(AppSetting.TakePicPeriod, appSetting.getTakePicPeriod());
			startActivityForResult(intent, requestCode_setting);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == requestCode_setting) {
			//设置返回后的处理
			if(resultCode == RESULT_OK) {
				String ss = (String) data.getExtras().get(AppSetting.PictureSize);
				int aa =(Integer) data.getExtras().get(AppSetting.TakePicPeriod);
				appSetting.save(ss, aa);
				cameraView.setConfigPicSize(appSetting.getConfigPictureSize());
				cameraView.setTakePicPeriod(appSetting.getTakePicPeriod());
			}
		}
		else if(requestCode == requestCode_barcode) {
			//FIXME 条码信息页面返回
			System.out.println("BBBBBBBBBBBB");
		}
	}

	@Override
	protected void onResume() {
		cameraView.resume();
		super.onResume();
	}

	@Override
	protected void onPause() {
		cameraView.pause();
		super.onPause();
	}
}
