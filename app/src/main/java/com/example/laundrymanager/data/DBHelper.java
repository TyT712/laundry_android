package com.example.laundrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.laundrymanager.model.Role;
import com.example.laundrymanager.util.PasswordHasher;

/**
 * Quản lý schema SQLite và dữ liệu mẫu. Việc truy vấn nằm ở các lớp {@code *Repository}.
 *
 * <p>Khi nâng {@link #VERSION}, dữ liệu cũ sẽ bị xóa và tạo lại (phù hợp giai đoạn phát triển).
 */
public final class DBHelper extends SQLiteOpenHelper {
    private static final String NAME = "laundry.db";
    private static final int VERSION = 4;
    private static final String DEFAULT_PASSWORD = "123456";

    private static DBHelper instance;

    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DBHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password TEXT NOT NULL, "
                + "fullname TEXT NOT NULL, "
                + "phone TEXT NOT NULL DEFAULT '', "
                + "role TEXT NOT NULL)");
        db.execSQL("CREATE TABLE customers("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "phone TEXT NOT NULL, "
                + "address TEXT NOT NULL DEFAULT '')");
        db.execSQL("CREATE TABLE services("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL UNIQUE, "
                + "price REAL NOT NULL)");
        db.execSQL("CREATE TABLE orders("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "customer_id INTEGER NOT NULL REFERENCES customers(id) ON DELETE RESTRICT, "
                + "service_id INTEGER NOT NULL REFERENCES services(id) ON DELETE RESTRICT, "
                + "weight REAL NOT NULL, "
                + "total REAL NOT NULL, "
                + "note TEXT NOT NULL DEFAULT '', "
                + "status TEXT NOT NULL, "
                + "payment_status TEXT NOT NULL, "
                + "created_at INTEGER NOT NULL)");
        seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS orders");
        db.execSQL("DROP TABLE IF EXISTS services");
        db.execSQL("DROP TABLE IF EXISTS customers");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    private static void seed(SQLiteDatabase db) {
        insertUser(db, "admin", "Quản Lý", "0901234567", Role.MANAGER);
        insertUser(db, "nhanvien", "nhân viên", "0909888777", Role.STAFF);

        insertService(db, "Giặt thường", 15000);
        insertService(db, "Giặt sấy", 20000);
        insertService(db, "Giặt hấp", 30000);
        insertService(db, "Giặt chăn màn", 25000);

        insertCustomer(db, "Lê Hoàng Nam", "0912345678", "123 Lê Lợi, Quận 1");
        insertCustomer(db, "Phạm Minh Anh", "0987654321", "45 Nguyễn Huệ, Quận 1");
    }

    private static void insertUser(SQLiteDatabase db, String username, String fullname, String phone, Role role) {
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", PasswordHasher.hash(DEFAULT_PASSWORD));
        values.put("fullname", fullname);
        values.put("phone", phone);
        values.put("role", role.name());
        db.insert("users", null, values);
    }

    private static void insertService(SQLiteDatabase db, String name, double price) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("price", price);
        db.insert("services", null, values);
    }

    private static void insertCustomer(SQLiteDatabase db, String name, String phone, String address) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("phone", phone);
        values.put("address", address);
        db.insert("customers", null, values);
    }
}

