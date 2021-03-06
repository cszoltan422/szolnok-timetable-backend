package org.zenbot.szolnok.timetable.backend.batch.stops.configuration

import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.support.CompositeItemProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zenbot.szolnok.timetable.backend.batch.stops.batch.step.stops.processor.TimetableToBusStopWithBusesItemProcessor
import org.zenbot.szolnok.timetable.backend.batch.stops.batch.step.stops.writer.StopsWithBusesMongoItemWriter
import org.zenbot.szolnok.timetable.backend.batch.utils.common.batch.processor.JsoupDocumentToTimetableProcessor
import org.zenbot.szolnok.timetable.backend.batch.utils.common.batch.processor.UrlResourceToDocumentJsoupProcessor
import org.zenbot.szolnok.timetable.backend.batch.utils.common.batch.reader.UrlResourceItemReader
import org.zenbot.szolnok.timetable.backend.domain.entity.stop.BusStopWithBusesEntity

@Configuration
class SaveStopsWithBusesStepConfiguration(
    private val stepBuilderFactory: StepBuilderFactory,
    private val urlResourceItemReader: UrlResourceItemReader,
    private val stopsWithBusesMongoItemWriter: StopsWithBusesMongoItemWriter
) {

    @Bean
    fun saveBusStopsStep(
        jsoupProcessor: UrlResourceToDocumentJsoupProcessor,
        jsoupDocumentToTimetableProcessor: JsoupDocumentToTimetableProcessor,
        timetableToBusStopWithBusesItemProcessor: TimetableToBusStopWithBusesItemProcessor
    ): Step {
        return stepBuilderFactory.get("saveBusStopsStep")
                .chunk<String, BusStopWithBusesEntity>(1)
                .reader(urlResourceItemReader)
                .processor(compositeIemProcessor(
                        jsoupProcessor,
                        jsoupDocumentToTimetableProcessor,
                        timetableToBusStopWithBusesItemProcessor
                ))
                .writer(stopsWithBusesMongoItemWriter)
                .build()
    }

    @Bean
    fun compositeIemProcessor(
        jsoupProcessor: UrlResourceToDocumentJsoupProcessor,
        jsoupDocumentToTimetableProcessor: JsoupDocumentToTimetableProcessor,
        timetableToBusStopWithBusesItemProcessor: TimetableToBusStopWithBusesItemProcessor
    ): CompositeItemProcessor<String, BusStopWithBusesEntity> {
        val compositeItemProcessor = CompositeItemProcessor<String, BusStopWithBusesEntity>()
        compositeItemProcessor.setDelegates(listOf(
                jsoupProcessor,
                jsoupDocumentToTimetableProcessor,
                timetableToBusStopWithBusesItemProcessor
        ))
        return compositeItemProcessor
    }
}
