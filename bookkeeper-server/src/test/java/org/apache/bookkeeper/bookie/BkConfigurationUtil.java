/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.bookkeeper.bookie;

import org.apache.bookkeeper.bookie.storage.ldb.DbLedgerStorage;
import org.apache.bookkeeper.common.allocator.PoolingPolicy;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Classe di utilità per creare le ServerConfiguration di default.
 * Fonte:  org.apache.bookkeeper.conf.TestBkConfiguration. (file di utilità nella cartella test eliminata all'inizio)
 */
public class BkConfigurationUtil {
    private static final Logger LOG = LoggerFactory.getLogger(BkConfigurationUtil.class);;

    public static ServerConfiguration newServerConfiguration() {
        ServerConfiguration confReturn = new ServerConfiguration();
        confReturn.setTLSEnabledProtocols("TLSv1.2,TLSv1.1");
        confReturn.setJournalFlushWhenQueueEmpty(true);
        // enable journal format version
        confReturn.setJournalFormatVersionToWrite(5);
        confReturn.setAllowEphemeralPorts(true);
        confReturn.setBookiePort(0);
        confReturn.setGcWaitTime(1000);
        confReturn.setDiskUsageThreshold(0.999f);
        confReturn.setDiskUsageWarnThreshold(0.99f);
        confReturn.setAllocatorPoolingPolicy(PoolingPolicy.UnpooledHeap);
        confReturn.setProperty(DbLedgerStorage.WRITE_CACHE_MAX_SIZE_MB, 4);
        confReturn.setProperty(DbLedgerStorage.READ_AHEAD_CACHE_MAX_SIZE_MB, 4);
        setLoopbackInterfaceAndAllowLoopback(confReturn);
        return confReturn;
    }

    public static ServerConfiguration setLoopbackInterfaceAndAllowLoopback(ServerConfiguration serverConf) {
        serverConf.setListeningInterface(getLoopbackInterfaceName());
        serverConf.setAllowLoopback(true);
        return serverConf;
    }

    private static String getLoopbackInterfaceName() {
        try {
            Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface nif : Collections.list(nifs)) {
                if (nif.isLoopback()) {
                    return nif.getName();
                }
            }
        } catch (SocketException se) {
            LOG.warn("Impossibile trovare la LoopBackInterfaceException. Useremo null", se);
            return null;
        }
        LOG.warn("Impossibile trovare la LoopBackInterfaceException. Useremo null");
        return null;
    }
    public static ClientConfiguration newClientConfiguration() {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setTLSEnabledProtocols("TLSv1.2,TLSv1.1");
        return clientConfiguration;
    }
}
