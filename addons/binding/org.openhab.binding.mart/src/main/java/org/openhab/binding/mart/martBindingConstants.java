/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mart;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link martBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Kwaku Tetteh - Initial contribution
 */
public class martBindingConstants {

    public static final String BINDING_ID = "mart";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_MART_ADAPTER = new ThingTypeUID(BINDING_ID, "martadapter");

    // List of all Channel ids
    public final static String CHANNEL_STATE = "state";
    public final static String CHANNEL_FRIDGE = "fridgeState";
    public final static String CHANNEL_OUTSIDE_LIGHT = "outsideLightState";
    public final static String CHANNEL_TELEVISION = "televisionState";
    public final static String CHANNEL_POWER_CONSUMED = "powerConsumed";
    public final static String CHANNEL_LAST_ON_TODAY = "lastOnFor";
    public final static String CHANNEL_ON_TODAY = "onToday";
    public final static String CHANNEL_ON_TOTAL = "onTotal";
    public final static String CHANNEL_OCCUPANCY_STATUS = "occupiedState";

    // Bridge Config Properties
    public static final String HOST = "ipAddress";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

}
