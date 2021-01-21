package io.getstream.chat.android.ui.options.attachment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.getstream.chat.android.ui.databinding.StreamUiFragmentAttachmentOptionsBinding
import io.getstream.chat.android.ui.view.FullScreenDialogFragment

internal class AttachmentOptionsDialogFragment : FullScreenDialogFragment() {
    private var _binding: StreamUiFragmentAttachmentOptionsBinding? = null
    private val binding get() = _binding!!

    private var showInChatHandler: AttachmentOptionHandler? = null
    private var deleteHandler: AttachmentOptionHandler? = null
    private var replyHandler: AttachmentOptionHandler? = null
    private var saveImageHandler: AttachmentOptionHandler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return StreamUiFragmentAttachmentOptionsBinding.inflate(inflater, container, false)
            .apply { _binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.attachmentOptionsMenu.setReplyClickListener(
            object : AttachmentOptionsView.ReplyClickListener {
                override fun onClick() {
                    replyHandler?.onClick()
                    dismiss()
                }
            }
        )
        binding.attachmentOptionsMenu.setDeleteClickListener(
            object : AttachmentOptionsView.DeleteClickListener {
                override fun onClick() {
                    deleteHandler?.onClick()
                    dismiss()
                }
            }
        )
        binding.attachmentOptionsMenu.setShowInChatClickListener(
            object : AttachmentOptionsView.ShowInChatClickListener {
                override fun onClick() {
                    showInChatHandler?.onClick()
                    dismiss()
                }
            }
        )
        binding.attachmentOptionsMenu.setSaveImageClickListener(
            object : AttachmentOptionsView.SaveImageClickListener {
                override fun onClick() {
                    saveImageHandler?.onClick()
                    dismiss()
                }
            }
        )
        binding.root.setOnClickListener { dismiss() }
    }

    companion object {
        const val TAG = "AttachmentOptionsDialogFragment"

        fun newInstance(
            showInChatHandlerAttachment: AttachmentOptionHandler,
            replyHandlerAttachment: AttachmentOptionHandler,
            deleteHandlerAttachment: AttachmentOptionHandler? = null,
            saveImageHandlerAttachment: AttachmentOptionHandler? = null,
        ): AttachmentOptionsDialogFragment {
            return AttachmentOptionsDialogFragment().apply {
                this.showInChatHandler = showInChatHandlerAttachment
                this.deleteHandler = deleteHandlerAttachment
                this.replyHandler = replyHandlerAttachment
                this.saveImageHandler = saveImageHandlerAttachment
            }
        }
    }

    interface AttachmentOptionHandler {
        fun onClick(): Unit
    }
}


