package com.smart.home.backend.controller;

import com.smart.home.backend.constant.HeatingZonePeriod;
import com.smart.home.backend.constant.RoomHeatingMode;
import com.smart.home.backend.input.*;
import com.smart.home.backend.model.heating.DefaultTemperatures;
import com.smart.home.backend.model.heating.HeatingModel;
import com.smart.home.backend.model.heating.HeatingZone;
import com.smart.home.backend.model.houselayout.Room;
import com.smart.home.backend.model.simulationparameters.location.LocationPosition;
import com.smart.home.backend.model.simulationparameters.module.command.shh.*;
import com.smart.home.backend.service.OutputConsole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Smart Home Heating Controller
 */
@Getter
@Setter
@RestController
public class HeatingController {
    
    private HeatingModel heatingModel;

    @Autowired
    public HeatingController(HeatingModel heatingModel) {
        this.heatingModel = heatingModel;
    }
    
    /**
     * Retrieving the heating model
     * @return heating model
     */
    @GetMapping("/heating")
    public ResponseEntity<HeatingModel> getModel() {
        return new ResponseEntity<>(this.getHeatingModel(), HttpStatus.OK);
    }
    
    /**
     * Retrieving heating system status.
     * @return current heating system status
     */
    @GetMapping("heating/on")
    public ResponseEntity<Boolean> getSystemOn() {
        return new ResponseEntity<>(this.getHeatingModel().getOn(), HttpStatus.OK);
    }
    
    /**
     * Set heating on or off.
     * @param heatingOnInput input for setting heating on or off
     * @return new heating status (true: on, false: off)
     */
    @PutMapping("heating/on")
    public ResponseEntity<Boolean> setSystemOn(@RequestBody HeatingOnInput heatingOnInput) {
        return new SetHeatingOnCommand().execute(this.heatingModel, heatingOnInput);
    }
    
    /**
     * add heating zone
     * @param heatingZoneInput input for a heating zone
     * @return heating zone name
     */
    @PostMapping("/heating/zones")
    public ResponseEntity<HeatingZone> addHeatingZone(@RequestBody HeatingZoneInput heatingZoneInput) {
        return new AddHeatingZoneCommand().execute(this.heatingModel, heatingZoneInput);
    }
    
    /**
     * Retrieving all heating zones
     * @return found zone
     */
    @GetMapping("/heating/zones")
    public ResponseEntity<List<HeatingZone>> getHeatingZones() {
        return new ResponseEntity<>(this.getHeatingModel().getZones(), HttpStatus.OK);
    }
    
