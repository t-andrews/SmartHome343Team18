package com.smart.home.backend.config;

import com.smart.home.backend.model.heating.HeatingModel;
import com.smart.home.backend.model.houselayout.HouseLayoutModel;
import com.smart.home.backend.model.security.SecurityModel;
import com.smart.home.backend.model.simulationcontext.SimulationContextModel;
import com.smart.home.backend.model.simulationparameters.DateIncrementTask;
import com.smart.home.backend.model.simulationparameters.SimulationParametersModel;
import com.smart.home.backend.model.simulationparameters.SystemParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Configuration for the listeners within the system.
 */
@Configuration
public class ListenerConfig {
	
	private final HouseLayoutModel houseLayoutModel;
	private final SimulationParametersModel simulationParametersModel;
	private final SystemParameters systemParameters;
	private final SimulationContextModel simulationContextModel;
	private final SecurityModel securityModel;
	private final HeatingModel heatingModel;
	private final DateIncrementTask dateIncrementTask;
	
	@Autowired
	public ListenerConfig(
			HouseLayoutModel houseLayoutModel,
			SimulationParametersModel simulationParametersModel,
			SystemParameters systemParameters,
			SimulationContextModel simulationContextModel,
			SecurityModel securityModel,
			HeatingModel heatingModel,
			DateIncrementTask dateIncrementTask
	) {
		this.houseLayoutModel = houseLayoutModel;
		this.simulationParametersModel = simulationParametersModel;
		this.simulationContextModel = simulationContextModel;
		this.securityModel = securityModel;
		this.heatingModel = heatingModel;
		this.dateIncrementTask = dateIncrementTask;
		this.systemParameters = systemParameters;
	}
	
	@PostConstruct
	public void addListeners() {
		this.houseLayoutModel.addListener(securityModel);
		this.houseLayoutModel.addListener(heatingModel);
		
		this.systemParameters.addListener(securityModel);
		this.systemParameters.addListener(heatingModel);
		
		this.simulationContextModel.addListener(securityModel);
		
		this.securityModel.addListener(houseLayoutModel);
		this.securityModel.addListener(heatingModel);
		
		this.dateIncrementTask.addListener(securityModel);
		this.dateIncrementTask.addListener(heatingModel);
	}
	
}
