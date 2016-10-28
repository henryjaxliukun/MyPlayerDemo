package com.example.player;

public abstract class AbsMediaPlayer implements IMediaPlayer {

	protected OnProgressChangeListener onProgressChangeListener;
	protected OnErrorListener onErrorListener;
	protected OnCompletedListener onCompletedListener;
	protected OnPlayerPreparedListener onPlayerPreparedListener;
	protected int lastPositionMs;

	@Override
	public void setPlayerPreparedListener(OnPlayerPreparedListener listener) {
		this.onPlayerPreparedListener = listener;
	}

	@Override
	public void setProgressChangedListener(OnProgressChangeListener progressChangedListener) {
		this.onProgressChangeListener = progressChangedListener;
	}

	@Override
	public void setErrorListener(OnErrorListener listener) {
		this.onErrorListener = listener;
	}

	@Override
	public void setCompletedListener(OnCompletedListener listener) {
		this.onCompletedListener = listener;
	}

	protected void initProgressChangeListener(final OnProgressChangeListener mProgressChangedListener) {
		new Thread(new Runnable() {
			@Override
			public synchronized void run() {
				while (isPlayerReady()) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					int positionMs = getPlayerPositionMs();
					if (isPlayerPlaying() && mProgressChangedListener != null) {
						mProgressChangedListener.onProgressChanged(positionMs);
					}
					lastPositionMs = positionMs;
				}
			}
		}).start();
	}

}
