package my.drivebit.events

import kotlin.js.Json

@JsName("BroadcastChannel")
external class BroadcastChannel(
    name: String,
) {
    fun postMessage(message: Any?)

    var onmessage: ((event: dynamic) -> Unit)?

    fun close()
}

object BroadcastChannelEventBus {
    private const val CHANNEL_NAME = "drivebit_events"
    private val channel: BroadcastChannel? =
        if (js("typeof BroadcastChannel !== 'undefined'").unsafeCast<Boolean>()) {
            BroadcastChannel(CHANNEL_NAME)
        } else {
            null
        }

    private val listeners = mutableMapOf<String, MutableList<(String, String) -> Unit>>()
    private var isInitialized = false

    fun init() {
        if (isInitialized || channel == null) return

        channel.onmessage = { event ->
            val data = event.data as? Json
            if (data != null) {
                val type = data["type"] as? String ?: ""
                val payload = data["payload"] as? String ?: ""
                notifyListeners(type, payload)
            }
        }

        isInitialized = true
    }

    fun emit(
        type: String,
        payload: String = "",
    ) {
        if (channel == null) return

        val message = js("{}").unsafeCast<Json>()
        message["type"] = type
        message["payload"] = payload

        channel.postMessage(message)
        notifyListeners(type, payload)
    }

    fun on(
        eventType: String? = null,
        handler: (String, String) -> Unit,
    ): () -> Unit {
        val key = eventType ?: "*"
        listeners.getOrPut(key) { mutableListOf() }.add(handler)
        return { listeners[key]?.remove(handler) }
    }

    fun once(
        eventType: String? = null,
        handler: (String, String) -> Unit,
    ) {
        var unsubscribe: (() -> Unit)? = null
        unsubscribe =
            on(eventType) { type, payload ->
                handler(type, payload)
                unsubscribe?.invoke()
            }
    }

    private fun notifyListeners(
        type: String,
        payload: String,
    ) {
        listeners["*"]?.forEach { it(type, payload) }
        listeners[type]?.forEach { it(type, payload) }
    }

    fun isSupported(): Boolean = channel != null

    fun clearListeners() {
        listeners.clear()
    }

    fun close() {
        channel?.close()
        listeners.clear()
        isInitialized = false
    }
}
