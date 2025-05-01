package com.example.mebelar.domain.model.ar

import com.google.ar.core.Plane

data class IntersectionResult(
    val point: FloatArray,
    val distance: Float
)

data class Ray(
    val origin: FloatArray,
    val direction: FloatArray
)

// Расширение для Plane остается без изменений
fun Plane.hitTest(ray: Ray): IntersectionResult? {
    val planeNormal = floatArrayOf(0f, 1f, 0f)
    val planePoint = floatArrayOf(centerPose.tx(), centerPose.ty(), centerPose.tz())

    val denominator = planeNormal[0] * ray.direction[0] +
            planeNormal[1] * ray.direction[1] +
            planeNormal[2] * ray.direction[2]

    if (kotlin.math.abs(denominator) < 0.0001f) return null

    val w = floatArrayOf(
        planePoint[0] - ray.origin[0],
        planePoint[1] - ray.origin[1],
        planePoint[2] - ray.origin[2]
    )

    val numerator = w[0] * planeNormal[0] + w[1] * planeNormal[1] + w[2] * planeNormal[2]
    val t = numerator / denominator

    if (t < 0f) return null

    val intersectionPoint = floatArrayOf(
        ray.origin[0] + t * ray.direction[0],
        ray.origin[1] + t * ray.direction[1],
        ray.origin[2] + t * ray.direction[2]
    )

    if (this.type == Plane.Type.HORIZONTAL_UPWARD_FACING) {
        val extentX = this.extentX
        val extentZ = this.extentZ
        val dx = intersectionPoint[0] - planePoint[0]
        val dz = intersectionPoint[2] - planePoint[2]
        if (kotlin.math.abs(dx) > extentX / 2 || kotlin.math.abs(dz) > extentZ / 2) return null
    }

    return IntersectionResult(point = intersectionPoint, distance = t)
}