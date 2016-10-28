package com.example.player.exo;

import com.example.player.exo.DemoPlayer.RendererBuilder;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.extractor.Extractor;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.text.TextTrackRenderer;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.UdpDataSource;

import android.content.Context;
import android.media.MediaCodec;
import android.net.Uri;

/**
 * A {@link RendererBuilder} for streams that can be read using an
 * {@link Extractor}.
 */
public class UdpExtractorRendererBuilder implements DemoPlayer.RendererBuilder {

	private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
	private static final int BUFFER_SEGMENT_COUNT = 256;

	private final Context context;
	private final Uri uri;

	public UdpExtractorRendererBuilder(Context context, Uri uri) {
		this.context = context;
		this.uri = uri;
	}

	@Override
	public void buildRenderers(DemoPlayer player) {
		Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);

		// Build the video and audio renderers.
		DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(player.getMainHandler(), null);
		DataSource dataSource = new UdpDataSource(bandwidthMeter);
		ExtractorSampleSource sampleSource = new ExtractorSampleSource(uri, dataSource, allocator,
				BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);

		MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(context, sampleSource,
				MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000, player.getMainHandler(),
				player, 50);
		MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource,
				MediaCodecSelector.DEFAULT, null, true, player.getMainHandler(), player);
		TrackRenderer textRenderer = new TextTrackRenderer(sampleSource, player, player.getMainHandler().getLooper());

		// Invoke the callback.
		TrackRenderer[] renderers = new TrackRenderer[DemoPlayer.RENDERER_COUNT];
		renderers[DemoPlayer.TYPE_VIDEO] = videoRenderer;
		renderers[DemoPlayer.TYPE_AUDIO] = audioRenderer;
		renderers[DemoPlayer.TYPE_TEXT] = textRenderer;
		player.onRenderers(renderers, bandwidthMeter);
	}

	@Override
	public void cancel() {
		// Do nothing.
	}

}
