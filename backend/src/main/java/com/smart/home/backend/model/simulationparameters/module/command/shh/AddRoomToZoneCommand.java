package com.smart.home.backend.model.simulationparameters.module.command.shh;

import com.smart.home.backend.input.HeatingZoneRoomInput;
import com.smart.home.backend.model.heating.HeatingModel;
import com.smart.home.backend.model.heating.HeatingZone;
import com.smart.home.backend.model.heating.RoomAlreadyInZoneException;
import com.smart.home.backend.model.houselayout.Room;
import com.smart.home.backend.model.simulationparameters.location.LocationPosition;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Adding room to a zone command
 */
public class AddRoomToZoneCommand extends SHHAbstractCommand<HeatingModel, HeatingZoneRoomInput, Room>{

    public AddRoomToZoneCommand() {
        super("Adding room to zone");
    }
    
    @Override
    public ResponseEntity<Room> execute(HeatingModel heatingModel, HeatingZoneRoomInput heatingZoneRoomInput) {
        HeatingZone zone = heatingModel.findZone(heatingZoneRoomInput.getZoneId());
        Room foundRoom = heatingModel.getHouseLayoutModel().findRoom(new LocationPosition(heatingZoneRoomInput.getRowId(), heatingZoneRoomInput.getRoomId()));
        
        if (foundRoom != null) {
            try {
                heatingModel.addRoomToZone(zone, foundRoom);
                this.logAction("Added " + foundRoom.getName() + " to zone " + zone.getName());
            } catch (RoomAlreadyInZoneException e) {
                this.logAction(e.getMessage());
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        } else {
            this.logAction("Room not found and could not be added");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(foundRoom, HttpStatus.OK);
    }
    
}
