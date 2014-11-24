package fr.nastysoft.rssfeedreader.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fr.nastysoft.rssfeedreader.model.listener.OnItemClickListener;
import fr.nastysoft.rssfeedreader.ui.viewholder.NewsViewHolder;
import fr.nastysoft.rssfeedreader.R;
import fr.nastysoft.rssfeedreader.model.object.News;

/**
 * Created by Damien on 21/11/2014.
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsViewHolder> {

    private List<News> newsList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public NewsAdapter(List<News> newsList, Context context) {
        this.newsList = newsList;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    @Override
    public void onBindViewHolder(NewsViewHolder newsViewHolder, int i) {
        News news = newsList.get(i);
        newsViewHolder.getTitle().setText(news.getTitle());
          newsViewHolder.getPubdate().setText(toNiceDate(news.getStoredDate()));

        Picasso
                .with(context)
                .load(news.getEnclosure())
                .resize(104, 104)
                .centerCrop()
                .placeholder(R.drawable.ic_file_download_grey600)
                .error(R.drawable.ic_warning_grey600)
                .into(newsViewHolder.getImage());
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);
        return new NewsViewHolder(itemView, onItemClickListener);
    }

    private String toNiceDate(String storedDate) {
        final String OLD_FORMAT = "yyyy-MM-dd HH:mm:ss";
        final String NEW_FORMAT = "EEEE dd MMMM yyyy HH:mm:ss";

        SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT, Locale.FRANCE);
        Date d = null;
        try {
            d = sdf.parse(storedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sdf.applyPattern(NEW_FORMAT);

        return sdf.format(d);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.onItemClickListener = mItemClickListener;
    }
}
