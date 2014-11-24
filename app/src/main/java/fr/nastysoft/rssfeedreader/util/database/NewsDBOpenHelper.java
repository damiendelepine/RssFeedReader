package fr.nastysoft.rssfeedreader.util.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Damien on 21/11/2014.
 */
public class NewsDBOpenHelper extends SQLiteOpenHelper {

    private static volatile NewsDBOpenHelper instance = null;

    // database setup
    private final static String DATABASE_NAME = "newsDataBase.db" ;	// base
    public final static String DATABASE_TABLE = "News" ;					// table
    private final static int DATABASE_VERSION = 1 ;						// version de la base
    private static final String DATABASE_PATH = "/data/data/fr.nastysoft.rssfeedreader/databases/" + DATABASE_NAME;

    // database fields
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_LINK= "link";
    public static final String COLUMN_ENCLOSURE= "enclosure";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_PUBDATE = "pubDate";
    public static final String COLUMN_GUID = "guid";
    public static final String COLUMN_STOREDDATE = "storedDate";

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table if not exists "
            + DATABASE_TABLE + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_TITLE + " text not null, " +
            COLUMN_LINK + " text not null, " +
            COLUMN_ENCLOSURE + " text not null, " +
            COLUMN_DESCRIPTION + " text not null, " +
            COLUMN_PUBDATE + " text not null, " +
            COLUMN_GUID + " text not null, " +
            COLUMN_STOREDDATE + " text not null);";

    // general variables
    private static final String LOGTAG_DEBUG = "DATABASE SETUP";

    private NewsDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        Log.i(LOGTAG_DEBUG, "Database created !");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(NewsDBOpenHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }

    public final static NewsDBOpenHelper getNewsDBOpenHelper(Context context) {
        if (NewsDBOpenHelper.instance == null) {
            // Le mot-clé synchronized sur ce bloc empêche toute instanciation multiple même par différents "threads".
            synchronized(NewsDBOpenHelper.class) {
                if (NewsDBOpenHelper.instance == null) {
                    NewsDBOpenHelper.instance = new NewsDBOpenHelper(context);
                }
            }
        }
        return NewsDBOpenHelper.instance;
    }

    public static void backupDatabase() throws IOException {
        //Open your local db as the input stream
        String inFileName = DATABASE_PATH;
        File dbFile = new File(inFileName);
        FileInputStream fis = new FileInputStream(dbFile);

        String outFileName = Environment.getExternalStorageDirectory() + "/" + DATABASE_NAME + ".sqlite";
        //Open the empty db as the output stream
        OutputStream output = new FileOutputStream(outFileName);
        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer))>0){
            output.write(buffer, 0, length);
        }
        //Close the streams
        output.flush();
        output.close();
        fis.close();
    }
}