/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.connection;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.hapcan.handler.HapcanBridgeHandler;
import org.openhab.binding.hapcan.internal.exceptions.HapcanException;
import org.openhab.binding.hapcan.internal.exceptions.HapcanMessageNotImplementedException;
import org.openhab.binding.hapcan.internal.messages.HapcanRecvMessage;
import org.openhab.binding.hapcan.internal.messages.HapcanRecvMessageFactory;
import org.openhab.binding.hapcan.internal.messages.commands.HapcanCommand;
import org.openhab.binding.hapcan.internal.util.HapcanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects events from TCP connection and propagates them to registered ConnectionListeners.
 *
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanConnection {
    private final Logger logger = LoggerFactory.getLogger(HapcanConnection.class);

    private ScheduledExecutorService scheduler;
    private Reader reader;
    private Socket socket;
    private String host;
    private int port;
    private HapcanBridgeHandler bridgeHandler;
    private int connectionTimeout;

    public HapcanConnection(String host, int port, int connectionTimeout, ScheduledExecutorService scheduler,
            HapcanBridgeHandler bridgeHandler) {
        this.host = host;
        this.port = port;
        this.scheduler = scheduler;
        this.bridgeHandler = bridgeHandler;
        this.connectionTimeout = connectionTimeout;
    }

    public synchronized void closeConnection() {
        if (reader != null) {
            logger.debug("Closing TCP connection to port {}...", port);
            reader.close();
            reader = null;
            closeSocketSilently();
            socket = null;
            bridgeHandler.dispatchEvent(new ConnectionStatusEvent(false, true));
        }
    }

    public synchronized void openConnection() throws SocketTimeoutException {
        if (reader != null) {
            logger.debug("TCP connection is already open!");
            return;
        }

        logger.debug("Opening TCP connection to host {} port {}...", host, port);
        try {
            logger.debug("Creating TCP socket");
            socket = new Socket();
            socket.connect(new InetSocketAddress(this.host, this.port), connectionTimeout);
            socket.setTcpNoDelay(true);
            socket.getOutputStream().flush();
            logger.debug("TCP socket created.");

            reader = new Reader(socket);
            scheduler.execute(reader);

            bridgeHandler.dispatchEvent(new ConnectionStatusEvent(true));
        } catch (IOException ex) {
            if (socket != null) {
                closeSocketSilently();
            }

            bridgeHandler.dispatchEvent(
                    new ConnectionStatusEvent(false, true, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage()));
        }
    }

    private void closeSocketSilently() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.debug("Failed to close socket.", e);
        }
    }

    private class Reader implements Runnable {
        private Socket socket;
        private BufferedInputStream inputStream;

        private volatile boolean isRunning = true;
        byte buf[] = new byte[15];
        int len = 0;
        byte checksum = 0;

        private Reader(Socket socket) throws IOException {
            this.socket = socket;
            this.inputStream = new BufferedInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
            logger.debug("Reader for TCP port {} starting...", port);

            try {
                while (isRunning) {
                    int readValue = inputStream.read();
                    if (readValue == -1) {
                        throw new IOException("Got EOF on port " + port);
                    }
                    byte readByte = (byte) readValue;

                    if (len == 0) {
                        if (readByte == (byte) 0xAA) {
                            inputStream.mark(14);
                        } else {
                            logger.debug("Ignoring received byte: {}", String.format("%02X", readByte));
                            continue;
                        }
                    }

                    buf[len++] = readByte;

                    if (len >= 13) {
                        if (buf[len - 1] == (byte) 0xA5) {
                            checksum = 0;
                            for (int i = 1; i < len - 2; i++) {
                                checksum += buf[i];
                            }
                            if (checksum == buf[len - 2]) {
                                logger.debug("HAPCAN message received (len={}): {}", len,
                                        HapcanUtils.bytesToHex(buf, len, "-"));
                                propagateMessage(buf, len);
                                len = 0;
                                checksum = 0;
                                continue;
                            }
                        }
                    }
                    if (len == 15) {
                        inputStream.reset();
                        len = 0;
                        checksum = 0;
                        continue;
                    }
                }
            } catch (IOException ex) {
                if (isRunning) {
                    logger.debug("IOException in thread");
                    closeConnection();
                    bridgeHandler.dispatchEvent(new ConnectionStatusEvent(false, true,
                            ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage()));
                }
            } finally {
                logger.debug("Reader for TCP port {} finished...", port);
            }

            logger.debug("KONIEC WATKU");
        }

        public void close() {
            logger.debug("Shutting down reader for TCP port {}...", port);
            try {
                isRunning = false;
                socket.close();
                inputStream.close();
                // outputStream.close();
            } catch (IOException ex) {
                logger.debug("Failed to close TCP port {}!", port, ex);
            }
        }
    }

    public void sendMessage(HapcanCommand message) {
        byte buf[] = message.decodeMessage();
        logger.debug("Send HAPCAN message (len={}): {}", buf.length, HexUtils.bytesToHex(buf, "-"));

        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(buf);
            outputStream.flush();
        } catch (IOException ex) {
            logger.debug("Error writing to output stream!", ex);
            closeConnection();
            bridgeHandler.dispatchEvent(
                    new ConnectionStatusEvent(false, true, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage()));
        }
    }

    private void propagateMessage(byte[] data, int len) {
        try {
            HapcanRecvMessage message = HapcanRecvMessageFactory.createMessage(data, len);
            bridgeHandler.handleMessage(message);
        } catch (HapcanMessageNotImplementedException e) {
            logger.debug("Message not supported, data: {}", HapcanUtils.bytesToHex(data, len));
        } catch (HapcanException e) {
            logger.error("Error occurred during packet receiving, data: {}", HapcanUtils.bytesToHex(data, len), e);
        }
    }

}
