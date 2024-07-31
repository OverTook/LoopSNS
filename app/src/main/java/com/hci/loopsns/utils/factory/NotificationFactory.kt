package com.hci.loopsns.utils.factory

import com.hci.loopsns.storage.models.NotificationInterface
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

object NotificationFactory {

    private val notifications: Queue<NotificationInterface> = ConcurrentLinkedQueue<NotificationInterface>()
    private var listener: NotificationFactoryEventListener? = null
    fun addNotification(notification: NotificationInterface) {
        notifications.add(notification)
        listener?.onCreatedNotification(notification)
    }

    fun addEventListener(listener: NotificationFactoryEventListener) {
        this.listener = listener

        var item: NotificationInterface? = notifications.poll()
        while (item != null) {
            listener.onCreatedNotification(item)
            item = notifications.poll()
        }
    }
}

interface NotificationFactoryEventListener {
    fun onCreatedNotification(item: NotificationInterface)
}