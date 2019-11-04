package org.zenbot.szolnok.timetable.backend.batch.bus.configuration

import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.support.CompositeItemProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.zenbot.szolnok.timetable.backend.batch.bus.batch.step.bus.processor.TimetableToBusItemProcessor
import org.zenbot.szolnok.timetable.backend.batch.bus.batch.step.bus.writer.BusRepositoryItemWriter
import org.zenbot.szolnok.timetable.backend.batch.utils.common.batch.processor.JsoupDocumentToTimetableProcessor
import org.zenbot.szolnok.timetable.backend.batch.utils.common.batch.processor.UrlResourceToDocumentJsoupProcessor
import org.zenbot.szolnok.timetable.backend.batch.utils.common.batch.reader.UrlResourceItemReader
import org.zenbot.szolnok.timetable.backend.domain.entity.bus.BusEntity
import java.util.Arrays

@Configuration
open class SaveBusStepConfiguration(
    private val stepBuilderFactory: StepBuilderFactory,
    private val urlResourceItemReader: UrlResourceItemReader,
    private val busRepositoryItemWriter: BusRepositoryItemWriter,
    private val transactionManager: PlatformTransactionManager
) {

    @Bean
    fun saveBusStep(
        jsoupProcessor: UrlResourceToDocumentJsoupProcessor,
        documentToTimetableProcessor: JsoupDocumentToTimetableProcessor,
        timetableToBusItemProcessor: TimetableToBusItemProcessor
    ): Step {
        return stepBuilderFactory.get("saveBusStep")
                .transactionManager(transactionManager)
                .chunk<String, BusEntity>(1)
                .reader(urlResourceItemReader)
                .processor(
                        compositeItemProcessor(
                                jsoupProcessor,
                                documentToTimetableProcessor,
                                timetableToBusItemProcessor)
                )
                .writer(busRepositoryItemWriter)
                .build()
    }

    @Bean
    fun compositeItemProcessor(
        jsoupProcessor: UrlResourceToDocumentJsoupProcessor,
        documentToTimetableProcessor: JsoupDocumentToTimetableProcessor,
        timetableToBusItemProcessor: TimetableToBusItemProcessor
    ): CompositeItemProcessor<String, BusEntity> {
        val compositeItemProcessor = CompositeItemProcessor<String, BusEntity>()
        compositeItemProcessor.setDelegates(
                Arrays.asList(
                        jsoupProcessor,
                        documentToTimetableProcessor,
                        timetableToBusItemProcessor
                )
        )
        return compositeItemProcessor
    }
}
