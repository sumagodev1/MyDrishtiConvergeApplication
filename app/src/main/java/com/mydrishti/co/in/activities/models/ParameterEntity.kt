package com.mydrishti.co.`in`.activities.models

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "parameters")
data class ParameterEntity(
    val parameterDisplayName: String,
    @PrimaryKey val parameterId: Int,
    val parameterName: String,
    val uomDisplayName: String,
    val deviceId: Int
)