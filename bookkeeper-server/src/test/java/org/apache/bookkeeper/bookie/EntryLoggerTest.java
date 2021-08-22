package org.apache.bookkeeper.bookie;

import io.netty.buffer.ByteBufAllocator;
import org.apache.bookkeeper.common.allocator.impl.ByteBufAllocatorImpl;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.util.DiskChecker;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import static org.junit.Assert.*;

// @RunWith(Parameterized.class)
public class EntryLoggerTest {
    private static EntryLogger entryLogger;

  /*  public EntryLoggerTest(EntryLogger entryLogger) {
        this.entryLogger = entryLogger;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters(){
        EntryLogger el = Mockito.mock(EntryLogger.class);
        return Arrays.asList(new Object[][]{
                {el},
        });
    }*/

    @BeforeClass
    public static void setUp() throws IOException {
        ServerConfiguration s = new ServerConfiguration();
        // File tmpBkData = new File("/tmp/bk-data/");
        LedgerDirsManager mock = Mockito.mock(LedgerDirsManager.class);
        // if(!tmpBkData.exists()) tmpBkData.mkdir();
        entryLogger = new EntryLogger(s, mock);
    }

    @Test
    public void putInReadChannels() throws IOException {
        File f = new File("/tmp/entryloggertest");
        f.createNewFile();
        BufferedChannel bc = new BufferedChannel(ByteBufAllocator.DEFAULT, FileChannel.open(f.toPath()), 4);
        // ritorna null se non hai messo niente prima
        assertNull(entryLogger.putInReadChannels(0L, bc));
        //ritorna bc se hai messo qualcosa prima.
        assertEquals(bc, entryLogger.putInReadChannels(0L, bc));
    }

    @Test
    public void removeFromChannelsAndClose() {
        entryLogger.removeFromChannelsAndClose(0L);
    }


    @Test
    public void addEntryForCompaction() {
    }

    @Test
    public void flushCompactionLog() {
    }

    @Test
    public void createNewCompactionLog() {
    }

    @Test
    public void removeCurCompactionLog() {
    }

    @Test
    public void checkEntry() {
    }

    @Test
    public void readEntry() {
    }

    @Test
    public void logExists() {
    }

    @Test
    public void getEntryLogsSet() {
    }

    @Test
    public void scanEntryLog() {
    }

    @Test
    public void getEntryLogMetadata() {
    }

    @Test
    public void shutdown() {
    }
}