package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pin_posts")
data class PinPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val destinationLink: String,
    val boardId: String,
    val boardName: String,
    val imageUri: String, // Local image file path or Uri string
    val scheduledTime: Long? = null, // In milliseconds. Null if immediate or draft
    val status: String = "DRAFT", // "DRAFT", "SCHEDULED", "POSTED", "FAILED"
    val postedTime: Long? = null,
    val failureReason: String? = null,
    val pinId: String? = null, // ID returned by Pinterest API
    val createdAt: Long = System.currentTimeMillis()
)
