package dev.teamhub.firebase.functions

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.Mapper
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import java.util.concurrent.TimeUnit

actual val Firebase.functions
    get() = FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance())

actual fun Firebase.functions(region: String) =
    FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance(region))

actual fun Firebase.functions(app: FirebaseApp) =
    FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance(app.android))

actual fun Firebase.functions(app: FirebaseApp, region: String) =
    FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance(app.android, region))

actual class FirebaseFunctions internal constructor(val android: com.google.firebase.functions.FirebaseFunctions) {
    actual fun httpsCallable(name: String, timeout: Long?) =
        HttpsCallableReference(android.getHttpsCallable(name).apply { timeout?.let { setTimeout(it, TimeUnit.MILLISECONDS) } })
}

actual class HttpsCallableReference internal constructor(val android: com.google.firebase.functions.HttpsCallableReference) {
    actual suspend fun call() = HttpsCallableResult(android.call().await())

    actual suspend inline fun <reified T> call(data: T) =
        HttpsCallableResult(android.call(Mapper.map(data as Any)).await())

    actual suspend inline fun <reified T> call(data: T, strategy: SerializationStrategy<T>) =
        HttpsCallableResult(android.call(Mapper.map(strategy, data)).await())
}

actual class HttpsCallableResult constructor(val android: com.google.firebase.functions.HttpsCallableResult) {

    actual inline fun <reified T> data() =
        Mapper.decode<T>(android.data)

    actual inline fun <reified T> data(strategy: DeserializationStrategy<T>) =
        Mapper.decode(strategy, android.data)
}

actual typealias FirebaseFunctionsException = com.google.firebase.functions.FirebaseFunctionsException