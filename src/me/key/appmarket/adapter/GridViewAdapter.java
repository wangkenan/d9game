package me.key.appmarket.adapter;

import me.key.appmarket.utils.LogUtils;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.market.d9game.R;

public class GridViewAdapter extends BaseAdapter {

	private Context context;
	public int currentIndex = 0;
	private boolean isAlive = true;
	private Gallery squareGallery;
	private String[] images;

	// ѡ��״̬
	private Integer[] thumbIds = { R.drawable.ball_selected,
			R.drawable.ball_unselected };

	public GridViewAdapter(Context context, Gallery squareGallery,
			String[] images) {
		this.context = context;
		this.squareGallery = squareGallery;
		this.images = images;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return images.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		int index = 0;
		if (position == currentIndex) {
			index = 0;
		} else {
			index = 1;
		}
		return thumbIds[index];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ImageView img = new ImageView(context);
		img.setLayoutParams(new GridView.LayoutParams(
				GridView.LayoutParams.WRAP_CONTENT,
				GridView.LayoutParams.WRAP_CONTENT));
		// �����ʾѡ��״̬
		if (position == currentIndex) {
			img.setImageResource(thumbIds[0]);
		} else {
			img.setImageResource(thumbIds[1]);
		}
		img.setScaleType(ScaleType.FIT_CENTER);
		return img;
	}

	public void autoPlay() {
		LogUtils.d("Sina", "autoplayִ����");
		// ���ü��5���Զ����ŵĹ���
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (isAlive) {
					currentIndex = currentIndex % images.length;
					squareGallery.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							squareGallery.setSelection(currentIndex);
						}

					});
					// ����ʱ���� 5��
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					currentIndex++;
				}
			}

		}).start();
	}
}
