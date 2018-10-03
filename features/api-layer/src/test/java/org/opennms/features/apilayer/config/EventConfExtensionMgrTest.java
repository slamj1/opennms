/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;
import org.opennms.integration.api.v1.config.events.EventConfExtension;
import org.opennms.integration.api.v1.config.events.EventDefinition;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.xml.eventconf.Events;

public class EventConfExtensionMgrTest {

    @Test
    public void canPrioritizeEvents() {
        EventConfDao eventConfDao = mock(EventConfDao.class);
        EventConfExtensionMgr eventConfExtensionMgr = new EventConfExtensionMgr(eventConfDao);

        // No events yet
        Events events = eventConfExtensionMgr.getObject();
        assertThat(events.getEvents(), hasSize(0));

        // Expose an extension
        EventConfExtension ext1 = mock(EventConfExtension.class);
        EventDefinition eventDefinitionA = mock(EventDefinition.class);
        when(eventDefinitionA.getUei()).thenReturn("uei/A");
        when(eventDefinitionA.getPriority()).thenReturn(100);
        when(ext1.getEventDefinitions()).thenReturn(Collections.singletonList(eventDefinitionA));
        eventConfExtensionMgr.onBind(ext1, new HashMap());

        // One event
        events = eventConfExtensionMgr.getObject();
        assertThat(events.getEvents(), hasSize(1));
        assertThat(events.getEvents().get(0).getUei(), equalTo("uei/A"));

        // Expose another extension
        EventConfExtension ext2 = mock(EventConfExtension.class);
        EventDefinition eventDefinitionB = mock(EventDefinition.class);
        when(eventDefinitionB.getUei()).thenReturn("uei/B");
        when(eventDefinitionA.getPriority()).thenReturn(10);
        when(ext2.getEventDefinitions()).thenReturn(Collections.singletonList(eventDefinitionB));
        eventConfExtensionMgr.onBind(ext2, new HashMap());

        // Aggregated events
        events = eventConfExtensionMgr.getObject();
        assertThat(events.getEvents(), hasSize(2));
        assertThat(events.getEvents().get(0).getUei(), equalTo("uei/B"));
        assertThat(events.getEvents().get(1).getUei(), equalTo("uei/A"));

        // Now remove an extension
        eventConfExtensionMgr.onUnbind(ext1, new HashMap());

        // Back to one event
        events = eventConfExtensionMgr.getObject();
        assertThat(events.getEvents(), hasSize(1));
        assertThat(events.getEvents().get(0).getUei(), equalTo("uei/B"));

        // Now remove the other extension
        eventConfExtensionMgr.onUnbind(ext2, new HashMap());

        // No events
        events = eventConfExtensionMgr.getObject();
        assertThat(events.getEvents(), hasSize(0));
    }
}
