package com.eddy.ebarcode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PictureCallback {

	private SurfaceHolder holder;
	private Camera camera;
	private Context context;
	private int[] configPicSize;
	private int takePicPeriod;
	private BarCodeDecoder barCodeDecoder;
	
	private Timer timer = null;
	private ArrayList<String> supportedPicSizes = new ArrayList<String>();
	
	AutoFocusCallback focusCallback = new AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			System.out.println("take picture start ...");
			camera.takePicture(null, null, CameraView.this);
			System.out.println("BBBBBBBBBB");
			System.out.println("-------------------");
		}
	};

	public CameraView(Context context) {
		this(context, null);
	}
	
	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		holder = getHolder();
		holder.addCallback(this);
		barCodeDecoder = new BarCodeDecoder();
	}
	
	private int getCameraId(boolean isFront) {
		CameraInfo ci = new CameraInfo();
		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
			Camera.getCameraInfo(i, ci);
			if (isFront) {
				if (ci.facing == CameraInfo.CAMERA_FACING_FRONT) {
					return i;
				}
			} else {
				if (ci.facing == CameraInfo.CAMERA_FACING_BACK) {
					return i;
				}
			}
		}
		return -1; // No front-facing camera found
	}
	
	private void initCamera() throws Exception {
		int frontCamera = getCameraId(false);
		camera = Camera.open(frontCamera);
		camera.setDisplayOrientation(90);
		camera.setPreviewDisplay(holder);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			initCamera();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		try {
			Camera.Parameters perameters = camera.getParameters();
			
			List<Camera.Size> supportedPictureSizes = perameters.getSupportedPictureSizes();
			for(Camera.Size s : supportedPictureSizes) {
				supportedPicSizes.add(s.width + "X" + s.height);
			}
			
//			perameters.setPreviewSize(320, 240);
			perameters.setPictureSize(configPicSize[0], configPicSize[1]);
			camera.setParameters(perameters);
			
			camera.startPreview();
			startTimer();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.setPreviewCallback(null);
		camera.stopPreview();
		camera.release();
		camera = null;
	}
	
	private File getAlbumDir() {
		String filePath = Environment.getExternalStorageDirectory().getPath() + "/" + getResources().getString(R.string.dir_name) + "/pics/";
		File f = new File(filePath);
		if(!f.exists() && !f.isDirectory()) {
			f.mkdirs();
		}
		return f;
	}
	
	private void galleryAddPic(String path) {
		Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(path);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		context.sendBroadcast(mediaScanIntent);
	}

	private void savePicToGallery(byte[] data) throws Exception {
		String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
		String imageFileName = "IMG_" + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, ".jpg", albumF);
		OutputStream outStream = new FileOutputStream(imageF);
		outStream.write(data);
		outStream.close();
		galleryAddPic(imageF.getPath());
		Toast.makeText(this.context, "Save Pic Successfully!", Toast.LENGTH_LONG).show();
//		String saved = MediaStore.Images.Media.insertImage(this.context.getContentResolver(), picture, imageFileName, "description");
//        Uri sdCardUri = Uri.parse("file://" + Environment.getExternalStorageDirectory());
//        this.context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, sdCardUri));
	}
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		try {
			System.out.println("onPictureTaken");
			
			Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			picture.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			baos.close();
			
			//FIXME savePicToGallery
//			savePicToGallery(data);
			Map<String, Object> decodeMap = barCodeDecoder.decode(picture);
			boolean success = (Boolean) decodeMap.get("result");
			if(success) {
				Intent intent = new Intent(this.context, ResultActivity.class);
				intent.putExtra("info", decodeMap.get("info").toString());
				((Activity) this.context).startActivityForResult(intent, MainActivity.requestCode_barcode);
			}
			else {
				camera.startPreview();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void pause() {
		this.timer.cancel();
		this.timer.purge();
		this.timer = null;
//		camera.release();
	}

	public void resume() {
		try {
			if(camera != null) {
				Camera.Parameters perameters = camera.getParameters();
				perameters.setPictureSize(configPicSize[0], configPicSize[1]);
				camera.setParameters(perameters);
			}
			this.startTimer();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void doFocusAndTakePic() {
		if(camera != null) {
			System.out.println("focus start ...");
			camera.autoFocus(focusCallback);
		}
	}
	
	private void startTimer() {
		if(timer != null)
			return;
		
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				doFocusAndTakePic();
			}
		}, 3 * 1000, takePicPeriod * 1000);
	}

	public ArrayList<String> getSupportedPicSizes() {
		return supportedPicSizes;
	}

	public void setConfigPicSize(int[] configPicSize) {
		this.configPicSize = configPicSize;
	}

	public void setTakePicPeriod(int takePicPeriod) {
		this.takePicPeriod = takePicPeriod;
	}

//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		this.context.A
//		this.context.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		return super.onTouchEvent(event);
//	}
	
}

////横竖屏处�?
//@Override
//public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
//    mCamera.setDisplayOrientation(0);
//}else{
//        mCamera.setDisplayOrientation(90);
//}
//}
