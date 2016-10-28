package com.example.mediaplayerdemo.main;

import java.text.DecimalFormat;

import com.example.mediaplayerdemo.R;
import com.example.player.ExoMediaPlayer;
import com.example.player.IMediaPlayer;
import com.example.player.VideoPlayerTouchAdapter;
import com.example.player.customview.LightnessControl;
import com.example.player.customview.VolumeController;
import com.example.util.DensityUtil;
import com.example.util.ToastUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * 点播播放界面
 */
public class MediaPlayerActivity extends Activity implements TextureView.SurfaceTextureListener {

	private static final String TAG = "VideoPlayerActivity";

	// video info
	private int playPosition = 0;// 播放位置
	private int videoDuration = 0;// 影片时长
	private Uri mMediaUri;

	// activity
	private Context mContext;

	// player widgets
	private IMediaPlayer mPlayer;// 播放器
	private Surface surface;
	private SeekBar seekbar;
	private TextView tvPlayTimeNow;// 当前时间
	private TextView tvPlayTimeTotal;// 总时间
	private ProgressBar progressBarBuffering;

	// UI info
	private boolean isFullScreen = false;// 是否全屏
	private boolean isSeekBarTouching = false;// 是否正在拖动进度条
	private float videoViewHeightDp;// 播放高度

	// handler
	private PlayerHandler handler = new PlayerHandler(this);

	// other
	private AudioManager audioManager;
	private VolumeController volumeController;// 声音调节
	private VolumeController lightController;// 亮度调节

