/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.messages;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.hapcan.internal.exceptions.HapcanException;
import org.openhab.binding.hapcan.internal.exceptions.HapcanMessageNotImplementedException;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanRecvMessageFactory {
    @SuppressWarnings("serial")
    private static final Map<Integer, Class<? extends HapcanRecvMessage>> MESSAGE_CLASSES = Collections
            .unmodifiableMap(new HashMap<Integer, Class<? extends HapcanRecvMessage>>() {
                {
                    put(HapcanFrameType.HardwareTypeRequestToNode.getTypeId(), HapcanHardwareMessage.class);
                    put(HapcanFrameType.HardwareTypeRequestToGroup.getTypeId(), HapcanHardwareMessage.class);
                    put(HapcanFrameType.FirmwareTypeRequestToGroup.getTypeId(), HapcanFirmwareMessage.class); // 0x105
                    put(HapcanFrameType.FirmwareTypeRequestToNode.getTypeId(), HapcanFirmwareMessage.class); // 0x106
                    put(HapcanFrameType.SupplyVoltageRequestToGroup.getTypeId(), HapcanVoltageMessage.class); // 0x10B
                    put(HapcanFrameType.SupplyVoltageRequestToNode.getTypeId(), HapcanVoltageMessage.class); // 0x10C
                    put(HapcanFrameType.DescriptionRequestToGroup.getTypeId(), HapcanDescriptionMessage.class); // 0x10D
                    put(HapcanFrameType.DescriptionRequestToNode.getTypeId(), HapcanDescriptionMessage.class); // 0x10E

                    put(HapcanFrameType.ButtonStatusFrame.getTypeId(), HapcanButtonMessage.class); // 0x301
                    put(HapcanFrameType.RelayStatusFrame.getTypeId(), HapcanRelayMessage.class); // 0x302
                    // put(HapcanFrameType.InfraredFrameType.getTypeId(), HapcanInfraredMessage.class); // 0x303
                    put(HapcanFrameType.TemperatureStatusFrame.getTypeId(), HapcanTemperatureMessage.class); // 0x304

                    put(HapcanFrameType.DimmerStatusFrame.getTypeId(), HapcanDimmerMessage.class); // 0x306
                    put(HapcanFrameType.BlindControllerStatusFrame.getTypeId(), HapcanBlindMessage.class); // 0x307
                    put(HapcanFrameType.RgbLedControllerStatusFrame.getTypeId(), HapcanRgbMessage.class); // 0x308
                    put(HapcanFrameType.OCOutputsStatusFrame.getTypeId(), HapcanRelayMessage.class); // 0x309

                }
            });

    public static HapcanRecvMessage createMessage(byte[] packet, int len) throws HapcanException {
        int frameType = ((packet[1] & 0xFF) << 4) | ((packet[2] & 0xFF) >> 4);

        try {
            Class<? extends HapcanRecvMessage> cl = MESSAGE_CLASSES.get(frameType);
            if (cl == null) {
                throw new HapcanMessageNotImplementedException(
                        "Message " + String.format("%04X", frameType) + " not implemented");
            }
            Constructor<?> c = cl.getConstructor(byte[].class, int.class);
            return (HapcanRecvMessage) c.newInstance(packet, len);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof HapcanException) {
                throw (HapcanException) e.getCause();
            } else {
                throw new HapcanException(e);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            throw new HapcanException(e);
        }
    }

}
