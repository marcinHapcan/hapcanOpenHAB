/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HapcanBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marcin Wiatrak - Initial contribution
 */
@NonNullByDefault
public class HapcanBindingConstants {

    private static final String BINDING_ID = "hapcan";

    // bridge
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "ethernet_interface");

    // buttons
    public static final ThingTypeUID THING_TYPE_BUTTON_3_1_0 = new ThingTypeUID(BINDING_ID, "button_3_1_0");
    public static final ThingTypeUID THING_TYPE_BUTTON_3_1_1 = new ThingTypeUID(BINDING_ID, "button_3_1_1");
    public static final ThingTypeUID THING_TYPE_BUTTON_3_1_2 = new ThingTypeUID(BINDING_ID, "button_3_1_2");
    public static final ThingTypeUID THING_TYPE_BUTTON_3_1_3 = new ThingTypeUID(BINDING_ID, "button_3_1_3");
    public static final ThingTypeUID THING_TYPE_BUTTON_1_0_1_1 = new ThingTypeUID(BINDING_ID, "button_1_0_1_1");
    public static final ThingTypeUID THING_TYPE_BUTTON_1_0_1_21 = new ThingTypeUID(BINDING_ID, "button_1_0_1_21");
    public static final ThingTypeUID THING_TYPE_BUTTON_1_0_1_33 = new ThingTypeUID(BINDING_ID, "button_1_0_1_33");
    public static final Set<ThingTypeUID> THING_TYPES_BUTTON = Stream
            .of(THING_TYPE_BUTTON_3_1_0, THING_TYPE_BUTTON_3_1_1, THING_TYPE_BUTTON_3_1_2, THING_TYPE_BUTTON_3_1_3,
                    THING_TYPE_BUTTON_1_0_1_1, THING_TYPE_BUTTON_1_0_1_21, THING_TYPE_BUTTON_1_0_1_33)
            .collect(Collectors.toSet());
    public static final Set<ThingTypeUID> EXTENDED_CHANNELS_BUTTONS = Collections.singleton(THING_TYPE_BUTTON_3_1_3);

    // relays
    public static final ThingTypeUID THING_TYPE_RELAY_2OUTPUTS = new ThingTypeUID(BINDING_ID, "relay_2outputs");
    public static final ThingTypeUID THING_TYPE_RELAY_6OUTPUTS = new ThingTypeUID(BINDING_ID, "relay_6outputs");
    public static final ThingTypeUID THING_TYPE_RELAY_10OUTPUTS = new ThingTypeUID(BINDING_ID, "relay_10outputs");
    public static final Set<ThingTypeUID> THING_TYPES_RELAY = Stream
            .of(THING_TYPE_RELAY_2OUTPUTS, THING_TYPE_RELAY_6OUTPUTS, THING_TYPE_RELAY_10OUTPUTS)
            .collect(Collectors.toSet());
    public static final Set<ThingTypeUID> EXTENDED_CHANNELS_RELAYS = Collections.singleton(THING_TYPE_RELAY_10OUTPUTS);

    // temperature sensors
    public static final ThingTypeUID THING_TYPE_TEMP_SENSOR_1CHANNEL = new ThingTypeUID(BINDING_ID,
            "temperature_sensor_1channel");
    public static final Set<ThingTypeUID> THING_TYPES_TEMP_SENSOR = Collections
            .singleton(THING_TYPE_TEMP_SENSOR_1CHANNEL);

    // dimmers
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final Set<ThingTypeUID> THING_TYPES_DIMMER = Collections
            .singleton(HapcanBindingConstants.THING_TYPE_DIMMER);

    // blinds controllers
    public static final ThingTypeUID THING_TYPE_BLINDS_CONTROLLER_3CHANNELS = new ThingTypeUID(BINDING_ID,
            "blinds_controller_3channels");
    public static final ThingTypeUID THING_TYPE_BLINDS_CONTROLLER_4CHANNELS = new ThingTypeUID(BINDING_ID,
            "blinds_controller_4channels");
    public static final Set<ThingTypeUID> THING_TYPES_BLINDS_CONTROLLER = Stream
            .of(THING_TYPE_BLINDS_CONTROLLER_3CHANNELS, THING_TYPE_BLINDS_CONTROLLER_4CHANNELS)
            .collect(Collectors.toSet());

    // RGB controllers
    public static final ThingTypeUID THING_TYPE_RGB_CONTROLLER = new ThingTypeUID(BINDING_ID, "rgb_controller");
    public static final Set<ThingTypeUID> THING_TYPES_RGB_CONTROLLER = Collections
            .singleton(HapcanBindingConstants.THING_TYPE_RGB_CONTROLLER);

    // additional properties
    public static final String PROPERTY_BOOTLOADER_VERSION = "Bootloader Version";
    public static final String PROPERTY_SERIAL_NUMBER = "Serial Number";
    public static final String PROPERTY_MODULE_NAME = "Module Name";
    public static final String PROPERTY_CPU_INFO = "CPU Info";

    // thing type sets
    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(BRIDGE_THING_TYPE);
    public static final Set<ThingTypeUID> DEVICES_THING_TYPES_UIDS = Stream.of(THING_TYPES_RELAY, THING_TYPES_BUTTON,
            THING_TYPES_DIMMER, THING_TYPES_BLINDS_CONTROLLER, THING_TYPES_RGB_CONTROLLER)
            .flatMap(uids -> uids.stream()).collect(Collectors.toSet());

    // List of channel IDS
    public static final String CHANNEL_BUS_VOLTAGE = "bus_voltage";
    public static final String CHANNEL_CPU_VOLTAGE = "cpu_voltage";
    public static final String CHANNEL_OUTPUT = "output";
    public static final String CHANNEL_INPUT = "input";
    public static final String CHANNEL_TRIGGER = "trigger";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_THERMOSTAT_POSITION = "thermostat_position";
    public static final String CHANNEL_THERMOSTAT_STATE = "thermostat_state";
    public static final String CHANNEL_BLIND = "blind";
    public static final String CHANNEL_BLIND_MOVE = "blind_move";
}