    /**
     * Retrieving a heating zone
     * @param zoneId id for a zone
     * @return found zone
     */
    @GetMapping("/heating/zones/{zoneId}")
    public ResponseEntity<HeatingZone> getHeatingZone(@PathVariable Integer zoneId) {
        HeatingZone foundZone = this.getHeatingModel().findZone(zoneId);
        
        if (foundZone == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        return new ResponseEntity<>(foundZone, HttpStatus.OK);
    }

    /**
     * remove heating zone
     * @param zoneId id for a zone
     * @return zone id
     */
    @DeleteMapping("/heating/zones/{zoneId}")
    public ResponseEntity<Integer> removeHeatingZone(@PathVariable Integer zoneId) {
        return new RemoveHeatingZoneCommand().execute(this.heatingModel, zoneId);
    }

    /**
     * add a room to a zone
     * @param heatingZoneRoomInput heating zone room input
     * @return room id
     */
    @PostMapping("heating/zones/{zoneId}/rooms")
    public ResponseEntity<Room> addRoomToZone(@PathVariable Integer zoneId, @RequestBody HeatingZoneRoomInput heatingZoneRoomInput) {
        heatingZoneRoomInput.setZoneId(zoneId);
        return new AddRoomToZoneCommand().execute(this.heatingModel, heatingZoneRoomInput);
    }

    /**
     * remove room from a zone
     * @param heatingZoneRoomInput heating zone room input
     * @return room id
     */
    @DeleteMapping("heating/zones/rooms")
    public ResponseEntity<Integer> deleteRoomFromZone(@RequestBody HeatingZoneRoomInput heatingZoneRoomInput) {
        return new RemoveRoomFromZoneCommand().execute(this.heatingModel, heatingZoneRoomInput);
    }

    /**
     * set temperature for a zone
     * @param heatingZoneTemperatureInput heating zone temperature input
     * @return heating zone temperature
     */
    @PutMapping("heating/zones/{zoneId}/periods/{period}/temperature")
    public ResponseEntity<Double> setZoneTemperature(
            @PathVariable Integer zoneId,
            @PathVariable HeatingZonePeriod period,
            @RequestBody HeatingZoneTemperatureInput heatingZoneTemperatureInput
    ) {
        heatingZoneTemperatureInput.setZoneId(zoneId);
        heatingZoneTemperatureInput.setHeatingZonePeriod(period);
        return new SetZoneTemperatureCommand().execute(this.heatingModel, heatingZoneTemperatureInput);
    }

    /**
     * read temperature from a given room
     * @param locationPosition room's position
     * @return temperature of room
     */
    @GetMapping("heating/rows/{rowId}/rooms/{roomId}/temperature")
    public ResponseEntity<Double> readRoomTemperature(LocationPosition locationPosition) {
        double roomTemperature = this.heatingModel.getRoomTemperature(locationPosition);
        return new ResponseEntity<>(roomTemperature, HttpStatus.OK);
    }

    /**
     * Override room's temperature
     * @param locationPosition room's location
     * @param heatingZoneRoomTemperature heatingZoneRoomTemperature input
     * @return overridden temperature
     */
    @PutMapping("heating/rows/{rowId}/rooms/{roomId}/temperature")
    public ResponseEntity<Double> overrideRoomTemperature(LocationPosition locationPosition, @RequestBody HeatingZoneRoomTemperatureInput heatingZoneRoomTemperature) {
        heatingZoneRoomTemperature.setLocationPosition(locationPosition);
        return new OverrideRoomTemperatureCommand().execute(this.heatingModel, heatingZoneRoomTemperature);
    }
    
    /**
     * Remove room's temperature override
     * @param locationPosition room's location
     * @return new room heating mode
     */
    @PutMapping("heating/rows/{rowId}/rooms/{roomId}/heatingmode")
    public ResponseEntity<RoomHeatingMode> removeRoomOverride(LocationPosition locationPosition) {
        Room foundRoom = this.getHeatingModel().getHouseLayoutModel().findRoom(locationPosition);
        
        if (foundRoom == null || !foundRoom.getHeatingMode().equals(RoomHeatingMode.OVERRIDDEN)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        OutputConsole.log("SHH | Removed override for " + foundRoom.getName() + "'s temperature");
        
        foundRoom.setHeatingMode(this.getHeatingModel().getHeatingMode());
        
        return new ResponseEntity<>(foundRoom.getHeatingMode(), HttpStatus.OK);
    }
    
    /**
     * Retrieve the default temperatures for seasons in away mode
     * @return default temperatures
     */
    @GetMapping("heating/awaymode")
    public ResponseEntity<DefaultTemperatures> getDefaultTemperatures() {
        return new ResponseEntity<>(this.getHeatingModel().getDefaultTemperatures(), HttpStatus.OK);
    }
    
    /**
     * Set default temperature for the winter season
     * @param temperatureInput new temperature input
     * @return new winter temperature
     */
    @PutMapping("/heating/awaymode/winter/temperature")
    public ResponseEntity<Double> setWinterTemperature(@RequestBody TemperatureInput temperatureInput) {
        this.getHeatingModel().getDefaultTemperatures().setWinterTemp(temperatureInput.getTemperature());
        return new ResponseEntity<>(temperatureInput.getTemperature(), HttpStatus.OK);
    }
    
    /**
     * Set default temperature for the summer season
     * @param temperatureInput new temperature input
     * @return new summer temperature
     */
    @PutMapping("/heating/awaymode/summer/temperature")
    public ResponseEntity<Double> setSummerTemperature(@RequestBody TemperatureInput temperatureInput) {
        this.getHeatingModel().getDefaultTemperatures().setSummerTemp(temperatureInput.getTemperature());
        return new ResponseEntity<>(temperatureInput.getTemperature(), HttpStatus.OK);
    }

    /**
     * Initialize temperature of all rooms to be equal to the outside temperature
     */
    @PostMapping("heating/temperature/init")
    public void initTemperature(){
        this.getHeatingModel().getHouseLayoutModel().getAllRooms().forEach(
                room -> room.setTemperature(this.getHeatingModel().getOutsideTemp())
        );
    }

}
