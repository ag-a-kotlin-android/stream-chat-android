package io.getstream.chat.android.compose.ui.messages.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResultType
import io.getstream.chat.android.compose.state.messages.MessagesState
import io.getstream.chat.android.compose.state.messages.list.GiphyAction
import io.getstream.chat.android.compose.state.messages.list.MessageListItemState
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.rememberMessageListState
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel

/**
 * Default MessageList component, that relies on [MessageListViewModel] to connect all the data
 * handling operations. It also delegates events to the ViewModel to handle, like long item
 * clicks and pagination.
 *
 * @param viewModel The ViewModel that stores all the data and business logic required to show a
 * list of messages. The user has to provide one in this case, as we require the channelId to start
 * the operations.
 * @param modifier Modifier for styling.
 * @param lazyListState State of the lazy list that represents the list of messages. Useful for controlling the scroll state.
 * @param onThreadClick Handler when the user taps on the message, while there's a thread going.
 * @param onLongItemClick Handler for when the user long taps on a message and selects it.
 * @param onReactionsClick Handler when the user taps on message reactions and selects them.
 * @param onMessagesStartReached Handler for pagination.
 * @param onLastVisibleMessageChanged Handler that notifies us when the user scrolls and the last visible message changes.
 * @param onScrollToBottom Handler when the user reaches the bottom.
 * @param onGiphyActionClick Handler when the user clicks on a giphy action such as shuffle, send or cancel.
 * @param onImagePreviewResult Handler when the user selects an option in the Image Preview screen.
 * @param loadingContent Composable that represents the loading content, when we're loading the initial data.
 * @param emptyContent Composable that represents the empty content if there are no messages.
 * @param helperContent Composable that, by default, represents the helper content featuring scrolling behavior based on the list state.
 * @param loadingMoreContent Composable that represents the loading more content, when we're loading the next page.
 * @param itemContent Composable that represents each item in a list. By default, we provide
 * the [MessageContainer] which sets up different message types. Users can override this to provide fully custom UI and behavior.
 */
@Composable
public fun MessageList(
    viewModel: MessageListViewModel,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberMessageListState(parentMessageId = viewModel.currentMessagesState.parentMessageId),
    onThreadClick: (Message) -> Unit = { viewModel.openMessageThread(it) },
    onLongItemClick: (Message) -> Unit = { viewModel.selectMessage(it) },
    onReactionsClick: (Message) -> Unit = { viewModel.selectReactions(it) },
    onMessagesStartReached: () -> Unit = { viewModel.loadMore() },
    onLastVisibleMessageChanged: (Message) -> Unit = { viewModel.updateLastSeenMessage(it) },
    onScrollToBottom: () -> Unit = { viewModel.clearNewMessageState() },
    onGiphyActionClick: (GiphyAction) -> Unit = { viewModel.performGiphyAction(it) },
    onImagePreviewResult: (ImagePreviewResult?) -> Unit = {
        if (it?.resultType == ImagePreviewResultType.SHOW_IN_CHAT) {
            viewModel.focusMessage(it.messageId)
        }
    },
    loadingContent: @Composable () -> Unit = { DefaultMessageListLoadingIndicator(modifier) },
    emptyContent: @Composable () -> Unit = { DefaultMessageListEmptyContent(modifier) },
    helperContent: @Composable BoxScope.() -> Unit = {
        DefaultMessagesHelperContent(viewModel.currentMessagesState, lazyListState)
    },
    loadingMoreContent: @Composable () -> Unit = { DefaultMessagesLoadingMoreIndicator() },
    itemContent: @Composable (MessageListItemState) -> Unit = { messageListItem ->
        DefaultMessageContainer(
            messageListItem = messageListItem,
            onImagePreviewResult = onImagePreviewResult,
            onThreadClick = onThreadClick,
            onLongItemClick = onLongItemClick,
            onReactionsClick = onReactionsClick,
            onGiphyActionClick = onGiphyActionClick
        )
    },
) {
    MessageList(
        modifier = modifier,
        currentState = viewModel.currentMessagesState,
        lazyListState = lazyListState,
        onMessagesStartReached = onMessagesStartReached,
        onLastVisibleMessageChanged = onLastVisibleMessageChanged,
        onLongItemClick = onLongItemClick,
        onReactionsClick = onReactionsClick,
        onScrolledToBottom = onScrollToBottom,
        onImagePreviewResult = onImagePreviewResult,
        itemContent = itemContent,
        helperContent = helperContent,
        loadingMoreContent = loadingMoreContent,
        loadingContent = loadingContent,
        emptyContent = emptyContent
    )
}

