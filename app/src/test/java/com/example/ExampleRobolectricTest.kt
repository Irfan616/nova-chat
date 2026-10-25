package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.entity.ConversationEntity
import com.example.data.entity.MessageEntity
import com.example.data.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var database: AppDatabase

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext<Context>()
    database = AppDatabase.getDatabase(context)
  }

  @Test
  fun `read app name string from context`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("NovaChat", appName)
  }

  @Test
  fun `verify conversation database operations`() = runBlocking {
    val conv = ConversationEntity(
      id = "test_conv_1",
      title = "Quantum Architects",
      isGroup = true,
      lastMessageSnippet = "Hello quantum world"
    )
    database.conversationDao().insertConversation(conv)

    val retrieved = database.conversationDao().getConversationByIdSync("test_conv_1")
    assertNotNull(retrieved)
    assertEquals("Quantum Architects", retrieved?.title)
    assertTrue(retrieved?.isGroup == true)
  }

  @Test
  fun `verify message persistence and status update`() = runBlocking {
    val msg = MessageEntity(
      id = "test_msg_1",
      conversationId = "test_conv_1",
      senderId = "user_me",
      senderName = "Alex",
      text = "Encrypted packet",
      status = "PENDING"
    )
    database.messageDao().insertMessage(msg)

    val retrieved = database.messageDao().getMessageById("test_msg_1")
    assertEquals("PENDING", retrieved?.status)

    database.messageDao().updateStatus("test_msg_1", "DELIVERED")
    val updated = database.messageDao().getMessageById("test_msg_1")
    assertEquals("DELIVERED", updated?.status)
  }
}
