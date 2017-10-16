package acr.browser.lightning.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import acr.browser.lightning.R;
import acr.browser.lightning.app.BrowserApp;
import acr.browser.lightning.database.HistoryItem;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by roma on 13.09.2017.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

        private final List<HistoryItem> historyItemList;
        private Context context;
        private int theme;

        public HistoryAdapter(Context context, int theme) {
            this.historyItemList = new ArrayList<>();
            this.context = context;
            this.theme = theme;
        }

        public void setBookmarks(List<HistoryItem> bookmarkList) {
            historyItemList.clear();
            historyItemList.addAll(bookmarkList);
        }

        public List<HistoryItem> getHistoryItemList() {
            return historyItemList;
        }
        //------------------------------------------------------

        @Override
        public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            return new HistoryAdapter.ViewHolder(inflater.inflate(R.layout.downloads_list_item, parent, false));

        }

        @Override
        public void onBindViewHolder(final HistoryAdapter.ViewHolder holder, int position) {
            HistoryItem web = historyItemList.get(position);
            holder.name.setText(web.getTitle());
            holder.name.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));
            holder.url.setText(web.getUrl());
            holder.url.setTextColor(BrowserApp.getThemeManager().getDisabledIconColor(theme));
        }


        static class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.name)
            TextView name;
            @BindView(R.id.url)
            TextView url;

            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }

        @Override
        public int getItemCount() {
            return historyItemList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

}
