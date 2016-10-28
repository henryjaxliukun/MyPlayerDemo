package com.example.player;

import com.example.player.exo.DemoPlayer;
import com.example.player.exo.DemoPlayer.Listener;
import com.example.player.exo.ExtractorRendererBuilder;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.TrackRenderer;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;

public class ExoMediaPlayer extends AbsMediaPlayer {

	private DemoPlayer player;
	private int playStatus;
	private static final String TAG = "ExoMediaPlayer";
	private boolean isPlaying;
	private boolean firstFlag;

	private void resetStatus() {
		playStatus = ExoPlayer.STATE_IDLE;
		firstFlag = false;
	}

	@Override
	public boolean initPlayer(Surface s, Uri uri, Context context) {
		if (s == null || !s.isValid() || uri == null || uri.toString().equals("")) {
			return false;
		}
		resetStatus();
		ExtractorRendererBuilder builder = new ExtractorRendererBuilder(context, "exoplayer", uri);
		player = new DemoPlayer(builder);
		player.prepare();
		player.setSurface(s);
		setListeners(player);
		player.setPlayWhenReady(false);
		return true;
	}

	@Override
	public void releasePlayer() {
		if (isPlayerNull()) {
			return;
		}
		stopPlayer();
		player.release();
		player.blockingClearSurface();
	}

	@Override
	public void startPlayer() {
		if (isPlayerNull()) {
			return;
		}
		player.setPlayWhenReady(true);
		isPlaying = true;
	}

	@Override
	public void pausePlayer() {
		if (isPlayerNull()) {
			return;
		}
		player.setPlayWhenReady(false);
		isPlaying = false;
	}

	@Override
	public void stopPlayer() {
		pausePlayer();
		seekToPlayerMs(0);
	}

	@Override
	public int getPlayerPositionMs() {
		if (isPlayerNull()) {
			return 0;
		}
		return long2int(player.getCurrentPosition());
	}

	private boolean isPlayerNull() {
		if (player == null) {
			Log.e(TAG, "player object is null");
			return true;
		}
		return false;
	}

	@Override
	public void seekToPlayerMs(int positionMs) {
		if (isPlayerNull()) {
			return;
		}
		player.seekTo(positionMs);
	}

	@Override
	public int getPlayerDurationMs() {
		if (isPlayerNull()) {
			return 0;
		}
		return long2int(player.getDuration());
	}

	@Override
	public boolean isPlayerPlaying() {
		return playStatus == STATE_READY && isPlaying;
	}

	@Override
	public boolean isPlayerReady() {
		return playStatus == STATE_READY || playStatus == STATE_BUFFERING;
	}

	private void setListeners(DemoPlayer player) {
		player.addListener(new Listener() {

			@Override
			public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
					float pixelWidthHeightRatio) {
				// do nothing
			}

			@Override
			public void onStateChanged(boolean playWhenReady, int playbackState) {
				playStatus = playbackState;
				Log.i(TAG, "playbackState:" + playbackState);
				switch (playbackState) {
				case STATE_BUFFERING:// 3
					if (onProgressChangeListener != null) {
						onProgressChangeListener.onBuffering(true);
					}
					break;
				case STATE_ENDED:// 5
					if (onCompletedListener != null) {
						onCompletedListener.onCompleted();
					}
					break;
				case STATE_IDLE:// 1
					break;
				case STATE_PREPARING:// 2
					// TODO 点播时这个状态被跳过了
					break;
				case STATE_READY:// 4
					if (firstFlag == false) {
						if (onPlayerPreparedListener != null) {
							onPlayerPreparedListener.onPrepared();
						}
						initProgressChangeListener(onProgressChangeListener);
						firstFlag = true;
					}
					if (onProgressChangeListener != null) {
						onProgressChangeListener.onBuffering(false);
					}
					break;
				default:
					break;
				}
			}

			@Override
			public void onError(Exception e) {
				// TODO 这里需要一个适配
				if (onErrorListener != null) {
					onErrorListener.onError(0, 0);
				}
			}
		});
	}

	private static int long2int(long value) {
		return Integer.parseInt(String.valueOf(value));
	}

	/**
	 * The player is neither prepared or being prepared.
	 */
	public static final int STATE_IDLE = 1;
	/**
	 * The player is being prepared.
	 */
	public static final int STATE_PREPARING = 2;
	/**
	 * The player is prepared but not able to immediately play from the current
	 * position. The cause is {@link TrackRenderer} specific, but this state
	 * typically occurs when more data needs to be buffered for playback to
	 * start.
	 */
	public static final int STATE_BUFFERING = 3;
	/**
	 * The player is prepared and able to immediately play from the current
	 * position. The player will be playing if {@link #getPlayWhenReady()}
	 * returns true, and paused otherwise.
	 */
	public static final int STATE_READY = 4;
	/**
	 * The player has finished playing the media.
	 */
	public static final int STATE_ENDED = 5;
}
