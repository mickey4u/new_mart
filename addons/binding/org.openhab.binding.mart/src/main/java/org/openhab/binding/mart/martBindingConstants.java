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
    public final static ThingTypeUID THING_TYPE_FRIDGE = new ThingTypeUID(BINDING_ID, "fridge");
    public final static ThingTypeUID THING_TYPE_TELEVISION = new ThingTypeUID(BINDING_ID, "television");
    public final static ThingTypeUID THING_TYPE_OUTSIDELIGHT = new ThingTypeUID(BINDING_ID, "outsidelight");

    // List of all Channel ids
    public final static String CHANNEL_STATE = "state";
    public final static String CHANNEL_POWER_CONSUMED = "powerConsumed";
    public final static String CHANNEL_LAST_ON_TODAY = "lastOnFor";
    public final static String CHANNEL_ON_TODAY = "onToday";
    public final static String CHANNEL_ON_TOTAL = "onTotal";

}
