package pl.argo.argomobile.data.dto

data class RoverDto(
    val id: Int,
    val name: String,
    val topicPrefix: String,
    val jointNames: List<String>
) {
    class Builder {
        private var id: Int = 0
        private var name: String = ""
        private var topicPrefix: String = ""
        private var jointNames: List<String> = listOf()

        fun id(id: Int) = apply { this.id = id }
        fun name(name: String) = apply { this.name = name }
        fun topicPrefix(topicPrefix: String) = apply { this.topicPrefix = topicPrefix }
        fun jointNames(jointNames: List<String>) = apply { this.jointNames = jointNames }

        fun build() = RoverDto(id, name, topicPrefix, jointNames)
    }
}