package io.getstream.chat.android.livedata.controller

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.livedata.BaseDomainTest
import io.getstream.chat.android.livedata.randomString
import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ChannelControllerImplTest : BaseDomainTest() {

    private val chatClient: ChatClient = spy()
    private val channelType: String = randomString()
    private val channelId: String = randomString()
    private val call: Call<String> = mock()
    private lateinit var channelController: ChannelControllerImpl

    override fun setup() {
        super.setup()
        channelController =
            ChannelControllerImpl(channelType, channelId, chatClient, chatDomainImpl)
    }

    @Test
    fun `Should return successful result when sending an image`() {
        runBlocking(Dispatchers.IO) {
            val file = File(randomString())
            val expectedResult = Result(randomString())
            When calling call.execute() doReturn expectedResult
            When calling chatClient.sendImage(channelType, channelId, file) doReturn call

            val result = channelController.sendImage(file)

            result `should be` expectedResult
            Verify on chatClient that chatClient.sendImage(
                eq(channelType),
                eq(channelId),
                eq(file)
            ) was called
        }
    }

    @Test
    fun `Should return successful result when sending a file`() {
        runBlocking(Dispatchers.IO) {
            val file = File(randomString())
            val expectedResult = Result(randomString())
            When calling call.execute() doReturn expectedResult
            When calling chatClient.sendFile(channelType, channelId, file) doReturn call

            val result = channelController.sendFile(file)

            result `should be` expectedResult
            Verify on chatClient that chatClient.sendFile(
                eq(channelType),
                eq(channelId),
                eq(file)
            ) was called
        }
    }

    @Test
    fun `Should return failure result when sending an image`() {
        runBlocking(Dispatchers.IO) {
            val file = File(randomString())
            val expectedResult = Result<String>(ChatError(randomString()))
            When calling call.execute() doReturn expectedResult
            When calling chatClient.sendImage(channelType, channelId, file) doReturn call

            val result = channelController.sendImage(file)

            result `should be` expectedResult
            Verify on chatClient that chatClient.sendImage(
                eq(channelType),
                eq(channelId),
                eq(file)
            ) was called
        }
    }

    @Test
    fun `Should return failure result when sending a file`() {
        runBlocking(Dispatchers.IO) {
            val file = File(randomString())
            val expectedResult = Result<String>(ChatError(randomString()))
            When calling call.execute() doReturn expectedResult
            When calling chatClient.sendFile(channelType, channelId, file) doReturn call

            val result = channelController.sendFile(file)

            result `should be` expectedResult
            Verify on chatClient that chatClient.sendFile(
                eq(channelType),
                eq(channelId),
                eq(file)
            ) was called
        }
    }
}