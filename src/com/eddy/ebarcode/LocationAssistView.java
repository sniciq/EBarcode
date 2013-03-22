package com.eddy.ebarcode;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class LocationAssistView extends View {

	private Paint myPaint;
	
	public LocationAssistView(Context context, AttributeSet attrs) {
		super(context, attrs);
		myPaint = new Paint();
		myPaint.setColor(Color.BLACK);
		myPaint.setAlpha(128);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int w = canvas.getWidth();
		int h = canvas.getHeight();
		
		int bw = 300;
		int bh = 300;
		
		int left = (w - bw)/2;
		int top = (h - bh)/2;
		int right = (w - bw)/2 + bw;
		int bottom = (h - bh)/2 + bh;
		
//		canvas.drawRect(left, top, right, bottom, myPaint);
		
		
		int miniRectSize = 30;
		canvas.drawRect(left, top, left + miniRectSize, top + miniRectSize, myPaint);
		canvas.drawRect(right - miniRectSize, top, right, top + miniRectSize, myPaint);
		canvas.drawRect(left, bottom - miniRectSize, left + miniRectSize, bottom, myPaint);
	}
}
