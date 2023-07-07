package com.example.todoapp.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model for Yandex authorization network communication .
 */
@Serializable
data class YaLoginDtoModel(

    @SerialName("id")
    val id: String,
    @SerialName("login")
    val login: String,
    @SerialName("client_id")
    val clientId: String,
    @SerialName("display_name")
    val displayName: String,
    @SerialName("real_name")
    val realName: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    @SerialName("sex")
    val sex: String,
    @SerialName("psuid")
    val psuid: String
)
