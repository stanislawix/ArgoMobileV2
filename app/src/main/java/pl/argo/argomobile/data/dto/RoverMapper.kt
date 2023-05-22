package pl.argo.argomobile.data.dto

import pl.argo.argomobile.data.Joint
import pl.argo.argomobile.data.Rover
import java.util.stream.Collectors
import kotlin.streams.toList

class RoverMapper {
    fun toDto(rover: Rover, roverJoints: List<Joint?>): RoverDto = RoverDto.Builder()
        .id(rover.id)
        .name(rover.name!!)
        .topicPrefix(rover.topicPrefix!!)
        .jointNames(
            roverJoints.stream().map { it?.jointName!! }
                .collect(Collectors.toList())
        )
        .build()
}