package com.farego.app.db.entity;
// ============================================================
// FILE: app/src/main/java/com/farego/app/db/entity/User.java
// PURPOSE: Room entity for local user accounts.
//          Passwords are stored as SHA-256 hashes — never plaintext.
// ============================================================

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName  = "users",
        indices    = { @Index(value = "username", unique = true) }
)
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "password_hash")
    public String passwordHash;    // SHA-256 hex string

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "is_admin")
    public boolean isAdmin;        // true for admin accounts

    @ColumnInfo(name = "created_at")
    public long createdAt;         // Unix epoch ms

    public User(String username, String passwordHash,
                String email, boolean isAdmin, long createdAt) {
        this.username     = username;
        this.passwordHash = passwordHash;
        this.email        = email;
        this.isAdmin      = isAdmin;
        this.createdAt    = createdAt;
    }
}