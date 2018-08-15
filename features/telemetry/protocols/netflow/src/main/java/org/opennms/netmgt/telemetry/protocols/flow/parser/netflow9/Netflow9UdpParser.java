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

package org.opennms.netmgt.telemetry.protocols.flow.parser.netflow9;

import java.nio.ByteBuffer;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.protocols.common.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.flow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.flow.parser.UdpParserBase;
import org.opennms.netmgt.telemetry.protocols.flow.parser.ie.RecordProvider;
import org.opennms.netmgt.telemetry.protocols.flow.parser.netflow9.proto.Header;
import org.opennms.netmgt.telemetry.protocols.flow.parser.netflow9.proto.Packet;
import org.opennms.netmgt.telemetry.protocols.flow.parser.session.Session;

public class Netflow9UdpParser extends UdpParserBase {
    public Netflow9UdpParser(final String name,
                             final AsyncDispatcher<TelemetryMessage> dispatcher) {
        super(Protocol.NETFLOW9, name, dispatcher);
    }

    @Override
    protected RecordProvider parse(Session session, ByteBuffer buffer) throws Exception {
        final Header header = new Header(BufferUtils.slice(buffer, Header.SIZE));
        final Packet packet = new Packet(session, header, buffer);

        return packet;
    }
}
