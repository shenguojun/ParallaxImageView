package com.shengj.parallaximageview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Matrix.ScaleToFit
import android.graphics.RectF
import android.util.AttributeSet
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
    private var originXOffset = 0f
    private var originYOffset = 0f
    private var _scale: Float = 1f
    private var mScaleType: ScaleType = ScaleType.FIT_CENTER
    private var hasConfig = false

    companion object {
        private val sScaleTypeArray = arrayOf(
            ScaleType.MATRIX,
            ScaleType.FIT_XY,
            ScaleType.FIT_START,
            ScaleType.FIT_CENTER,
            ScaleType.FIT_END,
            ScaleType.CENTER,
            ScaleType.CENTER_CROP,
            ScaleType.CENTER_INSIDE
        )

        private fun scaleTypeToScaleToFit(st: ScaleType): ScaleToFit {
            return when (st) {
                ScaleType.FIT_XY -> ScaleToFit.FILL
                ScaleType.FIT_START -> ScaleToFit.START
                ScaleType.FIT_CENTER -> ScaleToFit.CENTER
                ScaleType.FIT_END -> ScaleToFit.END
                else -> ScaleToFit.FILL
            }
        }
    }

    override fun setScaleType(scaleType: ScaleType?) {
        if (scaleType == null) {
            throw NullPointerException()
        }
        if (mScaleType != scaleType) {
            mScaleType = scaleType
            hasConfig = false
            requestLayout()
            invalidate()
        }
    }

    override fun getScaleType(): ScaleType {
        return mScaleType
    }

    /**
     * 设置放大倍数
     */
    var scale: Float
        get() = _scale
        set(value) {
            if (value < 1) {
                throw RuntimeException("GravityImageView's scale must not less then 1")
            }
            _scale = value
            hasConfig = false
            requestLayout()
            invalidate()
            onGravityChange(0f, 0f)
        }

    init {
        val attr = context.obtainStyledAttributes(
            attrs, R.styleable.GravityImageView, defStyleAttr, 0
        )
        _scale = attr.getString(R.styleable.GravityImageView_scale)?.toFloat() ?: 1f
        if (_scale < 1) {
            throw RuntimeException("GravityImageView's scale must not less then 1")
        }
        val index: Int = attr.getInt(R.styleable.GravityImageView_gScaleType, -1)
        if (index >= 0) {
            mScaleType = sScaleTypeArray[index]
        }
        attr.recycle()
        if (isInEditMode) {
            super.setScaleType(mScaleType)
        } else {
            super.setScaleType(ScaleType.MATRIX)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        configureBounds()
    }

    private fun configureBounds() {
        if (drawable == null || hasConfig || width == 0 || height == 0) return
        val drawMatrix: Matrix
        val dWidth = drawable.intrinsicWidth
        val dHeight = drawable.intrinsicHeight
        val vWidth = width - paddingLeft - paddingRight
        val vHeight = height - paddingTop - paddingBottom

        val fits = ((dWidth < 0 || vWidth == dWidth)
                && (dHeight < 0 || vHeight == dHeight))
        // 根据不同缩放类型进行缩放
        if (dWidth <= 0 || dHeight <= 0 || ScaleType.FIT_XY == mScaleType) {
            drawable.setBounds(0, 0, vWidth, vHeight)
            drawMatrix = Matrix()
            val scaleY = vHeight.toFloat() / dHeight.toFloat()
            val scaleX = vWidth.toFloat() / dWidth.toFloat()
            drawMatrix.postScale(scaleX, scaleY)
        } else {
            drawable.setBounds(0, 0, dWidth, dHeight)
            when {
                ScaleType.MATRIX == mScaleType -> {
                    drawMatrix = if (matrix.isIdentity) {
                        Matrix()
                    } else {
                        matrix
                    }
                }
                fits -> {
                    drawMatrix = Matrix()
                }
                ScaleType.CENTER == mScaleType -> {
                    drawMatrix = Matrix()
                    drawMatrix.postTranslate((vWidth - dWidth) * 0.5f, (vHeight - dHeight) * 0.5f)
                }
                ScaleType.CENTER_CROP == mScaleType -> {
                    drawMatrix = Matrix()
                    val scale: Float
                    var dx = 0f
                    var dy = 0f
                    if (dWidth * vHeight > vWidth * dHeight) {
                        scale = vHeight.toFloat() / dHeight.toFloat()
                        dx = (vWidth - dWidth * scale) * 0.5f
                    } else {
                        scale = vWidth.toFloat() / dWidth.toFloat()
                        dy = (vHeight - dHeight * scale) * 0.5f
                    }
                    drawMatrix.postScale(scale, scale)
                    drawMatrix.postTranslate(dx, dy)
                }
                ScaleType.CENTER_INSIDE == mScaleType -> {
                    drawMatrix = Matrix()
                    val dx: Float
                    val dy: Float
                    val scale: Float = if (dWidth <= vWidth && dHeight <= vHeight) {
                        1.0f
                    } else {
                        (vWidth.toFloat() / dWidth.toFloat()).coerceAtMost(vHeight.toFloat() / dHeight.toFloat())
                    }
                    dx = (vWidth - dWidth * scale) * 0.5f
                    dy = (vHeight - dHeight * scale) * 0.5f
                    drawMatrix.postScale(scale, scale)
                    drawMatrix.postTranslate(dx, dy)
                }
                else -> {
                    val temp = RectF()
                    val tempDst = RectF()
                    temp.set(0f, 0f, dWidth.toFloat(), dHeight.toFloat())
                    tempDst.set(0f, 0f, vWidth.toFloat(), vHeight.toFloat())
                    drawMatrix = Matrix()
                    drawMatrix.setRectToRect(
                        temp,
                        tempDst,
                        scaleTypeToScaleToFit(mScaleType)
                    )
                }
            }
        }
        // 由于图片经过放大了Scale倍数，为了居中需要移动一下
        originXOffset = vWidth * (scale - 1) / 2
        originYOffset = vHeight * (scale - 1) / 2
        originMatrix = drawMatrix
        originMatrix?.postScale(scale, scale)
        originMatrix?.postTranslate(-originXOffset, -originYOffset)
        imageMatrix = originMatrix
        hasConfig = true
    }


    override fun onGravityChange(x: Float, y: Float) {
        if (!hasConfig) return
        matrix.apply {
            set(originMatrix)
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