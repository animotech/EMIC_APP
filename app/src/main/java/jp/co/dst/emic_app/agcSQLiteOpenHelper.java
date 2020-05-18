package jp.co.dst.emic_app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Mizukami Hisao on 2015/12/30.
 */
public class agcSQLiteOpenHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "agcCalSetting.db";
  // public static final String DB_NAME = Environment.getExternalStorageDirectory().getPath() + "/agcCalSetting.db";
    static final int DB_VERSION = 1;
    public static final String DB_TABLE = "agcCalTable";
    public static final String Key_NUM = "CalNum";
    public static final String Key_P2DATA[]={
            "pw2_1mm",
            "pw2_0_9mm",
            "pw2_0_8mm",
            "pw2_0_7mm",
            "pw2_0_6mm",
            "pw2_0_5mm",
            "pw2_0_4mm",
            "pw2_0_3mm",
            "pw2_0_2mm",
            "pw2_0_1mm",
    };

    public static final String Key_P1DATA[]={
            "pw1_1mm",
            "pw1_0_9mm",
            "pw1_0_8mm",
            "pw1_0_7mm",
            "pw1_0_6mm",
            "pw1_0_5mm",
            "pw1_0_4mm",
            "pw1_0_3mm",
            "pw1_0_2mm",
            "pw1_0_1mm",
    };
    public static final String Key_DATE = "CalDate";


    public agcSQLiteOpenHelper(Context c) {
        super(c, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // テーブル作成のクエリを発行
        db.execSQL(
                "create table if not exists " +
                        DB_TABLE +"( "+
                        "_id integer primary key autoincrement," +
                        Key_NUM + " text not null,"+
                        Key_P2DATA[0] + " integer,"+
                        Key_P2DATA[1] + " integer,"+
                        Key_P2DATA[2] + " integer,"+
                        Key_P2DATA[3] + " integer,"+
                        Key_P2DATA[4] + " integer,"+
                        Key_P2DATA[5] + " integer,"+
                        Key_P2DATA[6] + " integer,"+
                        Key_P2DATA[7] + " integer,"+
                        Key_P2DATA[8] + " integer,"+
                        Key_P2DATA[9] + " integer,"+
                        Key_P1DATA[0] + " integer,"+
                        Key_P1DATA[1] + " integer,"+
                        Key_P1DATA[2] + " integer,"+
                        Key_P1DATA[3] + " integer,"+
                        Key_P1DATA[4] + " integer,"+
                        Key_P1DATA[5] + " integer,"+
                        Key_P1DATA[6] + " integer,"+
                        Key_P1DATA[7] + " integer,"+
                        Key_P1DATA[8] + " integer,"+
                        Key_P1DATA[9] + " integer,"+
                        Key_DATE + " text);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // テーブルの破棄と再作成
        db.execSQL("drop table if exists "+
                DB_TABLE);
        onCreate(db);
    }

}
