package com.lis.lis.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;

import java.util.List;

public class HintAdapter extends ArrayAdapter<String> {
    public HintAdapter(Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
    }

    @Override
    public int getCount() {
        int count = super.getCount();
        return count > 0 ? count - 1 : count;
    }
}
