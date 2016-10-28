package com.example.mediaplayerdemo.main;

import java.io.File;

import com.example.mediaplayerdemo.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	TextView tv;
	EditText et;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initWidgets();
	}

	private void initWidgets() {
		tv = (TextView) findViewById(R.id.tv);
		et = (EditText) findViewById(R.id.et);
		Button btnPlay = (Button) findViewById(R.id.btn1);

		tv.setText("status");
		et.setText(Environment.getExternalStorageDirectory() + File.separator + "test.mp4");
//		et.setHint("video url");
		btnPlay.setText("play url");
		btnPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String uriString = et.getText().toString();
				Uri uri = Uri.parse(uriString);
				startPlayVideo(uri);
			}
		});
	}

	protected void startPlayVideo(Uri uri) {
		Intent intent = new Intent(MainActivity.this, MediaPlayerActivity.class);
		intent.setData(uri);
		startActivity(intent);
	}

}
