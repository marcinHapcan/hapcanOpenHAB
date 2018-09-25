/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.handler;

import static org.openhab.binding.hapcan.HapcanBindingConstants.EXTENDED_CHANNELS_RELAYS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hapcan.internal.exceptions.HapcanUnsupportedChannelException;
import org.openhab.binding.hapcan.internal.messages.commands.HapcanControlCommand;
import org.openhab.binding.hapcan.internal.messages.commands.HapcanRelayCommand;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
@NonNullByDefault
public class HapcanRelayHandler extends HapcanThingHandler {

    public HapcanRelayHandler(Thing thing) {
        super(thing);
    }

    private boolean hasExtendedChannels() {
        return EXTENDED_CHANNELS_RELAYS.contains(thing.getThingTypeUID());
    }

    @Override
    protected HapcanControlCommand convertCommand(ChannelUID channelUID, Command command)
            throws HapcanUnsupportedChannelException {
        HapcanRelayCommand message = new HapcanRelayCommand(getCpuVersion(), nodeId, hasExtendedChannels());
        message.convertFromState(channelUID.getId(), command);
        return message;
    }

}
