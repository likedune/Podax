package com.axelby.podax;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class Subscriptions {

	private static PublishSubject<SubscriptionCursor> _changeSubject = PublishSubject.create();
	public static void notifyChange(SubscriptionCursor c) {
		_changeSubject.onNext(c);
	}

	private static Observable<SubscriptionData> _changeWatcher = _changeSubject.map(SubscriptionData::new);
	public static Observable<SubscriptionData> getWatcher() {
		return _changeWatcher.observeOn(AndroidSchedulers.mainThread());
	}
	public static Observable<SubscriptionData> getWatcher(long id) {
		return _changeWatcher
			.filter(d -> d.getId() == id)
			.observeOn(AndroidSchedulers.mainThread());
	}

	public static Observable<SubscriptionData> getObservable(Context context, long id) {
		if (id < 0)
			return Observable.empty();

		return Subscriptions.getWatcher(id)
			.startWith(SubscriptionData.create(context, id))
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread());
	}

	public static Observable<SubscriptionData> getAll(Context context) {
		return Observable.create(subscriber -> {
			Cursor c = context.getContentResolver().query(SubscriptionProvider.URI, null, null, null, null);
			if (c != null) {
				while (c.moveToNext())
					subscriber.onNext(new SubscriptionData(new SubscriptionCursor(c)));
				c.close();
			}
			subscriber.onCompleted();
		});
	}

	/* -------
	   helpers
	   ------- */

	private static Observable<SubscriptionData> queryToObservable(Context context,
			  Uri uri, String selection, String[] selectionArgs, String sortOrder) {
		return Observable.create(subscriber -> {
			Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, sortOrder);
			if (cursor != null) {
				while (cursor.moveToNext())
					subscriber.onNext(new SubscriptionData(new SubscriptionCursor(cursor)));
				cursor.close();
			}
			subscriber.onCompleted();
		});
	}

	public static Observable<SubscriptionData> getForRSSUrl(Context context, String rssUrl) {
		String selection = SubscriptionProvider.COLUMN_URL + "=?";
		String[] selectionArgs = new String[] { rssUrl };
		return Subscriptions.queryToObservable(context, SubscriptionProvider.URI, selection, selectionArgs, null);
	}
}
