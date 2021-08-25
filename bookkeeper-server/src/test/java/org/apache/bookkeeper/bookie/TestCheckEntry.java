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

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Per eseguire i test, puoi usare l'IDE oppure il comando seguente.
 * mvn test
 * Non è possibile eseguire i test di questo modulo solamente perché dipende dagli altri.
 */
@RunWith(Parameterized.class)
public class TestCheckEntry {

    private final Params.CheckEntry params;
    private String error;
    private static File f;

    public TestCheckEntry(Params.CheckEntry params) {
        this.params = params;
    }

    /**
     * Chiamato da getParameters() per configurare la cartella di bookkeeper
     */
    public static void configure() {
        System.out.println("Beforeclass");
        f = new File(EntryLoggerUtil.BOOKKEEPER_DIRECTORY);
        f.mkdirs();
        File f2 = new File(EntryLoggerUtil.BOOKKEEPER_DIRECTORY+"/current/");
        if(!f2.exists()) f2.mkdirs();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        FileUtils.deleteDirectory(f);
    }

    @Parameterized.Parameters
    public static Collection<Params.CheckEntry> getParameters() {
        configure();
        return Arrays.asList(
                new Params.CheckEntry(Params.EntryLoggerStatus.EMPTY, -1, -1, -1, false),
                new Params.CheckEntry(Params.EntryLoggerStatus.ONE_ENTRY, 0, 0, 10, true)
        );
    }

    /**
     * Il test controlla che non vengano invocate eccezioni in caso di successo.
     * Se invece i parametri non sono corretti, mi aspetto una eccezione.
     */
    @Test
    public void checkEntry() {
        // Se non ci sono eccezioni, mi attendo un successo.
        boolean actualSuccess =  success(); // separata, così l'errore viene stampato
        System.out.println("position: " + EntryLoggerUtil.getPositionInEntryLog());
        assertEquals(String.format("Mi aspettavo : %s ma non è andata come previsto. Errore: %s", params.isExpectedSuccess(), error),params.isExpectedSuccess(),actualSuccess);
        System.out.println(params.isExpectedSuccess() ? "Test corretto come previsto" : "Test fallito come previsto. Errore: " + error);
    }

    public boolean success() {
        try {
            params.getEntryLogger().checkEntry(params.getLedgerId(), params.getEntryId(), params.getEntryOffset());
            return true;
        } catch (Exception e) {
            error = e.getClass().getSimpleName() + ": " + e.getMessage();
            return false;
        }
    }
}
