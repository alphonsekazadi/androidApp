package com.hedvig.android.feature.chat

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import arrow.fx.coroutines.parMap
import com.benasher44.uuid.Uuid
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.chat.data.ChatRepository
import com.hedvig.android.feature.chat.model.ChatMessage
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal sealed interface ChatEvent {
  data object FetchMoreMessages : ChatEvent

  data class SendTextMessage(val message: String) : ChatEvent

  data class SendPhotoMessage(val uri: Uri) : ChatEvent

  data class SendMediaMessage(val uri: Uri) : ChatEvent

  data class RetrySend(val chatMessageId: String) : ChatEvent
}

@Immutable
internal sealed interface ChatUiState {
  data object Initializing : ChatUiState

  data object DisabledByFeatureFlag : ChatUiState

  @Immutable
  data class Loaded(
    // The list of messages, ordered from the newest one to the oldest one
    val messages: ImmutableList<ChatMessage>,
    val errorMessage: ErrorMessage?,
    val fetchMoreMessagesUiState: FetchMoreMessagesUiState,
  ) : ChatUiState {
    sealed interface FetchMoreMessagesUiState {
      object FailedToFetch : FetchMoreMessagesUiState

      object FetchingMore : FetchMoreMessagesUiState

      object NothingMoreToFetch : FetchMoreMessagesUiState

      object StillInitializing : FetchMoreMessagesUiState
    }
  }
}

