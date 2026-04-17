package com.farego.app.db.dao;
// ============================================================
// FILE: app/src/main/java/com/farego/app/db/dao/UserDao.java
// PURPOSE: Room DAO for the users table.
// ============================================================

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.farego.app.db.entity.User;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertUser(User user);

    /** Used at login — compare against SHA-256 hash */
    @Query("SELECT * FROM users WHERE username = :username AND password_hash = :hash LIMIT 1")
    User findByCredentials(String username, String hash);

    /** Null-safety check at splash — if stored userId no longer exists, log out */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User findById(int userId);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);
}