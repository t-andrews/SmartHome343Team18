package com.smart.home.backend.model.simulationparameters.module.command;

import com.smart.home.backend.input.DoorInput;
import com.smart.home.backend.model.houselayout.HouseLayoutModel;
import com.smart.home.backend.model.houselayout.directional.Door;
import org.springframework.http.ResponseEntity;

public class DoorManagementCommand extends AbstractCommand<HouseLayoutModel,DoorInput,Door> {
    /**
     * Default Constructor
     */
    public DoorManagementCommand() {
        super("Door Management", true);
    }

    @Override
    public ResponseEntity<Door> execute(HouseLayoutModel houseLayoutModel, DoorInput doorInput) {
        return null;
    }
}