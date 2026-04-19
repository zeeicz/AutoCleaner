package com.z.cachecleaner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
    private final List<AppInfo> appList;

    public AppAdapter(List<AppInfo> appList) {
        this.appList = appList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo app = appList.get(position);
        holder.tvName.setText(app.appName);
        holder.tvCache.setText("Cache: " + (app.cacheSize / (1024 * 1024)) + " MB");
        holder.imgIcon.setImageDrawable(app.icon);
        
        holder.cbExclude.setOnCheckedChangeListener(null); // Hindari bug recycle view
        holder.cbExclude.setChecked(app.isExcluded);
        holder.cbExclude.setOnCheckedChangeListener((buttonView, isChecked) -> app.isExcluded = isChecked);
    }

    @Override
    public int getItemCount() { return appList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCache;
        ImageView imgIcon;
        CheckBox cbExclude;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAppName);
            tvCache = itemView.findViewById(R.id.tvCacheSize);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            cbExclude = itemView.findViewById(R.id.cbExclude);
        }
    }
}