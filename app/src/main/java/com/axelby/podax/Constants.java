package com.axelby.podax;

import android.net.Uri;

public class Constants {
	public static final int NOTIFICATION_UPDATE = 1;
	public static final int SUBSCRIPTION_UPDATE_ERROR = 2;
	public static final int NOTIFICATION_PLAYING = 3;
	public static final int NOTIFICATION_GPODDER_ERROR = 4;
	public static final int NOTIFICATION_DOWNLOAD_ERROR = 5;
	public static final int NOTIFICATION_DOWNLOADING_BASE = 100000;

	// active episode related
	public static final Uri ACTIVE_EPISODE_DATA_RESTART = Uri.parse("podax://activeepisode/restart");
	public static final Uri ACTIVE_EPISODE_DATA_BACK = Uri.parse("podax://activeepisode/back");
	public static final Uri ACTIVE_EPISODE_DATA_FORWARD = Uri.parse("podax://activeepisode/forward");
	public static final Uri ACTIVE_EPISODE_DATA_END = Uri.parse("podax://activeepisode/end");
	public static final Uri ACTIVE_EPISODE_DATA_PAUSE = Uri.parse("podax://activeepisode/pause");

	public static final String ACTION_REFRESH_ALL_SUBSCRIPTIONS = "com.axelby.podax.REFRESH_ALL_SUBSCRIPTIONS";
	public static final String ACTION_REFRESH_SUBSCRIPTION = "com.axelby.podax.REFRESH_SUBSCRIPTION";
	public static final String ACTION_DOWNLOAD_EPISODE = "com.axelby.podax.DOWNLOAD_EPISODE";
	public static final String ACTION_DOWNLOAD_EPISODES = "com.axelby.podax.DOWNLOAD_EPISODES";

	public static final String EXTRA_EPISODE_ID = "com.axelby.podax.episodeId";
	public static final String EXTRA_SUBSCRIPTION_ID = "com.axelby.podax.subscriptionId";
	public static final String EXTRA_SUBSCRIPTION_NAME = "com.axelby.podax.subscriptionName";
	public static final String EXTRA_ITUNES_ID = "com.axelby.podax.itunesId";
	public static final String EXTRA_MANUAL_REFRESH = "com.axelby.podax.manual_refresh";
    public static final String EXTRA_CATEGORY_ID = "com.axelby.podax.categoryId";
    public static final String EXTRA_FRAGMENT_CLASSNAME = "com.axelby.podax.fragmenClassName";
    public static final String EXTRA_ARGS = "com.axelby.podax.args";
	public static final String EXTRA_RSSURL = "com.axelby.podax.rssUrl";

	public static final String EXTRA_PLAYER_COMMAND = "com.axelby.podax.player_command";
	public static final String EXTRA_PLAYER_COMMAND_ARG = "com.axelby.podax.player_command_arg";
	public static final int PLAYER_COMMAND_PLAYPAUSE = 0;
	public static final int PLAYER_COMMAND_PLAY = 1;
	public static final int PLAYER_COMMAND_PAUSE = 2;
	public static final int PLAYER_COMMAND_STOP = 3;
	public static final int PLAYER_COMMAND_PLAYSTOP = 4;
	public static final int PLAYER_COMMAND_REFRESHEPISODE = 5;

	// reasons for pausing
	public static final int PAUSE_AUDIOFOCUS = 0;
	public static final int PAUSE_MEDIABUTTON = 1;

	// gpodder constants
	public static final String GPODDER_ACCOUNT_TYPE = "com.axelby.gpodder";
	public static final int GPODDER_UPDATE_NONE = 0;
	public static final int GPODDER_UPDATE_POSITION = 1;
}