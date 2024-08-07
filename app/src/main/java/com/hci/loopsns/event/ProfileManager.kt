package com.hci.loopsns.event

class ProfileManager {

    private val listeners = ArrayList<ProfileListener>()

    companion object {
        @Volatile
        private var instance: ProfileManager? = null

        fun getInstance(): ProfileManager {
            return instance ?: synchronized(this) {
                instance ?: ProfileManager().also { instance = it }
            }
        }
    }

    fun registerProfileListener(listener: ProfileListener) {
        listeners.add(listener)
    }

    fun removeProfileListener(listener: ProfileListener) {
        listeners.remove(listener)
    }

    fun onProfileChanged() {
        listeners.forEach {
            it.onChangedProfile()
        }
    }
}

interface ProfileListener {
    fun onChangedProfile()
}