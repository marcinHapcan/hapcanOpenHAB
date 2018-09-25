/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.discovery;

import static org.openhab.binding.hapcan.HapcanBindingConstants.DEVICES_THING_TYPES_UIDS;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.hapcan.handler.HapcanBridgeHandler;
import org.openhab.binding.hapcan.internal.HapcanDeviceInfo;
import org.openhab.binding.hapcan.internal.HapcanDiscoveryHandler;
import org.openhab.binding.hapcan.internal.messages.HapcanFrameType;
import org.openhab.binding.hapcan.internal.messages.HapcanRecvMessage;
import org.openhab.binding.hapcan.internal.messages.HapcanSystemMessage;
import org.openhab.binding.hapcan.internal.messages.commands.HapcanSystemCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for HAPCAN Modules.
 *
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanDiscoveryService extends AbstractDiscoveryService implements HapcanDiscoveryHandler {
    private static final int DISCOVER_TIMEOUT_SECONDS = 5;

    private static final String PROPERTY_GROUP_NUMBER = "groupNumber";
    private static final String PROPERTY_MODULE_NUMBER = "moduleNumber";

    private final Logger logger = LoggerFactory.getLogger(HapcanDiscoveryService.class);

    HapcanBridgeHandler bridge;
    AtomicBoolean capture = new AtomicBoolean();
    private Map<Integer, HapcanDeviceInfo> devices = new HashMap<>();

    /**
     * Creates the discovery service for the given handler
     */
    public HapcanDiscoveryService(HapcanBridgeHandler bridge) {
        super(DEVICES_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS, true);
        this.bridge = bridge;
    }

    @Override
    protected void startScan() {
        logger.debug("discovery started for bridge {}", bridge.getThing().getUID());
        removeOlderResults(getTimestampOfLastScan());

        try {
            bridge.startDiscovery(this);
            bridge.sendMessage(new HapcanSystemCommand(HapcanFrameType.HardwareTypeRequestToGroup, 0));
            Thread.sleep(500);
            bridge.sendMessage(new HapcanSystemCommand(HapcanFrameType.FirmwareTypeRequestToGroup, 0));
            Thread.sleep(500);
            bridge.sendMessage(new HapcanSystemCommand(HapcanFrameType.DescriptionRequestToGroup, 0));
        } catch (InterruptedException e) {
            // Ignore
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        bridge.stopDiscovery();
        devices.clear();
        logger.debug("Discovery stopped for bridge {}", bridge.getThing().getUID());
    }

    @Override
    protected void startBackgroundDiscovery() {
        startScan();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopScan();
    }

    @Override
    public void handleReading(HapcanSystemMessage systemMsg) {
        HapcanRecvMessage msg = (HapcanRecvMessage) systemMsg;
        int srcNodeId = msg.getSrcNodeId();

        HapcanDeviceInfo deviceInfo = devices.get(srcNodeId);
        if (deviceInfo == null) {
            deviceInfo = new HapcanDeviceInfo();
            deviceInfo.processSystemMessage(systemMsg);
            devices.put(srcNodeId, deviceInfo);
        } else {
            deviceInfo.processSystemMessage(systemMsg);
            if (deviceInfo.hasWholeInfo()) {
                onDeviceAddedInternal(srcNodeId, deviceInfo);
                devices.remove(srcNodeId);
            }
        }
    }

    private void onDeviceAddedInternal(int nodeId, HapcanDeviceInfo deviceInfo) {
        ThingUID thingUID = getThingUID(nodeId, deviceInfo);
        if (thingUID != null) {
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(PROPERTY_GROUP_NUMBER, nodeId >> 8);
            properties.put(PROPERTY_MODULE_NUMBER, nodeId & 0xFF);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                    .withLabel(deviceInfo.getModuleName()).withBridge(bridge.getThing().getUID())
                    .withRepresentationProperty("id").withProperties(properties).build();
            thingDiscovered(discoveryResult);
        } else {
            logger.debug("Discovered unsupported HAPCAN Module of type '{}' with ID {}", deviceInfo.getDeviceModel(),
                    String.format("%04X", nodeId));
        }
    }

    private ThingUID getThingUID(int nodeId, HapcanDeviceInfo deviceInfo) {
        ThingTypeUID typeUID = HapcanDevices.HAPCAN_DEVICES.get(deviceInfo.getDeviceModel());
        if (typeUID == null) {
            return null;
        }

        final String id = String.format("%04X", nodeId);
        ThingUID thingUID = new ThingUID(typeUID, id);

        return thingUID;
    }

}
