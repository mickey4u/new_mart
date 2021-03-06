/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.mart.internal;

import static org.openhab.binding.mart.martBindingConstants.THING_TYPE_MART_ADAPTER;

import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.mart.handler.MartHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link martHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Kwaku Tetteh - Initial contribution
 */

public class martHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(martHandlerFactory.class);
    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_MART_ADAPTER);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_MART_ADAPTER)) {
            return new MartHandler(thing);

        } else {
            logger.warn("ThingHandler not found for {}", thing.getThingTypeUID());
        }

        return null;
    }
}
