package com.mobile.groupchat4.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.mobile.groupchat4.R;


public class ColorPickerAdapter extends BaseAdapter {
    private Context context;
    private int[] colors;

    public ColorPickerAdapter(Context context, int[] colors) {
        this.context = context;
        this.colors = colors;
    }

    @Override
    public int getCount() {
        return colors.length;
    }

    @Override
    public Object getItem(int position) {
        return colors[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.color_picker_item, null);

        ImageView colorImageView = view.findViewById(R.id.colorImageView);
        colorImageView.setBackgroundColor(colors[position]);

        return view;
    }
}