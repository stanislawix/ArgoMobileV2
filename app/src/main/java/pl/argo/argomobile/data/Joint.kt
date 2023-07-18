package pl.argo.argomobile.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Joint(
    @PrimaryKey
    val id: Int, @ColumnInfo(name = "joint_name")
    val jointName: String?, @ColumnInfo(name = "rover_id")
    val roverId: Int
) {
    class Builder {
        private var id: Int = 0
        private var jointName: String? = null
        private var roverId: Int = 0

        fun id(id: Int) = apply { this.id = id }
        fun jointName(jointName: String?) = apply { this.jointName = jointName }
        fun roverId(roverId: Int) = apply { this.roverId = roverId }
        fun build() = Joint(id, jointName, roverId)
    }
}