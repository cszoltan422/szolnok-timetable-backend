package org.zenbot.szolnok.timetable.batch.configuration.properties;

import lombok.Data;

@Data
public class TimetableResourceProperties {
    private String folder;
    private String fileExtension;
    private String commentSign;
}