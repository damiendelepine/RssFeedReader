package fr.nastysoft.rssfeedreader.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import fr.nastysoft.rssfeedreader.R;
import fr.nastysoft.rssfeedreader.model.object.News;

public class NewsViewerActivity extends ActionBarActivity {

    private ImageView newsImage;
    private TextView newsTitle, newsDate, newsDescription;
    private News news;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsviewer);

        news = getIntent().getParcelableExtra("news");

        newsImage = (ImageView) findViewById(R.id.newsImage);
        newsTitle = (TextView) findViewById(R.id.newsTitle);
        newsDate = (TextView) findViewById(R.id.newsDate);
        newsDescription = (TextView) findViewById(R.id.newsDescription);

        Picasso
                .with(this)
                .load(news.getEnclosure())
                .resize(700, 250)
                .centerCrop()
                .placeholder(R.drawable.ic_file_download_grey600)
                .error(R.drawable.ic_warning_grey600)
                .into(newsImage);
        newsTitle.setText(news.getTitle());
        newsDate.setText(news.getPubDate());
        newsDescription.setText(news.getDescription());
    }

}
