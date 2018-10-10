package com.speedata.clientocr;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public final class LPRfinderView extends View {
	private static final long ANIMATION_DELAY = 50;
	private final Paint paint;
	private final Paint paintLine;
	private final int maskColor;
	private final int frameColor;
	private final int laserColor;

	private Paint mTextPaint;  
    private String mText;  

	private Rect frame;

	int w, h;
	boolean boo = false;
	int mPaddingLeft ;
    int  mPaddingTop ;
    int mPaddingRight ;
     int mPaddingBottom ;

	public LPRfinderView(Context context, int w, int h) {
		super(context);
		this.w = w;
		this.h = h;
		paint = new Paint();
		paintLine = new Paint();
		Resources resources = getResources();
		maskColor = resources.getColor(R.color.viewfinder_mask);
		frameColor = resources.getColor(R.color.viewfinder_frame);
		laserColor = resources.getColor(R.color.viewfinder_laser);

	}

	public LPRfinderView(Context context, int w, int h, boolean boo) {
		super(context);
		this.w = w;
		this.h = h;
		this.boo = boo;
		paint = new Paint();
		paintLine = new Paint();
		Resources resources = getResources();
		maskColor = resources.getColor(R.color.viewfinder_mask);
		frameColor = resources.getColor(R.color.viewfinder_frame);
		laserColor = resources.getColor(R.color.viewfinder_laser);
	}

	@Override
	public void onDraw(Canvas canvas) {
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		int t;
		int b;
		int l;
		int r;

//		l =  10;               //----------org
//		r = width-10;
//		int ntmpH =(r-l)*58/88;
//		t = (height-ntmpH)/2;
//		b =  t+ntmpH;


		l = (int)(width*0.2);
		r =(int)(width*0.8);
		int ntmpH =(r-l)*58/66;
		t = (height-ntmpH)/2;
		b =  t+ntmpH;

		frame = new Rect(l, t, r, b);

		paint.setColor(maskColor);
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
				paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);

		paintLine.setColor(frameColor);
		paintLine.setStrokeWidth(16);
		paintLine.setAntiAlias(true);
		int num = (r - l) / 10;
		canvas.drawLine(l - 8, t, l + num, t, paintLine);
		canvas.drawLine(l, t, l, t + num, paintLine);

		canvas.drawLine(r + 8, t, r - num, t, paintLine);
		canvas.drawLine(r, t, r, t + num, paintLine);

		canvas.drawLine(l - 8, b, l + num, b, paintLine);
		canvas.drawLine(l, b, l, b - num, paintLine);

		canvas.drawLine(r + 8, b, r - num, b, paintLine);
		canvas.drawLine(r, b, r, b - num, paintLine);

		paintLine.setColor(laserColor);
		paintLine.setAlpha(100);
		paintLine.setStrokeWidth(3);
		paintLine.setAntiAlias(true);
		canvas.drawLine(l, t + num, l, b - num, paintLine);

		canvas.drawLine(r, t + num, r, b - num, paintLine);

		canvas.drawLine(l + num, t, r - num, t, paintLine);

		canvas.drawLine(l + num, b, r - num, b, paintLine);


	     mText = "请将车牌置于框内";
	     mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	     mTextPaint.setStrokeWidth(3);
	     mTextPaint.setTextSize(50);
	     mTextPaint.setColor(frameColor);
	      mTextPaint.setTextAlign(Paint.Align.CENTER);
	     canvas.drawText(mText,w/2,h/2, mTextPaint);
	     //canvas.drawText(mText1,w/2,h/2+h/5, mTextPaint);
		if (frame == null) {
			return;
		}


		postInvalidateDelayed(ANIMATION_DELAY);
	}

}
