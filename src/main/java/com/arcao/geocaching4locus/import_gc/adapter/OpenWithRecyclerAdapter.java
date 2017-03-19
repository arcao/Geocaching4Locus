package com.arcao.geocaching4locus.import_gc.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.import_gc.data.ActivityInfoModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OpenWithRecyclerAdapter extends RecyclerView.Adapter<OpenWithRecyclerAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onAppListItemClick(Intent intent);
    }

    private final List<ActivityInfoModel> list;
    OnItemClickListener onItemClickListener;

    public OpenWithRecyclerAdapter(Context context, Intent intent) {

        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> infoList = packageManager.queryIntentActivities(intent, -1);

        list = new ArrayList<>(infoList.size());

        for (ResolveInfo info: infoList) {
            list.add(new ActivityInfoModel(context, info, intent));
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_open_with_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.icon) ImageView icon;
        @BindView(R.id.title) TextView title;

        private Intent intent;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(ActivityInfoModel model) {
            icon.setImageDrawable(model.icon);
            title.setText(model.title);
            itemView.setOnClickListener(this);

            this.intent = model.intent;
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null)
                onItemClickListener.onAppListItemClick(intent);
        }
    }
}
