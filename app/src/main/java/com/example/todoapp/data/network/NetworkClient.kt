package com.example.todoapp.data.network

import io.ktor.client.HttpClient
import io.ktor.client.features.ClientRequestException

import io.ktor.client.features.HttpResponseValidator

import io.ktor.client.features.ResponseException
import io.ktor.client.features.ServerResponseException
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.utils.EmptyContent
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

class NetworkClient {
    val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json {
                prettyPrint = false
                isLenient = true
                expectSuccess = false
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }

        HttpResponseValidator {
            validateResponse { response: HttpResponse ->
                when (response.status.value) {
                    400 -> throw ClientRequestException(response, "ClientRequestException")
                    401 -> throw AuthenticationException(response, "AuthenticationException")
                    404 -> throw ElementNotFoundException(response, "ElementNotFoundException")
                    in 500..599 -> throw ServerResponseException(response, "ServerResponseException")
                }
            }
            handleResponseException { ex: Throwable ->
                throw ex
            }
        }
    }


    suspend inline fun <reified Response> request(typeRequest: HttpMethod, url: String, bodyJson: Any?, lastKnownRevision: String): Response {
        return client.request<Response> {
            url(HttpResource.BASE_URL + url)
            method = typeRequest
            headers {
                append("Authorization", "Bearer ${HttpResource.authToken}")
                append("X-Last-Known-Revision", lastKnownRevision)
            }
            contentType(ContentType.Application.Json)
            body = bodyJson ?: EmptyContent
        }
    }


    suspend inline fun <reified T> postRequest(url: String, bodyJson: Any?, lastKnownRevision: String): T? {
        return request<T>(HttpMethod.Post, url, bodyJson, lastKnownRevision)
    }

    suspend inline fun <reified T> putRequest(url: String, bodyJson: Any?, lastKnownRevision: String): T? {
        return request<T>(HttpMethod.Put, url, bodyJson, lastKnownRevision)
    }

    suspend inline fun <reified T> deleteRequest(url: String, bodyJson: Any? = null, lastKnownRevision: String): T? {
        return request<T>(HttpMethod.Delete, url, bodyJson, lastKnownRevision)
    }

    suspend inline fun <reified T> patchRequest(url: String, bodyJson: Any? = null, lastKnownRevision: String): T? {
        return request<T>(HttpMethod.Patch, url, bodyJson, lastKnownRevision)
    }
}

class AuthenticationException(response: HttpResponse, message: String) : ResponseException(response, message) {
    constructor(response: HttpResponse) : this(response, "no response text provided")

    override val message: String = "Client request(${response.call.request.url}) " +
            "invalid: ${response.status}. Text: \"$message\""
}

class ElementNotFoundException(response: HttpResponse, message: String) : ResponseException(response, message) {
    constructor(response: HttpResponse) : this(response, "no response text provided")

    override val message: String = "Client request(${response.call.request.url}) " +
            "invalid: ${response.status}. Text: \"$message\""
}




