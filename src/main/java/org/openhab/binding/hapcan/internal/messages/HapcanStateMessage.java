/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.messages;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.types.State;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public abstract class HapcanStateMessage extends HapcanRecvMessage {

    Map<String, State> channelStates = new HashMap<>();
    Map<String, String> channelTriggers = new HashMap<>();

    public Map<String, State> getChannelStates() {
        return channelStates;
    }

    public Map<String, String> getChannelTriggers() {
        return channelTriggers;
    }

}
