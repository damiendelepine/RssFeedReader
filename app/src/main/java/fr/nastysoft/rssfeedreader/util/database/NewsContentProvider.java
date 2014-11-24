package fr.nastysoft.rssfeedreader.util.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.io.IOException;

/**
 * Created by Damien on 21/11/2014.
 */
public class NewsContentProvider extends ContentProvider {

    private NewsDBOpenHelper newsDBOpenHelper;
    private static final String AUTHORITY="fr.nastysoft.rssfeedreader.util.database";
    private static final String BASE_PATH = "News";
    public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+BASE_PATH);

    // Création d’un URIMatcher pour reconnaître le type d’URI
    private static final int ALLROWS= 1;
    private static final int SINGLE_ROW = 2;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, ALLROWS);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", SINGLE_ROW );
    }

    @Override
    public boolean onCreate() {
        // singleton
        newsDBOpenHelper = NewsDBOpenHelper.getNewsDBOpenHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(NewsDBOpenHelper.DATABASE_TABLE);
        switch (uriMatcher.match(uri)) {
            case ALLROWS:
                break;
            case SINGLE_ROW:
                // adding the ID to the original query
                queryBuilder.appendWhere(NewsDBOpenHelper.COLUMN_ID + "="+
                        uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = newsDBOpenHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ALLROWS:
                return "vnd.android.cursor.dir/vnd.apprts.elemental" ;
            case SINGLE_ROW:
                return "vnd.android.cursor.item/vnd.apprts.elemental" ;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase sqlDB = newsDBOpenHelper.getWritableDatabase();
        long id = 0;
        switch (uriMatcher.match(uri)) {
            case ALLROWS:
                id=sqlDB.insert(NewsDBOpenHelper.DATABASE_TABLE,
                        null, values);
                try {
                    NewsDBOpenHelper.backupDatabase();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            default:
                //throw newIllegalArgumentException("Unknown URI:" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase sqlDB = newsDBOpenHelper.getWritableDatabase() ;
        String rowId = uri.getLastPathSegment() ;
        int rowsDeleted = 0;
        switch (uriMatcher.match(uri)) {
            case ALLROWS:
                rowsDeleted=sqlDB.delete(NewsDBOpenHelper.DATABASE_TABLE, selection, selectionArgs);
                break;
            case SINGLE_ROW:
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(NewsDBOpenHelper.DATABASE_TABLE,
                            NewsDBOpenHelper.COLUMN_ID  + "=" + rowId, null);
                } else {
                    rowsDeleted = sqlDB.delete(NewsDBOpenHelper.DATABASE_TABLE,
                            NewsDBOpenHelper.COLUMN_ID + "=" + rowId
                                    + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase sqlDB = newsDBOpenHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriMatcher.match(uri)) {
            case SINGLE_ROW:
                String rowId = uri.getLastPathSegment();
                selection =  NewsDBOpenHelper.COLUMN_ID + "=" +
                        (!TextUtils.isEmpty(selection) ?" AND ("+selection+')':"");
            default :
                break;
        }
        int updateCount = sqlDB.update(NewsDBOpenHelper.DATABASE_TABLE,
                values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null); // notify
        return updateCount;
    }
}
