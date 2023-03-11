package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.DebugManager.printCurrentState;

import android.content.Context;
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
import java.util.function.Function;

import kotlin.jvm.functions.Function2;

public class WordSearchAdapter<T> extends ArrayAdapter<T> {
	private final Object mLock = new Object();
	private final LayoutInflater mInflater;
	private final int mResource;
	private List<T> mObjects;
	private int mFieldId = 0;
	private ArrayList<T> mOriginalValues;
	private ArrayFilter mFilter;
	
	Function2<T, String, Boolean> checker = (t, s) -> t.toString().toLowerCase().contains(s);
	public Function<T,CharSequence> getString=T::toString;
	
	public WordSearchAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<T> objects) {
		this(context, resource, 0, objects);
	}
	
	private WordSearchAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull List<T> objects) {
		super(context, resource, textViewResourceId, objects);
		printCurrentState();
		mInflater = LayoutInflater.from(context);
		mResource = resource;
		mObjects = objects;
		mFieldId = textViewResourceId;
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
		printCurrentState();
		final View view;
		final TextView text;
		
		if (convertView == null) {
			view = mInflater.inflate(mResource, parent, false);
		}else {
			view = convertView;
		}
		try {
			if (mFieldId == 0) {
				// If no custom field is assigned, assume the whole resource is a TextView
				text = (TextView) view;
			}else {
				// Otherwise, find the TextView field within the layout
				text = view.findViewById(mFieldId);
			}
		} catch (ClassCastException e) {
			throw new IllegalStateException(
				"ArrayAdapter requires the resource ID");
		}
		
		final T item = getItem(position);
		if (item instanceof CharSequence) {
			text.setText((CharSequence) item);
		}else {
			text.setText(getString.apply(item));
		}
		return view;
	}
	
	@Override
	public @NonNull Filter getFilter() {
		if (mFilter == null) {
			mFilter = new ArrayFilter();
		}
		return mFilter;
	}
	
	public void filter(Function2<T,String,Boolean> function,String key,
	                 Filter.FilterListener listener){
		this.checker=function;
		getFilter().filter(key,listener);
	}
	
	public void resetFilter(Filter.FilterListener listener){
		this.getString=T::toString;
		getFilter().filter("",listener);
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
			}
			else {
				final String prefixString = prefix.toString().toLowerCase();
				
				final ArrayList<T> values;
				synchronized (mLock) {
					values = new ArrayList<>(mOriginalValues);
				}
				final ArrayList<T> newValues = new ArrayList<>();
				
				//values.stream().filter(t->checker.invoke(t, prefixString)).forEach(newValues::add);
				for (var v : values)
					if (checker.invoke(v, prefixString))
						newValues.add(v);
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
			}
			else {
				notifyDataSetInvalidated();
			}
		}
	}
}