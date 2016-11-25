package com.example.player;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Surface;

public class MyMediaPlayer extends AbsMediaPlayer {

	private MediaPlayer mediaPlayer;
	private boolean isPlayerReady;

	@Override
	public boolean initPlayer(Surface s, Uri uri, Context context) {
		mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(context, uri);
			mediaPlayer.setSurface(s);
			mediaPlayer.prepareAsync();
			setListeners(mediaPlayer);
		} catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void releasePlayer() {
		if (isPlayerNull()) {
			return;
		}
		removeListeners(mediaPlayer);
		mediaPlayer.release();
		mediaPlayer = null;
		isPlayerReady = false;
	}

	public void resetStatus() {
		isPlayerReady = false;
	}

	@Override
	public void startPlayer() {
		if (isPlayerNull()) {
			return;
		}
		mediaPlayer.start();
	}

	@Override
	public void pausePlayer() {
		if (isPlayerNull()) {
			return;
		}
		mediaPlayer.pause();
	}

	@Override
	public void stopPlayer() {
		if (isPlayerNull()) {
			return;
		}
		mediaPlayer.stop();
	}

	@Override
	public int getPlayerPositionMs() {
		if (isPlayerNull()) {
			return 0;
		}
		return mediaPlayer.getCurrentPosition();
	}

	@Override
	public void seekToPlayerMs(int positionMs) {
		if (isPlayerNull()) {
			return;
		}
		mediaPlayer.seekTo(positionMs);
	}

	@Override
	public int getPlayerDurationMs() {
		if (isPlayerNull()) {
			return 0;
		}
		return mediaPlayer.getDuration();
	}

	@Override
	public boolean isPlayerPlaying() {
		if (isPlayerNull()) {
			return false;
		}
		return mediaPlayer.isPlaying();
	}

	@Override
	public boolean isPlayerReady() {
		return isPlayerReady && (!isPlayerNull());
	}

	public boolean isPlayerNull() {
		return mediaPlayer == null;
	}

	private void setListeners(MediaPlayer player) {
		player.setOnErrorListener(onMediaPlayerErrorListener);
		player.setOnCompletionListener(onCompletionListener);
		player.setOnPreparedListener(onPreparedListener);
	}

	private void removeListeners(MediaPlayer player) {
		player.setOnErrorListener(null);
		player.setOnCompletionListener(null);
		player.setOnPreparedListener(null);
	}

	MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {

		@Override
		public void onPrepared(MediaPlayer mp) {
			isPlayerReady = true;
			initProgressChangeListener();
			if (onPlayerPreparedListener != null) {
				onPlayerPreparedListener.onPrepared();
			}
		}
	};

	MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			if (onCompletedListener != null) {
				onCompletedListener.onCompleted();
			}
		}
	};

	MediaPlayer.OnErrorListener onMediaPlayerErrorListener = new MediaPlayer.OnErrorListener() {

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			if (onErrorListener != null) {
				onErrorListener.onError(what, extra);
			}
			return false;
		}
	};
}
