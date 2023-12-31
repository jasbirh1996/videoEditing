package com.stalmate.user.videoThumbnails

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.stalmate.user.R

class ThumbnailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatImageView(context, attrs) {

    init {
        scaleType = ScaleType.CENTER_CROP
        alpha = 0.4f
        val dimension = resources.getDimensionPixelSize(R.dimen.frames_video_height)
        layoutParams = LinearLayout.LayoutParams(dimension, dimension).apply { weight = 1f }
    }
}