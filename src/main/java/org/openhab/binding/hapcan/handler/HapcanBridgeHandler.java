/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.handler;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.hapcan.internal.HapcanDeviceInfo;
import org.openhab.binding.hapcan.internal.HapcanDiscoveryHandler;
import org.openhab.binding.hapcan.internal.config.HapcanBridgeConfig;
import org.openhab.binding.hapcan.internal.connection.ConnectionListener;
import org.openhab.binding.hapcan.internal.connection.ConnectionStatusEvent;
import org.openhab.binding.hapcan.internal.connection.HapcanConnection;
import org.openhab.binding.hapcan.internal.messages.HapcanFrameType;
import org.openhab.binding.hapcan.internal.messages.HapcanRecvMessage;
import org.openhab.binding.hapcan.internal.messages.HapcanStateMessage;
import org.openhab.binding.hapcan.internal.messages.HapcanSystemMessage;
import org.openhab.binding.hapcan.internal.messages.commands.HapcanCommand;
import org.openhab.binding.hapcan.internal.messages.commands.HapcanSystemCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanBridgeHandler extends BaseBridgeHandler implements ConnectionListener {
    private final Logger logger = LoggerFactory.getLogger(HapcanBridgeHandler.class);

    private @Nullable HapcanConnection connection;

    private int virtualNodeId;
    HapcanDeviceInfo deviceInfo = new HapcanDeviceInfo();

    public HapcanBridgeConfig config;

    private AtomicBoolean connectionInitialized = new AtomicBoolean(false);
    private AtomicBoolean refreshAllowed = new AtomicBoolean(true);
    private ScheduledFuture<?> connectJob;
    private ScheduledFuture<?> initJob;
    private ScheduledFuture<?> keepAliveJob;
    private ScheduledFuture<?> unlockRefresh;

    private final Set<ConnectionListener> eventListeners = new CopyOnWriteArraySet<>();

    private final AtomicReference<HapcanDiscoveryHandler> discoveryHandler = new AtomicReference<>();

    public HapcanBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            if (refreshAllowed.getAndSet(false)) {
                try {
                    Thread.sleep(10);
                    sendMessage(new HapcanSystemCommand(HapcanFrameType.SupplyVoltageRequestToNode));
                    Thread.sleep(10);
                    sendMessage(new HapcanSystemCommand(HapcanFrameType.StatusRequestToNode));
                } catch (InterruptedException e) {
                    // This can be ignored
                }
                unlockRefresh = scheduler.schedule(() -> {
                    refreshAllowed.set(true);
                }, 2, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing bridge handler");

        addEventListener(this);
        connectionInitialized.set(false);
        refreshAllowed.set(true);

        config = getConfigAs(HapcanBridgeConfig.class);
        virtualNodeId = config.groupNumber << 8 | config.moduleNumber;
        int connectionTimeout = config.connectionTimeout;

        if (StringUtils.isNotBlank(config.host)) {
            connection = new HapcanConnection(config.host, config.port, connectionTimeout, scheduler, this);
            try {
                connection.openConnection();
                updateStatus(ThingStatus.UNKNOWN);
            } catch (SocketTimeoutException ex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to HAPCAN Ethernet module. IP address or host name not set.");
        }

        logger.debug("Initializing bridge handler COMPLETED");
    }

    private void intializeConnection() {
        logger.debug("HAPCAN - try to intializeConnection");

        if (!connectionInitialized.getAndSet(true)) {
            try {
                setChildrensInitialized();

                logger.debug("HAPCAN - intializeConnection");
                sendMessage(new HapcanSystemCommand(HapcanFrameType.HardwareTypeRequestToNode));
                Thread.sleep(10);
                sendMessage(new HapcanSystemCommand(HapcanFrameType.FirmwareTypeRequestToNode));
                Thread.sleep(10);
                deviceInfo.clearSecondDescrPart();
                sendMessage(new HapcanSystemCommand(HapcanFrameType.DescriptionRequestToNode));

                Thread.sleep(100);

                for (int group = config.minGroupNumber; group <= config.maxGroupNumber; group++) {
                    sendMessage(new HapcanSystemCommand(HapcanFrameType.HardwareTypeRequestToGroup, group << 8));
                    Thread.sleep(500);
                    sendMessage(new HapcanSystemCommand(HapcanFrameType.FirmwareTypeRequestToGroup, group << 8));
                    Thread.sleep(500);
                    sendMessage(new HapcanSystemCommand(HapcanFrameType.DescriptionRequestToGroup, group << 8));

                    Thread.sleep(config.delayBetweenGroups);
                }

            } catch (InterruptedException ignored) {
                logger.debug("InterruptedException - This can be ignored, the thread will end anyway");
                // This can be ignored, the thread will end anyway
            }
        }
    }

    private void keepAliveSendCommand() {
        logger.debug("keepAliveSendCommand");

        try {
            sendMessage(new HapcanSystemCommand(HapcanFrameType.SupplyVoltageRequestToNode));
            Thread.sleep(10);
            sendMessage(new HapcanSystemCommand(HapcanFrameType.SupplyVoltageRequestToGroup, 0));
        } catch (InterruptedException ignored) {
            // This can be ignored, the thread will end anyway
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing bridge handler.");

        if (connectJob != null) {
            connectJob.cancel(true);
        }

        if (initJob != null) {
            initJob.cancel(true);
        }

        if (unlockRefresh != null) {
            unlockRefresh.cancel(true);
        }

        if (connection != null) {
            connection.closeConnection();
        }

        removeEventListener(this);
    }

    public @Nullable HapcanThingHandler getChildThingHandler(int nodeId) {
        List<Thing> things = getThing().getThings();
        for (Thing t : things) {
            HapcanThingHandler handler = (HapcanThingHandler) t.getHandler();
            if (handler != null && handler.getNodeId() == nodeId) {
                return handler;
            }
        }
        return null;
    }

    private void setChildrensInitialized() {
        List<Thing> things = getThing().getThings();
        for (Thing t : things) {
            HapcanThingHandler handler = (HapcanThingHandler) t.getHandler();
            if (handler != null) {
                handler.setConnectionInitialized(true);
            }
        }
    }

    public void handleMessage(HapcanRecvMessage message) {
        updateStatus(ThingStatus.ONLINE);

        if (message.isBridgeMessage()) {
            if (message instanceof HapcanSystemMessage) {
                deviceInfo.processSystemMessage((HapcanSystemMessage) message);
                updateProperties();
            } else if (message instanceof HapcanStateMessage) {
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
        } else {
            if (message instanceof HapcanSystemMessage) {
                HapcanDiscoveryHandler handler = discoveryHandler.get();
                if (handler != null) {
                    handler.handleReading((HapcanSystemMessage) message);
                }
            }

            HapcanThingHandler childthingHandler = getChildThingHandler(message.getSrcNodeId());
            if (childthingHandler == null) {
                logger.debug("Command from unregistered device {}", String.format("%04X", message.getSrcNodeId()));
                return;
            }
            childthingHandler.handleMessage(message);
        }
    }

    @Override
    public void connectionEvent(ConnectionStatusEvent event) {
        if (event.isConnected()) {
            logger.debug("Connection opened.");

            updateStatus(ThingStatus.ONLINE);
            refreshAllowed.set(true);

            if (connectJob != null) {
                connectJob.cancel(true);
                connectJob = null;
                logger.debug("Connection established. Reconnect cancelled.");
            }

            HapcanBridgeConfig config = getConfigAs(HapcanBridgeConfig.class);

            initJob = scheduler.schedule(() -> {
                intializeConnection();
            }, config.initDelay, TimeUnit.MILLISECONDS);
            keepAliveJob = scheduler.scheduleWithFixedDelay(() -> {
                keepAliveSendCommand();
            }, config.keepAliveDelay, config.keepAliveDelay, TimeUnit.SECONDS);

            logger.debug("Init commands scheduled in {} miliseconds.", config.initDelay);
        } else {
            logger.debug("Connection closed/aborted");

            updateStatus(ThingStatus.OFFLINE, event.getStatusDetail(), event.getDescription());
            connectionInitialized.set(false);

            if (keepAliveJob != null) {
                keepAliveJob.cancel(true);
                keepAliveJob = null;
            }
            if (initJob != null) {
                initJob.cancel(true);
                initJob = null;
            }

            if (event.tryToReconnect()) {
                connectJob = scheduler.schedule(() -> {
                    if (connection != null) {
                        try {
                            connection.openConnection();
                            updateStatus(ThingStatus.UNKNOWN);
                        } catch (SocketTimeoutException ex) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                        }
                    }
                }, 10, TimeUnit.SECONDS);

                logger.debug("Reconnect scheduled.");
            }
        }
    }

    public void sendMessage(HapcanCommand message) {
        message.setSrcNodeId(virtualNodeId);
        if (connection != null) {
            connection.sendMessage(message);
        }
    }

    protected void updateProperties() {
        Map<String, String> properties = editProperties();
        deviceInfo.updateProperties(properties);
        updateProperties(properties);
    }

    public void addEventListener(ConnectionListener eventListener) {
        this.eventListeners.add(eventListener);
    }

    public void removeEventListener(ConnectionListener eventListener) {
        this.eventListeners.remove(eventListener);
    }

    public void dispatchEvent(ConnectionStatusEvent event) {
        logger.debug("Distributing event: {}", event);
        for (ConnectionListener listener : eventListeners) {
            logger.trace("Distributing to {}", listener);
            listener.connectionEvent(event);
        }
    }

    public boolean isConnectionInitialized() {
        return connectionInitialized.get();
    }

    public void startDiscovery(HapcanDiscoveryHandler handler) {
        discoveryHandler.set(handler);
    }

    public void stopDiscovery() {
        discoveryHandler.set(null);
    }
}
