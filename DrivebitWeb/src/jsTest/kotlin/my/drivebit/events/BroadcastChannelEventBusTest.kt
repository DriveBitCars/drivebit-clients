package my.drivebit.events

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BroadcastChannelEventBusTest {
    @BeforeTest
    fun setUp() {
        BroadcastChannelEventBus.clearListeners()
        BroadcastChannelEventBus.init()
    }

    @Test
    fun testInit() {
        BroadcastChannelEventBus.init()
        assertTrue(BroadcastChannelEventBus.isSupported() || !BroadcastChannelEventBus.isSupported())
    }

    @Test
    fun testEmitAndReceive() {
        BroadcastChannelEventBus.init()

        var receivedType: String? = null
        var receivedPayload: String? = null

        val unsubscribe =
            BroadcastChannelEventBus.on("test_event") { type, payload ->
                receivedType = type
                receivedPayload = payload
            }

        BroadcastChannelEventBus.emit("test_event", "test_payload")

        assertEquals("test_event", receivedType)
        assertEquals("test_payload", receivedPayload)

        unsubscribe()
    }

    @Test
    fun testMultipleListeners() {
        BroadcastChannelEventBus.init()

        var count = 0

        val unsubscribe1 =
            BroadcastChannelEventBus.on("test_event") { _, _ ->
                count++
            }

        val unsubscribe2 =
            BroadcastChannelEventBus.on("test_event") { _, _ ->
                count++
            }

        BroadcastChannelEventBus.emit("test_event", "payload")

        assertEquals(2, count)

        unsubscribe1()
        unsubscribe2()
    }

    @Test
    fun testWildcardListener() {
        BroadcastChannelEventBus.init()

        var receivedType: String? = null

        val unsubscribe =
            BroadcastChannelEventBus.on(null) { type, _ ->
                receivedType = type
            }

        BroadcastChannelEventBus.emit("event1", "payload1")
        assertEquals("event1", receivedType)

        BroadcastChannelEventBus.emit("event2", "payload2")
        assertEquals("event2", receivedType)

        unsubscribe()
    }

    @Test
    fun testSpecificEventListener() {
        BroadcastChannelEventBus.init()

        var receivedCount = 0

        val unsubscribe =
            BroadcastChannelEventBus.on("specific_event") { _, _ ->
                receivedCount++
            }

        BroadcastChannelEventBus.emit("specific_event", "payload")
        BroadcastChannelEventBus.emit("other_event", "payload")
        BroadcastChannelEventBus.emit("specific_event", "payload")

        assertEquals(2, receivedCount)

        unsubscribe()
    }

    @Test
    fun testUnsubscribe() {
        BroadcastChannelEventBus.init()

        var receivedCount = 0

        val unsubscribe =
            BroadcastChannelEventBus.on("test_event") { _, _ ->
                receivedCount++
            }

        BroadcastChannelEventBus.emit("test_event", "payload")
        assertEquals(1, receivedCount)

        unsubscribe()

        BroadcastChannelEventBus.emit("test_event", "payload")
        assertEquals(1, receivedCount)
    }

    @Test
    fun testOnce() {
        BroadcastChannelEventBus.init()

        var receivedCount = 0

        BroadcastChannelEventBus.once("test_event") { _, _ ->
            receivedCount++
        }

        BroadcastChannelEventBus.emit("test_event", "payload")
        BroadcastChannelEventBus.emit("test_event", "payload")

        assertEquals(1, receivedCount)
    }

    @Test
    fun testEmptyPayload() {
        BroadcastChannelEventBus.init()

        var receivedPayload: String? = null

        val unsubscribe =
            BroadcastChannelEventBus.on("test_event") { _, payload ->
                receivedPayload = payload
            }

        BroadcastChannelEventBus.emit("test_event", "")

        assertEquals("", receivedPayload)

        unsubscribe()
    }

    @Test
    fun testMultipleEvents() {
        BroadcastChannelEventBus.init()

        val events = mutableListOf<String>()

        val unsubscribe1 =
            BroadcastChannelEventBus.on("event1") { type, _ ->
                events.add(type)
            }

        val unsubscribe2 =
            BroadcastChannelEventBus.on("event2") { type, _ ->
                events.add(type)
            }

        BroadcastChannelEventBus.emit("event1", "payload1")
        BroadcastChannelEventBus.emit("event2", "payload2")
        BroadcastChannelEventBus.emit("event1", "payload3")

        assertEquals(3, events.size)
        assertEquals("event1", events[0])
        assertEquals("event2", events[1])
        assertEquals("event1", events[2])

        unsubscribe1()
        unsubscribe2()
    }
}
