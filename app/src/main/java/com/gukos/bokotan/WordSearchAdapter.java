package com.gukos.bokotan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class WordSearchAdapter<T> extends ArrayAdapter<T> {
	private final Object mLock = new Object();
	private final LayoutInflater mInflater;
	private final int mResource;
	private List<T> mObjects;
	private ArrayList<T> mOriginalValues;
	private final ArrayFilter mFilter;

	public Function<T, CharSequence> stringConverter = T::toString;
	private Function<T, Boolean> checker = t -> true;
	private final Filter.FilterListener listener;

	public WordSearchAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<T> objects, Filter.FilterListener listener) {
		super(context, resource, 0, objects);
		mInflater = LayoutInflater.from(context);
		mResource = resource;
		mObjects = objects;
		mFilter = new ArrayFilter();
		this.listener = listener;
	}

	@Override
	public int getCount() {
		return mObjects.size();
	}

	@Override
	public T getItem(int position) {
		return mObjects.get(position);
	}

	@Override
	public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
		final TextView text;
		if (convertView == null) {
			text = (TextView) mInflater.inflate(mResource, parent, false);
		} else {
			text = (TextView) convertView;
		}
		text.setText(stringConverter.apply(getItem(position)));
		return text;
	}

	public void setFilter(Function<T, Boolean> function, Function<T, CharSequence> stringConverter) {
		checker = function;
		this.stringConverter = stringConverter;
		mFilter.filter("not null or void", listener);
	}

	public void resetFilter() {
		this.stringConverter = T::toString;
		mFilter.filter(null, listener);
	}

	private static final class FilterPublication {
		final Runnable publish;

		FilterPublication(Runnable publish) {
			this.publish = publish;
		}
	}

	private class ArrayFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			final FilterResults results = new FilterResults();
			if (mOriginalValues == null) {
				synchronized (mLock) {
					mOriginalValues = new ArrayList<>(mObjects);
				}
			}
			final ArrayList<T> list;
			synchronized (mLock) {
				list = new ArrayList<>(mOriginalValues);
			}
			final ArrayList<T> matches;
			if (prefix == null || prefix.length() == 0) {
				matches = list;
			} else {
				matches = new ArrayList<>();
				list.stream().filter(t -> checker.apply(t)).forEach(matches::add);
			}
			// FilterResults erases values to Object. Capture the typed list in a publication
			// callback instead of casting Object back to List<T> on the UI thread.
			results.values = new FilterPublication(() -> mObjects = matches);
			results.count = matches.size();
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			if (results.values instanceof FilterPublication) {
				((FilterPublication) results.values).publish.run();
			} else {
				mObjects = new ArrayList<>();
			}
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}