package org.zenbot.szolnok.timetable.backend.service.bus

/**
 * Indicates that there is no such bus for the supplied parameters
 */
class BusNotFoundException(
    override val message: String
) : RuntimeException(message)
