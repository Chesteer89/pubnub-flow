package com.pubnub.util.data

import com.pubnub.api.PubNubException
import com.pubnub.api.models.consumer.PNStatus

/**
 * Exception class with extra [PNStatus] data
 */
class PNException(val exception: PubNubException, val status: PNStatus) : Exception()