	// activity

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 屏幕常亮
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_video_player);
		mContext = this;

		videoViewHeightDp = ((float) DensityUtil.getWidthInDp(this) / (16.0f / 9.0f));
		updateVideoViewHeight(isFullScreen);

		mMediaUri = getIntent().getData();
		if (mMediaUri == null) {
			ToastUtil.showShortToast("uri is null");
			finish();
		}

		initWidgets();

		mPlayer = new ExoMediaPlayer();
		mPlayer.setErrorListener(errorListener);
		mPlayer.setPlayerPreparedListener(preparedListener);

		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		volumeController = new VolumeController(this);
		lightController = new VolumeController(this);
	}

	@Override
	protected void onResume() {
		startPlayer();
		super.onResume();
	}

	@Override
	protected void onStop() {
		pausePlayer();
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		onBack();
	}

	@Override
	protected void onDestroy() {
		mPlayer.releasePlayer();
		mPlayer = null;
		super.onDestroy();
	}

	// widget

	private void initWidgets() {
		initTextureView();
		initController();
	}

	private void initTextureView() {
		TextureView textureView = (TextureView) findViewById(R.id.texture_video_player);
		textureView.setSurfaceTextureListener(this);
	}

	private void initController() {
		findViewById(R.id.btn_start_or_stop_controller).setOnClickListener(btnListeners);
		findViewById(R.id.btn_full_screen_controller).setOnClickListener(btnListeners);
		seekbar = (SeekBar) findViewById(R.id.progressbar_controller);
		tvPlayTimeNow = (TextView) findViewById(R.id.tv_playtime_now_controller);
		tvPlayTimeTotal = (TextView) findViewById(R.id.tv_playtime_total_controller);
		seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				isSeekBarTouching = false;
				mPlayer.seekToPlayerMs(playPosition);
				if (!mPlayer.isPlayerPlaying()) {
					// 如果是暂停状态拖动进度条，则拖动完毕后直接开始播放视频
					startPlayer();
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				isSeekBarTouching = true;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					playPosition = progress;
					String playTimeNow = milliseconds2String(playPosition);
					tvPlayTimeNow.setText(playTimeNow);
				}
			}
		});
		progressBarBuffering = (ProgressBar) findViewById(R.id.progressbar_bufferring);
		findViewById(R.id.btn_return_player).setOnClickListener(btnListeners);
		View playerLayout = findViewById(R.id.player_layout);
		playerLayout
				.setOnTouchListener(new VideoPlayerTouchAdapter(new VideoPlayerTouchAdapter.VideoPlayerTouchListener() {

					@Override
					public void onHorizontalMove(float horizontalMoveDistance) {
						mPlayer.pausePlayer();
						float ratio = horizontalMoveDistance / DensityUtil.getWidthInPx(mContext);
						playPosition += (int) (ratio * videoDuration);
						if (playPosition < 0) {
							playPosition = 0;
						} else if (playPosition > videoDuration) {
							playPosition = videoDuration;
						}
						mPlayer.seekToPlayerMs(playPosition);
						showControllerAndAutoHide();
						updateVideoProgress();
					}

					@Override
					public void onLeftVerticalMove(float verticalMoveDistance) {
						adjustLight(verticalMoveDistance);
					}

					@Override
					public void onRightVerticalMove(float verticalMoveDistance) {
						adjustSound(verticalMoveDistance);
					}

					@Override
					public void onClick() {
						if (!isControllerShown()) {
							showControllerAndAutoHide();
						} else {
							setControllerVisible(false);
						}
					}

					@Override
					public void onHorizontalMoveEnd() {
						startPlayer();
					}
				}));
	}

	// 按钮监听
	private View.OnClickListener btnListeners = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.btn_return_player:
				onBack();
				break;
			case R.id.btn_full_screen_controller:
				isFullScreen = !isFullScreen;
				setFullScreen(isFullScreen);
				break;
			case R.id.btn_start_or_stop_controller:
				if (mPlayer.isPlayerPlaying()) {
					pausePlayer();
				} else {
					startPlayer();
				}
				break;
			}
		}
	};

	// TextureView callback

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		Log.i(TAG, "onSurfaceTextureAvailable");
		this.surface = new Surface(surface);
		handler.obtainMessage(PlayerHandler.MSG_INIT_PLAYER).sendToTarget();
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		Log.i(TAG, "onSurfaceTextureDestroyed");
		handler.obtainMessage(PlayerHandler.MSG_DESTROY_PLAYER).sendToTarget();
		return false;
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
		// do nothing
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		// do nothing
	}

	// listeners

	private IMediaPlayer.OnProgressChangeListener progressListener = new IMediaPlayer.OnProgressChangeListener() {

		@Override
		public void onProgressChanged(int progress) {
			// TODO Auto-generated method stub
			if (!mPlayer.isPlayerReady() || isSeekBarTouching) {
				return;
			}
			if (progress != 0)
				playPosition = progress;
			if (progress == videoDuration) {
				onVideoCompleted();
			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (seekbar != null) {
						updateVideoProgress();
					}
				}
			});
		}

		@Override
		public void onBuffering(boolean isBuffering) {
			progressBarBuffering.setVisibility(isBuffering ? View.VISIBLE : View.GONE);
		}

	};

	private IMediaPlayer.OnPlayerPreparedListener preparedListener = new IMediaPlayer.OnPlayerPreparedListener() {

		@Override
		public void onPrepared() {
			mPlayer.seekToPlayerMs(playPosition);
			mPlayer.startPlayer();
			seekbar.setMax(mPlayer.getPlayerDurationMs());
			videoDuration = mPlayer.getPlayerDurationMs();
			tvPlayTimeTotal.setText(milliseconds2String(videoDuration));
			mPlayer.setProgressChangedListener(progressListener);
			mPlayer.setCompletedListener(completedListener);
			showControllerAndAutoHide();
		}

	};

	private IMediaPlayer.OnErrorListener errorListener = new IMediaPlayer.OnErrorListener() {
		@Override
		public void onError(int what, int extra) {
			switch (what) {
			case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
				ToastUtil.showShortToast("格式不支持");
				finish();
				break;
			case MediaPlayer.MEDIA_ERROR_UNKNOWN:
				// TODO 有时会持续报错 E/MediaPlayer(10939): error (1,-1099)，也有-1007的情况
				showOnErrorDialog(what, extra);
				break;
			default:
				// 网络错误之类的，重启播放器
				ToastUtil.showShortToast("MEDIA_ERROR " + "what=" + what + " extra=" + extra);
				mPlayer.releasePlayer();
				playPosition += 10 * 1000;
				handler.obtainMessage(PlayerHandler.MSG_INIT_PLAYER).sendToTarget();
				break;
			}
		}
	};

	private IMediaPlayer.OnCompletedListener completedListener = new IMediaPlayer.OnCompletedListener() {
		@Override
		public void onCompleted() {
			onVideoCompleted();
		}
	};

	// player

	/**
	 * 释放播放器
	 */
	public void destroyPlayer() {
		int position;
		if ((position = mPlayer.getPlayerPositionMs()) != 0) {
			playPosition = position;
		}
		mPlayer.stopPlayer();
		mPlayer.releasePlayer();
	}

	/**
	 * 初始化播放器
	 */
	public void initPlayer() {
		// 如果此时mMediaUri为空，则这里先卡着
		if (mMediaUri == null || surface == null) {
			handler.sendEmptyMessageDelayed(PlayerHandler.MSG_INIT_PLAYER, 500);
		} else {
			if (!mPlayer.isPlayerReady()) {
				mPlayer.initPlayer(surface, mMediaUri, this);
			}
		}
	}

	// other

	// 视频播放完毕执行
	private void onVideoCompleted() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (isFullScreen) {
					isFullScreen = !isFullScreen;
					setFullScreen(isFullScreen);
				}
				ToastUtil.showShortToast("影片播放完毕");
				playPosition = 0;
				seekbar.setProgress(playPosition);
				mPlayer.seekToPlayerMs(playPosition);
				pausePlayer();
			}
		});
	}

	// 点击back键执行
	private void onBack() {
		if (isFullScreen) {
			isFullScreen = !isFullScreen;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setFullScreen(isFullScreen);
				}
			});
		} else {
			finish();
		}
	}

	// 开始播放
	private void startPlayer() {
		((ImageView) findViewById(R.id.btn_start_or_stop_controller)).setImageResource(R.drawable.ic_player_pause);
		mPlayer.startPlayer();
	}

	// 停止播放
	private void pausePlayer() {
		((ImageView) findViewById(R.id.btn_start_or_stop_controller)).setImageResource(R.drawable.ic_player_play);
		mPlayer.pausePlayer();
	}

	// 显示控制栏并且自动消失
	private void showControllerAndAutoHide() {
		setControllerVisible(true);
		handler.removeMessages(PlayerHandler.MSG_HIDE_CONTROLLER);
		handler.sendEmptyMessageDelayed(PlayerHandler.MSG_HIDE_CONTROLLER, 5000);
	}

	// 跟新视频播放进度
	private void updateVideoProgress() {
		seekbar.setProgress(playPosition);
		String playTimeNow = milliseconds2String(playPosition);
		tvPlayTimeNow.setText(playTimeNow);
	}

	// 毫秒转成文字
	private static String milliseconds2String(long millis) {
		long second = (millis / 1000) % (60);
		long minute = (millis / (60 * 1000)) % 60;
		long hour = (millis / (60 * 60 * 1000)) % 24;
		return formatDecimal(hour) + ":" + formatDecimal(minute) + ":" + formatDecimal(second);
	}

	// 格式化数字
	private static String formatDecimal(long i) {
		DecimalFormat decimalformat = new DecimalFormat("00");
		return decimalformat.format(i);
	}

	// 设置全屏
	private void setFullScreen(boolean isFullScreen) {
		// setFullScreen
		if (isFullScreen) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		this.setRequestedOrientation(
				isFullScreen ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		ScrollView detailView = (ScrollView) findViewById(R.id.scrollview_video_player);
		detailView.setVisibility(isFullScreen ? View.GONE : View.VISIBLE);
		updateVideoViewHeight(isFullScreen);
	}

	// 跟新播放界面的高度
	private void updateVideoViewHeight(boolean isFullScreen) {
		View videoView = findViewById(R.id.player_layout);
		int height = isFullScreen ? LinearLayout.LayoutParams.MATCH_PARENT
				: DensityUtil.dip2px(mContext, videoViewHeightDp);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
		videoView.setLayoutParams(lp);
	}

	// 设置控制栏显示与否
	public void setControllerVisible(boolean toShow) {
		LinearLayout controller = (LinearLayout) findViewById(R.id.ll_video_player_controller);
		if (controller.isShown() == toShow) {
			return;
		}
		controller.setVisibility(toShow ? View.VISIBLE : View.GONE);
	}

	// 判断控制栏是否显示
	private boolean isControllerShown() {
		return findViewById(R.id.ll_video_player_controller).isShown();
	}

	/**
	 * 调节屏幕亮度
	 */
	private void adjustLight(float moveDistance) {
		// 当前屏幕亮度
		setControllerVisible(false);
		int light = LightnessControl.getLightness(this);
		// 判断是否开启自动调节亮度功能，开启后调节可能不生效，关闭自动调节
		if (LightnessControl.isAutoBrightness(this)) {
			LightnessControl.stopAutoBrightness(this);
		}
		if (moveDistance < 0) {
			// 增大亮度值 255为亮度最大值
			light = light + 10;
			if (light >= 255) {
				light = 255;
			}
		} else {
			// 减小亮度值
			light = light - 10;
			if (light <= 0) {
				light = 0;
			}
		}
		LightnessControl.setLightness(this, light);
		lightController.show(light, "亮度", 255);
	}

	/**
	 * 调节音量
	 */
	private void adjustSound(float moveDistance) {
		setControllerVisible(false);
		float volume;
		int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		if (moveDistance < 0) {
			// 增大音量
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
					AudioManager.FLAG_PLAY_SOUND);
		} else {
			// 减小音量
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
					AudioManager.FLAG_PLAY_SOUND);
		}
		volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		volumeController.show(volume, "音量", max);
	}

	// 如果播放异常，则提示信息
	private void showOnErrorDialog(int what, int extra) {
		AlertDialog dialog = new AlertDialog.Builder(mContext).setCancelable(false)
				.setMessage("未知错误 " + "what=" + what + " extra=" + extra)
				.setPositiveButton("重启播放器", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 如果播放异常，则快进十秒
						progressBarBuffering.setVisibility(View.VISIBLE);
						playPosition += 10 * 1000;
						mPlayer.releasePlayer();
						handler.obtainMessage(PlayerHandler.MSG_INIT_PLAYER).sendToTarget();
					}
				}).setNegativeButton("退回主界面", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).create();
		dialog.show();
	}
}

class PlayerHandler extends Handler {
	public static final int MSG_INIT_PLAYER = 1;
	public static final int MSG_DESTROY_PLAYER = 2;
	public static final int MSG_HIDE_CONTROLLER = 3;

	private MediaPlayerActivity activity;

	PlayerHandler(MediaPlayerActivity activity) {
		this.activity = activity;
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_INIT_PLAYER:
			removeMessages(MSG_INIT_PLAYER);
			activity.initPlayer();
			break;
		case MSG_DESTROY_PLAYER:
			removeMessages(MSG_DESTROY_PLAYER);
			activity.destroyPlayer();
			break;
		case MSG_HIDE_CONTROLLER:
			removeMessages(MSG_HIDE_CONTROLLER);
			activity.setControllerVisible(false);
			break;
		}
	}
}
