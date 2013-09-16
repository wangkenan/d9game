package me.key.appmarket.widgets;

import com.market.d9game.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AppDialog extends Dialog implements
		android.view.View.OnClickListener, DialogInterface {

	private TextView mText;
	private Button mBtnOk, mBtnCancel;
	private OnClickListener mPositiveListener, mNegtiveListener;

	public static AppDialog show(Context context, boolean warning) {
		AppDialog cd = new AppDialog(context, warning);
		cd.show();
		return cd;
	}

	public AppDialog(Context context, boolean warning) {
		this(context);
	}

	protected AppDialog(Context context) {
		super(context, R.style.camera_dialog);
		setContentView(R.layout.dialog_camera);
		mText = (TextView) findViewById(R.id.message);
		mBtnOk = (Button) findViewById(R.id.btn_ok);
		mBtnCancel = (Button) findViewById(R.id.btn_cancel);
		mBtnOk.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public AppDialog setMessage(String s) {
		mText.setText(s);
		return this;
	}

	public AppDialog setMessage(int sid) {
		mText.setText(sid);
		return this;
	}

	public AppDialog setPositiveButton(String s, OnClickListener l) {
		mBtnOk.setText(s);
		mPositiveListener = l;
		return this;
	}

	public AppDialog setPositiveButton(int sid, OnClickListener l) {
		return setPositiveButton(getContext().getString(sid), l);
	}

	public AppDialog setNegtiveButton(String s, OnClickListener l) {
		mBtnCancel.setText(s);
		mNegtiveListener = l;
		return this;
	}

	public AppDialog setNegtiveButton(int sid, OnClickListener l) {
		return setNegtiveButton(getContext().getString(sid), l);
	}

	public AppDialog setAlignLeft() {
		mText.setGravity(Gravity.LEFT);
		return this;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_ok:
			if (mPositiveListener != null) {
				mPositiveListener.onClick(this, BUTTON_POSITIVE);
			}
			break;
		case R.id.btn_cancel:
			if (mNegtiveListener != null) {
				mNegtiveListener.onClick(this, BUTTON_NEGATIVE);
			}
			break;
		}
		dismiss();
	}
}
