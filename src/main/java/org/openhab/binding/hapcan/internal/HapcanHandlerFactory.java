/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal;

import static org.openhab.binding.hapcan.HapcanBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.hapcan.handler.HapcanBlindHandler;
import org.openhab.binding.hapcan.handler.HapcanBridgeHandler;
import org.openhab.binding.hapcan.handler.HapcanButtonHandler;
import org.openhab.binding.hapcan.handler.HapcanDimmerHandler;
import org.openhab.binding.hapcan.handler.HapcanRelayHandler;
import org.openhab.binding.hapcan.handler.HapcanRgbHandler;
import org.openhab.binding.hapcan.handler.HapcanThingHandler;
import org.openhab.binding.hapcan.internal.discovery.HapcanDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link HapcanHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Marcin Wiatrak - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.hapcan")
public class HapcanHandlerFactory extends BaseThingHandlerFactory {

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return DEVICES_THING_TYPES_UIDS.contains(thingTypeUID) || BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID)) {
            HapcanBridgeHandler handler = new HapcanBridgeHandler((Bridge) thing);
            registerDiscoveryService(handler);
            return handler;
        } else if (THING_TYPES_BUTTON.contains(thingTypeUID)) {
            return new HapcanButtonHandler(thing);
        } else if (THING_TYPES_RELAY.contains(thingTypeUID)) {
            return new HapcanRelayHandler(thing);
        } else if (THING_TYPES_TEMP_SENSOR.contains(thingTypeUID)) {
            return new HapcanThingHandler(thing);
        } else if (THING_TYPES_DIMMER.contains(thingTypeUID)) {
            return new HapcanDimmerHandler(thing);
        } else if (THING_TYPES_BLINDS_CONTROLLER.contains(thingTypeUID)) {
            return new HapcanBlindHandler(thing);
        } else if (THING_TYPES_RGB_CONTROLLER.contains(thingTypeUID)) {
            return new HapcanRgbHandler(thing);
        }

        return null;
    }

    private synchronized void registerDiscoveryService(HapcanBridgeHandler bridgeHandler) {
        HapcanDiscoveryService discoveryService = new HapcanDiscoveryService(bridgeHandler);
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof HapcanBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }

}
