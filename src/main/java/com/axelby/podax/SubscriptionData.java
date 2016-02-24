package com.axelby.podax;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.widget.CompoundButton;

import java.util.Date;
import java.util.List;

public class SubscriptionData {

	private final long _id;
	private final String _rawTitle;
	private final String _url;
	private final Date _lastModified;
	private final Date _lastUpdate;
	private final String _etag;
	private final String _thumbnail;
	private final String _titleOverride;
	private final String _description;
	private final boolean _singleUse;
	private final boolean _playlistNew;
	private final int _expirationDays;

	SubscriptionData(SubscriptionCursor sub) {
		_id = sub.getId();
		_rawTitle = sub.getRawTitle();
		_url = sub.getUrl();
		_lastModified = sub.getLastModified();
		_lastUpdate = sub.getLastUpdate();
		_etag = sub.getETag();
		_thumbnail = sub.getThumbnail();
		_titleOverride = sub.getTitleOverride();
		_description = sub.getDescription();
		_singleUse = sub.isSingleUse();
		_playlistNew = sub.areNewEpisodesAddedToPlaylist();
		_expirationDays = sub.getExpirationDays();
	}

	public static SubscriptionData create(Context context, long id) {
		if (id < 0)
			return null;

		SubscriptionCursor ep = SubscriptionCursor.getCursor(context, id);
		if (ep == null)
			return null;

		SubscriptionData data = new SubscriptionData(ep);
		ep.closeCursor();
		return data;
	}

	public long getId() { return _id; }
	public String getRawTitle() { return _rawTitle; }
	public String getUrl() { return _url; }
	public Date getLastModified() { return _lastModified; }
	public Date getLastUpdate() { return _lastUpdate; }
	public String getEtag() { return _etag; }
	public String getThumbnail() { return _thumbnail; }
	public String getTitleOverride() { return _titleOverride; }
	public String getDescription() { return _description; }
	public boolean isSingleUse() { return _singleUse; }
	public boolean isSubscribed() { return !_singleUse; }
	public boolean areNewEpisodesAddedToPlaylist() { return _playlistNew; }
	public int getExpirationDays() { return _expirationDays; }
	public String getTitle() {
		if (_titleOverride != null && _titleOverride.length() > 0)
			return _titleOverride;
		return _rawTitle;
	}

	/* -------
	   actions
	   ------- */

	public boolean isCurrentlyUpdating() {
		return UpdateService.getUpdatingSubscriptionId() == getId();
	}

	public List<EpisodeData> getEpisodes(Context context) {
		return EpisodeData.getForSubscriptionId(context, getId()).toBlocking().first();
	}

	public void changeSubscribe(CompoundButton button, boolean isChecked) {
		ContentValues values = new ContentValues(1);
		values.put(SubscriptionProvider.COLUMN_SINGLE_USE, !isChecked);
		Uri subscriptionUri = SubscriptionProvider.getContentUri(getId());
		button.getContext().getContentResolver().update(subscriptionUri, values, null, null);
	}

	public void changeAddNewToPlaylist(CompoundButton button, boolean isChecked) {
		ContentValues values = new ContentValues(1);
		values.put(SubscriptionProvider.COLUMN_PLAYLIST_NEW, isChecked);
		Uri subscriptionUri = SubscriptionProvider.getContentUri(getId());
		button.getContext().getContentResolver().update(subscriptionUri, values, null, null);
	}
}
