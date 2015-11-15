/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.handler;

import static org.openhab.binding.lutron.LutronBindingConstants.CHANNEL_LIFTLEVEL;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lutron.internal.protocol.LutronCommandType;

/**
 * Handler responsible for communicating with a shade.
 *
 * @author Allan Tong - Initial contribution
 */
public class ShadeHandler extends LutronHandler {
    private static final Integer ACTION_ZONELEVEL = 1;
    private static final Integer ACTION_RAISE = 2;
    private static final Integer ACTION_LOWER = 3;
    private static final Integer ACTION_STOP = 4;

    private int integrationId;

    public ShadeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public int getIntegrationId() {
        return this.integrationId;
    }

    @Override
    public void initialize() {
        Number id = (Number) getThing().getConfiguration().get("integrationId");

        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId");

            return;
        }

        this.integrationId = id.intValue();

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_LIFTLEVEL)) {
            // Refresh state when new item is linked.
            queryOutput(ACTION_ZONELEVEL);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_LIFTLEVEL)) {
            if (command instanceof Number) {
                int level = ((Number) command).intValue();

                output(ACTION_ZONELEVEL, level);
            } else if (command.equals(UpDownType.UP)) {
                output(ACTION_RAISE);
            } else if (command.equals(UpDownType.DOWN)) {
                output(ACTION_LOWER);
            } else if (command.equals(StopMoveType.STOP)) {
                output(ACTION_STOP);
            }
        }
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        if (type == LutronCommandType.OUTPUT && parameters.length > 1
                && ACTION_ZONELEVEL.toString().equals(parameters[0])) {
            BigDecimal level = new BigDecimal(parameters[1]);

            updateState(CHANNEL_LIFTLEVEL, new PercentType(level));
        }
    }
}
