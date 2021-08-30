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
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test del metodo FileInfo::read
 */
@RunWith(Parameterized.class)
public class TestWrite {
    private final Params.FileInfoWrite params;
    private static final String CARTELLA_FILE = "/tmp/file-info";
    private static final String NOME_FILE = IndexPersistenceMgr.getLedgerName(1);
    private static File file;
    private static FileInfo fileInfo;

    public TestWrite(Params.FileInfoWrite params){
        this.params = params;
    }

    @Before
    public void createFile() throws IOException {
        file = new File(CARTELLA_FILE, NOME_FILE);
        file.getParentFile().mkdirs();
        fileInfo = new FileInfo(file, "testPasswd".getBytes(StandardCharsets.UTF_8), FileInfo.CURRENT_HEADER_VERSION);
    }

    private static List<Params.FileInfoWrite> configure() {
        String legalBufferString = "ciao";
        int legalSize = legalBufferString.length();
        int legalEmptySize = 0;
        ByteBuffer[] emptyArray = new ByteBuffer[]{};
        ByteBuffer[] legalArray = new ByteBuffer[]{createBuffer(legalBufferString)};
        ByteBuffer[] legalEmptyArray = new ByteBuffer[]{createBuffer("")};

        // array vuoto, posizione = 0 -> 0 perché non ho scritto nulla.
        Params.FileInfoWrite p1 = new Params.FileInfoWrite(emptyArray, 50, 0, true);

        // array con un elemento legale lungo N, position = -1/0/1/100 -> N
        Params.FileInfoWrite p2 = new Params.FileInfoWrite(legalArray, 0, legalSize, false);
        Params.FileInfoWrite p3 = new Params.FileInfoWrite(legalArray, 1, legalSize, false);
        Params.FileInfoWrite p4 = new Params.FileInfoWrite(legalArray, 2, legalSize, false);
        Params.FileInfoWrite p5 = new Params.FileInfoWrite(legalArray, -1024, legalSize, false);

        // array con un buffer vuoto, posizione = -1/0/1/100 -> 0 perché non ho scritto nulla.
        Params.FileInfoWrite p6 = new Params.FileInfoWrite(legalEmptyArray, -1, legalEmptySize, false);
        Params.FileInfoWrite p7 = new Params.FileInfoWrite(legalEmptyArray, 0, legalEmptySize, false);
        Params.FileInfoWrite p8 = new Params.FileInfoWrite(legalEmptyArray, 1, legalEmptySize, false);
        Params.FileInfoWrite p9 = new Params.FileInfoWrite(legalEmptyArray, 100, legalEmptySize, false);
        // aggiunto per mutation testing
        Params.FileInfoWrite p10 = new Params.FileInfoWrite(legalEmptyArray, -10000, legalEmptySize, true);

        return Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10);
    }

    @Parameterized.Parameters(name = "{index} : {0}")
    public static Collection<Params.FileInfoWrite> getParameters() {
        return configure();
    }

    @After
    public void eliminaFile(){
        for (ByteBuffer buff : params.getBuffs()) {
            buff.position(0);
        }
        FileUtils.deleteQuietly(file);
    }


    /**
     * Esegue il metodo write.
     * La prima volta, se ci aspettiamo che ha successo, e il metodo ha successo,
     * controlliamo che scrive esattamente i byte che ci aspettavamo. Altrimenti controlliamo
     * che ci aspettavamo un errore.
     */
    @Test
    public void write(){
        try {
            // Prima scrittura
            assertEquals(params.getExpectedWrittenBytes(), fileInfo.write(params.getBuffs(), params.getPosition()));
            assertFalse(params.isError());
            if(params.getBuffs().length > 0) {
                System.out.printf("Scritti correttamente %d bytes%n", params.getExpectedWrittenBytes());
                assertEquals(Math.max(params.getExpectedWrittenBytes() + params.getPosition(), 0), fileInfo.size());
            } else {
                System.out.println("Scritti 0 bytes.");
            }
        } catch (Exception e) {
            System.out.println(params.toString() + ": errore: " + e.getMessage() + Arrays.toString(e.getStackTrace()));
            assertTrue(params.isError());
        }
    }

    private static ByteBuffer createBuffer(String content) {
        return ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8));
    }

}
