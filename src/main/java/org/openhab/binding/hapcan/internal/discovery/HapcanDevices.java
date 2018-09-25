/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.hapcan.HapcanBindingConstants;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanDevices {

    @SuppressWarnings("serial")
    public static final Map<String, ThingTypeUID> HAPCAN_DEVICES = Collections
            .unmodifiableMap(new HashMap<String, ThingTypeUID>() {
                {
                    // Button
                    put("UNIV 3.1.0.x", HapcanBindingConstants.THING_TYPE_BUTTON_3_1_0);
                    put("UNIV 3.1.1.x", HapcanBindingConstants.THING_TYPE_BUTTON_3_1_1);
                    put("UNIV 3.1.2.x", HapcanBindingConstants.THING_TYPE_BUTTON_3_1_2);
                    put("UNIV 3.1.3.x", HapcanBindingConstants.THING_TYPE_BUTTON_3_1_3);
                    put("UNIV 1.0.1.1", HapcanBindingConstants.THING_TYPE_BUTTON_1_0_1_1);
                    put("UNIV 1.0.1.21", HapcanBindingConstants.THING_TYPE_BUTTON_1_0_1_21);
                    put("UNIV 1.0.1.33", HapcanBindingConstants.THING_TYPE_BUTTON_1_0_1_33);

                    // Relay
                    put("UNIV 3.2.1.x", HapcanBindingConstants.THING_TYPE_RELAY_6OUTPUTS);
                    put("UNIV 3.2.2.x", HapcanBindingConstants.THING_TYPE_RELAY_6OUTPUTS);
                    put("UNIV 3.2.3.x", HapcanBindingConstants.THING_TYPE_RELAY_6OUTPUTS);
                    put("UNIV 3.2.4.x", HapcanBindingConstants.THING_TYPE_RELAY_6OUTPUTS);
                    put("UNIV 3.2.5.x", HapcanBindingConstants.THING_TYPE_RELAY_6OUTPUTS);
                    put("UNIV 3.9.0.x", HapcanBindingConstants.THING_TYPE_RELAY_10OUTPUTS);
                    put("UNIV 1.0.2.3", HapcanBindingConstants.THING_TYPE_RELAY_6OUTPUTS);
                    put("UNIV 1.0.2.4", HapcanBindingConstants.THING_TYPE_RELAY_6OUTPUTS);
                    put("UNIV 1.0.2.10", HapcanBindingConstants.THING_TYPE_RELAY_2OUTPUTS);
                    put("UNIV 1.0.2.11", HapcanBindingConstants.THING_TYPE_RELAY_6OUTPUTS);
                    put("UNIV 1.0.2.23", HapcanBindingConstants.THING_TYPE_RELAY_6OUTPUTS);

                    // Temperature sensor
                    put("UNIV 1.0.4.0", HapcanBindingConstants.THING_TYPE_TEMP_SENSOR_1CHANNEL);

                    // Dimmer
                    put("UNIV 3.6.0.x", HapcanBindingConstants.THING_TYPE_DIMMER);
                    put("UNIV 1.0.6.11", HapcanBindingConstants.THING_TYPE_DIMMER);
                    put("UNIV 1.0.6.22", HapcanBindingConstants.THING_TYPE_DIMMER);

                    // Blinds controller
                    put("UNIV 3.7.0.x", HapcanBindingConstants.THING_TYPE_BLINDS_CONTROLLER_3CHANNELS);
                    put("UNIV 1.0.7.1", HapcanBindingConstants.THING_TYPE_BLINDS_CONTROLLER_4CHANNELS);

                    // RGB controller
                    put("UNIV 3.8.0.x", HapcanBindingConstants.THING_TYPE_RGB_CONTROLLER);
                    put("UNIV 1.0.8.12", HapcanBindingConstants.THING_TYPE_RGB_CONTROLLER);
                }
            });
}
