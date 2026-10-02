package com.amirbahadoramiri.quickmail

interface QuickMailListener {
    fun onSuccess()
    fun onFailure(error: Exception)
}