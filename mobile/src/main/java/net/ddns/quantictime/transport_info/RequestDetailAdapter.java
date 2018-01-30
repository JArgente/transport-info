package net.ddns.quantictime.transport_info;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.ddns.quantictime.transport_info.business_object.FinalDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jorge on 16/01/2018.
 */

public class RequestDetailAdapter extends BaseAdapter {

    private List<FinalDetail> listaDetails = new ArrayList<>();

    @Override
    public int getCount() {
        return listaDetails.size();
    }

    @Override
    public FinalDetail getItem(int position) {
        if (position < 0 || position >= listaDetails.size()) {
            return null;
        } else {
            return listaDetails.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view = (convertView != null ? convertView : createView(parent));
        final RequestDetailViewHolder viewHolder = (RequestDetailViewHolder) view.getTag();
        viewHolder.setRequestDetail(getItem(position));
        return view;
    }

    public void setNextArrivals(@Nullable FinalDetail arrival) {
        if (arrival == null) {
            return;
        }

        listaDetails.add(arrival);
        notifyDataSetChanged();
    }

    public void initialize(){
        listaDetails.clear();
    }

    private View createView(ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.item_request_detail, parent, false);
        final RequestDetailViewHolder viewHolder = new RequestDetailViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    private static class RequestDetailViewHolder {

        private TextView estimatedTime;
        private TextView stationName;

        public RequestDetailViewHolder(View view) {
            estimatedTime = (TextView) view.findViewById(R.id.estimated_time);
            stationName = (TextView) view.findViewById(R.id.station_name);
        }

        public void setRequestDetail(FinalDetail detail) {
            estimatedTime.setText(detail.getNextArrivals());
            stationName.setText(detail.getName());
        }
    }
}
