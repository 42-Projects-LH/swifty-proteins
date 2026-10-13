package com.example.swiftyproteins.ui.viewer.gl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural Unit Sphere Mesh for rendering 3D atoms in OpenGL ES 2.0.
 */
class SphereMesh(private val stacks: Int = 18, private val slices: Int = 18) {

    private val vertexBuffer: FloatBuffer
    private val normalBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer
    private val indexCount: Int

    init {
        val vertices = ArrayList<Float>()
        val normals = ArrayList<Float>()
        val indices = ArrayList<Short>()

        val pi = Math.PI.toFloat()
        val twoPi = (Math.PI * 2.0).toFloat()

        for (i in 0..stacks) {
            val phi = pi / 2.0f - i.toFloat() * pi / stacks.toFloat()
            val y = sin(phi)
            val r = cos(phi)

            for (j in 0..slices) {
                val theta = j.toFloat() * twoPi / slices.toFloat()
                val x = r * cos(theta)
                val z = r * sin(theta)

                vertices.add(x)
                vertices.add(y)
                vertices.add(z)

                // For a unit sphere centered at origin, normal vector equals position
                normals.add(x)
                normals.add(y)
                normals.add(z)
            }
        }

        for (i in 0 until stacks) {
            val k1 = (i * (slices + 1)).toShort()
            val k2 = ((i + 1) * (slices + 1)).toShort()

            for (j in 0 until slices) {
                val a = (k1 + j).toShort()
                val b = (k2 + j).toShort()
                val c = (k1 + j + 1).toShort()
                val d = (k2 + j + 1).toShort()

                if (i != 0) {
                    indices.add(a)
                    indices.add(c)
                    indices.add(b)
                }
                if (i != stacks - 1) {
                    indices.add(b)
                    indices.add(c)
                    indices.add(d)
                }
            }
        }

        indexCount = indices.size

        // Direct NIO buffers allocated natively
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
