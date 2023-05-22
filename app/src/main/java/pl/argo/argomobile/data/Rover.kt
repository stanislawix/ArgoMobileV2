package pl.argo.argomobile.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Data

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

        fun name(name: String?): Builder {
            this.name = name
            return this
        }

        fun topicPrefix(topicPrefix: String?): Builder {
            this.topicPrefix = topicPrefix
            return this
        }

        fun build(): Rover {
            return Rover(id, name, topicPrefix)
        }
    }
}