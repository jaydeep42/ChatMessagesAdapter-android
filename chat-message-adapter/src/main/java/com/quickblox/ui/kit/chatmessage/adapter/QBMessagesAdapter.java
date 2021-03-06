package com.quickblox.ui.kit.chatmessage.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.content.model.QBFile;
import com.quickblox.ui.kit.chatmessage.adapter.listeners.QBChatAttachClickListener;
import com.quickblox.ui.kit.chatmessage.adapter.listeners.QBChatAttachImageClickListener;
import com.quickblox.ui.kit.chatmessage.adapter.listeners.QBChatAttachLocationClickListener;
import com.quickblox.ui.kit.chatmessage.adapter.utils.LocationUtils;
import com.quickblox.ui.kit.chatmessage.adapter.listeners.QBChatMessageLinkClickListener;
import com.quickblox.ui.kit.chatmessage.adapter.utils.QBMessageTextClickMovement;
import com.quickblox.users.model.QBUser;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QBMessagesAdapter<T extends QBChatMessage> extends RecyclerView.Adapter<QBMessagesAdapter.QBMessageViewHolder> implements QBBaseAdapter<T> {
    private static final String TAG = QBMessagesAdapter.class.getSimpleName();

    protected static final int TYPE_TEXT_RIGHT = 1;
    protected static final int TYPE_TEXT_LEFT = 2;
    protected static final int TYPE_ATTACH_RIGHT = 3;
    protected static final int TYPE_ATTACH_LEFT = 4;

    //Message TextView click listener
    //
    private QBChatMessageLinkClickListener messageTextViewLinkClickListener;
    private QBChatAttachImageClickListener attachImageClickListener;
    private QBChatAttachLocationClickListener attachLocationClickListener;
    private boolean overrideOnClick;

    private SparseIntArray containerLayoutRes = new SparseIntArray() {
        {
            put(TYPE_TEXT_RIGHT, R.layout.list_item_text_right);
            put(TYPE_TEXT_LEFT, R.layout.list_item_text_left);
            put(TYPE_ATTACH_RIGHT, R.layout.list_item_attach_right);
            put(TYPE_ATTACH_LEFT, R.layout.list_item_attach_left);
        }
    };

    protected QBMessageViewHolder qbViewHolder;

    protected List<T> chatMessages;
    protected LayoutInflater inflater;
    protected Context context;


    public QBMessagesAdapter(Context context, List<T> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.inflater = LayoutInflater.from(context);
    }

    public QBChatMessageLinkClickListener getMessageTextViewLinkClickListener() {
        return messageTextViewLinkClickListener;
    }

    /**
     * Sets listener for handling pressed links on message text.
     *
     * @param textViewLinkClickListener listener to set. Must to implement {@link QBChatMessageLinkClickListener}
     * @param overrideOnClick           set 'true' if have to himself manage onLinkClick event or set 'false' for delegate
     *                                  onLinkClick event to {@link android.text.util.Linkify}
     */
    public void setMessageTextViewLinkClickListener(QBChatMessageLinkClickListener textViewLinkClickListener, boolean overrideOnClick) {
        this.messageTextViewLinkClickListener = textViewLinkClickListener;
        this.overrideOnClick = overrideOnClick;
    }

    public void setAttachImageClickListener(QBChatAttachImageClickListener clickListener) {
        attachImageClickListener = clickListener;
    }

    public void setAttachLocationClickListener(QBChatAttachLocationClickListener clickListener) {
        attachLocationClickListener = clickListener;
    }

    public void removeAttachImageClickListener(QBChatAttachImageClickListener clickListener) {
        attachImageClickListener = null;
    }

    public void removeLocationImageClickListener(QBChatAttachLocationClickListener clickListener) {
        attachLocationClickListener = null;
    }

    /**
     * Removes listener for handling onLinkClick event on message text.
     */
    public void removeMessageTextViewLinkClickListener() {
        this.messageTextViewLinkClickListener = null;
        this.overrideOnClick = false;
    }

    @Override
    public QBMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_TEXT_RIGHT:
                qbViewHolder = new TextMessageHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_text_message, R.id.msg_text_time_message);
                return qbViewHolder;
            case TYPE_TEXT_LEFT:
                qbViewHolder = new TextMessageHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_text_message, R.id.msg_text_time_message);
                return qbViewHolder;
            case TYPE_ATTACH_RIGHT:
                qbViewHolder = new ImageAttachHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_image_attach, R.id.msg_progressbar_attach,
                        R.id.msg_text_time_attach);
                return qbViewHolder;
            case TYPE_ATTACH_LEFT:
                qbViewHolder = new ImageAttachHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_image_attach, R.id.msg_progressbar_attach,
                        R.id.msg_text_time_attach);
                return qbViewHolder;

            default:
                Log.d(TAG, "onCreateViewHolder case default");
                // resource must be set manually by creating custom adapter
                return onCreateCustomViewHolder(parent, viewType);
        }
    }

    protected QBMessageViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
        Log.e(TAG, "You must create ViewHolder by your own");
        return null;
    }

    protected void setMsgLayoutResourceByType(int typeLayout, @LayoutRes int messageLayoutResource) {
        containerLayoutRes.put(typeLayout, messageLayoutResource);
    }

    @Override
    public void onBindViewHolder(QBMessageViewHolder holder, int position) {
        T chatMessage = getItem(position);
        int valueType = getItemViewType(position);
        switch (valueType) {
            case TYPE_TEXT_RIGHT:
                onBindViewMsgRightHolder((TextMessageHolder) holder, chatMessage, position);
                break;
            case TYPE_TEXT_LEFT:
                onBindViewMsgLeftHolder((TextMessageHolder) holder, chatMessage, position);
                break;
            case TYPE_ATTACH_RIGHT:
                Log.i(TAG, "onBindViewHolder TYPE_ATTACH_RIGHT");
                onBindViewAttachRightHolder((ImageAttachHolder) holder, chatMessage, position);
                break;
            case TYPE_ATTACH_LEFT:
                Log.i(TAG, "onBindViewHolder TYPE_ATTACH_LEFT");
                onBindViewAttachLeftHolder((ImageAttachHolder) holder, chatMessage, position);
                break;
            default:
                onBindViewCustomHolder(holder, chatMessage, position);
                Log.i(TAG, "onBindViewHolder TYPE_ATTACHMENT_CUSTOM");
                break;
        }
    }

    protected void onBindViewCustomHolder(QBMessageViewHolder holder, T chatMessage, int position) {
    }

    protected void onBindViewAttachRightHolder(ImageAttachHolder holder, T chatMessage, int position) {
        setDateSentAttach(holder, chatMessage);
        displayAttachment(holder, position);

        int valueType = getItemViewType(position);
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }

        setItemClickListener(getAttachListenerByType(position), holder, getQBAttach(position), position);
    }

    protected void onBindViewAttachLeftHolder(ImageAttachHolder holder, T chatMessage, int position) {
        setDateSentAttach(holder, chatMessage);
        displayAttachment(holder, position);

        int valueType = getItemViewType(position);
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }

        setItemClickListener(getAttachListenerByType(position), holder, getQBAttach(position), position);
    }

    protected void onBindViewMsgLeftHolder(TextMessageHolder holder, T chatMessage, int position) {
        holder.messageTextView.setText(chatMessage.getBody());
        holder.timeTextMessageTextView.setText(getDate(chatMessage.getDateSent()));

        setMessageTextViewLinkClickListener(holder, position);

        int valueType = getItemViewType(position);
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }
    }

    protected void onBindViewMsgRightHolder(TextMessageHolder holder, T chatMessage, int position) {
        holder.messageTextView.setText(chatMessage.getBody());
        holder.timeTextMessageTextView.setText(getDate(chatMessage.getDateSent()));

        setMessageTextViewLinkClickListener(holder, position);

        int valueType = getItemViewType(position);
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }
    }

    private void setMessageTextViewLinkClickListener(TextMessageHolder holder, int position) {
        if (messageTextViewLinkClickListener != null) {
            QBMessageTextClickMovement customClickMovement =
                    new QBMessageTextClickMovement(messageTextViewLinkClickListener, overrideOnClick, context);
            customClickMovement.setPositionInAdapter(position);

            holder.messageTextView.setMovementMethod(customClickMovement);
        }
    }

    private QBChatAttachClickListener getAttachListenerByType(int position) {
        QBAttachment attachment = getQBAttach(position);

        if (QBAttachment.PHOTO_TYPE.equalsIgnoreCase(attachment.getType())||
            QBAttachment.IMAGE_TYPE.equalsIgnoreCase(attachment.getType())) {
              return attachImageClickListener;
        } else if (QBAttachment.LOCATION_TYPE.equalsIgnoreCase(attachment.getType())) {
              return attachLocationClickListener;
        }
        return null;
    }

    protected void setDateSentAttach(ImageAttachHolder holder, T chatMessage) {
        holder.attachTextTime.setText(getDate(chatMessage.getDateSent()));
    }

    /**
     * ObtainAvatarUrl must be implemented in derived class
     *
     * @return String avatar url
     */
    @Nullable
    public String obtainAvatarUrl(int valueType, T chatMessage) {
        return null;
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public T getItem(int position) {
        return chatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        T chatMessage = getItem(position);

        if (hasAttachments(chatMessage)) {
            QBAttachment attachment = getQBAttach(position);

            if (QBAttachment.PHOTO_TYPE.equalsIgnoreCase(attachment.getType()) ||
                QBAttachment.IMAGE_TYPE.equalsIgnoreCase(attachment.getType())) {
                  return isIncoming(chatMessage) ? TYPE_ATTACH_LEFT : TYPE_ATTACH_RIGHT;
            } else if (QBAttachment.LOCATION_TYPE.equalsIgnoreCase(attachment.getType())) {
                  return getLocationView(chatMessage);
            }

        } else {
            return isIncoming(chatMessage) ? TYPE_TEXT_LEFT : TYPE_TEXT_RIGHT;
        }
        return customViewType(position);
    }

    protected int getLocationView(T chatMessage) {
        return isIncoming(chatMessage) ? TYPE_ATTACH_LEFT : TYPE_ATTACH_RIGHT;
    }

    protected int customViewType(int position) {
        return -1;
    }

    @Override
    public void add(T item) {
        chatMessages.add(item);
        notifyDataSetChanged();
    }

    @Override
    public List<T> getList() {
        return chatMessages;
    }

    @Override
    public void addList(List<T> items) {
        chatMessages.clear();
        chatMessages.addAll(items);
        notifyDataSetChanged();
    }

    protected boolean isIncoming(T chatMessage) {
        QBUser currentUser = QBChatService.getInstance().getUser();
        return chatMessage.getSenderId() != null && !chatMessage.getSenderId().equals(currentUser.getId());
    }

    protected boolean hasAttachments(T chatMessage) {
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        return attachments != null && !attachments.isEmpty();
    }

    /**
     * @return string in "Hours:Minutes" format, i.e. <b>10:15</b>
     */
    protected String getDate(long seconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dateFormat.format(new Date(seconds * 1000));
    }

    /**
     * displayAttachment must be implemented in derived class
     */
    protected void displayAttachment(QBMessageViewHolder holder, int position) {
        QBAttachment attachment = getQBAttach(position);

        if (QBAttachment.PHOTO_TYPE.equalsIgnoreCase(attachment.getType())||
            QBAttachment.IMAGE_TYPE.equalsIgnoreCase(attachment.getType())) {
              showPhotoAttach(holder, position);
        } else if (QBAttachment.LOCATION_TYPE.equalsIgnoreCase(attachment.getType())) {
              showLocationAttach(holder, position);
        }
    }

    protected void showPhotoAttach(QBMessageViewHolder holder, int position) {
        String imageUrl = getImageUrl(position);
        showImageByURL(holder, imageUrl, position);
    }

    protected void showLocationAttach(QBMessageViewHolder holder, int position) {
        String locationUrl = getLocationUrl(position);
        showImageByURL(holder, locationUrl, position);
    }

    public String getImageUrl(int position) {
        QBAttachment attachment = getQBAttach(position);
        return QBFile.getPrivateUrlForUID(attachment.getId());
    }

    public String getLocationUrl(int position) {
        QBAttachment attachment = getQBAttach(position);

        LocationUtils.BuilderParams params = LocationUtils.defaultUrlLocationParams(context);

        return LocationUtils.getRemoteUri(attachment.getData(), params);
    }

    protected QBAttachment getQBAttach(int position) {
        T chatMessage = getItem(position);
        return chatMessage.getAttachments().iterator().next();
    }

    private void showImageByURL(QBMessageViewHolder holder, String url, int position) {
        int preferredImageWidth = (int) context.getResources().getDimension(R.dimen.attach_image_width_preview);
        int preferredImageHeight = (int) context.getResources().getDimension(R.dimen.attach_image_height_preview);

        Glide.with(context)
                .load(url)
                .listener(getRequestListener(holder, position))
                .override(preferredImageWidth, preferredImageHeight)
                .dontTransform()
                .error(R.drawable.ic_error)
                .into(((ImageAttachHolder) holder).attachImageView);
    }

    protected RequestListener getRequestListener(QBMessageViewHolder holder, int position) {
        return new ImageLoadListener((ImageAttachHolder) holder);
    }

    /**
     * displayAvatarImage must be implemented in derived class
     */
    @Override
    public void displayAvatarImage(String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.placeholder_user)
                .dontAnimate()
                .into(imageView);
    }

    protected void setItemClickListener(QBChatAttachClickListener listener, QBMessageViewHolder holder, QBAttachment qbAttachment, int position) {
        if (listener != null) {
            holder.itemView.setOnClickListener(new QBItemClickListenerFilter(listener, qbAttachment, position));
        }
    }

    protected static class TextMessageHolder extends QBMessageViewHolder {
        public TextView messageTextView;
        public TextView timeTextMessageTextView;

        public TextMessageHolder(View itemView, @IdRes int msgId, @IdRes int timeId) {
            super(itemView);
            messageTextView = (TextView) itemView.findViewById(msgId);
            timeTextMessageTextView = (TextView) itemView.findViewById(timeId);
        }
    }

    protected static class ImageAttachHolder extends QBMessageViewHolder {
        public ImageView attachImageView;
        public ProgressBar attachmentProgressBar;
        public TextView attachTextTime;

        public ImageAttachHolder(View itemView, @IdRes int attachId, @IdRes int progressBarId, @IdRes int timeId) {
            super(itemView);
            attachImageView = (ImageView) itemView.findViewById(attachId);
            attachmentProgressBar = (ProgressBar) itemView.findViewById(progressBarId);
            attachTextTime = (TextView) itemView.findViewById(timeId);
        }
    }

    protected abstract static class QBMessageViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatar;

        public QBMessageViewHolder(View itemView) {
            super(itemView);
            avatar = (ImageView) itemView.findViewById(R.id.msg_image_avatar);
        }
    }

    protected static class ImageLoadListener implements RequestListener {
        private ImageAttachHolder holder;

        protected ImageLoadListener(ImageAttachHolder holder) {
            this.holder = holder;
            holder.attachmentProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
            Log.e(TAG, "ImageLoadListener Exception= " + e);
            holder.attachImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            holder.attachmentProgressBar.setVisibility(View.GONE);
            return false;
        }

        @Override
        public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
            holder.attachImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.attachmentProgressBar.setVisibility(View.GONE);
            return false;
        }
    }

    private class QBItemClickListenerFilter implements View.OnClickListener {
        private int position;
        private QBAttachment attachment;
        private QBChatAttachClickListener chatAttachClickListener;

        QBItemClickListenerFilter(QBChatAttachClickListener qbChatAttachClickListener, QBAttachment attachment, int position) {
            this.position = position;
            this.attachment = attachment;
            this.chatAttachClickListener = qbChatAttachClickListener;
        }

        @Override
        public void onClick(View view) {
            chatAttachClickListener.onLinkClicked(attachment, position);
        }
    }
}