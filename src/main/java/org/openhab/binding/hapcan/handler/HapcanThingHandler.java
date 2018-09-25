/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.handler;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.hapcan.internal.HapcanDeviceInfo;
import org.openhab.binding.hapcan.internal.config.HapcanBridgeConfig;
import org.openhab.binding.hapcan.internal.config.HapcanDeviceConfig;
import org.openhab.binding.hapcan.internal.connection.ConnectionListener;
import org.openhab.binding.hapcan.internal.connection.ConnectionStatusEvent;
import org.openhab.binding.hapcan.internal.exceptions.HapcanUnsupportedChannelException;
import org.openhab.binding.hapcan.internal.messages.HapcanFirmwareMessage;
import org.openhab.binding.hapcan.internal.messages.HapcanFrameType;
import org.openhab.binding.hapcan.internal.messages.HapcanHardwareMessage;
import org.openhab.binding.hapcan.internal.messages.HapcanRecvMessage;
import org.openhab.binding.hapcan.internal.messages.HapcanStateMessage;
import org.openhab.binding.hapcan.internal.messages.HapcanSystemMessage;
import org.openhab.binding.hapcan.internal.messages.commands.HapcanCommand;
import org.openhab.binding.hapcan.internal.messages.commands.HapcanControlCommand;
import org.openhab.binding.hapcan.internal.messages.commands.HapcanControlCommand.CpuVersion;
import org.openhab.binding.hapcan.internal.messages.commands.HapcanSystemCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HapcanThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcin Wiatrak - Initial contribution
 */
@NonNullByDefault
public class HapcanThingHandler extends BaseThingHandler implements ConnectionListener {
    private final Logger logger = LoggerFactory.getLogger(HapcanThingHandler.class);

    protected @Nullable HapcanBridgeHandler bridgeHandler;
    protected int nodeId;
    private @Nullable HapcanDeviceInfo deviceInfo;

    private @Nullable ScheduledFuture<?> initJob;
    private @Nullable ScheduledFuture<?> unlockRefresh;

    private AtomicBoolean connectionInitialized = new AtomicBoolean(false);
    private AtomicBoolean moduleInitialized = new AtomicBoolean(false);
    // private AtomicBoolean requiresRefresh = new AtomicBoolean(true);
    private AtomicBoolean refreshAllowed = new AtomicBoolean(true);

