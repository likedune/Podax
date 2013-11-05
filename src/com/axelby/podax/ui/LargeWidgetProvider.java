package com.axelby.podax.ui;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.RemoteViews;

import com.axelby.podax.ActivePodcastReceiver;
import com.axelby.podax.Constants;
import com.axelby.podax.Helper;
import com.axelby.podax.PlayerService;
import com.axelby.podax.PlayerStatus;
import com.axelby.podax.R;

public class LargeWidgetProvider extends AppWidgetProvider {
	public static void setPlayerServiceClickIntent(Context context, RemoteViews views, int resourceId, int command) {
		Intent intent = new Intent(context, PlayerService.class);
		// pendingintent will reuse intent if possible, does not look at extras so datauri makes this unique to command
		intent.setData(Uri.parse("podax://playercommand/" + command));
		intent.putExtra(Constants.EXTRA_PLAYER_COMMAND, command);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
		views.setOnClickPendingIntent(resourceId, pendingIntent);
	}

	public static void setActivePodcastClickIntent(Context context, RemoteViews views, int resourceId, Uri command) {
		Intent intent = new Intent(context, ActivePodcastReceiver.class);
		intent.setData(command);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		views.setOnClickPendingIntent(resourceId, pendingIntent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		if (appWidgetIds.length == 0)
			return;

		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.largewidget);

		PlayerStatus playerState = PlayerStatus.getCurrentState(context);

		updatePodcastDetails(playerState, views);

		// set up pending intents
		setActivePodcastClickIntent(context, views, R.id.restart_btn, Constants.ACTIVE_PODCAST_DATA_RESTART);
		setActivePodcastClickIntent(context, views, R.id.rewind_btn, Constants.ACTIVE_PODCAST_DATA_BACK);
		setPlayerServiceClickIntent(context, views, R.id.play_btn, Constants.PLAYER_COMMAND_PLAYSTOP);
		setActivePodcastClickIntent(context, views, R.id.skip_btn, Constants.ACTIVE_PODCAST_DATA_FORWARD);
		setActivePodcastClickIntent(context, views, R.id.next_btn, Constants.ACTIVE_PODCAST_DATA_END);

		Bitmap thumbnail = Helper.getCachedImage(playerState.getSubscriptionThumbnailUrl());
		if (thumbnail != null)
			views.setImageViewBitmap(R.id.show_btn, thumbnail);
		else
			views.setImageViewResource(R.id.show_btn, R.drawable.icon);

		Intent showIntent = new Intent(context, MainActivity.class);
		PendingIntent showPendingIntent = PendingIntent.getActivity(context, 0, showIntent, 0);
		views.setOnClickPendingIntent(R.id.show_btn, showPendingIntent);

		for (int widgetId : appWidgetIds)
			appWidgetManager.updateAppWidget(widgetId, views);

		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	public void updatePodcastDetails(PlayerStatus player, RemoteViews views) {
		if (player.hasActivePodcast()) {
			views.setTextViewText(R.id.title, player.getTitle());
			views.setTextViewText(R.id.podcast, player.getSubscriptionTitle());
			PodcastProgress.remoteSet(views, player.getPosition(), player.getDuration());

			int imageRes = player.isPlaying() ? R.drawable.ic_media_pause : R.drawable.ic_media_play;
			views.setImageViewResource(R.id.play_btn, imageRes);
		} else {
			views.setTextViewText(R.id.title, "Queue empty");
			views.setTextViewText(R.id.podcast, "");
			PodcastProgress.remoteClear(views);
			views.setImageViewResource(R.id.play_btn, R.drawable.ic_media_play);
		}
	}
}
