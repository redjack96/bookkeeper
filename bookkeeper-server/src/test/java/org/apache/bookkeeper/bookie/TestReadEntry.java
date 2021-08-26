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

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test per il metodo EntryLogger::readEntry
 */
@RunWith(Parameterized.class)
public class TestReadEntry {

    private final Params.Entry params;
    private static List<EntryLogger> entryLoggers;

    public TestReadEntry(Params.Entry params) {
        this.params = params;
    }

    /**
     * Chiamato da getParameters() per configurare la cartella di bookkeeper prima di iniziare i test.
     * getParameters() viene eseguito PRIMA di un metodo @BeforeClass
     */
    public static List<Params.Entry> configure() {
        EntryLoggerUtil.createTempDir("bkTest", ".dir");

        long ledger1 = 10;
        long entry1 = 10;
        long ledger2 = 20;
        long entry2 = 20;
        long entry2b = 25;
        long entry3 = -30;
        long ledger3 = 30;
        long entry4 = 40;
        long ledger4 = 40;

        // prima devo creare tre diverse entryLogger: una per ogni classe di equivalenza (vuota, con una entry, con una entry diversa da quella cercata)
        EntryLogger e0 = EntryLoggerUtil.createEmptyEntryLogger(); // un file entrylog vuoto
        EntryLogger e1 = EntryLoggerUtil.createNonEmptyEntryLogger(ledger1, entry1); // un file entry log con una entry
        EntryLogger e2 = EntryLoggerUtil.createEntryLoggerWithOneLedgerWithTwoEntries(ledger2, entry2, entry2b); // un file entry log con 1 ledger e due entries
        EntryLogger e3 = EntryLoggerUtil.createEntryLoggerWithTwoLedgerWithOneEntry(ledger3, entry3, ledger4, entry4); // un file entry log con 2 ledgers e una entry ciascuno
        // Di default ogni EntryLogger costruisce un log file con diversi ledger al suo interno.

        // ledgerid valido, entryid esistente, location compatibile, logEntry compatibile -> dati letti = dati iniziali
        Params.Entry p1 = new Params.Entry(ledger1, entry1, EntryLoggerUtil.getPositionInEntryLog(ledger1, entry1, e1), e1, true);
        // ledgerid non valido(negativo), entryid esistente, location compatibile, logEntry compatibile -> Exception
        Params.Entry p2 = new Params.Entry(-1, entry1, EntryLoggerUtil.getPositionInEntryLog(ledger1, entry1, e1), e1, false);
        // ledgerid non compatibile, entryid esistente, location compatibile, logEntry compatibile -> Exception
        Params.Entry p3 = new Params.Entry(ledger1, entry2, EntryLoggerUtil.getPositionInEntryLog(ledger2, entry2, e2), e2, false);
        // ledgerid valido, entryid non esistente, location compatibile, logEntry compatibile -> Exception
        Params.Entry p4 = new Params.Entry(ledger1, entry2, EntryLoggerUtil.getPositionInEntryLog(ledger1, entry1, e1), e1, false);
        // ledgerid valido, entryid esistente, location non valida (negativa), logEntry compatibile -> Exception
        Params.Entry p5 = new Params.Entry(ledger1, entry1, -18, e1, false);
        // ledgerid valido, entryid esistente, location non compatibile, logEntry compatibile -> Exception
        Params.Entry p6 = new Params.Entry(ledger2, entry2, EntryLoggerUtil.getPositionInEntryLog(ledger2, entry2b, e2), e2, false);
        // ledgerid valido, entryid esistente, location compatibile (per le 2 entry), logEntry vuoto -> Exception
        Params.Entry p7 = new Params.Entry(ledger2, entry2, EntryLoggerUtil.getPositionInEntryLog(ledger2, entry2, e2), e0, false);
        // ledgerid valido, entryid esistente, location compatibile, logEntry non compatibile (contiene una entry diversa, un ledger diverso e a una posizione diversa) -> Exception
        Params.Entry p8 = new Params.Entry(ledger3, entry3, EntryLoggerUtil.getPositionInEntryLog(ledger3, entry3, e3), e2, false);
        entryLoggers = Arrays.asList(e0, e1, e2, e3);

        return Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8);
    }

    /**
     * Elimino l' entry logger e la cartella temporanea
     */
    @AfterClass
    public static void afterClass() {
        for (EntryLogger entryLogger : entryLoggers) {
            if (entryLogger != null) entryLogger.shutdown();
        }
        EntryLoggerUtil.deleteDirs();
    }

    @Parameterized.Parameters(name = "index: {0}")
    public static Collection<Params.Entry> getParameters() {
        return configure();
    }

    @Test
    public void readEntry() {
        EntryLogger e = params.getEntryLogger();
        try {
            assertEquals(EntryLoggerUtil.generateEntry(params.getLedgerId(), params.getEntryId()), e.readEntry(params.getLedgerId(), params.getEntryId(), params.getOffset()));
            assertTrue(params.isExpectedSuccess());
        } catch (IOException ioException) {
            ioException.printStackTrace();
            assertFalse(params.isExpectedSuccess());
        }
    }
}
