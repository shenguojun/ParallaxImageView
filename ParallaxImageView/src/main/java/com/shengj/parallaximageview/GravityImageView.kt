package com.shengj.parallaximageview

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.ViewTreeObserver
import androidx.appcompat.widget.AppCompatImageView

/**
 * @description 根据重力感应偏移的ImageView
 *
 * @author shengj
 * @date 2021/7/29 15:03
 */
class GravityImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr), GravitySensor.GravityListener {
    private var originMatrix: Matrix? = null
    private val onPreDrawListener = SizeOnPreDrawListener()
    private var originXOffset = 0f
    private var originYOffset = 0f
    private var _scale: Float = 1f
    private var hasDraw = false

    /**
     * 设置放大倍数，需要调用[onGravityChange]方法才会生效
     */
    var scale: Float
        get() = _scale
        set(value) {
            if (value < 1) {
                throw RuntimeException("GravityImageView's scale must not less then 1")
            }
            _scale = value
            onPreDrawListener.onPreDraw()
        }

    init {
        val attr = context.obtainStyledAttributes(
            attrs, R.styleable.GravityImageView, defStyleAttr, 0
        )
        _scale = attr.getString(R.styleable.GravityImageView_scale)?.toFloat() ?: 1f
        if (_scale < 1) {
            throw RuntimeException("GravityImageView's scale must not less then 1")
        }
        attr.recycle()
        scaleType = ScaleType.MATRIX
        viewTreeObserver.addOnPreDrawListener(onPreDrawListener)
    }

    inner class SizeOnPreDrawListener : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            if (measuredWidth != 0 && measuredHeight != 0) {
                viewTreeObserver.removeOnPreDrawListener(this)
                // 由于图片经过放大了Scale倍数，为了居中需要移动一下
                originXOffset = measuredWidth * (scale - 1) / 2
                originYOffset = measuredHeight * (scale - 1) / 2
                originMatrix = Matrix().also {
                    // 因为当重力变化时需要放大，因此这里先缩小一下
                    it.postTranslate(-originXOffset / scale, -originYOffset / scale)
                }
                hasDraw = true
            }
            return true
        }
    }


    override fun onGravityChange(x: Float, y: Float) {
        if (!hasDraw) return
        matrix.apply {
            set(originMatrix)
            postScale(scale, scale)
            // 根据最大偏移算出当前偏移
            postTranslate(
                x * originXOffset / GravitySensor.G,
                y * originYOffset / GravitySensor.G
            )
        }.also {
            imageMatrix = it
        }
    }
}