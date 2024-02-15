package com.example.viewsystem.feature.legotext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viewsystem.commons.util.UiText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import timber.log.Timber

class LegoTextViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MessagesUiState>(MessagesUiState())
    val messagesUiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<MessagesUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _messages: MutableStateFlow<List<Message>> = MutableStateFlow<List<Message>>(
        emptyList()
    )

    val accept: (MessagesUiAction) -> Unit

    private val typingState = MutableSharedFlow<MessagesUiAction>()

    init {
        accept = { uiAction -> onUiAction(uiAction) }

        _messages
            .map { messages ->
                messages
                    .sortedByDescending { it.ts }
                    .map { message -> MessageUiModel.Item(message) }
            }
            .onEach { uiModels ->
                _uiState.update { state ->
                    state.copy(
                        messages = uiModels
                    )
                }
            }
            .launchIn(viewModelScope)

        typingState
            .filterIsInstance<MessagesUiAction.TypingMessage>()
            .onEach { action ->
                _uiState.update {
                    it.copy(
                        typedMessage = action.typed
                    )
                }
            }
            .launchIn(viewModelScope)

        typingState
            .filterIsInstance<MessagesUiAction.Scrolled>()
            .debounce(50)
            .distinctUntilChanged()
            .onEach { action ->
                Timber.d("onScrolled: $action")
                _uiState.update { it.copy(pageDate = null) }
            }
            .launchIn(viewModelScope)
    }

    private fun onUiAction(action: MessagesUiAction) {
        when (action) {
            /* UI Actions that happens fast and continuously */
            is MessagesUiAction.TypingMessage,
            is MessagesUiAction.Scrolled -> {
                viewModelScope.launch { typingState.emit(action) }
            }
            is MessagesUiAction.SendClick -> {
                validateInternal()
            }
        }
    }

    private fun validateInternal() {
        val messageText = messagesUiState.value.typedMessage

        val message = Message(
            id = System.currentTimeMillis(),
            ts = System.currentTimeMillis(),
            text = messageText
        )
        viewModelScope.launch {
            val newList = _messages.value.toMutableList().apply {
                add(message)
            }
            _messages.update { newList }
            _uiState.update { state ->
                state.copy(
                    typedMessage = "",
                )
            }
            sendEvent(MessagesUiEvent.MessageSent(message.id))
        }
    }

    private fun sendEvent(newEvent: MessagesUiEvent) = viewModelScope.launch {
        _uiEvent.emit(newEvent)
    }
}

data class MessagesUiState(
    val messages: List<MessageUiModel> = emptyList(),
    val typedMessage: String = "",
    val pageDate: LocalDateTime? = null,
)

interface MessageUiModel {
    data class Item(val message: Message) : MessageUiModel
    data class DateSeparator(val display: String, val date: LocalDateTime): MessageUiModel
}

interface MessagesUiAction {
    data class TypingMessage(val typed: String) : MessagesUiAction
    data class Scrolled(
        val visibleItemCount: Int,
        val totalItemCount: Int,
        val lastVisibleItemPosition: Int,
        val dy: Int) : MessagesUiAction
    object SendClick : MessagesUiAction
}

interface MessagesUiEvent {
    data class ShowToast(val message: UiText) : MessagesUiEvent
    data class ShowSnack(val message: UiText) : MessagesUiEvent
    data class MessageSent(val messageId: Long) : MessagesUiEvent
}

data class Message(
    val id: Long,
    val ts: Long,
    val text: String,
)
