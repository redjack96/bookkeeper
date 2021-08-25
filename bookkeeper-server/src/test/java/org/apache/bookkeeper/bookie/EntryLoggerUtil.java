/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.apache.bookkeeper.bookie;

import io.netty.buffer.Unpooled;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.util.DiskChecker;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class EntryLoggerUtil {

    private static long positionInEntryLog;
    public static final String BOOKKEEPER_DIRECTORY = "/tmp/bk-data/";

    public static long getPositionInEntryLog() {
        return EntryLoggerUtil.positionInEntryLog;
    }

    public static EntryLogger createEmptyEntryLogger(LedgerDirsManager something) {
        try {
            return new EntryLogger(new ServerConfiguration(), something);
        } catch (Exception e) {
            throw new IllegalStateException("impossibile creare un EntryLogger vuoto: " + e.getMessage());
        }
    }

    public static EntryLogger createNonEmptyEntryLogger() {
        try {
            EntryLogger emptyEntryLogger = createEmptyEntryLogger(getLedgerDirsManager());
            positionInEntryLog = emptyEntryLogger.addEntry(0, Unpooled.copiedBuffer("ciao".getBytes(StandardCharsets.UTF_8)));
            return emptyEntryLogger;
        } catch (Exception e){
            throw new IllegalStateException("impossibile creare un EntryLogger con una entry: " + e.getMessage());
        }
    }

    public static LedgerDirsManager getLedgerDirsManager() {
        File f = new File(BOOKKEEPER_DIRECTORY);
        if (!f.exists()) f.mkdirs();

        float threshold = 0.9f;
        DiskChecker diskChecker = new DiskChecker(threshold, threshold * 0.75f);
        return new LedgerDirsManager(new ServerConfiguration(), new File[]{f}, diskChecker);
    }

    private static long getPositionOfEntryInLog(EntryLogger e, long ledgerId, long entryId){
        return 10; // TODO trovare il modo per ricavare l'offset/posizione
    }
}