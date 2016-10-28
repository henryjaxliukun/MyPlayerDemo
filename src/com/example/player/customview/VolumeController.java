package com.example.player.customview;

import com.example.mediaplayerdemo.R;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;


/**
 * 调节声音和亮度指示控制器
 * com.jsht.mobile.biz.VolumeController
 * @author makai 
 * 2015年2月6日上午11:35:01
 *
 */
public class VolumeController {
	private Toast t;
	private VolumeView volumnView;

	private Context context;

	public VolumeController(Context context) {
		this.context = context;
	}

	public void show(float progress,String text,int max) {
		if (t == null) {
			t = new Toast(context);
			View layout = LayoutInflater.from(context).inflate(R.layout.adjust_indicator, null);
			volumnView = (VolumeView) layout.findViewById(R.id.volumnView);
			volumnView.setText(text);
			volumnView.setMax(max);
			t.setView(layout);
			t.setGravity(Gravity.TOP, 0, 100);
			t.setDuration(Toast.LENGTH_SHORT);
		}
		volumnView.setProgress(progress);
		t.show();
	}
}
