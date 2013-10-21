package me.key.appmarket;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.market.d9game.R;

public class ContentFragment extends Fragment {
	String text = null;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// inflater the layout
		View view = inflater.inflate(R.layout.main, null);
		TextView textView = (TextView) view.findViewById(R.id.main);
		if (!TextUtils.isEmpty(text)) {
			textView.setText(text);
		}
		return view;
	}
}
