package io.getstream.chat.android.livedata.repository

import androidx.collection.LruCache
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.dao.MessageDao
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.extensions.isPermanent
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest
import java.security.InvalidParameterException
import java.util.Date

class MessageRepository(var messageDao: MessageDao, var cacheSize: Int = 100, private val currentUser: User, var client: ChatClient) {
    // the message cache, specifically caches messages on which we're receiving events (saving a few trips to the db when you get 10 likes on 1 message)
    var messageCache = LruCache<String, Message>(cacheSize)

    internal suspend fun selectMessagesForChannel(
        cid: String,
        pagination: AnyChannelPaginationRequest
    ): List<MessageEntity> {
        if (pagination.hasFilter()) {
            // handle the differences between gt, gte, lt and lte
            val message = messageDao.select(pagination.messageFilterValue)
            if (message?.createdAt == null) return listOf()
            val messageLimit = pagination.messageLimit
            val messageTime = message.createdAt!!

            when (pagination.messageFilterDirection) {
                Pagination.GREATER_THAN_OR_EQUAL -> {
                    return messageDao.messagesForChannelEqualOrNewerThan(cid, messageLimit, messageTime)
                }
                Pagination.GREATER_THAN -> {
                    return messageDao.messagesForChannelNewerThan(cid, messageLimit, messageTime)
                }
                Pagination.LESS_THAN_OR_EQUAL -> {
                    return messageDao.messagesForChannelEqualOrOlderThan(cid, messageLimit, messageTime)
                }
                Pagination.LESS_THAN -> {
                    return messageDao.messagesForChannelOlderThan(cid, messageLimit, messageTime)
                }
            }
        }
        return messageDao.messagesForChannel(cid, pagination.messageLimit)
    }

    suspend fun removeReaction(messageId: String, reaction: Reaction) {
        messageDao.select(messageId)?.also { messageEntity ->
            messageEntity.removeReaction(reaction, currentUser.id == reaction.user?.id)
            messageDao.insert(messageEntity)
        }
    }

    suspend fun addReaction(messageId: String, reaction: Reaction) {
        messageDao.select(messageId)?.also { messageEntity ->
            messageEntity.addReaction(reaction, currentUser.id == reaction.user?.id)
            messageDao.insert(messageEntity)
        }
    }

    suspend fun select(messageIds: List<String>, usersMap: Map<String, User>): List<Message> {
        val cachedMessages: MutableList<Message> = mutableListOf()
        for (messageId in messageIds) {
            val messageEntity = messageCache.get(messageId)
            messageEntity?.let { cachedMessages.add(it) }
        }
        val missingMessageIds = messageIds.filter { messageCache.get(it) == null }
        val dbMessages = messageDao.select(missingMessageIds).map { it.toMessage(usersMap) }.toMutableList()

        dbMessages.addAll(cachedMessages)
        return dbMessages
    }

    suspend fun select(messageId: String, usersMap: Map<String, User>): Message? {
        return messageCache[messageId] ?: messageDao.select(messageId)?.toMessage(usersMap)
    }

    suspend fun insert(messages: List<Message>, cache: Boolean = false) {
        if (messages.isEmpty()) return
        for (message in messages) {
            if (message.cid == "") {
                throw InvalidParameterException("message.cid cant be empty")
            }
        }
        for (m in messages) {
            if (messageCache.get(m.id) != null || cache) {
                messageCache.put(m.id, m)
            }
        }
        messageDao.insertMany(messages.map(::MessageEntity))
    }

    suspend fun insert(message: Message, cache: Boolean = false) {
        insert(listOf(message), cache)
    }

    suspend fun selectSyncNeeded(): List<MessageEntity> {
        return messageDao.selectSyncNeeded()
    }

    suspend fun retryMessages(): List<MessageEntity> {
        val userMap: Map<String, User> = mutableMapOf(currentUser.id to currentUser)

        val messageEntities = selectSyncNeeded()
        for (messageEntity in messageEntities) {
            val channel = client.channel(messageEntity.cid)
            // support sending, deleting and editing messages here
            val result = when {
                messageEntity.deletedAt != null -> {
                    channel.deleteMessage(messageEntity.id).execute()
                }
                messageEntity.sendMessageCompletedAt != null -> {
                    client.updateMessage(messageEntity.toMessage(userMap)).execute()
                }
                else -> {
                    channel.sendMessage(messageEntity.toMessage(userMap)).execute()
                }
            }

            if (result.isSuccess) {
                // TODO: 1.1 image upload support
                messageEntity.syncStatus = SyncStatus.COMPLETED
                messageEntity.sendMessageCompletedAt = messageEntity.sendMessageCompletedAt
                    ?: Date()
                messageDao.insert(messageEntity)
            } else if (result.isError && result.error().isPermanent()) {
                messageEntity.syncStatus = SyncStatus.FAILED_PERMANENTLY
                messageDao.insert(messageEntity)
            }
        }

        return messageEntities
    }

    suspend fun deleteChannelMessagesBefore(cid: String, hideMessagesBefore: Date) {
        // delete the messages
        messageDao.deleteChannelMessagesBefore(cid, hideMessagesBefore)
        // wipe the cache
        messageCache = LruCache(cacheSize)
    }

    suspend fun deleteChannelMessage(message: Message) {
        messageDao.deleteMessage(message.cid, message.id)
        messageCache.remove(message.id)
    }
}
