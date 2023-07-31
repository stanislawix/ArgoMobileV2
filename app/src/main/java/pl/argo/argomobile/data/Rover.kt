package pl.argo.argomobile.data

data class Rover(
    val id: Int,
    val name: String?,
    val topicPrefix: String?,
    val jointNames: List<String>
) {

    class Builder {
        private var id = 0
        private var name: String? = null
        private var topicPrefix: String? = null
        private var jointNames: List<String> = emptyList()

        fun id(id: Int) = apply { this.id = id }
        fun name(name: String) = apply { this.name = name }
        fun topicPrefix(topicPrefix: String) = apply { this.topicPrefix = topicPrefix }
        fun jointNames(jointNames: List<String>) = apply { this.jointNames = jointNames }
        fun build() = Rover(id, name, topicPrefix, jointNames)
    }
}