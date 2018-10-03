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

package org.opennms.features.apilayer.model;

import java.util.Map;
import java.util.Objects;

import org.opennms.integration.api.v1.model.Alarm;
import org.opennms.netmgt.model.OnmsAlarm;

import com.google.common.collect.ImmutableMap;

public class AlarmBean implements Alarm {

    private final OnmsAlarm alarm;

    private final Map<String, String> attributes;

    public AlarmBean(OnmsAlarm alarm) {
        this.alarm = Objects.requireNonNull(alarm);
        this.attributes = ImmutableMap.copyOf(alarm.getDetails());
    }

    @Override
    public String getReductionKey() {
        return alarm.getReductionKey();
    }

    @Override
    public Integer getId() {
        return alarm.getId();
    }

    @Override
    public String getManagedObjectInstance() {
        return alarm.getManagedObjectInstance();
    }

    @Override
    public String getManagedObjectType() {
        return alarm.getManagedObjectType();
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

}
