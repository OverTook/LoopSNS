package com.hci.loopsns.utils

import android.content.res.Resources
import androidx.recyclerview.widget.RecyclerView
import com.hci.loopsns.AutoRefresherInterface

val Float.dp: Float
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f)

fun <T : AutoRefresherInterface> RecyclerView.registerAutoRefresh(activity: T) {
    this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(!recyclerView.canScrollVertically(1)) {
                //끝까지 도달함
                if(activity.requested || activity.noMoreData) return
                activity.requested = true

                val anim = activity.getRequestAnimation()
                anim.animate()
                    .cancel()

                anim.animate()
                    .translationY((-60f).dp)
                    .setDuration(100)
                    .start()

                activity.requestMoreData()
            }
        }
    })
}

fun AutoRefresherInterface.requestEnd(noMoreData: Boolean) {
    this.noMoreData = noMoreData
    this.requested = false

    val anim = getRequestAnimation()
    anim.animate()
        .cancel()

    anim.animate()
        .translationY((0f).dp)
        .setDuration(100)
        .start()
}