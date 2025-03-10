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

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test per il metodo EntryLogger::checkEntry
 *
 * Per eseguire i test, puoi usare l'IDE oppure il comando seguente.
 * mvn test
 * Non è possibile eseguire i test di questo modulo solamente perché dipende dagli altri.
 */
@RunWith(Parameterized.class)
public class TestCheckEntry {

    private final Params.Entry params;
    private String error;
    private static List<EntryLogger> entryLoggers;

    public TestCheckEntry(Params.Entry params) {
        this.params = params;
    }

    /**
     * Chiamato da getParameters() per configurare la cartella di bookkeeper prima di iniziare i test.
     * getParameters() viene eseguito PRIMA di un metodo @BeforeClass
     * @return
     */
    public static Collection<Params.Entry> configure() {
        EntryLoggerUtil.createTempDir("bkTest", ".dir");
        long ledger1 = 10;
        long entry1 = 10;
        long ledger2 = 20;
        long entry2 = 20;
        long entry3 = -30;
        long ledger3 = 30;
        // prima devo creare tre diverse entryLogger: una per ogni classe di equivalenza (vuota, con una entry, con una entry diversa da quella cercata)
        EntryLogger e0 = EntryLoggerUtil.createEmptyEntryLogger(); // un file entrylog vuoto
        EntryLogger e1 = EntryLoggerUtil.createNonEmptyEntryLogger(ledger1, entry1); // un file entry log con una entry
        EntryLogger e2 = EntryLoggerUtil.createNonEmptyEntryLogger(ledger2, entry2); // un file entry log con un' altra entry
        EntryLogger e3 = EntryLoggerUtil.createNonEmptyEntryLogger(ledger3, entry3); // un file entry log con un' altra entry
        // per poterli eliminare al termine...
        entryLoggers = Arrays.asList(e0, e1, e2);

        // ledgerId non esistente, entryId non esistente, location non valida, entry log vuoto -> Exception
        Params.Entry p1 = new Params.Entry(-1, -1, -1, e0, false);
        // ledgerId esistente, entryId esistente, location valida e corrispondente, entry log compatibile -> nulla
        Params.Entry p2 = new Params.Entry(ledger1, entry1, EntryLoggerUtil.getPositionInEntryLog(ledger1, entry1, e1), e1, true);
        // ledgerId non esistente, entryId esistente (in un altro ledger), location valida ma non corrispondente, entry log non vuoto ma senza una entry compatibile -> Exception
        Params.Entry p3 = new Params.Entry(80, entry2, EntryLoggerUtil.getPositionInEntryLog(ledger2, entry2, e2), e2, false);
        // ledgerId esistente, entryId non esistente, location non valida(negativa), entry log non vuoto ma senza una entry compatibile->Exception
        Params.Entry p4 = new Params.Entry(ledger1, entry2, -1, e1, false);
        // ledgerId esistente, entryId esistente, location valida ma non corrispondente, entry log non compatibile-> Exception
        Params.Entry p5 = new Params.Entry(ledger1, entry1, 18, e1, false);
        // ledgerId esistente, entryId non esistente, location valida ma non corrispondente, entry log non compatibile -> Exception
        Params.Entry p6 = new Params.Entry(ledger1, entry2, EntryLoggerUtil.getPositionInEntryLog(ledger2, entry2, e2), e2, false);

        // Test aggiunti successivamente
        Params.Entry p7 = new Params.Entry(-1, entry2, EntryLoggerUtil.getPositionInEntryLog(ledger2, entry2, e2), e2, false);
        Params.Entry p8 = new Params.Entry(ledger3, entry3, EntryLoggerUtil.getPositionInEntryLog(ledger3, entry3, e3), e3, true);

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

    /**
     * Il test controlla che non vengano invocate eccezioni in caso di successo.
     * Se invece i parametri non sono corretti, mi aspetto una eccezione.
     */
    @Test
    public void checkEntry() {
        // Se non ci sono eccezioni, mi attendo un successo.
        boolean actualSuccess = success(); // separata, così l'errore viene stampato
        System.out.println("position: " + EntryLoggerUtil.getPositionInEntryLog(params.getLedgerId(), params.getEntryId(), params.getEntryLogger()));
        assertEquals(String.format("Mi aspettavo : %s ma non è andata come previsto. Errore: %s", params.isExpectedSuccess(), error), params.isExpectedSuccess(), actualSuccess);
        System.out.println(params.isExpectedSuccess() ? "Metodo ha dato esito corretto come previsto" : "Metodo fallito come previsto. Errore: " + error);
    }

    public boolean success() {
        try {
            params.getEntryLogger().checkEntry(params.getLedgerId(), params.getEntryId(), params.getOffset());
            return true;
        } catch (Exception e) {
            error = e.getClass().getSimpleName() + ": " + e.getMessage();
            return false;
        }
    }
}
