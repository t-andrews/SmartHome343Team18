package com.smart.home.backend.model.simulationparameters;

import com.smart.home.backend.model.simulationparameters.location.PersonLocationPosition;
import lombok.Getter;
import lombok.Setter;

/**
 * Model class for the User.
 */
@Getter
@Setter
public class User {
    
    private UserProfile profile;
    private String name;
    private PersonLocationPosition location;
    
    /**
     * 3-parameter constructor.
     * @param profile user's profile
     * @param name user's name
     * @param location user's location
     */
    public User(UserProfile profile, String name, PersonLocationPosition location) {
        this.profile = profile;
        this.name = name;
        this.setLocation(location);
    }
    
}