internal class ChatPresenter(
  private val chatRepository: Provider<ChatRepository>,
  private val featureManager: FeatureManager,
  private val clock: Clock,
) : MoleculePresenter<ChatEvent, ChatUiState> {
  @Composable
  override fun MoleculePresenterScope<ChatEvent>.present(lastState: ChatUiState): ChatUiState {
    val isChatDisabled by produceState(false || lastState is ChatUiState.DisabledByFeatureFlag) {
      value = featureManager.isFeatureEnabled(Feature.DISABLE_CHAT)
    }
    if (isChatDisabled) {
      return ChatUiState.DisabledByFeatureFlag
    }

    val messages: SnapshotStateList<ChatMessage> = remember {
      mutableStateListOf(
        *(lastState.safeCast<ChatUiState.Loaded>()?.messages ?: emptyList()).toTypedArray(),
      )
    }

    // We are considered to still be initializing before we get the first cache emission
    var isStillInitializing by remember { mutableStateOf(lastState is ChatUiState.Initializing) }

    var fetchMoreState by remember { mutableStateOf<FetchMoreState>(FetchMoreState.HaveNotReceivedInitialFetchUntil) }
    var fetchMoreMessagesFetchIndex by remember { mutableIntStateOf(0) }
    var failedToFetchMoreMessages by remember { mutableStateOf(false) }

    val photosToSend = remember { Channel<Uri>(Channel.UNLIMITED) }
    var photosFailedToBeSent: SnapshotStateList<FailedMessage.FailedUri> = remember { mutableStateListOf() }
    val mediaToSend = remember { Channel<Uri>(Channel.UNLIMITED) }
    var mediaFailedToBeSent: SnapshotStateList<FailedMessage.FailedUri> = remember { mutableStateListOf() }
    val messagesToSend = remember { Channel<String>(Channel.UNLIMITED) }
    var messagesFailedToBeSent: SnapshotStateList<FailedMessage.FailedText> = remember { mutableStateListOf() }

    LaunchMessagesWatchingEffect(
      onCachedMessagesReceived = { cachedMessages ->
        Snapshot.withMutableSnapshot {
          isStillInitializing = false
          messages.clear()
          messages.addAll(cachedMessages)
        }
      },
    )
    LaunchPeriodicMessagePollsEffect(
      isChatDisabled = isChatDisabled,
      reportNextUntilFromPolling = { nextUntil: Instant ->
        // If we have not received a `nextUntil` value yet, we set the first value from the polling query
        if (fetchMoreState is FetchMoreState.HaveNotReceivedInitialFetchUntil) {
          fetchMoreState = FetchMoreState.IdleWithKnownNextFetch(nextUntil)
        }
      },
    )
    LaunchFetchMoreMessagesEffect(
      fetchMoreMessagesFetchIndex = fetchMoreMessagesFetchIndex,
      fetchMoreState = fetchMoreState,
      reportNothingMoreToFetch = { fetchMoreState = FetchMoreState.NothingMoreToFetch },
      succededInFetchingMoreMessages = { nextUntil ->
        fetchMoreState = FetchMoreState.IdleWithKnownNextFetch(nextUntil)
      },
      failedToFetchMoreMessages = { failedToFetchMoreMessages = true },
    )
    LaunchNewPhotoSendingEffect(
      photosToSend = photosToSend,
      reportPhotoFailedToBeSent = { uri ->
        photosFailedToBeSent.add(FailedMessage.FailedUri(Uuid.randomUUID().toString(), clock.now(), uri))
      },
    )
    LaunchNewMediaSendingEffect(
      mediaToSend = mediaToSend,
      reportMediaFailedToBeSent = { uri ->
        mediaFailedToBeSent.add(FailedMessage.FailedUri(Uuid.randomUUID().toString(), clock.now(), uri))
      },
    )
    LaunchNewMessageSendingEffect(
      messagesToSend = messagesToSend,
      reportMessageFailedToBeSent = { message ->
        messagesFailedToBeSent.add(FailedMessage.FailedText(Uuid.randomUUID().toString(), clock.now(), message))
      },
    )

    CollectEvents { event ->
      logcat { "ChatPresenter handling event:$event" }
      when (event) {
        ChatEvent.FetchMoreMessages -> {
          if (failedToFetchMoreMessages) {
            Snapshot.withMutableSnapshot {
              failedToFetchMoreMessages = false
              fetchMoreMessagesFetchIndex++
            }
          } else {
            val fetchMoreStateValue = fetchMoreState
            if (fetchMoreStateValue is FetchMoreState.IdleWithKnownNextFetch) {
              fetchMoreState = FetchMoreState.FetchUntil(fetchMoreStateValue.fetchUntil)
            }
          }
        }

        is ChatEvent.SendPhotoMessage -> {
          photosToSend.trySend(event.uri)
        }

        is ChatEvent.SendMediaMessage -> {
          mediaToSend.trySend(event.uri)
        }

        is ChatEvent.SendTextMessage -> {
          messagesToSend.trySend(event.message)
        }

        is ChatEvent.RetrySend -> {
          val failedMessage = messagesFailedToBeSent.find { it.id == event.chatMessageId }
          val failedPhoto = photosFailedToBeSent.find { it.id == event.chatMessageId }
          val failedMedia = mediaFailedToBeSent.find { it.id == event.chatMessageId }
          when {
            failedMessage != null -> {
              messagesToSend.trySend(failedMessage.message)
              messagesFailedToBeSent.removeIf { it.id == failedMessage.id }
            }

            failedPhoto != null -> {
              photosToSend.trySend(failedPhoto.uri)
              photosFailedToBeSent.removeIf { it.id == failedPhoto.id }
            }

            failedMedia != null -> {
              mediaToSend.trySend(failedMedia.uri)
              mediaFailedToBeSent.removeIf { it.id == failedMedia.id }
            }

            else -> {
              logcat(LogPriority.WARN) {
                "ChatPresenter: Tried to retry sending a message which was not found in any of the queues"
              }
            }
          }
        }
      }
    }

    return if (isStillInitializing) {
      ChatUiState.Initializing
    } else {
      val fetchMoreMessagesUiState = run {
        if (failedToFetchMoreMessages) return@run ChatUiState.Loaded.FetchMoreMessagesUiState.FailedToFetch
        when (fetchMoreState) {
          is FetchMoreState.HaveNotReceivedInitialFetchUntil ->
            ChatUiState.Loaded.FetchMoreMessagesUiState.StillInitializing

          is FetchMoreState.FetchUntil -> ChatUiState.Loaded.FetchMoreMessagesUiState.FetchingMore
          is FetchMoreState.IdleWithKnownNextFetch -> ChatUiState.Loaded.FetchMoreMessagesUiState.FetchingMore
          is FetchMoreState.NothingMoreToFetch -> ChatUiState.Loaded.FetchMoreMessagesUiState.NothingMoreToFetch
        }
      }
      val failedChatMessages = (messagesFailedToBeSent + photosFailedToBeSent + mediaFailedToBeSent)
        .map(FailedMessage::toChatMessage)
      ChatUiState.Loaded(
        messages = (messages + failedChatMessages)
          .sortedByDescending(ChatMessage::sentAt)
          .toPersistentList(),
        errorMessage = null,
        fetchMoreMessagesUiState = fetchMoreMessagesUiState,
      )
    }
  }

  @Composable
  private fun LaunchPeriodicMessagePollsEffect(
    isChatDisabled: Boolean,
    reportNextUntilFromPolling: (nextUntil: Instant) -> Unit,
  ) {
    val updatedReportNextUntilFromPolling by rememberUpdatedState(reportNextUntilFromPolling)
    LaunchedEffect(isChatDisabled) {
      if (isChatDisabled) return@LaunchedEffect
      while (isActive) {
        chatRepository.provide().pollNewestMessages().onRight { result ->
          updatedReportNextUntilFromPolling(result.nextUntil)
        }
        delay(5.seconds)
      }
    }
  }

  @Composable
  private fun LaunchMessagesWatchingEffect(onCachedMessagesReceived: (List<ChatMessage>) -> Unit) {
    val updatedOnCachedMessagesReceived by rememberUpdatedState(onCachedMessagesReceived)
    LaunchedEffect(Unit) {
      chatRepository.provide().watchMessages()
        .mapNotNull { it.getOrNull() }
        .collect { cachedMessages ->
          updatedOnCachedMessagesReceived(cachedMessages)
        }
    }
  }

  @Composable
  private fun LaunchFetchMoreMessagesEffect(
    fetchMoreMessagesFetchIndex: Int,
    fetchMoreState: FetchMoreState,
    reportNothingMoreToFetch: () -> Unit,
    succededInFetchingMoreMessages: (nextUntil: Instant) -> Unit,
    failedToFetchMoreMessages: () -> Unit,
  ) {
    val updatedReportNothingMoreToFetch by rememberUpdatedState(reportNothingMoreToFetch)
    val updatedSuccededInFetchingMoreMessages by rememberUpdatedState(succededInFetchingMoreMessages)
    val updatedFailedToFetchMoreMessages by rememberUpdatedState(failedToFetchMoreMessages)
    LaunchedEffect(fetchMoreMessagesFetchIndex, fetchMoreState) {
      val fetchUntil = fetchMoreState as? FetchMoreState.FetchUntil ?: return@LaunchedEffect
      chatRepository.provide()
        .fetchMoreMessages(fetchUntil.fetchUntil)
        .fold(
          ifLeft = {
            logcat { "Chat failed to fetch more messages:$it" }
            updatedFailedToFetchMoreMessages()
          },
          ifRight = { chatMessagesResult ->
            if (!chatMessagesResult.hasNext) {
              logcat { "Chat has fetched new data, but has no more messages to fetch, reached the end" }
              updatedReportNothingMoreToFetch()
            } else {
              logcat { "Chat has fetched new data, and even more exist, next until:${chatMessagesResult.nextUntil}" }
              updatedSuccededInFetchingMoreMessages(chatMessagesResult.nextUntil)
            }
          },
        )
    }
  }

  @Composable
  private fun LaunchNewPhotoSendingEffect(photosToSend: Channel<Uri>, reportPhotoFailedToBeSent: (Uri) -> Unit) {
    val updatedReportPhotoFailedToBeSent by rememberUpdatedState(reportPhotoFailedToBeSent)
    LaunchedEffect(photosToSend) {
      photosToSend.receiveAsFlow().parMap { uri: Uri ->
        logcat { "Handling sending photo with uri:${uri.path}" }
        chatRepository.provide().sendPhoto(uri).onLeft {
          logcat(LogPriority.WARN) { "Failed to send photo:${uri.path} | $it" }
          updatedReportPhotoFailedToBeSent(uri)
        }
      }.collect()
    }
  }

  @Composable
  private fun LaunchNewMediaSendingEffect(mediaToSend: Channel<Uri>, reportMediaFailedToBeSent: (Uri) -> Unit) {
    val updatedReportMediaFailedToBeSent by rememberUpdatedState(reportMediaFailedToBeSent)
    LaunchedEffect(mediaToSend) {
      mediaToSend.receiveAsFlow().parMap { uri: Uri ->
        logcat { "Handling sending media with uri:${uri.path}" }
        chatRepository.provide().sendMedia(uri).onLeft {
          logcat(LogPriority.WARN) { "Failed to send media:${uri.path} | $it" }
          updatedReportMediaFailedToBeSent(uri)
        }
      }.collect()
    }
  }

  @Composable
  private fun LaunchNewMessageSendingEffect(
    messagesToSend: Channel<String>,
    reportMessageFailedToBeSent: (String) -> Unit,
  ) {
    val updatedReportMessageFailedToBeSent by rememberUpdatedState(reportMessageFailedToBeSent)
    LaunchedEffect(messagesToSend) {
      messagesToSend.consumeAsFlow().parMap { message: String ->
        logcat { "Handling sending message with text:$message" }
        chatRepository.provide().sendMessage(message).onLeft {
          logcat(LogPriority.WARN) { "Failed to send message:$it" }
          updatedReportMessageFailedToBeSent(message)
        }
      }.collect()
    }
  }
}

@Immutable
private sealed interface FetchMoreState {
  data object HaveNotReceivedInitialFetchUntil : FetchMoreState

  data class IdleWithKnownNextFetch(val fetchUntil: Instant) : FetchMoreState

  data class FetchUntil(val fetchUntil: Instant) : FetchMoreState

  data object NothingMoreToFetch : FetchMoreState
}

private sealed interface FailedMessage {
  val id: String
  val sentAt: Instant

  data class FailedText(
    override val id: String,
    override val sentAt: Instant,
    val message: String,
  ) : FailedMessage

  data class FailedUri(
    override val id: String,
    override val sentAt: Instant,
    val uri: Uri,
  ) : FailedMessage
}

private fun FailedMessage.toChatMessage(): ChatMessage {
  return when (this) {
    is FailedMessage.FailedText -> {
      ChatMessage.FailedToBeSent.ChatMessageText(
        id = this.id,
        sentAt = this.sentAt,
        text = this.message,
      )
    }

    is FailedMessage.FailedUri -> {
      ChatMessage.FailedToBeSent.ChatMessageUri(
        id = this.id,
        sentAt = this.sentAt,
        uri = this.uri,
      )
    }
  }
}