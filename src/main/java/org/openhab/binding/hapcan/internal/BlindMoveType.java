/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal;

import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.State;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public enum BlindMoveType implements PrimitiveType, State {
    STOPPED,
    GOING_DOWN,
    GOING_UP;

    @Override
    public String format(String pattern) {
        return String.format(pattern, this.toString());
    }

    @Override
    public String toFullString() {
        return toString();
    }
}
