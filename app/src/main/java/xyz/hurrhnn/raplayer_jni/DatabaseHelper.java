package xyz.hurrhnn.raplayer_jni;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "user.db"; // 데이터베이스 명
    public static final String TABLE_NAME1 = "user";
    public static final String TABLE_NAME2 = "stats";

    // 테이블 항목
    public static final String USERID = "userid";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String INTRODUCTION = "introduction";
    public static final String IMAGE_URL = "image_url";

    public static final String ROOMID = "roomid";
    public static final String STARTTIME = "starttime";
    public static final String ENDTIME = "endtime";
    public static final String TIME = "time";

    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");




    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME1 + "(userid TEXT PRIMARY KEY, username TEXT, password TEXT, introduction TEXT, image_url TEXT)");
        db.execSQL("create table " + TABLE_NAME2 + "(roomid TEXT, STARTTIME TEXT, ENDTIME TEXT, TIME TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME1);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME2);
        onCreate(db);
    }

    public boolean userinsertData(String userid, String username, String password, String introduction, String image_url){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USERID, userid);
        contentValues.put(USERNAME,username);
        contentValues.put(PASSWORD,password);
        contentValues.put(INTRODUCTION,introduction);
        contentValues.put(IMAGE_URL,image_url);
        long result = db.insert(TABLE_NAME1, null,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public boolean statsinsertData(String roomid, String starttime, String endtime) throws ParseException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String time = "";

        contentValues.put(ROOMID,roomid);
        contentValues.put(STARTTIME,starttime);
        contentValues.put(ENDTIME,endtime);

        mDate = mFormat.parse(starttime);
        long startTime = mDate.getTime();
        mDate = mFormat.parse(endtime);
        long endTime = mDate.getTime();

        time = String.valueOf((endTime - startTime)/1000);

        contentValues.put(TIME,time);
        long result = db.insert(TABLE_NAME2, null,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor usergetData(String userid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.query(TABLE_NAME1, new String[]{USERID, USERNAME, PASSWORD, INTRODUCTION, IMAGE_URL}, "userid=?", new String[]{userid}, null, null, null, null);
        return  res;
    }

    public Cursor statsgetData(String roomid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.query(TABLE_NAME2, new String[]{ROOMID, STARTTIME, ENDTIME, TIME}, "roomid=?", new String[]{roomid}, null, null, null);
        return  res;
    }

    public Cursor statsgetallData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.query(TABLE_NAME2, new String[]{ROOMID, STARTTIME, ENDTIME, TIME}, null, null, null, null, null);
        return  res;
    }

    public Integer deleteData(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME1, "userid = ? ",new String[]{id});
    }

    public boolean userupdateData(String userid, String username, String password, String introduction, String image_url){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USERNAME,username);
        contentValues.put(PASSWORD,password);
        contentValues.put(INTRODUCTION,introduction);
        contentValues.put(IMAGE_URL,image_url);
        System.out.println(image_url);
        db.update(TABLE_NAME1,contentValues,"userid = ?", new String[] { userid });
        return true;
    }

    public boolean statsupdateData(String roomid, String starttime, String endtime) throws ParseException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String time = "";

        Cursor res = db.query(TABLE_NAME2, new String[]{ROOMID, STARTTIME, ENDTIME, TIME}, "roomid=?", new String[]{roomid}, null, null, null);
        if (res.getCount() != 0) {
            while(res.moveToNext()){
                time = res.getString(3);
            }
        }

        long btime = Long.parseLong(time);

        contentValues.put(ROOMID,roomid);
        contentValues.put(STARTTIME,starttime);
        contentValues.put(ENDTIME,endtime);

        mDate = mFormat.parse(starttime);
        long startTime = mDate.getTime();
        mDate = mFormat.parse(endtime);
        long endTime = mDate.getTime();

        time = String.valueOf(btime + ((endTime - startTime)/1000));

        contentValues.put(TIME,time);
        long result = db.update(TABLE_NAME2,contentValues,"roomid = ?", new String[] { roomid });
        if(result == -1)
            return false;
        else
            return true;
    }
}
