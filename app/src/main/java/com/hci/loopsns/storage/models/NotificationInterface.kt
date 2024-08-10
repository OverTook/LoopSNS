package com.hci.loopsns.storage.models

import java.util.Date

interface NotificationInterface {
    val time: Date
    var readed: Boolean
    var userId: String
}