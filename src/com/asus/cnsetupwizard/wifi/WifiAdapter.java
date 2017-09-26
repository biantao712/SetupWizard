package com.asus.cnsetupwizard.wifi;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.asus.cnsetupwizard.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;


/**
 * Created by czy on 17-3-29.
 */

public class WifiAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<HashMap<String, Object>> mItem = new ArrayList<HashMap<String, Object>>();
    List<AccessPoint> mAccessPointList = new ArrayList<AccessPoint>();

    public WifiAdapter(Context mActivityContext, ArrayList<HashMap<String, Object>> lstImageItem, List<AccessPoint> list) {
        mContext = mActivityContext;
        mItem = lstImageItem;
        mAccessPointList = list;
    }

    @Override
    public int getCount() {
        return mItem.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.wifi_list_item, null);
            holder.name = (TextView)convertView.findViewById(R.id.name);
            holder.summary = (TextView)convertView.findViewById(R.id.summary);
            holder.wifi = (ImageView) convertView.findViewById(R.id.wifi_icon);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.name.setText(mItem.get(position).get("ItemSSID").toString());
        if(mAccessPointList.get(position).getSummary() == mContext.getResources().getString(R.string.wifi_disabled_password_failure)){
            holder.summary.setText(mAccessPointList.get(position).getSummary());
        }else if(mAccessPointList.get(position).networkId != INVALID_NETWORK_ID && mAccessPointList.get(position).getState() == null) {
            holder.summary.setText(mContext.getResources().getString(R.string.wifi_remembered));
        }else {
            holder.summary.setText(mAccessPointList.get(position).getSummary());
        }
        holder.wifi.setImageResource((Integer) mItem.get(position).get("ItemImage"));

        return convertView;
    }

    class ViewHolder{
        TextView name;
        TextView summary;
        ImageView wifi;
    }
}
