package org.zenbot.szolnok.timetable.batch.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@NoArgsConstructor
@AllArgsConstructor
public class BusStop {

    @Id
    private String id;
    private String busStopName;
    private Schedule workDaySchedule;
    private Schedule saturdaySchedule;
    private Schedule sundaySchedule;

}
