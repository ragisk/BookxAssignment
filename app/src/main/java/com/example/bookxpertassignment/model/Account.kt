package com.example.bookxpertassignment.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    val ActName: String,
    @PrimaryKey
    val actid: Int,
    val alternateName: String? = null
)

