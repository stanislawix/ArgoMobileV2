package pl.argo.argomobile.data

data class Joint(
    val id: Int,
    val roverId: Int,
    val jointName: String?
) {
    class Builder {
        private var id: Int = 0
        private var roverId: Int = 0
        private var jointName: String? = null

        fun id(id: Int) = apply { this.id = id }
        fun roverId(roverId: Int) = apply { this.roverId = roverId }
        fun jointName(jointName: String?) = apply { this.jointName = jointName }
        fun build() = Joint(id, roverId, jointName)
    }
}