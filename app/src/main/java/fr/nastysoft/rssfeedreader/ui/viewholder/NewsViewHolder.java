package fr.nastysoft.rssfeedreader.ui.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import fr.nastysoft.rssfeedreader.R;
import fr.nastysoft.rssfeedreader.model.listener.OnItemClickListener;

/**
 * Created by Damien on 21/11/2014.
 */
public class NewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    protected TextView title, pubdate;
    protected ImageView image;
    protected OnItemClickListener itemClickListener;

    public NewsViewHolder(View cardView, OnItemClickListener onItemClickListener) {
        super(cardView);
        title = (TextView) cardView.findViewById(R.id.newsTitle);
        image = (ImageView) cardView.findViewById(R.id.newsImage);
        pubdate = (TextView) cardView.findViewById(R.id.newsPubDate);
        cardView.setOnClickListener(this);
        this.itemClickListener = onItemClickListener;
    }

    public TextView getTitle() {
        return title;
    }

    public void setTitle(TextView title) {
        this.title = title;
    }

    public ImageView getImage() {
        return image;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }

    public TextView getPubdate() {
        return pubdate;
    }

    public void setPubdate(TextView pubdate) {
        this.pubdate = pubdate;
    }

    @Override
    public void onClick(View v) {
        if(itemClickListener != null)
            itemClickListener.onItemClick(v, getPosition());
    }
}
