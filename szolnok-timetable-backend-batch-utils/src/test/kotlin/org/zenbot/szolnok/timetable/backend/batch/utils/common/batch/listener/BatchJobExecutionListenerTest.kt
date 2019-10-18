package org.zenbot.szolnok.timetable.backend.batch.utils.common.batch.listener

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.any
import org.mockito.BDDMockito.anyLong
import org.mockito.BDDMockito.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.never
import org.mockito.BDDMockito.times
import org.mockito.BDDMockito.verify
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobInstance
import org.springframework.batch.item.ExecutionContext
import org.zenbot.szolnok.timetable.backend.batch.utils.common.properties.TimetableProperties
import org.zenbot.szolnok.timetable.backend.batch.utils.common.properties.TimetableSelectorProperties
import org.zenbot.szolnok.timetable.backend.domain.entity.job.BatchJobEntity
import org.zenbot.szolnok.timetable.backend.domain.entity.job.BatchJobStatus
import org.zenbot.szolnok.timetable.backend.repository.BatchJobRepository
import java.time.LocalDateTime
import java.util.Optional

class BatchJobExecutionListenerTest {
    private lateinit var testSubject: BatchJobExecutionListener

    @Mock
    private lateinit var batchJobRepository: BatchJobRepository

    private lateinit var properties: TimetableProperties

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        properties = TimetableProperties()
        properties.selector = TimetableSelectorProperties()

        testSubject = BatchJobExecutionListener(batchJobRepository, properties)
    }

    @Test
    fun `beforeJob should save a new batchJob and add zero as id to the executionContext if the save returned null`() {
        // GIVEN
        val jobExecution = mock(JobExecution::class.java)
        val executionContext = mock(ExecutionContext::class.java)
        val jobInstance = mock(JobInstance::class.java)
        properties.resource.selectedBuses = emptyList()

        val argumentCaptor = ArgumentCaptor.forClass(BatchJobEntity::class.java)

        given(jobExecution.jobInstance).willReturn(jobInstance)
        given(jobInstance.jobName).willReturn("jobName")
        given(jobExecution.executionContext).willReturn(executionContext)
        given(batchJobRepository.saveAndFlush(any(BatchJobEntity::class.java))).willReturn(BatchJobEntity())

        // WHEN
        testSubject.beforeJob(jobExecution)

        // THEN
        verify(jobExecution, times(2)).jobInstance
        verify(jobInstance, times(2)).jobName
        verify(jobExecution).executionContext
        verify(executionContext).putLong("batchJobEntityId", 0L)

        verify(batchJobRepository).saveAndFlush(argumentCaptor.capture())

        assertThat(argumentCaptor.value.startTime).isBeforeOrEqualTo(LocalDateTime.now())
        assertThat(argumentCaptor.value.status).isEqualTo(BatchJobStatus.STARTED)
        assertThat(argumentCaptor.value.type).isEqualTo("jobName")
        assertThat(argumentCaptor.value.parameters).isEqualTo("[]")
        assertThat(argumentCaptor.value.finished).isFalse()
    }

    @Test
    fun `beforeJob should save a new batchJob and add the id to the executionContext`() {
        // GIVEN
        val jobExecution = mock(JobExecution::class.java)
        val executionContext = mock(ExecutionContext::class.java)
        val jobInstance = mock(JobInstance::class.java)
        properties.resource.selectedBuses = emptyList()

        val argumentCaptor = ArgumentCaptor.forClass(BatchJobEntity::class.java)

        given(jobExecution.jobInstance).willReturn(jobInstance)
        given(jobInstance.jobName).willReturn("jobName")
        given(jobExecution.executionContext).willReturn(executionContext)
        given(batchJobRepository.saveAndFlush(any(BatchJobEntity::class.java))).willReturn(BatchJobEntity(id = 10L))

        // WHEN
        testSubject.beforeJob(jobExecution)

        // THEN
        verify(jobExecution, times(2)).jobInstance
        verify(jobInstance, times(2)).jobName
        verify(jobExecution).executionContext
        verify(executionContext).putLong("batchJobEntityId", 10L)

        verify(batchJobRepository).saveAndFlush(argumentCaptor.capture())

        assertThat(argumentCaptor.value.startTime).isBeforeOrEqualTo(LocalDateTime.now())
        assertThat(argumentCaptor.value.status).isEqualTo(BatchJobStatus.STARTED)
        assertThat(argumentCaptor.value.type).isEqualTo("jobName")
        assertThat(argumentCaptor.value.parameters).isEqualTo("[]")
        assertThat(argumentCaptor.value.finished).isFalse()
    }

    @Test
    fun `afterJob should fetch the batchJobEntityId and not save the entity if not present`() {
        // GIVEN
        val jobExecution = mock(JobExecution::class.java)
        val executionContext = mock(ExecutionContext::class.java)

        given(jobExecution.executionContext).willReturn(executionContext)
        given(executionContext.getLong(anyString())).willReturn(0L)
        given(batchJobRepository.findById(anyLong())).willReturn(Optional.empty())

        // WHEN
        testSubject.afterJob(jobExecution)

        // THEN
        verify(jobExecution).executionContext
        verify(executionContext).getLong("batchJobEntityId")
        verify(batchJobRepository).findById(0L)
        verify(batchJobRepository, never()).save(any(BatchJobEntity::class.java))
    }

    @Test
    fun `afterJob should fetch the batchJobEntityId and update the existing entity`() {
        // GIVEN
        val jobExecution = mock(JobExecution::class.java)
        val executionContext = mock(ExecutionContext::class.java)
        val entity = BatchJobEntity()

        given(jobExecution.executionContext).willReturn(executionContext)
        given(executionContext.getLong(anyString())).willReturn(0L)
        given(batchJobRepository.findById(anyLong())).willReturn(Optional.of(entity))

        // WHEN
        testSubject.afterJob(jobExecution)

        // THEN
        verify(jobExecution).executionContext
        verify(executionContext).getLong("batchJobEntityId")
        verify(batchJobRepository).findById(0L)
        verify(batchJobRepository).save(entity)
        assertThat(entity.finished).isTrue()
        assertThat(entity.status).isEqualTo(BatchJobStatus.FINISHED)
        assertThat(entity.finishTime).isBeforeOrEqualTo(LocalDateTime.now())
    }
}