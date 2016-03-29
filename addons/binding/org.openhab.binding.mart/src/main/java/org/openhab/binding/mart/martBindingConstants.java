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
    /*
     * public final static ThingTypeUID THING_TYPE_TELEVISION = new ThingTypeUID(BINDING_ID, "television");
     * public final static ThingTypeUID THING_TYPE_OUTSIDELIGHT = new ThingTypeUID(BINDING_ID, "outsidelight");
     */

    // List of all Channel ids
    public final static String CHANNEL_TELEVISION_STATE = "televisionState";
    public final static String CHANNEL_FRIDGE_STATE = "fridgeState";
    public final static String CHANNEL_LIGHT_STATE = "lightState";
    public final static String CHANNEL_TELEVISION_POWER_CONSUMED = "televisionPowerConsumed";
    public final static String CHANNEL_FRIDGE_POWER_CONSUMED = "fridgePowerConsumed";
    public final static String CHANNEL_LIGHT_POWER_CONSUMED = "lightPowerConsumed";
    public final static String CHANNEL_TELEVISION_LAST_ON_TODAY = "televisionLastOnFor";
    public final static String CHANNEL_FRIDGE_LAST_ON_TODAY = "fridgeLastOnFor";
    public final static String CHANNEL_LIGHT_LAST_ON_TODAY = "lightLastOnFor";
    public final static String CHANNEL_TELEVISION_ON_TODAY = "televisionOnToday";
    public final static String CHANNEL_FRIDGE_ON_TODAY = "fridgeOnToday";
    public final static String CHANNEL_LIGHT_ON_TODAY = "lightOnToday";
    public final static String CHANNEL_TELEVISION_ON_TOTAL = "televisionOnTotal";
    public final static String CHANNEL_FRIDGE_ON_TOTAL = "fridgeOnTotal";
    public final static String CHANNEL_LIGHT_ON_TOTAL = "lightOnTotal";

}
