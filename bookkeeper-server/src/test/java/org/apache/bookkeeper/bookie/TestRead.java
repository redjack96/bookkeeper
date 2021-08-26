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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test del metodo FileInfo::read
 */
@RunWith(Parameterized.class)
public class TestRead {
    private final Params.FileInfoWrite params;
    private static final String CARTELLA_FILE = "/tmp/file-info";
    private static final String NOME_FILE = IndexPersistenceMgr.getLedgerName(1);
    private static final String PATHNAME_FILE = CARTELLA_FILE + "/" + NOME_FILE;
    private static File file;

    public TestRead(Params.FileInfoWrite params){
        this.params = params;
    }

    @Before
    public void createFile() {
        file = new File(CARTELLA_FILE, NOME_FILE);
        file.getParentFile().mkdirs();
    }

    private static List<Params.FileInfoWrite> configure() throws IOException {
        ByteBuffer[] emptyArray = new ByteBuffer[]{};
        ByteBuffer[] legalArray = new ByteBuffer[]{FileInfoUtil.createBuffer("ciao")};
        ByteBuffer[] legalEmptyArray = new ByteBuffer[]{FileInfoUtil.createBuffer("")};

        // array vuoto, posizione = 0 -> 0 perché non ho scritto nulla.
        Params.FileInfoWrite p1 = new Params.FileInfoWrite(emptyArray, 0, 0);

        // array vuoto, position = -1 -> IOException

        // array vuoto, posizione = 1 -> 0
        // array vuoto, posizione size-2 -> 0
        // array vuoto, posizione size-1 -> 0
        // array con un elemento legale lungo N, position = -1 -> IOException
        // array con un elemento legale lungo N, posizione = 0 -> N perché non ho scritto nulla.
        // array con un elemento legale lungo N, posizione = 1 -> N
        // array con un elemento legale lungo N, posizione size-2 -> 1
        // array con un elemento legale lungo N, posizione size-1 -> 0
        // array con un buffer vuoto , position = -1 -> IOException
        // array con un buffer vuoto, posizione = 0 -> 0 perché non ho scritto nulla.
        // array con un buffer vuoto, posizione = 1 -> 0
        // array con un buffer vuoto, posizione size-2 -> 0
        // array con un buffer vuoto, posizione size-1 -> 0


        return Arrays.asList(p1);
    }

    @Parameterized.Parameters
    public static Collection<Params.FileInfoWrite> data() throws IOException {
        return configure();
    }

    @After
    public void eliminaFile(){
        FileUtils.deleteQuietly(file);
    }


    @Test
    public void write(){
        try {
            FileInfo fileInfo = new FileInfo(file, "testPasswd".getBytes(StandardCharsets.UTF_8), FileInfo.CURRENT_HEADER_VERSION);
            fileInfo.write(new ByteBuffer[]{ ByteBuffer.wrap("BKLE".getBytes(StandardCharsets.UTF_8)) }, 0);
            assertEquals(params.getExpectedWrittenBytes(), fileInfo.write(params.getBuffs(), params.getPosition()));
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(params.getExpectedWrittenBytes() == -1);
        }
    }
}
