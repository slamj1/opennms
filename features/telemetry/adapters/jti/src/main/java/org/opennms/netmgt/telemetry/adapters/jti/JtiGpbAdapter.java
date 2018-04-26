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

package org.opennms.netmgt.telemetry.adapters.jti;

import com.google.common.collect.Iterables;
import com.google.protobuf.ExtensionRegistry;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.telemetry.adapters.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.adapters.api.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.adapters.collection.AbstractPersistingAdapter;
import org.opennms.netmgt.telemetry.adapters.collection.CollectionSetWithAgent;
import org.opennms.netmgt.telemetry.adapters.collection.ScriptedCollectionSetBuilder;
import org.opennms.netmgt.telemetry.adapters.jti.proto.CpuMemoryUtilizationOuterClass;
import org.opennms.netmgt.telemetry.adapters.jti.proto.FirewallOuterClass;
import org.opennms.netmgt.telemetry.adapters.jti.proto.LogicalPortOuterClass;
import org.opennms.netmgt.telemetry.adapters.jti.proto.LspMon;
import org.opennms.netmgt.telemetry.adapters.jti.proto.LspStatsOuterClass;
import org.opennms.netmgt.telemetry.adapters.jti.proto.Port;
import org.opennms.netmgt.telemetry.adapters.jti.proto.TelemetryTop;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * An adapter for handling Junos Telemetry Interface packets.
 *
 * Messages are decoded using the corresponding classes generated by the Google
 * Protobuf definitions and forwarded to a script for further processing.
 *
 * @author jwhite
 */
public class JtiGpbAdapter extends AbstractPersistingAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(JtiGpbAdapter.class);

    private static final ExtensionRegistry s_registry = ExtensionRegistry.newInstance();
    static {
        CpuMemoryUtilizationOuterClass.registerAllExtensions(s_registry);
        FirewallOuterClass.registerAllExtensions(s_registry);
        LogicalPortOuterClass.registerAllExtensions(s_registry);
        LspMon.registerAllExtensions(s_registry);
        LspStatsOuterClass.registerAllExtensions(s_registry);
        Port.registerAllExtensions(s_registry);
        TelemetryTop.registerAllExtensions(s_registry);
    }

    private CollectionAgentFactory collectionAgentFactory;

    private InterfaceToNodeCache interfaceToNodeCache;

    private NodeDao nodeDao;

    private TransactionOperations transactionTemplate;


    @Override
    public Optional<CollectionSetWithAgent> handleMessage(TelemetryMessage message, TelemetryMessageLog messageLog)
            throws Exception {
        final TelemetryTop.TelemetryStream jtiMsg = TelemetryTop.TelemetryStream.parseFrom(message.getByteArray(),
                s_registry);

        CollectionAgent agent = null;
        try {
            // Attempt to resolve the systemId to an InetAddress
            final InetAddress inetAddress = InetAddress.getByName(jtiMsg.getSystemId());
            final Optional<Integer> nodeId = interfaceToNodeCache.getFirstNodeId(messageLog.getLocation(), inetAddress);
            if (nodeId.isPresent()) {
                // NOTE: This will throw a IllegalArgumentException if the
                // nodeId/inetAddress pair does not exist in the database
                agent = collectionAgentFactory.createCollectionAgent(Integer.toString(nodeId.get()), inetAddress);
            }
        } catch (UnknownHostException e) {
            LOG.debug("Could not convert system id to address: {}", jtiMsg.getSystemId());
        }

        if (agent == null) {
            // We were unable to build the agent by resolving the systemId,
            // try finding
            // a node with a matching label
            agent = transactionTemplate.execute(new TransactionCallback<CollectionAgent>() {
                @Override
                public CollectionAgent doInTransaction(TransactionStatus status) {
                    final OnmsNode node = Iterables.getFirst(nodeDao.findByLabel(jtiMsg.getSystemId()), null);
                    if (node != null) {
                        final OnmsIpInterface primaryInterface = node.getPrimaryInterface();
                        return collectionAgentFactory.createCollectionAgent(primaryInterface);
                    }
                    return null;
                }
            });
        }

        if (agent == null) {
            LOG.warn("Unable to find node and inteface for system id: {}", jtiMsg.getSystemId());
            return Optional.empty();
        }

        ScriptedCollectionSetBuilder builder = getCollectionBuilder();

        final CollectionSet collectionSet = builder.build(agent, jtiMsg);
        return Optional.of(new CollectionSetWithAgent(agent, collectionSet));
    }

    public void setCollectionAgentFactory(CollectionAgentFactory collectionAgentFactory) {
        this.collectionAgentFactory = collectionAgentFactory;
    }

    public void setInterfaceToNodeCache(InterfaceToNodeCache interfaceToNodeCache) {
        this.interfaceToNodeCache = interfaceToNodeCache;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public void setTransactionTemplate(TransactionOperations transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

}
