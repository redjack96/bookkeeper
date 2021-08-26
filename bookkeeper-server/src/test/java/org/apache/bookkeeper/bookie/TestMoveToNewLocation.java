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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class TestMoveToNewLocation {
    private final Params.NewLocation params;
    private static final String CARTELLA_FILE = "/tmp/file-info";
    private static final String NUOVA_CARTELLA_FILE = "/tmp/nuovo-file-info";
    private static final String NOME_FILE = IndexPersistenceMgr.getLedgerName(1); // TODO: attenzione a questo...
    private static File file;

    public TestMoveToNewLocation(Params.NewLocation params) {
        this.params = params;
    }


    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Params.NewLocation> getParameters() throws IOException {
        return configure();
    }

    private static List<Params.NewLocation> configure() throws IOException {
        // (COND1)  il file iniziale è stato eliminato.
        // (COND2)  il secondo file ha gli stessi contenuti del primo fino alla posizione size.
        // (COND3)  avviene una exception se vengono passati dei parametri illegali per il metodo (es. size = -1)

        List<Params.NewLocation> parameters = new ArrayList<>();
        // file null, size -1 -> COND3 (Exception)
        parameters.add(new Params.NewLocation(null, -1, true));
        // file non esistente, size 1 -> COND3
        parameters.add(new Params.NewLocation(new File("non-esisto"), 1, true));

        // file esistente, size 0/100/MAX_VALUE/-1 -> COND1 e COND2
        File fileEsistente = new File(NUOVA_CARTELLA_FILE, NOME_FILE);
        fileEsistente.mkdirs();
        parameters.add(new Params.NewLocation(fileEsistente, 0L, false));
        parameters.add(new Params.NewLocation(fileEsistente, 1124L, false));
        parameters.add(new Params.NewLocation(fileEsistente, Long.MAX_VALUE, false));
        parameters.add(new Params.NewLocation(fileEsistente, -1, false));

        return parameters;
    }

    @Before
    public void createFile() {
        file = new File(CARTELLA_FILE, NOME_FILE);
        file.getParentFile().getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void eliminaFile() throws IOException {
        FileUtils.deleteDirectory(new File(NUOVA_CARTELLA_FILE));
    }

    /**
     * Sposta il file in una nuova cartella e verifica che il file precedente sia
     * stato eliminato e che il nuovo file contenga le stesse cose del file precedente.
     *
     * Prima di spostare il file vengono scritti alcuni bytes.
     */
    @Test
    public void moveToNewLocation() {
        try {
            FileInfo fileInfo = new FileInfo(file, "testPasswd".getBytes(StandardCharsets.UTF_8), FileInfo.CURRENT_HEADER_VERSION);
            // scrivo qualcosa nel file
            fileInfo.write(new ByteBuffer[]{ByteBuffer.wrap("qualcosa".getBytes(StandardCharsets.UTF_8))}, 0);

            byte[] bi = readFile(fileInfo.getLf());

            fileInfo.moveToNewLocation(params.getFile(), params.getSize());
            assertFalse(params.isError());
            assertFalse(file.exists()); // il file iniziale non deve esistere.
            // fileInfo = new FileInfo(fileInfo.getLf(), "testPasswd".getBytes(StandardCharsets.UTF_8), FileInfo.CURRENT_HEADER_VERSION);
            byte[] bf = readFile(fileInfo.getLf());

            System.out.println(bi.length + "\n" + Arrays.toString(bi));
            System.out.println(bf.length + "\n" + Arrays.toString(bf));
            for (int i = 0; i < Math.min(bf.length, bi.length); i++) {
                assertEquals("I contenuti non coincidono: ", bi[i], bf[i]);
            }
            System.out.println("Il file precedente è stato eliminato e i contenuti coincidono");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(params.isError());
        }
    }

    private byte[] readFile(File theFile) {
        try {
            return FileUtils.readFileToByteArray(theFile);
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
