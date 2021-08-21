package org.apache.bookkeeper.bookie;

import org.junit.Test;
import org.mockito.Mockito;

// @RunWith(Parameterized.class)
public class EntryLoggerTest {
    private EntryLogger entryLogger = Mockito.mock(EntryLogger.class);;

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

    @Test
    public void putInReadChannels() {
        BufferedChannel mock = Mockito.mock(BufferedChannel.class);
        entryLogger.putInReadChannels(0L, mock);
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