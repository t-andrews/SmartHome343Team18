package com.smart.home.backend.model.heating;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smart.home.backend.constant.HeatingZonePeriod;
import com.smart.home.backend.constant.RoomHeatingMode;
import com.smart.home.backend.constant.WindowState;
import com.smart.home.backend.model.ModelObject;
import com.smart.home.backend.model.houselayout.Room;
import com.smart.home.backend.model.houselayout.directional.Window;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class for a heating zone.
 */
@Getter
@Setter
@SuperBuilder
public class HeatingZone extends ModelObject {
	
	private static final Double INCREMENT_VALUE_HAVC = 0.1;
	private static final Double INCREMENT_VALUE = 0.05;
	
	@Builder.Default
	private String name = "";
	
	@Builder.Default
	private List<Room> rooms = new ArrayList<>();
	
	@Builder.Default
	@JsonIgnore
	private HeatingZonePeriods periods = new HeatingZonePeriods();
	
	/**
	 * Adds a room to the rooms list.
	 * @param room room to add
	 * @return Whether the room was added
	 */
	public boolean addRoom(Room room) {
		if (this.getRooms().contains(room)) {
			return false;
		}
		
		return this.getRooms().add(room);
	}
	
	/**
	 * Removes a room from the rooms list.
	 * @param room room to remove
	 * @return Removed room
	 */
	public Room removeRoom(Room room) {
		this.getRooms().remove(room);
		return room;
	}
	
	/**
	 * Adjusts rooms' temperatures according to the target temperature.
	 */
	public void adjustRoomTemperatures(LocalDateTime date, RoomHeatingMode globalHeatingMode, double defaultTemperature, Double outsideTemp, boolean isSummer) {
		double targetTemperature = this.determineTargetTemperature(date, globalHeatingMode, defaultTemperature);
		for (Room room: rooms) {
			if (!isSummerBreeze(outsideTemp, room, isSummer)){
				room.setHavc(isHavcOn(outsideTemp, targetTemperature, room));
			}
			else{
				room.setHavc(false);
				for (Window window : room.getWindows()) {
					window.setState(WindowState.OPEN);
				}
			}
			if (!room.getHeatingMode().equals(RoomHeatingMode.OVERRIDDEN)) {
				double tempDelta = (room.getHavc() ? targetTemperature : outsideTemp) - room.getTemperature();
				int multiplier = 0;
				double increment = (room.getHavc() ? INCREMENT_VALUE_HAVC : INCREMENT_VALUE);
				if (tempDelta <= -increment) {
					multiplier = -1;
				} else if (tempDelta >= increment) {
					multiplier = 1;
				}

				room.setTemperature(room.getTemperature() + multiplier * increment);
			}
		}
	}
	
	/**
	 * Method to determine if the HAVC should be on or off
	 * @param outsideTemp
	 * @param targetTemperature
	 * @param room
	 * @return true if HAVC should on and false if it should be off
	 */
	private boolean isHavcOn(Double outsideTemp, double targetTemperature, Room room) {
		boolean targetReached = Math.abs(room.getTemperature() - targetTemperature) <= 0.1;
		boolean outsideReached = Math.abs(room.getTemperature() - outsideTemp) <= 0.25;
		return (room.getHavc() && !targetReached) || (!room.getHavc() && outsideReached);
	}

	private boolean isSummerBreeze(Double outsideTemp, Room room, boolean isSummer){
		if (isSummer && (outsideTemp - room.getTemperature() < 0) && room.getHeatingMode() != RoomHeatingMode.AWAY){
			for (Window window : room.getWindows()) {
				if (window.getState() == WindowState.BLOCKED){
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Determine which temperature to use.
	 * @param date current date
	 * @param globalHeatingMode the system's current global heating mode
	 * @param defaultTemperature default temperature for AWAY mode
	 * @return Which temperature to use
	 */
	private double determineTargetTemperature(LocalDateTime date, RoomHeatingMode globalHeatingMode, double defaultTemperature) {
		int hour = date.getHour();
		double temperature;
		
		if (globalHeatingMode.equals(RoomHeatingMode.ZONE)) {
			HeatingZonePeriod period;
			if (hour >= 5 && hour <= 11) {
				period = HeatingZonePeriod.MORNING;
			} else if (hour > 11 && hour <= 21) {
				period = HeatingZonePeriod.AFTERNOON;
			} else {
				period = HeatingZonePeriod.NIGHT;
			}
			temperature = this.getPeriods().getTargetTemperature(period);
		} else {
			temperature = defaultTemperature;
		}
		
		return temperature;
	}
	
	/**
	 * Json creator for the period maps
	 * @return periodMap
	 */
	@JsonCreator
	@JsonProperty("periods")
	public Map<HeatingZonePeriod, Double> getPeriodMap() {
		return this.getPeriods().getPeriodMap();
	}
	
}
