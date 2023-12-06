package xyz.hurrhnn.raplayer_jni;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "user.db"; // 데이터베이스 명
    public static final String TABLE_NAME = "user"; // 테이블 명

    // 테이블 항목
    public static final String COL_1 = "userid";
    public static final String COL_2 = "username";
    public static final String COL_3 = "password";
    public static final String COL_4 = "introduction";
    public static final String COL_5 = "image_url";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(userid TEXT PRIMARY KEY, username TEXT, password TEXT, introduction TEXT, image_url TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String userid, String username, String password, String introduction, String image_url){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, userid);
        contentValues.put(COL_2,username);
        contentValues.put(COL_3,password);
        contentValues.put(COL_4,introduction);
        contentValues.put(COL_5,image_url);
        long result = db.insert(TABLE_NAME, null,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getData(String userid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.query(TABLE_NAME, new String[]{COL_1, COL_2, COL_3, COL_4, COL_5}, "userid=?", new String[]{userid}, null, null, null, null);
        return  res;
    }

    //데이터베이스 항목 읽어오기 Read
    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return  res;
    }

    // 데이터베이스 삭제하기
    public Integer deleteData(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "userid = ? ",new String[]{id});
    }

    //데이터베이스 수정하기
    public boolean updateData(String userid, String username, String password, String introduction, String image_url){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,username);
        contentValues.put(COL_3,password);
        contentValues.put(COL_4,introduction);
        contentValues.put(COL_5,image_url);
        System.out.println(image_url);
        db.update(TABLE_NAME,contentValues,"userid = ?", new String[] { userid });
        return true;
    }
}
