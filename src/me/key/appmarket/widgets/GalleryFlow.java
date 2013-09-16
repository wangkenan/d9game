package me.key.appmarket.widgets;

import java.lang.reflect.Field;

import android.R.attr;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;

public class GalleryFlow extends Gallery {
	public interface IOnItemClickListener {
		public void onItemClick(int position);
	}

	private Camera mCamera;
	private int mWidth;
	private int mPaddingLeft;
	private boolean flag;
	private static int firstChildWidth;
	private static int firstChildPaddingLeft;
	private int offsetX;

	private IOnItemClickListener mListener;

	public GalleryFlow(Context context) {
		super(context);
		mCamera = new Camera();
		this.setStaticTransformationsEnabled(true);
	}

	public GalleryFlow(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCamera = new Camera();
		setAttributesValue(context, attrs);
		this.setStaticTransformationsEnabled(true);
	}

	public GalleryFlow(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mCamera = new Camera();
		setAttributesValue(context, attrs);
		this.setStaticTransformationsEnabled(true);
	}

	private void setAttributesValue(Context context, AttributeSet attrs) {
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				new int[] { attr.paddingLeft });
		mPaddingLeft = typedArray.getDimensionPixelSize(0, 0);
		typedArray.recycle();
	}

	protected boolean getChildStaticTransformation(View child, Transformation t) {
		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);
		mCamera.save();
		final Matrix imageMatrix = t.getMatrix();
		if (flag) {
			firstChildWidth = getChildAt(0).getWidth();
			firstChildPaddingLeft = getChildAt(0).getPaddingLeft();
			flag = false;
		}
		offsetX = firstChildWidth / 2 + firstChildPaddingLeft + mPaddingLeft
				- mWidth / 2;
		mCamera.translate(offsetX, 0f, 0f);
		mCamera.getMatrix(imageMatrix);
		mCamera.restore();
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		event.offsetLocation(-offsetX, 0);
		mPager.requestDisallowInterceptTouchEvent(true);
		return super.onTouchEvent(event);
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (!flag) {
			mWidth = w * 2;
			getLayoutParams().width = mWidth;
			flag = true;
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}

	public boolean onSingleTapUp(MotionEvent e) {
		try {
			Field f = GalleryFlow.class.getSuperclass().getDeclaredField(
					"mDownTouchPosition");
			f.setAccessible(true);
			int position = f.getInt(this);
			if (null != mListener && position >= 0) {
				mListener.onItemClick(position);
			}
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (NoSuchFieldException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e2) {
			e2.printStackTrace();
		} catch (IllegalAccessException e3) {
			e3.printStackTrace();
		}
		return false;
	}

	public void setOnItemClickListener(IOnItemClickListener listener) {
		mListener = listener;
	}

	private ViewPager mPager;

	public ViewPager getmPager() {
		return mPager;
	}

	public void setmPager(ViewPager mPager) {
		this.mPager = mPager;
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

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

}