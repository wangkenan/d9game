package me.key.appmarket.widgets;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

public class CustomGallery extends Gallery implements OnGestureListener {
	private ViewPager mPager;

	public ViewPager getmPager() {
		return mPager;
	}

	public void setmPager(ViewPager mPager) {
		this.mPager = mPager;
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public CustomGallery(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param attrs
	 * 
	 */
	public CustomGallery(Context context, AttributeSet attrs) {
		super(context, attrs); // TODO Auto-generated constructor stub
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		mPager.requestDisallowInterceptTouchEvent(true);
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		mPager.requestDisallowInterceptTouchEvent(true);
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		mPager.requestDisallowInterceptTouchEvent(true);
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		 int kEvent;
	        if(isScrollingLeft(e1, e2)){ //Check if scrolling left
	          kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
	        }
	        else{ //Otherwise scrolling right
	          kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
	        }
	        onKeyDown(kEvent, null);
	        return true; 
	}
	 private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2){
	        return e2.getX() > e1.getX();
	 }
}
