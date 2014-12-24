package com.axelby.podax.ui;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.axelby.podax.Constants;
import com.axelby.podax.EpisodeCursor;
import com.axelby.podax.EpisodeProvider;
import com.axelby.podax.Helper;
import com.axelby.podax.PlayerService;
import com.axelby.podax.R;
import com.axelby.podax.SubscriptionCursor;
import com.axelby.podax.SubscriptionProvider;

import java.text.DateFormat;

import javax.annotation.Nonnull;

public class EpisodeListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private PodcastAdapter _adapter = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		_adapter = new PodcastAdapter();
		getLoaderManager().initLoader(0, getArguments(), this);
	}

	@Override
	public View onCreateView(@Nonnull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_episodelist, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		RecyclerView listView = (RecyclerView) getActivity().findViewById(R.id.list);
		listView.setLayoutManager(new LinearLayoutManager(getActivity()));
		listView.setItemAnimator(new DefaultItemAnimator());
		listView.setAdapter(_adapter);

        setTitle();
	}

	public void setTitle() {
		if (getView() == null)
			return;

        long subscriptionId = getArguments().getLong(Constants.EXTRA_SUBSCRIPTION_ID, -1);
        if (subscriptionId == -1)
            return;

		Uri subscriptionUri = ContentUris.withAppendedId(SubscriptionProvider.URI, subscriptionId);
		String[] subscriptionProjection = {
				SubscriptionProvider.COLUMN_ID,
				SubscriptionProvider.COLUMN_TITLE,
				SubscriptionProvider.COLUMN_URL,
		};
		Cursor subscriptionCursor = getActivity().getContentResolver().query(subscriptionUri, subscriptionProjection, null, null, null);
		if (subscriptionCursor == null)
			return;

        if (subscriptionCursor.moveToNext()) {
            SubscriptionCursor cursor = new SubscriptionCursor(subscriptionCursor);

            TextView subscription = (TextView) getView().findViewById(R.id.subscription);
            subscription.setText(cursor.getTitle());

            ImageView thumbnail = (ImageView) getView().findViewById(R.id.thumbnail);
            thumbnail.setImageBitmap(SubscriptionCursor.getThumbnailImage(getActivity(), subscriptionId));
        }

        subscriptionCursor.close();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = ContentUris.withAppendedId(SubscriptionProvider.URI, args.getLong(Constants.EXTRA_SUBSCRIPTION_ID));
		uri = Uri.withAppendedPath(uri, "podcasts");
		String[] projection = {
				EpisodeProvider.COLUMN_ID,
				EpisodeProvider.COLUMN_TITLE,
                EpisodeProvider.COLUMN_PUB_DATE,
                EpisodeProvider.COLUMN_DURATION,
				EpisodeProvider.COLUMN_MEDIA_URL,
				EpisodeProvider.COLUMN_FILE_SIZE,
				EpisodeProvider.COLUMN_PLAYLIST_POSITION,
		};
		return new CursorLoader(getActivity(), uri, projection, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (getActivity() == null)
			return;

		_adapter.changeCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		_adapter.changeCursor(null);
	}

	private class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.ViewHolder> {

        private final DateFormat _pubDateFormat = DateFormat.getDateInstance();
		private Cursor _cursor;

        class ViewHolder extends RecyclerView.ViewHolder {
			public View container;
            public TextView title;
            public TextView date;
            public TextView duration;
            public Button play;
            public Button playlist;

            public ViewHolder (View view) {
				super(view);

				container = view;
                title = (TextView) view.findViewById(R.id.title);
                date = (TextView) view.findViewById(R.id.date);
                duration = (TextView) view.findViewById(R.id.duration);
                play = (Button) view.findViewById(R.id.play);
                playlist = (Button) view.findViewById(R.id.playlist);

				play.setOnClickListener(_playHandler);
				playlist.setOnClickListener(_playlistHandler);
				view.setOnClickListener(_clickHandler);
            }
        }

		public PodcastAdapter() {
			setHasStableIds(true);
        }

		public void changeCursor(Cursor cursor) {
			_cursor = cursor;
			notifyDataSetChanged();
		}

        OnClickListener _playHandler = new OnClickListener() {
            @Override
            public void onClick(View view) {
                long episodeId = (Long) view.getTag();
                PlayerService.play(view.getContext(), episodeId);
				startActivity(PodaxFragmentActivity.createIntent(getActivity(), EpisodeDetailFragment.class, Constants.EXTRA_EPISODE_ID, episodeId));
            }
        };

        OnClickListener _playlistHandler = new OnClickListener() {
            @Override
            public void onClick(View view) {
                long episodeId = (Long) view.getTag(R.id.episodeId);
                Integer position = (Integer) view.getTag(R.id.playlist);

                ContentValues values = new ContentValues(1);
                values.put(EpisodeProvider.COLUMN_PLAYLIST_POSITION, position);
                view.getContext().getContentResolver().update(EpisodeProvider.getContentUri(episodeId), values, null, null);
            }
        };

		OnClickListener _clickHandler = new OnClickListener() {
			@Override
			public void onClick(View view) {
				long episodeId = (Long) view.getTag();
				startActivity(PodaxFragmentActivity.createIntent(getActivity(), EpisodeDetailFragment.class, Constants.EXTRA_EPISODE_ID, episodeId));
			}
		};

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_episoidelist_item, parent, false);
			return new ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			if (_cursor == null)
				return;
			Context context = holder.container.getContext();

			_cursor.moveToPosition(position);
			EpisodeCursor episode = new EpisodeCursor(_cursor);

			holder.container.setTag(episode.getId());
            holder.title.setText(episode.getTitle());
            holder.date.setText(context.getString(R.string.released_on) + " " + _pubDateFormat.format(episode.getPubDate()));
            if (episode.getDuration() > 0) {
                holder.duration.setText(Helper.getVerboseTimeString(context, episode.getDuration() / 1000f) + " " + context.getString(R.string.in_duration));
                holder.duration.setVisibility(View.VISIBLE);
            } else
                holder.duration.setVisibility(View.GONE);
            holder.play.setTag(episode.getId());
            holder.playlist.setTag(R.id.episodeId, episode.getId());

            Integer inPlaylist = episode.getPlaylistPosition();
            if (inPlaylist == null) {
                holder.playlist.setTag(R.id.playlist, Integer.MAX_VALUE);
                holder.playlist.setText(R.string.add_to_playlist);
            } else {
                holder.playlist.setTag(R.id.playlist, null);
                holder.playlist.setText(R.string.remove_from_playlist);
            }

			if (episode.isDownloaded(context))
				holder.play.setText(R.string.play);
			else
				holder.play.setText(R.string.stream);
		}

		@Override
        public long getItemId(int position) {
			_cursor.moveToPosition(position);
            return new EpisodeCursor(_cursor).getId();
        }

		@Override
		public int getItemCount() {
			if (_cursor == null)
				return 0;
			return _cursor.getCount();
		}
	}
}