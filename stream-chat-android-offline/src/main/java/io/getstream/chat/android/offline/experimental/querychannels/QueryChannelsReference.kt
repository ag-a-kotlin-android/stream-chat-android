package io.getstream.chat.android.offline.experimental.querychannels

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.experimental.extensions.state
import io.getstream.chat.android.offline.experimental.plugin.QueryReference
import io.getstream.chat.android.offline.experimental.querychannels.state.QueryChannelsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@InternalStreamChatApi
@ExperimentalStreamChatApi
public class QueryChannelsReference internal constructor(
    public val request: QueryChannelsRequest,
    private val chatClient: ChatClient
) : QueryReference<List<Channel>, QueryChannelsState> {
    override fun get(): Call<List<Channel>> {
        return chatClient.queryChannels(request)
    }

    override fun asState(scope: CoroutineScope): QueryChannelsState {
        scope.launch {
            get().await()
        }

        return chatClient.state.queryChannels(request.filter, request.querySort)
    }
}