/**
 * The default message container item.
 *
 * @param messageListItem The state of the message list item.
 * @param onImagePreviewResult Handler when the user receives a result from the Image Preview.
 * @param onThreadClick Handler when the user taps on a thread within a message item.
 * @param onLongItemClick Handler when the user long taps on an item.
 * @param onReactionsClick Handler when the user taps on message reactions.
 * @param onGiphyActionClick Handler when the user taps on Giphy message actions.
 */
@Composable
internal fun DefaultMessageContainer(
    messageListItem: MessageListItemState,
    onImagePreviewResult: (ImagePreviewResult?) -> Unit,
    onThreadClick: (Message) -> Unit,
    onLongItemClick: (Message) -> Unit,
    onReactionsClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit,
) {
    MessageContainer(
        messageListItem = messageListItem,
        onLongItemClick = onLongItemClick,
        onReactionsClick = onReactionsClick,
        onThreadClick = onThreadClick,
        onGiphyActionClick = onGiphyActionClick,
        onImagePreviewResult = onImagePreviewResult
    )
}

/**
 * The default message list loading indicator.
 *
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultMessageListLoadingIndicator(modifier: Modifier) {
    LoadingIndicator(modifier)
}

/**
 * The default empty placeholder that is displayed when there are no messages in the channel.
 *
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultMessageListEmptyContent(modifier: Modifier) {
    Box(
        modifier = modifier.background(color = ChatTheme.colors.appBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.stream_compose_message_list_empty_messages),
            style = ChatTheme.typography.body,
            color = ChatTheme.colors.textLowEmphasis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Clean representation of the MessageList that is decoupled from ViewModels. This components allows
 * users to connect the UI to their own data providers, as it relies on pure state.
 *
 * @param currentState The state of the component, represented by [MessagesState].
 * @param modifier Modifier for styling.
 * @param lazyListState State of the lazy list that represents the list of messages. Useful for controlling the scroll state.
 * @param onMessagesStartReached Handler for pagination.
 * @param onLastVisibleMessageChanged Handler that notifies us when the user scrolls and the last visible message changes.
 * @param onScrolledToBottom Handler when the user scrolls to the bottom.
 * @param onThreadClick Handler for when the user taps on a message with an active thread.
 * @param onLongItemClick Handler for when the user long taps on an item.
 * @param onReactionsClick Handler when the user taps on message reactions and selects them.
 * @param onImagePreviewResult Handler when the user selects an option in the Image Preview screen.
 * @param onGiphyActionClick Handler when the user clicks on a giphy action such as shuffle, send or cancel.
 * @param loadingContent Composable that represents the loading content, when we're loading the initial data.
 * @param emptyContent Composable that represents the empty content if there are no messages.
 * @param helperContent Composable that, by default, represents the helper content featuring scrolling behavior based on the list state.
 * @param loadingMoreContent Composable that represents the loading more content, when we're loading the next page.
 * @param itemContent Composable that represents each item in the list, that the user can override
 * for custom UI and behavior.
 */
@Composable
public fun MessageList(
    currentState: MessagesState,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberMessageListState(parentMessageId = currentState.parentMessageId),
    onMessagesStartReached: () -> Unit = {},
    onLastVisibleMessageChanged: (Message) -> Unit = {},
    onScrolledToBottom: () -> Unit = {},
    onThreadClick: (Message) -> Unit = {},
    onLongItemClick: (Message) -> Unit = {},
    onReactionsClick: (Message) -> Unit = {},
    onImagePreviewResult: (ImagePreviewResult?) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    loadingContent: @Composable () -> Unit = { DefaultMessageListLoadingIndicator(modifier) },
    emptyContent: @Composable () -> Unit = { DefaultMessageListEmptyContent(modifier) },
    helperContent: @Composable BoxScope.() -> Unit = {
        DefaultMessagesHelperContent(currentState, lazyListState)
    },
    loadingMoreContent: @Composable () -> Unit = { DefaultMessagesLoadingMoreIndicator() },
    itemContent: @Composable (MessageListItemState) -> Unit = {
        DefaultMessageContainer(
            messageListItem = it,
            onLongItemClick = onLongItemClick,
            onThreadClick = onThreadClick,
            onReactionsClick = onReactionsClick,
            onGiphyActionClick = onGiphyActionClick,
            onImagePreviewResult = onImagePreviewResult
        )
    },
) {
    val (isLoading, _, _, messages) = currentState

    when {
        isLoading -> loadingContent()
        !isLoading && messages.isNotEmpty() -> Messages(
            modifier = modifier,
            messagesState = currentState,
            lazyListState = lazyListState,
            onMessagesStartReached = onMessagesStartReached,
            onLastVisibleMessageChanged = onLastVisibleMessageChanged,
            onScrolledToBottom = onScrolledToBottom,
            helperContent = helperContent,
            loadingMoreContent = loadingMoreContent,
            itemContent = itemContent
        )
        else -> emptyContent()
    }
}
