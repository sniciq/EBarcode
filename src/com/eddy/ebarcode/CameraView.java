package com.eddy.ebarcode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PictureCallback, BarCodeDecodeOverListener {
	private SurfaceHolder holder;
	private Camera myCamera;
	private Context context;
	private int[] configPicSize;
	private int takePicPeriod;
	int frontCamera;
	boolean isProcessingPic = false;
	private android.hardware.Camera.CameraInfo cameraInfo = new android.hardware.Camera.CameraInfo();
	
	private Timer timer = null;
	private BarCodeDecoder barCodeDecoder;
	private ArrayList<String> supportedPicSizes = new ArrayList<String>();
	
//	private final ShutterCallback shutterCallback = new ShutterCallback() {
//        public void onShutter() {
//        	Log.i("AAAAAAAAAAAAACCC", "ffffffff");
//            AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//            mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
//        }
//    };
    
    private final AutoFocusCallback focusCallback = new AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
//			camera.takePicture(shutterCallback, null, CameraView.this);
//			if(!success)
//				return;
			Log.i("AAAAAAAAAAAAACCC", "onAutoFocus");
			myCamera.setOneShotPreviewCallback(previewCallback);
		}
	};
	
	private final Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			Camera.Parameters parameters = camera.getParameters();
			int imageFormat = parameters.getPreviewFormat();
			int w = parameters.getPreviewSize().width;
			int h = parameters.getPreviewSize().height;
			Rect rect = new Rect(0, 0, w, h);
			YuvImage yuvImg = new YuvImage(data, imageFormat, w, h, null);
			try {
				ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
				yuvImg.compressToJpeg(rect, 100, outputstream);
				Bitmap picture = BitmapFactory.decodeByteArray(outputstream.toByteArray(), 0, outputstream.size());
				if(picture != null) {
					isProcessingPic = true;
					barCodeDecoder.decodeInThread(picture);
					Log.i("AAAAAAAAAAAAACCC", "barCodeDecoder.decodeInThread(picture)");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public CameraView(Context context) {
		this(context, null);
	}
	
	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		holder = getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.addCallback(this);
		barCodeDecoder = new BarCodeDecoder();
		barCodeDecoder.addListener(this);
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
		frontCamera = getCameraId(false);
		myCamera = Camera.open(frontCamera);
		myCamera.setDisplayOrientation(90);
		myCamera.setPreviewDisplay(holder);
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
			Camera.Parameters perameters = myCamera.getParameters();
			
			List<Camera.Size> supportedPictureSizes = perameters.getSupportedPictureSizes();
			supportedPicSizes.clear();
			for(Camera.Size s : supportedPictureSizes) {
				supportedPicSizes.add(s.width + "X" + s.height);
			}
			
			if(configPicSize == null) {
				configPicSize = new int[2];
				configPicSize[0] = supportedPictureSizes.get(0).width;
				configPicSize[1] = supportedPictureSizes.get(0).height;
			}
			
			if(supportedPicSizes.contains("800X480")) {
				configPicSize[0] = 800;
				configPicSize[1] = 480;
			}
			else if(supportedPicSizes.contains("640X480")) {
				configPicSize[0] = 640;
				configPicSize[1] = 480;
			}
			
			perameters.setPictureSize(configPicSize[0], configPicSize[1]);
			int rotation = 0;
			if (frontCamera == CameraInfo.CAMERA_FACING_FRONT) {
			     rotation = (cameraInfo.orientation - 90 + 360) % 360;
			} else {  // back-facing camera
			     rotation = (cameraInfo.orientation + 90) % 360;
			}
			perameters.setRotation(rotation);
			perameters.setZoom(perameters.getMaxZoom()/5);
			myCamera.setParameters(perameters);
			myCamera.startPreview();
			startTimer();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		myCamera.setPreviewCallback(null);
		myCamera.stopPreview();
		myCamera.release();
		myCamera = null;
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

	private void savePicToGallery(Bitmap bitmap) throws Exception {
		String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
		String imageFileName = "IMG_" + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, ".jpg", albumF);
		
		FileOutputStream out = new FileOutputStream(imageF);
		bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
		galleryAddPic(imageF.getPath());
		Toast.makeText(this.context, "Save Pic Successfully!", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		try {
			
			Log.i("AAAAAAAAAAAAACCC", "onPictureTaken");
			
			isProcessingPic = true;
			Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
			camera.startPreview();
			barCodeDecoder.decodeInThread(picture);
			
			
//			//取中间区域
//			int bw = picture.getWidth() / 2;
//			int bh = picture.getHeight() / 2;
//			int x = (picture.getWidth() - bw) / 2;
//			int y = (picture.getHeight() - bh) /2;
//			Bitmap nb = Bitmap.createBitmap(picture, x, y, bw, bh);
			
			
//			
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			nb.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//			baos.close();
//			
//			//FIXME savePicToGallery
//			savePicToGallery(nb);
//			Map<String, Object> decodeMap = barCodeDecoder.decode(nb);
//			boolean success = (Boolean) decodeMap.get("result");
//			if(success) {
//				String info = (String) decodeMap.get("info");
//				Intent intent = new Intent(this.context, ResultActivity.class);
//				intent.putExtra("info", info);
//				
//				((Activity) this.context).startActivityForResult(intent, MainActivity.requestCode_barcode);
//			}
//			else {
//				camera.startPreview();
//			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void pause() {
		this.timer.cancel();
		this.timer.purge();
		this.timer = null;
	}

	public void resume() {
		try {
			if(myCamera != null) {
				Camera.Parameters perameters = myCamera.getParameters();
				perameters.setPictureSize(configPicSize[0], configPicSize[1]);
				myCamera.setParameters(perameters);
			}
			this.startTimer();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void doFocusAndTakePic() {
		if(myCamera != null && !isProcessingPic) {
			System.out.println("focus start ...");
			myCamera.autoFocus(focusCallback);
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

	public int[] getConfigPicSize() {
		return configPicSize;
	}

	public void setTakePicPeriod(int takePicPeriod) {
		this.takePicPeriod = takePicPeriod;
	}

	@Override
	public void deCodeOver(Map<String, Object> decodeMap) {
		isProcessingPic = false;
		boolean success = (Boolean) decodeMap.get("result");
		if(success) {
			String info = (String) decodeMap.get("info");
			Intent intent = new Intent(this.context, ResultActivity.class);
			intent.putExtra("info", info);
			((Activity) this.context).startActivityForResult(intent, MainActivity.requestCode_barcode);
		}
	}
}

////横竖屏处理
//@Override
//public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
//    mCamera.setDisplayOrientation(0);
//}else{
//        mCamera.setDisplayOrientation(90);
//}
//}
