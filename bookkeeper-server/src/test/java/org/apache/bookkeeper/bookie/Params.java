package org.apache.bookkeeper.bookie;

import lombok.Data;
import org.mockito.Mockito;

/**
 * Classe che possiede al suo interno delle classi interne
 * utili per inizializzare i parametri dei test JUnit implementati.
 */
public class Params {
    public enum EntryLoggerStatus {EMPTY, ONE_ENTRY, DIFFERENT_ENTRY}


    @Data // Permette di creare a runtime getter e setter.
    public static class CheckEntry{
        private final EntryLogger entryLogger;
        private final long ledgerId;
        private final long entryId;
        private final long entryOffset;
        private final boolean isExpectedSuccess;

        public CheckEntry(EntryLoggerStatus status, long ledgerId, long entryId, long entryOffset, boolean isExpectedSuccess) {
            if(status.equals(EntryLoggerStatus.EMPTY)){
                this.entryLogger = EntryLoggerUtil.createEmptyEntryLogger(Mockito.mock(LedgerDirsManager.class));
            } else if(status.equals(EntryLoggerStatus.ONE_ENTRY)){
                // TODO: passare ledger Id al metodo
                this.entryLogger = EntryLoggerUtil.createNonEmptyEntryLogger();
            } else {
                // TODO: passare un altro ledger id al metodo, diverso da quello in input
                this.entryLogger = EntryLoggerUtil.createNonEmptyEntryLogger();
            }
            this.ledgerId = ledgerId;
            this.entryId = entryId; // TODO: eventualmente ricavarlo dall'entry logger... può essere che se viene creao più di un entry non sia sempre 0!
            this.entryOffset = entryOffset; // possibilmente usare il metodo getPositionOfEntryInLog, quando implementato.
            this.isExpectedSuccess = isExpectedSuccess;
        }
    }
}
