package com.example.swiftyproteins.ui.viewer.gl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural Unit Cylinder Mesh for rendering 3D chemical bonds in OpenGL ES 2.0.
 * Extends along the Z-axis from z = 0.0 to z = 1.0 with radius = 1.0.
 */
class CylinderMesh(private val segments: Int = 16) {

    private val vertexBuffer: FloatBuffer
    private val normalBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer
    private val indexCount: Int

    init {
        val vertices = ArrayList<Float>()
        val normals = ArrayList<Float>()
        val indices = ArrayList<Short>()

        val twoPi = (Math.PI * 2.0).toFloat()

        // Generate side cylinder wall
        for (i in 0..segments) {
            val theta = i.toFloat() * twoPi / segments.toFloat()
            val cosT = cos(theta)
            val sinT = sin(theta)

            // Bottom vertex (z = 0)
            vertices.add(cosT)
            vertices.add(sinT)
            vertices.add(0.0f)
            normals.add(cosT)
            normals.add(sinT)
            normals.add(0.0f)

            // Top vertex (z = 1)
            vertices.add(cosT)
            vertices.add(sinT)
            vertices.add(1.0f)
            normals.add(cosT)
            normals.add(sinT)
            normals.add(0.0f)
        }

        // Side indices
        for (i in 0 until segments) {
            val v0 = (i * 2).toShort()
            val v1 = (i * 2 + 1).toShort()
            val v2 = ((i + 1) * 2).toShort()
            val v3 = ((i + 1) * 2 + 1).toShort()

            indices.add(v0)
            indices.add(v2)
            indices.add(v1)

            indices.add(v2)
            indices.add(v3)
            indices.add(v1)
        }

        indexCount = indices.size

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                for (v in vertices) put(v)
                position(0)
            }
        }

        normalBuffer = ByteBuffer.allocateDirect(normals.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                for (n in normals) put(n)
                position(0)
            }
        }

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                for (idx in indices) put(idx)
                position(0)
            }
        }
    }

    fun draw(posAttrHandle: Int, normalAttrHandle: Int) {
        GLES20.glEnableVertexAttribArray(posAttrHandle)
        GLES20.glVertexAttribPointer(posAttrHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glEnableVertexAttribArray(normalAttrHandle)
        GLES20.glVertexAttribPointer(normalAttrHandle, 3, GLES20.GL_FLOAT, false, 0, normalBuffer)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        GLES20.glDisableVertexAttribArray(posAttrHandle)
        GLES20.glDisableVertexAttribArray(normalAttrHandle)
    }
}
