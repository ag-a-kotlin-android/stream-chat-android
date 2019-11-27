package com.getstream.sdk.chat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.getstream.sdk.chat.adapter.MessageViewHolderFactory;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.storage.Sync;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.List;

import top.defaults.drawabletoolbox.DrawableBuilder;

public class DefaultBubbleHelper {
    private static int topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius;
    private static int bgColor, strokeColor, strokeWidth;

    public static MessageListView.BubbleHelper initDefaultBubbleHelper(MessageListViewStyle style, Context context) {
        return new MessageListView.BubbleHelper() {
            @Override
            public Drawable getDrawableForMessage(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions) {
                if (style.getMessageBubbleDrawableMine() != -1)
                    return context.getDrawable(style.getMessageBubbleDrawableMine());

                configParams(style, mine);
                if (isDefaultBubble(style, mine, context))
                    applyStyleDefault(positions, mine, context);
                if (mine) {
                    // set background for Failed or Error message
                    if (message.getSyncStatus() == Sync.LOCAL_FAILED
                            || message.getType().equals(ModelType.message_error))
                        bgColor = context.getResources().getColor(R.color.stream_message_failed);
                }
                return getBubbleDrawable();
            }

            @Override
            public Drawable getDrawableForAttachment(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions, Attachment attachment) {
                if (attachment == null
                        || attachment.getType().equals(ModelType.attach_unknown))
                    return null;

                if (style.getMessageBubbleDrawableTheirs() != -1)
                    return context.getDrawable(style.getMessageBubbleDrawableTheirs());

                configParams(style, mine);
                if (isDefaultBubble(style, mine, context))
                    applyStyleDefault(positions, mine, context);
                if (mine) {
                    // set corner radius if the attachment has title or description
                    if (!TextUtils.isEmpty(attachment.getTitle()) && !attachment.getType().equals(ModelType.attach_file))
                        bottomLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                }else {
                    // set corner radius if the attachment has title or description
                    if (!TextUtils.isEmpty(attachment.getTitle()) && !attachment.getType().equals(ModelType.attach_file))
                        bottomRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                }
                bgColor = Color.WHITE;
                return getBubbleDrawable();
            }

            @Override
            public Drawable getDrawableForAttachmentDescription(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions){
                if (mine){
                    if (style.getMessageBubbleDrawableMine() != -1)
                        return context.getDrawable(style.getMessageBubbleDrawableMine());
                }else {
                    if (style.getMessageBubbleDrawableTheirs() != -1)
                        return context.getDrawable(style.getMessageBubbleDrawableTheirs());
                }
                configParams(style, mine);
                if (isDefaultBubble(style, mine, context))
                    applyStyleDefault(positions, mine, context);

                topLeftRadius = 0;
                topRightRadius = 0;
                return getBubbleDrawable();
            }
        };
    }

    private static void configParams(MessageListViewStyle style, boolean isMine){
        bgColor = style.getMessageBackgroundColor(isMine);
        strokeColor = style.getMessageBorderColor(isMine);
        strokeWidth = style.getMessageBorderWidth(isMine);
        topLeftRadius = style.getMessageTopLeftCornerRadius(isMine);
        topRightRadius = style.getMessageTopRightCornerRadius(isMine);
        bottomRightRadius = style.getMessageBottomRightCornerRadius(isMine);
        bottomLeftRadius = style.getMessageBottomLeftCornerRadius(isMine);
    }

    private static void applyStyleDefault(List<MessageViewHolderFactory.Position> positions, boolean isMine, Context context){
        if (isMine){
            topLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
            bottomLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
            if (positions.contains(MessageViewHolderFactory.Position.TOP)) {
                topRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
                bottomRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
            } else {
                topRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                bottomRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
            }
        }else{
            topRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
            bottomRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
            if (positions.contains(MessageViewHolderFactory.Position.TOP)) {
                topLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
                bottomLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
            } else {
                topLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                bottomLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
            }
        }
    }

    private static boolean isDefaultBubble(MessageListViewStyle style, boolean isMine, Context context) {
        if (isMine)
            return (style.getMessageTopLeftCornerRadius(isMine) == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1)) &&
                    (style.getMessageTopRightCornerRadius(isMine) == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1)) &&
                    (style.getMessageBottomRightCornerRadius(isMine) == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2)) &&
                    (style.getMessageBottomLeftCornerRadius(isMine) == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1));

        return (style.getMessageTopLeftCornerRadius(isMine) == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1)) &&
                (style.getMessageTopRightCornerRadius(isMine) == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1)) &&
                (style.getMessageBottomRightCornerRadius(isMine) == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1)) &&
                (style.getMessageBottomLeftCornerRadius(isMine) == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2));
    }

    private static Drawable getBubbleDrawable(){
        return new DrawableBuilder()
                .rectangle()
                .strokeColor(strokeColor)
                .strokeWidth(strokeWidth)
                .solidColor(bgColor)
                .cornerRadii(topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius)
                .build();
    }
}