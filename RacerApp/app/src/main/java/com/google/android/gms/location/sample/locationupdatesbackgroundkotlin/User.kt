package com.example.bupazar

import android.app.Application

class User:Application() {
    companion object {
        var password: String? = null
        var username: String? = null
    }
}