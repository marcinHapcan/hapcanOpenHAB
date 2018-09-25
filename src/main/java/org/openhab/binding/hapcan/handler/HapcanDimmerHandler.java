/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hapcan.internal.config.HapcanDimmerConfig;
import org.openhab.binding.hapcan.internal.exceptions.HapcanUnsupportedChannelException;
import org.openhab.binding.hapcan.internal.messages.commands.HapcanControlCommand;
import org.openhab.binding.hapcan.internal.messages.commands.HapcanRgbCommand;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
@NonNullByDefault
public class HapcanDimmerHandler extends HapcanThingHandler {

    private int upDownStep;

    public HapcanDimmerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        HapcanDimmerConfig config = getConfigAs(HapcanDimmerConfig.class);
        upDownStep = config.upDownStep;
        super.initialize();
    }

    @Override
    protected HapcanControlCommand convertCommand(ChannelUID channelUID, Command command)
            throws HapcanUnsupportedChannelException {
        HapcanRgbCommand message = new HapcanRgbCommand(getCpuVersion(), nodeId, upDownStep);
        message.convertFromState(channelUID.getId(), command);
        return message;
    }

}
