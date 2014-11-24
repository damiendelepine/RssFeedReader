package fr.nastysoft.rssfeedreader.ui.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import fr.nastysoft.rssfeedreader.model.listener.OnItemClickListener;
import fr.nastysoft.rssfeedreader.util.parser.LeMondeXMLParser;
import fr.nastysoft.rssfeedreader.R;
import fr.nastysoft.rssfeedreader.model.object.News;
import fr.nastysoft.rssfeedreader.ui.adapter.NewsAdapter;
import fr.nastysoft.rssfeedreader.util.database.NewsContentProvider;
import fr.nastysoft.rssfeedreader.util.database.NewsDBOpenHelper;


public class FeedActivity extends ActionBarActivity {

    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";
    private static final String XML_URL = "http://www.lemonde.fr/rss/une.xml";
    private static final String XML_URL2 = "http://www.lemonde.fr/m-actu/rss_full.xml";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeView;
    private NewsAdapter adapter;

    private List<News> storedNewsList = new ArrayList<News>();
    private List<News> rssFeedNewsList = new ArrayList<News>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // retrieve stored news in database
        getnewsFromDB();

        // RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.cardList);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);        // UI update
        adapter = new NewsAdapter(storedNewsList, FeedActivity.this);
        recyclerView.setAdapter(adapter);

        // adding the custom onclicklistener for recyclerview
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // display news
                Intent intent = new Intent(FeedActivity.this, NewsViewerActivity.class);
                intent.putExtra("news", storedNewsList.get(position));
                startActivity(intent);
            }
        });

        // refresh data
        // TODO : website seems to restrict the number of refresh
        // TODO : find out why...
        swipeView = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPage();
            }
        });
        swipeView.setColorScheme(
                android.R.color.holo_red_light,
                android.R.color.holo_green_light,
                android.R.color.holo_blue_bright,
                android.R.color.holo_orange_light);
    }

    // Uses AsyncTask to download the XML feed from LeMonde
    public void loadPage() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected) {
            swipeView.setRefreshing(true);
            new DownloadXmlTask().execute(XML_URL);
        }
        else {
            // TODO : show Error "no network"
            Toast.makeText(getApplicationContext(), "No network connection detected.", Toast.LENGTH_LONG).show();
            swipeView.setRefreshing(false);
        }
    }

    // Implementation of AsyncTask used to download XML feed from stackoverflow.com.
    private class DownloadXmlTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                rssFeedNewsList.clear();
                return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if(rssFeedNewsList.size() != 0) {
                Log.d("DOWNLOAD SIZE", String.valueOf(rssFeedNewsList.size()));
                swipeView.setRefreshing(false);
                storedNewsList.clear();
                getnewsFromDB();
                NewsAdapter newsAdapter = new NewsAdapter(storedNewsList, FeedActivity.this);
                recyclerView.setAdapter(newsAdapter);

                // adding the custom onclicklistener for recyclerview
                newsAdapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // display news
                        Intent intent = new Intent(FeedActivity.this, NewsViewerActivity.class);
                        intent.putExtra("news", storedNewsList.get(position));
                        startActivity(intent);
                    }
                });

                Toast.makeText(getApplicationContext(), "RSS feed updated !", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "RSS feed already up-to-date !", Toast.LENGTH_LONG).show();
                swipeView.setRefreshing(false);
            }
        }
    }

    private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiate the parser
        LeMondeXMLParser leMondeXmlParser = new LeMondeXMLParser(this);

        try {
            stream = downloadUrl(urlString);
            rssFeedNewsList = leMondeXmlParser.parse(stream);
            // Makes sure that the InputStream is closed after the app is finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return "Parsing ok.";
    }

    // Given a string representation of a URL, sets up a connection and gets an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    private void getnewsFromDB() {
        ContentResolver cr = getContentResolver();
        String []allColumns = {
                NewsDBOpenHelper.COLUMN_ID,             // 0
                NewsDBOpenHelper.COLUMN_TITLE,          // 1
                NewsDBOpenHelper.COLUMN_LINK,           // 2
                NewsDBOpenHelper.COLUMN_DESCRIPTION,    // 3
                NewsDBOpenHelper.COLUMN_ENCLOSURE,      // 4
                NewsDBOpenHelper.COLUMN_PUBDATE,        // 5
                NewsDBOpenHelper.COLUMN_GUID,           // 6
                NewsDBOpenHelper.COLUMN_STOREDDATE,     // 7

        };
        Cursor cursor = cr.query(NewsContentProvider.CONTENT_URI, allColumns, null, null, NewsDBOpenHelper.COLUMN_STOREDDATE + " DESC");
        if(cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                storedNewsList.add(buildNewsFromCursor(cursor)); // create match from cursor and add it to the list
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    private News buildNewsFromCursor(Cursor cursor) {
        News news = new News();
            news.setTitle(cursor.getString(1));
            news.setLink(cursor.getString(2));
            news.setDescription(cursor.getString(3));
            news.setEnclosure(cursor.getString(4));
            news.setPubDate(cursor.getString(5));
            news.setGuid(cursor.getString(6));
        return news;
    }
}
