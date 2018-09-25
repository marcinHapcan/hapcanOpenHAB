/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.messages;

import org.openhab.binding.hapcan.internal.util.ShortEnumWrapper;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public enum HapcanFrameType implements ShortEnumWrapper {
    RebootRequestToGroup(0x101, "Reboot request to group"),
    RebootRequestToNode(0x102, "Reboot request to node"),
    HardwareTypeRequestToGroup(0x103, "Hardware type request to group"),
    HardwareTypeRequestToNode(0x104, "Hardware type request to node"),
    FirmwareTypeRequestToGroup(0x105, "Firmware type request to group"),
    FirmwareTypeRequestToNode(0x106, "Firmware type request to node"),
    SetDefaultNumbers(0x107, "Set default node and group numbers request to node"),
    StatusRequestToGroup(0x108, "Status request to group"),
    StatusRequestToNode(0x109, "Status request to node"),
    ControlMessage(0x10A, "Control message"),
    SupplyVoltageRequestToGroup(0x10B, "Supply voltage request to group"),
    SupplyVoltageRequestToNode(0x10C, "Supply voltage request to node"),
    DescriptionRequestToGroup(0x10D, "Description request to group"),
    DescriptionRequestToNode(0x10E, "Description request to node"),

    ButtonStatusFrame(0x301, "Button node status frame"),
    RelayStatusFrame(0x302, "Relay status frame"),
    InfraredFrameType(0x303, "Infrared status frame"),
    TemperatureStatusFrame(0x304, "Temperature status frame"),

    DimmerStatusFrame(0x306, "Dimmer status frame"),
    BlindControllerStatusFrame(0x307, "Blind controller status frame"),
    RgbLedControllerStatusFrame(0x308, "RGB LED controller status frame"),
    OCOutputsStatusFrame(0x309, "RGB LED controller status frame"),

    UnknownFrame(0xFFFF, "Unknown frame");

    private final int typeId;
    private final String label;

    HapcanFrameType(int typeId, String label) {
        this.typeId = typeId;
        this.label = label;
    }

    @Override
    public short toShort() {
        return (short) typeId;
    }

    public int getTypeId() {
        return typeId;
    }

    public String getLabel() {
        return label;
    }
}
