/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.telemetry.daemon;

import java.lang.reflect.Constructor;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.api.parser.Parser;
import org.opennms.netmgt.telemetry.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.config.api.ParserDefinition;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

public class ParserFactory {

    public static Parser buildParser(final ParserDefinition parserDef,
                                     final AsyncDispatcher<TelemetryMessage> dispatcher) {
        // Instantiate the associated class
        final Constructor<? extends Parser> ctor;
        try {
            ctor = Class.forName(parserDef.getClassName())
                    .asSubclass(Parser.class)
                    .getConstructor();
        } catch (final ClassNotFoundException | NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("%s not found",
                    parserDef.getClassName()));
        } catch (final ClassCastException e) {
            throw new IllegalArgumentException(String.format("%s must implement %s",
                    parserDef.getClassName(),
                    Parser.class.getCanonicalName()));
        }

        final Parser parser;
        try {
            parser = ctor.newInstance();
        } catch (final Exception e) {
            throw new RuntimeException(String.format("Failed to instantiate parser with class name '%s'.",
                    parserDef.getClassName()), e);
        }

        // Apply the parameters
        final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(parser);
        wrapper.setPropertyValues(parserDef.getParameterMap());

        // Update the name - the one given in the definition wins over any that may be set by the parameters
        parser.setName(parserDef.getName());

        // Use the given dispatcher
        parser.setDispatcher(dispatcher);

        return parser;
    }
}
