package fr.nastysoft.rssfeedreader.util.parser;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import fr.nastysoft.rssfeedreader.model.object.News;
import fr.nastysoft.rssfeedreader.util.database.NewsContentProvider;
import fr.nastysoft.rssfeedreader.util.database.NewsDBOpenHelper;

/**
 * Created by Damien on 21/11/2014.
 */
public class LeMondeXMLParser {

    private Context context;

    public LeMondeXMLParser(Context context) {
        this.context = context;
    }

    public List parse(InputStream in) throws XmlPullParserException, IOException {

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readChannel(parser);
        } finally {
            in.close();
        }
    }

    private List readChannel(XmlPullParser parser) throws XmlPullParserException, IOException {


        List news = new ArrayList();
        parser.require(XmlPullParser.START_TAG, null, "rss");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("channel")) {
                news = readItems(parser);
            } else {
                skip(parser);
            }
        }
        return news;
    }

    private List readItems(XmlPullParser parser) throws XmlPullParserException, IOException {

        List news = new ArrayList();
        parser.require(XmlPullParser.START_TAG, null, "channel");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            Log.d("item name", name);
            // Starts by looking for the entry tag
            if (name.equals("item")) {
                news.add(readNews(parser));
            } else {
                skip(parser);
            }
        }
        return news;
    }

    private News readNews(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "item");
        News news = new News();
        String title = null;
        String description = null;
        String pubDate = null;
        String enclosure = null;
        String guid = null;
        String link = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("title")) {
                news.setTitle(readTitle(parser));
            }
            else if (name.equals("description")) {
                news.setDescription(readDescription(parser));
            }
            else if (name.equals("link")) {
                news.setLink(readLink(parser));
            }
            else if (name.equals("enclosure")) {
                news.setEnclosure(readEnclosure(parser));
            }
            else if (name.equals("guid")) {
                news.setGuid(readGuid(parser));
            }
            else if (name.equals("pubDate")) {
                news.setPubDate(readPubDate(parser));
            } else {
                skip(parser);
            }
        }
        // adding the new to the DB
        Log.d("adding ?", String.valueOf(getNewsFromDB(news)));
        if(getNewsFromDB(news) == 0) {
            addNewsToDB(news);
        }

        return news;
    }

    private int getNewsFromDB(News news) {
        ContentResolver cr = context.getContentResolver();
        String []allColumns = {
                NewsDBOpenHelper.COLUMN_ID,             // 0
                NewsDBOpenHelper.COLUMN_GUID,           // 1

        };
        Cursor cursor = cr.query(NewsContentProvider.CONTENT_URI, allColumns, NewsDBOpenHelper.COLUMN_GUID + "=?", new String[] { news.getGuid() }, null);
        int lenght = cursor.getCount();
        cursor.close();
        return lenght;
    }

    // Processes title tags in the feed.
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "title");
        return title;
    }

    // Processes link tags in the feed.
    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "link");
        String link = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "link");
        return link;
    }

    // Processes description tags in the feed.
    private String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "description");
        String description = readText(parser);
        description = description.substring(0, description.indexOf("<"));
        parser.require(XmlPullParser.END_TAG, null, "description");
        return description;
    }

    // Processes guid tags in the feed.
    private String readGuid(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "guid");
        String guid = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "guid");
        // Extract unique id from unique link
        // e.g. : http://www.lemonde.fr/tiny/4525885/
        guid = guid.replaceAll("http://www.lemonde.fr/tiny/", "");
        guid = guid.replaceAll("/", "");
        return guid;
    }

    // Processes publication date tags in the feed.
    private String readPubDate(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "pubDate");
        String pubDate = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "pubDate");
        pubDate = pubDate.replaceAll(" GMT", "");
        return pubDate;
    }

    // Processes enclosure date tags in the feed.
    private String readEnclosure(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "enclosure");
        String enclosure = parser.getAttributeValue(null, "url");
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, null, "enclosure");
        return enclosure;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private void addNewsToDB(News news) {
        // tools variables
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        // put values
        values.put(NewsDBOpenHelper.COLUMN_TITLE, news.getTitle());
        values.put(NewsDBOpenHelper.COLUMN_LINK, news.getLink());
        values.put(NewsDBOpenHelper.COLUMN_DESCRIPTION, news.getDescription());
        values.put(NewsDBOpenHelper.COLUMN_ENCLOSURE, news.getEnclosure());
        values.put(NewsDBOpenHelper.COLUMN_PUBDATE, news.getPubDate());
        values.put(NewsDBOpenHelper.COLUMN_GUID, news.getGuid());
        values.put(NewsDBOpenHelper.COLUMN_STOREDDATE, news.getStoredDate());

        // adding news to database
        cr.insert(NewsContentProvider.CONTENT_URI, values);
    }
}
