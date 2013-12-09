package me.key.appmarket.widgets;

import me.key.appmarket.utils.LogUtils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.ImageView;

public class RoundImageView extends ImageView {
	//int wmWidth;

	public RoundImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RoundImageView(Context context) {
		super(context);
		//this.wmWidth = wmWidth;
		init();
	}

	private final RectF roundRect = new RectF();
	private float rect_adius = 5;
	private final Paint maskPaint = new Paint();
	private final Paint zonePaint = new Paint();

	private void init() {
		maskPaint.setAntiAlias(true);
		maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		zonePaint.setAntiAlias(true);
		float density = getResources().getDisplayMetrics().density;
		rect_adius = rect_adius * density;
	}

	public void setRectAdius(float adius) {
		rect_adius = adius;
		invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		WindowManager wm = (WindowManager) getContext()
				.getSystemService(Context.WINDOW_SERVICE);

		int w = wm.getDefaultDisplay().getWidth();
		w = (int)(w-16)/3;
		LogUtils.i("wm", w+"W"+getWidth());
		//int w = 150;//getWidth();
		int h = getHeight()*w/getWidth();
		LogUtils.i("wm", h+"H"+getHeight());
		//int h = w/3*2;
		roundRect.set(0, 0, w, h);
	}

	@Override
	public void draw(Canvas canvas) {

		canvas.saveLayer(roundRect, zonePaint, Canvas.ALL_SAVE_FLAG);
		canvas.drawRoundRect(roundRect, rect_adius, rect_adius, zonePaint);
		canvas.saveLayer(roundRect, maskPaint, Canvas.ALL_SAVE_FLAG);
		super.draw(canvas);
		canvas.restore();
	}


}