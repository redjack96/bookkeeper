package org.apache.bookkeeper.bookie;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
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

    private static List<Params.FileInfoWrite> configure() {
        String legalBufferString = "ciao";
        int legalSize = legalBufferString.length();
        int legalEmptySize = 0;
        ByteBuffer[] emptyArray = new ByteBuffer[]{};
        ByteBuffer[] legalArray = new ByteBuffer[]{FileInfoUtil.createBuffer(legalBufferString)};
        ByteBuffer[] legalEmptyArray = new ByteBuffer[]{FileInfoUtil.createBuffer("")};

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

        return Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8, p9);
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
        test();
    }

    private void test(){
        try {
            FileInfo fileInfo = new FileInfo(file, "testPasswd".getBytes(StandardCharsets.UTF_8), FileInfo.CURRENT_HEADER_VERSION);
            // Prima scrittura
            assertEquals(params.getExpectedWrittenBytes(), fileInfo.write(params.getBuffs(), params.getPosition()));
            assertFalse(params.isError());
            if(params.getBuffs().length > 0) {
                System.out.printf("Scritti correttamente %d bytes%n", params.getExpectedWrittenBytes());
            } else {
                System.out.println("Scritti 0 bytes.");
            }
        } catch (Exception e) {
            System.out.println(params.toString() + ": errore: " + e.getMessage() + Arrays.toString(e.getStackTrace()));
            assertTrue(params.isError());
        }
    }

}
