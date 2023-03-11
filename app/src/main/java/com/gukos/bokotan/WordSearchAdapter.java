package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.DebugManager.printCurrentState;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class WordSearchAdapter<T> extends ArrayAdapter<T> {
	private final Object mLock = new Object();
	private final LayoutInflater mInflater;
	private final Context mContext;
	private final int mResource;
	private List<T> mObjects;
	private int mFieldId = 0;
	private ArrayList<T> mOriginalValues;
	private ArrayFilter mFilter;
	
	public WordSearchAdapter(@NonNull Context context, @LayoutRes int resource,
	                         @NonNull List<T> objects) {
		this(context, resource, 0, objects);
	}
	
	private WordSearchAdapter(@NonNull Context context, @LayoutRes int resource,
	                          @IdRes int textViewResourceId, @NonNull List<T> objects ) {
		super(context, resource, textViewResourceId, objects);
		printCurrentState();
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mResource  = resource;
		mObjects = objects;
		mFieldId = textViewResourceId;
	}

	public void add_(T object) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.add(object);
			} else {
				mObjects.add(object);
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mObjects.size();
	}

	@Override
	public T getItem(int position) {
		printCurrentState(mObjects.get(position).toString());
		return mObjects.get(position);
	}

	@Override
	public @NonNull View getView(int position, View convertView,
			@NonNull ViewGroup parent) {
		printCurrentState();
		return createViewFromResource(mInflater, position, convertView, parent, mResource);
	}

	private @NonNull View createViewFromResource(@NonNull LayoutInflater inflater, int position,
			View convertView, @NonNull ViewGroup parent, int resource) {
		final View view;
		final TextView text;

		if (convertView == null) {
			view = inflater.inflate(resource, parent, false);
		} else {
			view = convertView;
		}

		try {
			if (mFieldId == 0) {
				// If no custom field is assigned, assume the whole resource is a TextView
				text = (TextView) view;
			} else {
				// Otherwise, find the TextView field within the layout
				text = view.findViewById(mFieldId);

				if (text == null) {
					throw new RuntimeException("Failed to find view with ID "
							+ mContext.getResources().getResourceName(mFieldId)
							+ " in item layout");
				}
			}
		} catch (ClassCastException e) {
			Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
			throw new IllegalStateException(
					"ArrayAdapter requires the resource ID to be a TextView", e);
		}

		final T item = getItem(position);
		if (item instanceof CharSequence) {
			text.setText((CharSequence) item);
		} else {
			text.setText(item.toString());
		}
		
		printCurrentState("view="+view);
		return view;
	}

	@Override
	public @NonNull Filter getFilter() {
		if (mFilter == null) {
			mFilter = new ArrayFilter();
		}
		return mFilter;
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

			if (prefix == null || prefix.length() == 0) {
				final ArrayList<T> list;
				synchronized (mLock) {
					list = new ArrayList<>(mOriginalValues);
				}
				results.values = list;
				results.count = list.size();
			} else {
				final String prefixString = prefix.toString().toLowerCase();

				final ArrayList<T> values;
				synchronized (mLock) {
					values = new ArrayList<>(mOriginalValues);
				}

				final int count = values.size();
				final ArrayList<T> newValues = new ArrayList<>();

				for (int i = 0; i < count; i++) {
					final T value = values.get(i);
					final String valueText = value.toString().toLowerCase();

					// First match against the whole, non-splitted value
					if (valueText.startsWith(prefixString)) {
						newValues.add(value);
					} else {
						final String[] words = valueText.split(" ");
						for (String word : words) {
							if (word.endsWith(prefixString)) {
								newValues.add(value);
								break;
							}
						}
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			// noinspection unchecked
			mObjects = (List<T>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}