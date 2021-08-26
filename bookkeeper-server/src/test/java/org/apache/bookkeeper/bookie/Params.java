package org.apache.bookkeeper.bookie;

import lombok.Data;
import org.apache.bookkeeper.client.LedgerEntry;
import org.apache.bookkeeper.client.impl.LedgerEntryImpl;

/**
 * Classe che possiede al suo interno delle classi interne
 * utili per inizializzare i parametri dei test JUnit implementati.
 */
public class Params {
    public enum EntryLoggerStatus {EMPTY, CONTAINS_ENTRY, NOT_CONTAINS_ENTRY}
    public enum Offset {COMPATIBLE, INCOMPATIBLE, NEGATIVE}

    @Data // Permette di creare a runtime getter e setter.
    public static class CheckEntry {
        private final EntryLogger entryLogger;
        private final long ledgerId;
        private final long entryId;
        private final Offset offsetType;
        private final boolean isExpectedSuccess;
        private long offset;

        /**
         * Crea un set di parametri da passare al test TestCheckEntry
         * @param status status dell' EntryLogger: EMPTY, CONTAINS_ENTRY o NOT_CONTAINS_ENTRY
         * @param ledgerId un qualsiasi long che rappresenta l'id del ledger
         * @param entryId un qualsiasi long che rappresenta l'id della entry
         * @param offsetType COMPATIBLE, INCOMPATIBLE, NEGATIVE
         * @param isExpectedSuccess false se il metodo checkEntry deve causare un' eccezione, true se deve avere successo
         */
        public CheckEntry(EntryLoggerStatus status, long ledgerId, long entryId, Offset offsetType, boolean isExpectedSuccess) {
            switch (status) {
                case CONTAINS_ENTRY:
                    this.entryLogger = EntryLoggerUtil.createNonEmptyEntryLogger(ledgerId, entryId);
                    break;
                case NOT_CONTAINS_ENTRY:
                    this.entryLogger = EntryLoggerUtil.createNonEmptyEntryLogger(ledgerId + 30L, entryId + 40L);
                    break;
                default:
                    this.entryLogger = EntryLoggerUtil.createEmptyEntryLogger();
                    break;
            }
            this.ledgerId = ledgerId;
            this.entryId = entryId;
            this.offsetType = offsetType; // possibilmente usare il metodo getPositionOfEntryInLog, quando implementato.
            this.isExpectedSuccess = isExpectedSuccess;

            switch(offsetType){
                case NEGATIVE:
                    this.offset = -1;
                case COMPATIBLE:
                    this.offset = EntryLoggerUtil.getPositionInEntryLog(ledgerId, entryId);
                case INCOMPATIBLE:
                    this.offset = 0;
            }

        }
    }
}
