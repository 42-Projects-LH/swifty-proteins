package com.example.swiftyproteins.ui.viewer.gl

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewConfiguration
import com.example.swiftyproteins.data.model.Atom
import kotlin.math.abs

/**
 * Interactive GLSurfaceView with 1-finger rotation, 2-finger pinch zoom,
 * 2-finger panning, and single-tap raycast picking.
 */
class ProteinSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    val renderer = ProteinRenderer()

    var onAtomSelectedListener: ((Atom?) -> Unit)? = null

    private val scaleDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector

    private var previousX = 0f
    private var previousY = 0f
    private var downX = 0f
    private var downY = 0f
    private val touchSlop: Int

    private val touchScaleFactor = 180.0f / 450.0f // Sensitivity
    private val panScaleFactor = 0.02f

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY

        touchSlop = ViewConfiguration.get(context).scaledTouchSlop

        scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scale = detector.scaleFactor
                if (scale != 0f) {
                    val newDist = renderer.cameraDistance / scale
                    renderer.cameraDistance = newDist.coerceIn(1.5f, 150.0f)
                }
                return true
            }
        })

        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val tappedAtom = renderer.pickAtom(e.x, e.y)
                renderer.selectedAtom = tappedAtom
                onAtomSelectedListener?.invoke(tappedAtom)
                return true
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Feed pinch-to-zoom detector
        scaleDetector.onTouchEvent(event)
        // Feed tap detector
        gestureDetector.onTouchEvent(event)

        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = x
                downY = y
                previousX = x
                previousY = y
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = x - previousX
                val dy = y - previousY

                if (event.pointerCount == 1 && !scaleDetector.isInProgress) {
                    // Check if movement exceeds slop to avoid drift during tap
                    val distMoved = abs(x - downX) + abs(y - downY)
                    if (distMoved > touchSlop) {
                        renderer.rotationAngleY += dx * touchScaleFactor
                        val newPitch = renderer.rotationAngleX + dy * touchScaleFactor
                        // Clamp pitch to prevent flipping upside down
                        renderer.rotationAngleX = newPitch.coerceIn(-89.0f, 89.0f)
                    }
                } else if (event.pointerCount == 2 && !scaleDetector.isInProgress) {
                    // 2-finger pan
                    renderer.panX += dx * panScaleFactor * (renderer.cameraDistance / 20.0f)
                    renderer.panY -= dy * panScaleFactor * (renderer.cameraDistance / 20.0f)
                }

                previousX = x
                previousY = y
            }
        }

        return true
    }
}
