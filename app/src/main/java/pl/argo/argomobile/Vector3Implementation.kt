package pl.argo.argomobile

import geometry_msgs.Vector3
import org.ros.internal.message.RawMessage

class Vector3Implementation : Vector3 {
    private var x: Double
    private var y: Double
    private var z: Double

    override fun getX(): Double {
        return x
    }

    override fun setX(x: Double) {
        this.x = x
    }

    override fun getY(): Double {
        return y
    }

    override fun setY(y: Double) {
        this.y = y
    }

    override fun getZ(): Double {
        return z
    }

    override fun setZ(z: Double) {
        this.z = z
    }

    constructor() {
        x = 0.0
        y = 0.0
        z = 0.0
    }

    constructor(x: Double, y: Double, z: Double) {
        this.x = x
        this.y = y
        this.z = z
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as Vector3Implementation
        if (that.x.compareTo(x) != 0) return false
        return if (that.y.compareTo(y) != 0) false else that.z.compareTo(z) == 0
    }

    override fun hashCode(): Int {
        var result: Int
        var temp: Long
        temp = java.lang.Double.doubleToLongBits(x)
        result = (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(y)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(z)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        return result
    }

    override fun toRawMessage(): RawMessage? {
        return null
    }
}