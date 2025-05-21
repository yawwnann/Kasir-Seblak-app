package com.example.seblak.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import com.example.seblak.model.Menu

open class MenuDao(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun tambahMenu(menu: Menu): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_MENU_NAMA, menu.nama)
            put(DatabaseHelper.COLUMN_MENU_HARGA, menu.harga)
            put(DatabaseHelper.COLUMN_MENU_DESKRIPSI, menu.deskripsi)
            put(DatabaseHelper.COLUMN_MENU_IMAGE_URI, menu.imageUri) // Tambahkan imageUri
        }
        val id = db.insert(DatabaseHelper.TABLE_MENU, null, values)
        // db.close()
        return id
    }

    @SuppressLint("Range")
    open fun getAllMenu(): List<Menu> {
        val menuList = mutableListOf<Menu>()
        val db = dbHelper.readableDatabase
        db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_MENU} ORDER BY ${DatabaseHelper.COLUMN_MENU_NAMA} ASC", null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    menuList.add(
                        Menu(
                            id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MENU_ID)),
                            nama = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MENU_NAMA)),
                            harga = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MENU_HARGA)),
                            deskripsi = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MENU_DESKRIPSI)),
                            imageUri = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MENU_IMAGE_URI)) // Ambil imageUri
                        )
                    )
                } while (cursor.moveToNext())
            }
        }
        // db.close()
        return menuList
    }

    @SuppressLint("Range")
    fun getMenuById(menuId: Long): Menu? {
        val db = dbHelper.readableDatabase
        var menu: Menu? = null
        db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_MENU} WHERE ${DatabaseHelper.COLUMN_MENU_ID} = ?", arrayOf(menuId.toString()))?.use { cursor ->
            if (cursor.moveToFirst()) {
                menu = Menu(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MENU_ID)),
                    nama = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MENU_NAMA)),
                    harga = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MENU_HARGA)),
                    deskripsi = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MENU_DESKRIPSI)),
                    imageUri = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MENU_IMAGE_URI)) // Ambil imageUri
                )
            }
        }
        // db.close()
        return menu
    }


    fun updateMenu(menu: Menu): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_MENU_NAMA, menu.nama)
            put(DatabaseHelper.COLUMN_MENU_HARGA, menu.harga)
            put(DatabaseHelper.COLUMN_MENU_DESKRIPSI, menu.deskripsi)
            put(DatabaseHelper.COLUMN_MENU_IMAGE_URI, menu.imageUri) // Update imageUri
        }
        val rowsAffected = db.update(
            DatabaseHelper.TABLE_MENU,
            values,
            "${DatabaseHelper.COLUMN_MENU_ID} = ?",
            arrayOf(menu.id.toString())
        )
        // db.close()
        return rowsAffected
    }

    fun deleteMenu(menuId: Long): Int {
        val db = dbHelper.writableDatabase
        val rowsAffected = db.delete(
            DatabaseHelper.TABLE_MENU,
            "${DatabaseHelper.COLUMN_MENU_ID} = ?",
            arrayOf(menuId.toString())
        )
        // db.close()
        return rowsAffected
    }
}