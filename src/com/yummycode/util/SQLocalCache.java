package com.yummycode.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLocalCache extends SQLiteOpenHelper
{
    private static final String DB_NAME = "socialme_local_cache";
    private static final int DB_VERSION = 1;
    
    public SQLocalCache(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
