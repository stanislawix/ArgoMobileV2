package pl.argo.argomobile.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import lombok.Builder
import lombok.Getter

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

        fun id(id: Int): Builder {
            this.id = id
            return this
        }

        fun jointName(jointName: String?): Builder {
            this.jointName = jointName
            return this
        }

        fun roverId(roverId: Int): Builder {
            this.roverId = roverId
            return this
        }

        fun build(): Joint {
            return Joint(id, jointName, roverId)
        }
    }
}