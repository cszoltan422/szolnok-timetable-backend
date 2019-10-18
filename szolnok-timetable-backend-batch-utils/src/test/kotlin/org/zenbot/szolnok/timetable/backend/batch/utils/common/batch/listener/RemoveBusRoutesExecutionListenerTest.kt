package org.zenbot.szolnok.timetable.backend.batch.utils.common.batch.listener

import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.springframework.batch.core.JobExecution
import org.zenbot.szolnok.timetable.backend.batch.utils.common.properties.TimetableProperties
import org.zenbot.szolnok.timetable.backend.batch.utils.common.properties.TimetableResourceProperties
import org.zenbot.szolnok.timetable.backend.domain.entity.bus.BusEntity
import org.zenbot.szolnok.timetable.backend.repository.BusRepository

class RemoveBusRoutesExecutionListenerTest {

    private lateinit var testSubject: RemoveBusRoutesExecutionListener

    @Mock
    private lateinit var busRepository: BusRepository

    private lateinit var properties: TimetableProperties

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        properties = TimetableProperties()
        properties.resource = TimetableResourceProperties()

        testSubject = RemoveBusRoutesExecutionListener(busRepository, properties)
    }

    @Test
    fun `beforeJob should remove all buses if selected buses is empty`() {
        // GIVEN
        val jobExecution = Mockito.mock(JobExecution::class.java)
        properties.resource.selectedBuses = emptyList()

        // WHEN
        testSubject.beforeJob(jobExecution)

        // THEN
        verify(busRepository).deleteAll()
        verify(busRepository, never()).delete(any(BusEntity::class.java))
        verify(busRepository, never()).findAll()
    }

    @Test
    fun `beforeJob should not remove the bus which is not present in the selected buses list`() {
        // GIVEN
        val jobExecution = Mockito.mock(JobExecution::class.java)
        properties.resource.selectedBuses = listOf("1")
        val buses = mutableListOf(BusEntity(busName = "2"))

        given(busRepository.findAll()).willReturn(buses)

        // WHEN
        testSubject.beforeJob(jobExecution)

        // THEN
        verify(busRepository).findAll()
        verify(busRepository, never()).deleteAll()
        verify(busRepository, never()).delete(any(BusEntity::class.java))
    }

    @Test
    fun `beforeJob should remove the bus which is present in the selected buses list`() {
        // GIVEN
        val jobExecution = Mockito.mock(JobExecution::class.java)
        properties.resource.selectedBuses = listOf("1")
        val bus = BusEntity(busName = "1")
        val buses = mutableListOf(bus)

        given(busRepository.findAll()).willReturn(buses)

        // WHEN
        testSubject.beforeJob(jobExecution)

        // THEN
        verify(busRepository).findAll()
        verify(busRepository).delete(bus)
        verify(busRepository, never()).deleteAll()
    }
}
