package com.mogujie.tt.utils.menu.action;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.utils.BackgroundTasks;

import java.util.ArrayList;
import java.util.List;

public class PopDialogAdapter extends BaseAdapter {

    private List<PopMenuAction> dataSource = new ArrayList<>();
    private Context context;

    public PopDialogAdapter(Context context) {
        this.context = context;
    }


    public void setDataSource(final List datas) {
        dataSource = datas;
//        BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
                notifyDataSetChanged();
//            }
//        });
    }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return dataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.pop_dialog_adapter, parent, false);
            holder = new ViewHolder();
            holder.text = convertView.findViewById(R.id.pop_dialog_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        PopMenuAction action = (PopMenuAction) getItem(position);
        holder.text.setText(action.getActionName());
        return convertView;
    }

    static class ViewHolder {
        TextView text;
    }
}
