package com.axelby.podax;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Xml;

class SubscriptionUpdater {
	// TODO: allow subscriptions to be added to running process
	private static boolean _isRunning = false;
	private Context _context;
	Vector<Integer> _toUpdate = new Vector<Integer>();
	
	public static final SimpleDateFormat rfc822DateFormats[] = new SimpleDateFormat[] {
			new SimpleDateFormat("EEE, d MMM yy HH:mm:ss z"),
			new SimpleDateFormat("EEE, d MMM yy HH:mm z"),
			new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z"),
			new SimpleDateFormat("EEE, d MMM yyyy HH:mm z"),
			new SimpleDateFormat("d MMM yy HH:mm z"),
			new SimpleDateFormat("d MMM yy HH:mm:ss z"),
			new SimpleDateFormat("d MMM yyyy HH:mm z"),
			new SimpleDateFormat("d MMM yyyy HH:mm:ss z"), };
	
	SubscriptionUpdater(Context context) {
		_context = context;
	}
	
	public void addSubscriptionId(int id) {
		_toUpdate.add(id);
	}
	
	public void addAllSubscriptions() {
		final DBAdapter dbAdapter = DBAdapter.getInstance(_context);
		for (Subscription s : dbAdapter.getSubscriptions())
			_toUpdate.add(s.getId());
	}
	
	public void run() {
		if (_isRunning)
			return;
		_isRunning = true;
		new Thread(_worker).start();
	}
	
	private Runnable _worker = new Runnable() {
		public void run() {
			try {
				final DBAdapter dbAdapter = DBAdapter.getInstance(_context);
				while (_toUpdate.size() > 0) {
					final Subscription subscription = dbAdapter.loadSubscription(_toUpdate.get(0));
					_toUpdate.remove(0);

					URL u = new URL(subscription.getUrl());
					URLConnection c = u.openConnection();
					
					String eTag = c.getHeaderField("ETag");
					if (subscription.getETag() != null && subscription.getETag().equals(eTag))
						continue;

					updateUpdateNotification(subscription, "Downloading Feed");
					InputStream response = c.getInputStream();
					if (response == null) {
						showUpdateErrorNotification(subscription, "Feed not available. Check the URL.");
						continue;
					} 

					Vector<RssPodcast> podcasts = new Vector<RssPodcast>();
					RssPodcast podcast = null;
					String name;
					Date lastBuildDate = null;
					boolean done = false;
					
					XmlPullParser parser = Xml.newPullParser();
					parser.setInput(response, null);
					int eventType = parser.getEventType();
					do {
						switch (eventType) {
						case XmlPullParser.START_DOCUMENT:
							break;
						case XmlPullParser.START_TAG:
							name = parser.getName();
							if (name.equalsIgnoreCase("lastBuildDate")) {
								for (SimpleDateFormat df : rfc822DateFormats) {
									try {
										lastBuildDate = df.parse(parser.nextText());
										if (lastBuildDate.getTime() == subscription.getLastModified().getTime())
											done = true;
										break;
									} catch (ParseException e) {
									}
								}
							} else if (name.equalsIgnoreCase("item")) {
								podcast = new RssPodcast(subscription);
							} else if (name.equalsIgnoreCase("title")) {
								if (podcast != null)
									podcast.setTitle(parser.nextText());
								else
									dbAdapter.updateSubscriptionTitle(subscription.getUrl(), parser.nextText());
							} else if (name.equalsIgnoreCase("link")) {
								if (podcast != null)
									podcast.setLink(parser.nextText());
							} else if (name.equalsIgnoreCase("description")) {
								if (podcast != null)
									podcast.setDescription(parser.nextText());
							} else if (name.equalsIgnoreCase("pubDate")) {
								if (podcast != null)
									podcast.setPubDate(parser.nextText());
							} else if (name.equalsIgnoreCase("enclosure")) {
								if (podcast != null)
									podcast.setMediaUrl(parser.getAttributeValue(null, "url"));
							}
							break;
						case XmlPullParser.END_TAG:
							name = parser.getName();
							if (name.equalsIgnoreCase("item")) {
								if (podcast.getMediaUrl() != null && podcast.getMediaUrl().length() > 0) {
									//updateUpdateNotification(subscription, podcast.getTitle());
									podcasts.add(podcast);
								}
								podcast = null;
							} else if (name.equalsIgnoreCase("channel")) {
								done = true;
							}
							break;
						}
						eventType = parser.next();
					} while (!done && eventType != XmlPullParser.END_DOCUMENT);
					
					updateUpdateNotification(subscription, "Saving...");
					if (podcasts.size() > 0)
					dbAdapter.updatePodcastsFromFeed(podcasts);
					if (lastBuildDate != null && lastBuildDate.getTime() != subscription.getLastModified().getTime())
						dbAdapter.updateSubscriptionLastModified(subscription, lastBuildDate);
					if (eTag != null && !eTag.equals(subscription.getETag()))
						dbAdapter.updateSubscriptionETag(subscription, eTag);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				removeUpdateNotification();
				_isRunning = false;

				_context.sendBroadcast(new Intent(NotificationIds.SUBSCRIPTION_UPDATE_BROADCAST));
				UpdateService.downloadPodcasts(_context);
			}
		}
	};

	private void showUpdateErrorNotification(Subscription subscription, String reason) {
		int icon = R.drawable.icon;
		CharSequence tickerText = "Error Updating Subscription";
		long when = System.currentTimeMillis();			
		Notification notification = new Notification(icon, tickerText, when);
		
		CharSequence contentTitle = "Error updating " + subscription.getDisplayTitle();
		CharSequence contentText = reason;
		Intent notificationIntent = new Intent(_context, SubscriptionListActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(_context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(_context, contentTitle, contentText, contentIntent);
		
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager notificationManager = (NotificationManager) _context.getSystemService(ns);
		notificationManager.notify(NotificationIds.SUBSCRIPTION_UPDATE_ERROR, notification);
	}
	

	private void updateUpdateNotification(Subscription subscription, String status) {
		int icon = R.drawable.icon;
		CharSequence tickerText = "Updating Subscriptions";
		long when = System.currentTimeMillis();			
		Notification notification = new Notification(icon, tickerText, when);
		
		CharSequence contentTitle = "Updating " + subscription.getDisplayTitle();
		CharSequence contentText = status;
		Intent notificationIntent = new Intent(_context, SubscriptionUpdater.class);
		PendingIntent contentIntent = PendingIntent.getActivity(_context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(_context, contentTitle, contentText, contentIntent);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager notificationManager = (NotificationManager) _context.getSystemService(ns);
		notificationManager.notify(NotificationIds.SUBSCRIPTION_UPDATE_ONGOING, notification);
	}
	
	private void removeUpdateNotification() {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager notificationManager = (NotificationManager) _context.getSystemService(ns);
		notificationManager.cancel(NotificationIds.SUBSCRIPTION_UPDATE_ONGOING);
	}
}