package com.pubnub.util.flow

import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.BasePubSubResult
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult
import com.pubnub.api.models.consumer.pubsub.objects.PNObjectEventResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach

fun PubNub.subscribeBy(
        onStatus: (PNStatus) -> Unit = {},
        onMessage: (PNMessageResult) -> Unit = {},
        onMessageAction: (PNMessageActionResult) -> Unit = {},
        onObjects: (PNObjectEventResult) -> Unit = {},
        onPresence: (PNPresenceEventResult) -> Unit = {},
        onSignal: (PNSignalResult) -> Unit = {}
){
    val callback: SubscribeCallback = object : SubscribeCallback(){
        override fun status(pubnub: PubNub, pnStatus: PNStatus) {
            onStatus.invoke(pnStatus)
        }

        override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
            onMessage.invoke(pnMessageResult)
        }

        override fun messageAction(pubnub: PubNub, pnMessageActionResult: PNMessageActionResult) {
            onMessageAction.invoke(pnMessageActionResult)
        }

        override fun objects(pubnub: PubNub, objectEvent: PNObjectEventResult) {
            onObjects.invoke(objectEvent)
        }

        override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
            onPresence.invoke(pnPresenceEventResult)
        }

        override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {
            onSignal.invoke(pnSignalResult)
        }
    }
    this.addListener(callback)
}

val PubNub.status
get(): Flow<PNStatus> =
        callbackFlow {
            val callback: SubscribeCallback = object : SubscribeCallback(){
                override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                    sendBlocking(pnStatus)
                }
            }
            this@status.addListener(callback)

            awaitClose { this@status.removeListener(callback) }
        }

val PubNub.event
get(): Flow<BasePubSubResult> =
        callbackFlow {
            val callback: SubscribeCallback = object : SubscribeCallback(){
                override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                    // just ignore
                }

                override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                    sendBlocking(pnMessageResult)
                }

                override fun messageAction(pubnub: PubNub, pnMessageActionResult: PNMessageActionResult) {
                    sendBlocking(pnMessageActionResult)
                }

                override fun objects(pubnub: PubNub, objectEvent: PNObjectEventResult) {
                    sendBlocking(objectEvent)
                }

                override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                    sendBlocking(pnPresenceEventResult)
                }

                override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {
                    sendBlocking(pnSignalResult)
                }
            }
            this@event.addListener(callback)

            awaitClose { this@event.removeListener(callback) }
        }

// todo: cannot use BasePubSubResult cause objects doesn't extends it...
fun Flow<Any>.onMessage(action: (PNMessageResult) -> Unit): Flow<Any> =
        onEach { if(it is PNMessageResult) action.invoke(it) }

fun Flow<Any>.onMessageAction(action: (PNMessageActionResult) -> Unit): Flow<Any> =
        onEach { if(it is PNMessageActionResult) action.invoke(it) }

fun Flow<Any>.onObject(action: (PNObjectEventResult) -> Unit): Flow<Any> =
        onEach { if(it is PNObjectEventResult) action.invoke(it) }

fun Flow<Any>.onPresence(action: (PNPresenceEventResult) -> Unit): Flow<Any> =
        onEach { if(it is PNPresenceEventResult) action.invoke(it) }

fun Flow<Any>.onSignal(action: (PNSignalResult) -> Unit): Flow<Any> =
        onEach { if(it is PNSignalResult) action.invoke(it) }


@Suppress("UNCHECKED_CAST")
val Flow<Any>.message: Flow<PNMessageResult>
get() = filter { it is PNMessageResult } as Flow<PNMessageResult>

@Suppress("UNCHECKED_CAST")
val Flow<Any>.messageAction: Flow<PNMessageActionResult>
get() = filter { it is PNMessageActionResult } as Flow<PNMessageActionResult>

@Suppress("UNCHECKED_CAST")
val Flow<Any>.`object`: Flow<PNObjectEventResult>
get() = filter { it is PNObjectEventResult } as Flow<PNObjectEventResult>

@Suppress("UNCHECKED_CAST")
val Flow<Any>.presence: Flow<PNPresenceEventResult>
get() = filter { it is PNPresenceEventResult } as Flow<PNPresenceEventResult>

@Suppress("UNCHECKED_CAST")
val Flow<Any>.signal: Flow<PNSignalResult>
get() = filter { it is PNSignalResult } as Flow<PNSignalResult>