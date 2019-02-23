package cat.uab.falldetectionapp.com.falldetection;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class sqlite_IO extends SQLiteOpenHelper{
        private email_const email_const;

        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "DB_USER_EMAILS.DB";
        private static final String TABLE_NAME = "emails";
        private static final String KEY_ID = "id";
        private static final String KEY_EMAIL = "email_name";
        private static final String[] COLUMNS = { KEY_ID, KEY_EMAIL};

        public sqlite_IO(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String CREATE_QUERY ="CREATE TABLE emails" + "(id " + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, email_name" + " TEXT);";
            db.execSQL(CREATE_QUERY);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            this.onCreate(db);
        }


        public void add_email(email_const email) {
            SQLiteDatabase db = this.getWritableDatabase();
            System.out.println(db);
            ContentValues values = new ContentValues();
            values.put(KEY_EMAIL, email.getEmail());
            db.insert(TABLE_NAME,null, values);
            db.close();
        }

        public int update_email(email_const email) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_EMAIL, email.getEmail());

            int i = db.update(TABLE_NAME,
                    values, // column/value
                    "id = ?",
                    new String[] { String.valueOf(email.getEmail_id()) });
            db.close();

            return i;
        }

        public String checkValues(){
            String exists = "";
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_NAME, // a. table
                    COLUMNS, // b. column names
                    " id = ?", // c. selections
                    new String[] { String.valueOf(1) }, // d. selections args
                    null, // e. group by
                    null, // f. having
                    null, // g. order by
                    null); // h. limit
            if (cursor.moveToFirst()){
                do{
                    String data = cursor.getString(cursor.getColumnIndex(KEY_EMAIL));
                    System.out.println(data);
                    exists = data;
                }while(cursor.moveToNext());
            }
            return exists;
        }

}
