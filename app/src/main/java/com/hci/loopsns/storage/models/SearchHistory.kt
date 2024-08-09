package com.hci.loopsns.storage.models

import org.litepal.crud.LitePalSupport

data class SearchHistory (
    val text: String
) : LitePalSupport()
