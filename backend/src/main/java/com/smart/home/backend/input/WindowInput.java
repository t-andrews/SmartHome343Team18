package com.smart.home.backend.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smart.home.backend.constant.Direction;
import com.smart.home.backend.constant.WindowState;

import com.smart.home.backend.model.simulationparameters.location.RoomItemLocationPosition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WindowInput {
    
    @JsonProperty("direction")
    private Direction direction;

    @JsonProperty("state")
    private WindowState state;
    
    private RoomItemLocationPosition location;
}
