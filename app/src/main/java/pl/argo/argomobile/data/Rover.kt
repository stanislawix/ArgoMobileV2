package pl.argo.argomobile.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ros.Topics

@Entity
data class Rover(
    @JvmField
    @PrimaryKey val id: Int, @JvmField
    @ColumnInfo(name = "name") val name: String?, @JvmField
    @ColumnInfo(name = "topic_prefix") val topicPrefix: String?
) {

    class Builder {
        private var id = 0
        private var name: String? = null
        private var topicPrefix: String? = null

        fun id(id: Int): Builder {
            this.id = id
            return this
        }

        fun name(name: String) = apply { this.name = name }
        fun topicPrefix(topicPrefix: String) = apply { this.topicPrefix = topicPrefix }
        fun build() = Rover(id, name, topicPrefix)
    }
}