package com.smart.home.backend.model.simulationParameters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * System Parameters Model
 */
@Getter
@Setter
public class SystemParameters {
    private double outsideTemp;
    private double insideTemp;
    private LocalDateTime date;

    /**
     * Constructor
     * @param outsideTemp temperature
     * @param insideTemp
     * @param date date and time
     */
    public SystemParameters(double outsideTemp, double insideTemp, LocalDateTime date) {
        this.outsideTemp = outsideTemp;
        this.insideTemp = insideTemp;
        this.date = date;
    }
}