    public HapcanThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        if (command instanceof RefreshType) {
            if (refreshAllowed.getAndSet(false)) {
                try {
                    Thread.sleep(10);
                    sendMessage(new HapcanSystemCommand(HapcanFrameType.SupplyVoltageRequestToNode, nodeId));
                    Thread.sleep(10);
                    sendMessage(new HapcanSystemCommand(HapcanFrameType.StatusRequestToNode, nodeId));
                } catch (InterruptedException e) {
                    // Ignore
                }
                unlockRefresh = scheduler.schedule(() -> {
                    refreshAllowed.set(true);
                }, 2, TimeUnit.SECONDS);
            }
        } else if (bridgeHandler != null && moduleInitialized.get()) {
            try {
                logger.debug("Channel {}", channelUID.getId());

                String deviceId = thing.getThingTypeUID().getId();
                // channelUID.getThingUID().getThingTypeUID();
                logger.debug("deviceId {}", deviceId);

                HapcanControlCommand message = convertCommand(channelUID, command);
                sendMessage(message);
            } catch (HapcanUnsupportedChannelException ex) {
                logger.debug(ex.getMessage());
            }
        }
    }

    @Override
    public void initialize() {
        HapcanDeviceConfig config = getConfigAs(HapcanDeviceConfig.class);
        HapcanBridgeConfig bridgeConfig = getConfigAs(HapcanBridgeConfig.class);

        refreshAllowed.set(true);
        nodeId = config.groupNumber << 8 | config.moduleNumber;
        connectionInitialized.set(false);
        moduleInitialized.set(false);
        deviceInfo = new HapcanDeviceInfo();
        updateProperties();

        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();
            if (handler != null && handler instanceof HapcanBridgeHandler) {
                bridgeHandler = (HapcanBridgeHandler) handler;
                bridgeHandler.addEventListener(this);

                if (bridge.getStatus() == ThingStatus.ONLINE) {
                    initJob = scheduler.schedule(() -> {
                        intializeConnection();
                    }, bridgeConfig.initDelay, TimeUnit.MILLISECONDS);
                }
            }
            updateStatus(ThingStatus.UNKNOWN);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    private void intializeConnection() {
        logger.debug("HAPCAN MODULE - try to intializeConnection");

        if (!connectionInitialized.getAndSet(true)) {
            try {
                sendMessage(new HapcanSystemCommand(HapcanFrameType.HardwareTypeRequestToNode, nodeId));
                Thread.sleep(10);
                sendMessage(new HapcanSystemCommand(HapcanFrameType.FirmwareTypeRequestToNode, nodeId));
                Thread.sleep(10);
                if (deviceInfo != null) {
                    deviceInfo.clearSecondDescrPart();
                }
                sendMessage(new HapcanSystemCommand(HapcanFrameType.DescriptionRequestToGroup, nodeId));
            } catch (InterruptedException ignored) {
                // Ignore
            }
        } else {
            logger.debug("HAPCAN MODULE - already initialized");
        }
    }

    @Override
    public void connectionEvent(ConnectionStatusEvent event) {
        if (event.isConnected()) {
            refreshAllowed.set(true);
        } else if (!event.isConnected()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            connectionInitialized.set(false);
            if (initJob != null) {
                initJob.cancel(true);
                initJob = null;
            }
        }
    }

    public void sendMessage(HapcanCommand message) {
        if (bridgeHandler != null) {
            bridgeHandler.sendMessage(message);
        }
    }

    public void handleMessage(HapcanRecvMessage message) {
        updateStatus(ThingStatus.ONLINE);

        if (message instanceof HapcanSystemMessage) {
            if (deviceInfo != null) {
                deviceInfo.processSystemMessage((HapcanSystemMessage) message);
            }
            updateProperties();

            if ((message instanceof HapcanHardwareMessage) || (message instanceof HapcanFirmwareMessage)) {
                moduleInitialized.set(true);
            }
        } else if (message instanceof HapcanStateMessage) {
            logger.debug("HapcanStateMessage");
            HapcanStateMessage stateMessage = (HapcanStateMessage) message;

            for (Entry<String, State> entry : stateMessage.getChannelStates().entrySet()) {
                logger.debug("updateState {} {}", entry.getKey(), entry.getValue());
                updateState(entry.getKey(), entry.getValue());
            }
            for (Entry<String, String> entry : stateMessage.getChannelTriggers().entrySet()) {
                logger.debug("triggerChannel {} {}", entry.getKey(), entry.getValue());
                triggerChannel(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void dispose() {
        if (bridgeHandler != null) {
            bridgeHandler.removeEventListener(this);
            bridgeHandler = null;
        }

        if (initJob != null) {
            initJob.cancel(true);
        }

        if (unlockRefresh != null) {
            unlockRefresh.cancel(true);
        }
    }

    protected void updateProperties() {
        Map<String, String> properties = editProperties();
        if (deviceInfo != null) {
            deviceInfo.updateProperties(properties);
            updateProperties(properties);
        }
    }

    public void setConnectionInitialized(boolean connectionInitialized) {
        this.connectionInitialized.set(connectionInitialized);
    }

    protected HapcanControlCommand convertCommand(ChannelUID channelUID, Command command)
            throws HapcanUnsupportedChannelException {
        throw new HapcanUnsupportedChannelException("This device does not support commands");
    }

    public int getNodeId() {
        return nodeId;
    }

    public CpuVersion getCpuVersion() {
        return deviceInfo != null ? deviceInfo.getCpuVersion() : CpuVersion.UNKNOWN;
    }
}
