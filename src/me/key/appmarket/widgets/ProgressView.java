package me.key.appmarket.widgets;

import com.market.d9game.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ProgressView extends View {
	private float fArcNum;
	private float fMax = 100;
	private float density = 1f;

	Context context;

	private int size = 40;

	public float getDensity() {
		return density;
	}

	public void setDensity(float density) {
		this.density = density;
	}

	public ProgressView(Context context) {
		super(context);
	}

	public ProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		size = getWidth();

		Paint paint = new Paint();
		// if (fArcNum > 0) {
		paint.setColor(context.getResources().getColor(R.color.progress_color));
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		canvas.drawCircle(size * density / 2, size * density / 2, size
				* density / 2, paint);
		// }

		paint.setColor(context.getResources().getColor(R.color.yellow_light));
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		RectF rect = new RectF(0, 0, size * density, size * density);
		canvas.drawArc(rect, -90, fArcNum, true, paint);
		paint.setColor(Color.WHITE);
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		canvas.drawCircle(size * density / 2, size * density / 2, size
				* density / 2 - 5, paint);
	}

	public void setProgress(float num) {
		fArcNum = (num / fMax) * 360;
	}

	public float getfArcNum() {
		return fArcNum;
	}

	public void setfArcNum(float fArcNum) {
		this.fArcNum = fArcNum;
	}

	public float getfMax() {
		return fMax;
	}

	public void setfMax(float fMax) {
		this.fMax = fMax;
	}
}
