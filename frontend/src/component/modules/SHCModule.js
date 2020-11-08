import React from "react";
import "../../style/Modules.css";
import HouseLayoutService from "../../service/HouseLayoutService";
import Select from "react-select";
import {Container, Button, Col, Row, ListGroup} from "react-bootstrap";
import ParametersService from "../../service/ParametersService";
import Command from "./Command";

const ITEMS = ["Window", "Light", "Door"];
const OUTSIDE = ["Backyard", "Entrance"];

export default class SHCModule extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            locations: [],
            loaded: false
        };

        this.setup = this.setup.bind(this);
        this.onSelectedLocation = this.onSelectedLocation.bind(this);
        this.itemSelected = this.itemSelected.bind(this);
        this.openCloseWindow = this.openCloseWindow.bind(this);
        this.modifyLightState = this.modifyLightState.bind(this);
        this.openCloseDoor = this.openCloseDoor.bind(this);
        this.autoMode = this.autoMode.bind(this);
		this.onSelectedItem = this.onSelectedItem.bind(this);
    }

    async componentDidMount() {
        window.addEventListener("updatePermissions", async () => {
            await this.setup()
        });

        await this.setup();
    }

    async setup() {
        await this.setState({
            locations: await HouseLayoutService.getAllLocations(),
            user: await ParametersService.getUser(),
            selectedLocation: null,
            selectedWindow: null,
            selectedDoor: null,
            loaded: true
        });
    }

    async onSelectedLocation(evt) {
        await this.setState({
            locations: await HouseLayoutService.getAllLocations(),
            selectedLocation: null,
            selectedWindow: null,
            selectedDoor: null,
            loaded: true,
            selectedWindowItem: false,
            selectedLightItem: false,
            selectedDoorItem: false,
            selectedPersonItem: false,
        });

        const windows = OUTSIDE.includes(evt.label) ? [] : evt.value.windows.map(w => {
            return {
                value: w,
                label: w.direction
            };
        });

        const doors = OUTSIDE.includes(evt.label) ? [] : evt.value.doors.map(d => {
            return {
                value: d,
                label: d.direction
            };
        })

        await this.setState({
            selectedLocation: {
                light: evt.value.light,
                rowId: evt.value.rowId,
                roomId: evt.value.roomId,
                label: evt.label
            },
            windows: windows,
            doors: doors,
            selectedWindow: null,
            selectedPerson: null,
			selectedDoor: null
        });
    }

    async onSelectedItem(item, evt) {
        await this.setState({
            [`selected${item}`]: evt
        });
    }

    async openCloseWindow(windowState) {
        if (windowState === "OPEN") {
            await HouseLayoutService.openWindow(this.state.selectedLocation.rowId, this.state.selectedLocation.roomId, this.state.selectedWindow.value.id)
        } else {
            await HouseLayoutService.unblockWindow(this.state.selectedLocation.rowId, this.state.selectedLocation.roomId, this.state.selectedWindow.value.id)
        }
        this.setState({
            selectedWindow: {
                ...this.state.selectedWindow,
                value: {
                    ...this.state.selectedWindow.value,
                    state: windowState
                }
            }
        });
        window.dispatchEvent(new Event("updateLayout"));
    }

    async modifyLightState(lightState) {

        const action = OUTSIDE.includes(this.state.selectedLocation.label) ?
            async () => HouseLayoutService.modifyOutsideLightState({ location: this.state.selectedLocation.label, state: lightState })
            :
            async () => HouseLayoutService.modifyRoomLightState(this.state.selectedLocation.rowId, this.state.selectedLocation.roomId, { state: lightState })

        await action().then(async () =>{
            await this.setState({
                selectedLocation: {
                    ...this.state.selectedLocation,
                    light: {
                        state: lightState
                    }
                }
            });
        });

        window.dispatchEvent(new Event("updateLayout"));
    }

    async openCloseDoor(doorState) {
        await HouseLayoutService.changeDoorState(
            this.state.selectedLocation.rowId,
            this.state.selectedLocation.roomId,
            this.state.selectedDoor.value.id,
            { state: doorState }
        )
		.then(async () => {
			await this.setState({
	            selectedDoor: {
	                ...this.state.selectedDoor,
	                value: {
	                    ...this.state.selectedDoor.value,
	                    state: doorState
	                }
	            }
	        });
		});

		window.dispatchEvent(new Event("updateLayout"));
    }

    async autoMode(mode) {
        if (mode) {
            console.log("automode set");
        } else {
            console.log("automode removed");
        }
    }

    async itemSelected(item) {
        const state = {
            selectedWindowItem: false,
            selectedLightItem: false,
            selectedDoorItem: false
        }

        state[`selected${item}Item`] = true;

        await this.setState(state)
    }

    render() {
        return (
            <Container>
                {
                    this.state.loaded ?
                        <div className="Module">
                            <br/>
                            Locations
                            <Select
                                styles={{
                                    option: provided => ({...provided, width: "100%"}),
                                    menu: provided => ({...provided, width: "50%"}),
                                    control: provided => ({...provided, width: "50%"}),
                                    singleValue: provided => provided
                                }}
                                options={this.state.locations}
                                onChange={this.onSelectedLocation}
                            />
                            <br/>
                            {
                                this.state.selectedLocation !== null ?
									<Container>
										<Row>
											<Col>
												<ListGroup>
													{
                                                        !OUTSIDE.includes(this.state.selectedLocation.label) ?
															ITEMS.map((item) =>
																<ListGroup.Item
                                                                    key={ITEMS.indexOf(item)} className="ItemsTable"
                                                                    bsPrefix="list-group-item py-1" action
                                                                    onClick={() => this.itemSelected(item)}
                                                                    variant="dark">{item}</ListGroup.Item
                                                                >
															)
															:
															[
																<ListGroup.Item
                                                                    key={ITEMS.indexOf("Light")}
                                                                    className="ItemsTable"
                                                                    bsPrefix="list-group-item py-1" action
                                                                    onClick={() => this.itemSelected("Light")}
                                                                    variant="dark">Light</ListGroup.Item
                                                                >
															]
													}
												</ListGroup>
											</Col>
										</Row>
										{
											this.state.selectedWindowItem ?
												<Row>
													{
														!OUTSIDE.includes(this.state.selectedLocation.label) ?
															<Col>
                                                                <Command
                                                                    name="Window management"
                                                                    location={this.state.selectedLocation}
                                                                >
                                                                    Windows
                                                                    <Select
                                                                        styles={{
                                                                            option: provided => ({...provided, width: "50%"}),
                                                                            menu: provided => ({...provided, width: "50%"}),
                                                                            control: provided => ({...provided, width: "50%"}),
                                                                            singleValue: provided => provided
                                                                        }}
                                                                        options={this.state.windows}
                                                                        value={this.state.selectedWindow}
                                                                        onChange={(evt) => this.onSelectedItem("Window", evt)}
                                                                    />
                                                                    {
                                                                        this.state.selectedWindow !== null ?
                                                                            <div>

                                                                                    {
                                                                                        this.state.selectedWindow.value.state !== "BLOCKED" ?
                                                                                            this.state.selectedWindow.value.state === "CLOSED" ?
                                                                                                <Button
                                                                                                    onClick={() => this.openCloseWindow("OPEN")}
                                                                                                    variant="secondary"
                                                                                                    size="sm">Open</Button>
                                                                                                :
                                                                                                <Button
                                                                                                    onClick={() => this.openCloseWindow("CLOSED")}
                                                                                                    variant="secondary"
                                                                                                    size="sm">Close</Button>
                                                                                            : null
                                                                                    }
                                                                            </div>
                                                                            : null
                                                                    }
                                                                </Command>
                                                            </Col>
															: null
													}
												</Row>
												: null
										}
										{
											this.state.selectedLightItem ?
												<Command
													name="Light management"
													location={this.state.selectedLocation}
												>
													<Row>
														<Col>
															Light
															{
																this.state.selectedLocation.light.state === "OFF" ?
																	<div>
																		<Button onClick={() => this.modifyLightState("ON")}
																				variant="secondary" size="md">On</Button>
																	</div>
																	:
																	<div>
																		<Button onClick={() => this.modifyLightState("OFF")}
																				variant="secondary" size="md">Off</Button>
																	</div>
															}
														</Col>
														<Col>
															<div style={{margin: "25px"}}>
																<input type="checkbox" id="autoMode" name="autoMode" value="true"/>
																<label>Enable Auto Mode</label>
															</div>
														</Col>
													</Row>
												</Command>
												: null
										}
                                        {
                                            this.state.selectedDoorItem ?
                                                <Row>
                                                    {
                                                        !OUTSIDE.includes(this.state.selectedLocation.label) ?
                                                            <Col>
                                                                <Command
                                                                    name="Door management"
                                                                    location={this.state.selectedLocation}
                                                                >
                                                                    Doors
                                                                    <Select
                                                                        styles={{
                                                                            option: provided => ({...provided, width: "50%"}),
                                                                            menu: provided => ({...provided, width: "50%"}),
                                                                            control: provided => ({...provided, width: "50%"}),
                                                                            singleValue: provided => provided
                                                                        }}
                                                                        options={this.state.doors}
                                                                        value={this.state.selectedDoor}
                                                                        onChange={(evt) => this.onSelectedItem("Door", evt)}
                                                                    />
                                                                    {
                                                                        this.state.selectedDoor !== null ?
                                                                            <div>
                                                                                    {
                                                                                        this.state.selectedDoor.value.state !== "LOCKED" ?
                                                                                            <Button
                                                                                                onClick={() => this.openCloseDoor("LOCKED")}
                                                                                                variant="secondary"
                                                                                                size="sm">Lock</Button>
                                                                                            :
                                                                                            <Button
                                                                                                onClick={() => this.openCloseDoor(this.state.selectedDoor.value.state === "CLOSED"? "OPEN":"CLOSED")}
                                                                                                variant="secondary"
                                                                                                size="sm">Unlock</Button>
                                                                                    }
                                                                                    {
                                                                                        this.state.selectedDoor.value.state !== "LOCKED" ?
                                                                                            this.state.selectedDoor.value.state === "CLOSED" ?
                                                                                                <Button
                                                                                                    onClick={() => this.openCloseDoor("OPEN")}
                                                                                                    variant="secondary"
                                                                                                    size="sm">Open</Button>
                                                                                                :
                                                                                                <Button
                                                                                                    onClick={() => this.openCloseDoor("CLOSED")}
                                                                                                    variant="secondary"
                                                                                                    size="sm">Close</Button>
                                                                                            : null
                                                                                    }
                                                                            </div>
                                                                            : null
                                                                    }
                                                                </Command>
                                                            </Col>
                                                            : null
                                                    }
                                                </Row>
                                                : null
                                        }
									</Container>
                                    : null
                            }
                        </div>
						: null
                }
            </Container>
        );
    }

}
