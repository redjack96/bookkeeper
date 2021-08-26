package org.apache.bookkeeper.bookie;

import lombok.Data;

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
        private final boolean isExpectedSuccess;
        private long offset;
        private EntryLoggerUtil.SimpleLedgerEntry simpleEntry;

        public String toString(){
            return "LEDGER " + ledgerId + " ENTRY " + entryId + " OFFSET " + offset;
        }

        /**
         * Crea un set di parametri da passare al test TestCheckEntry
         * @param entryLogger una istanza valida di entryLogger, non nulla. Può essere vuoto o contenere dei ledger.
         * @param ledgerId un qualsiasi long che rappresenta l'id del ledger (o che non lo rappresenti)
         * @param entryId un qualsiasi long che rappresenta l'id della entry (o che non lo rappresenti)
         * @param offset posizione ricavata con EntryLoggerUtil.getPositionInEntryLog()
         * @param isExpectedSuccess false se il metodo checkEntry deve causare un' eccezione, true se deve avere successo
         */
        public CheckEntry(EntryLogger entryLogger, long ledgerId, long entryId, long offset, boolean isExpectedSuccess) {
            this.entryLogger = entryLogger;
            this.ledgerId = ledgerId;
            this.entryId = entryId;
            this.offset = offset; // possibilmente usare il metodo getPositionOfEntryInLog, quando implementato.
            this.isExpectedSuccess = isExpectedSuccess;

            this.simpleEntry = new EntryLoggerUtil.SimpleLedgerEntry();
            simpleEntry.setEntryLogger(entryLogger); // può essere vuoto
            simpleEntry.setPosition(offset); // può essere sbagliata!!!
            simpleEntry.setLedgerId(ledgerId); // può essere qualsiasi cosa
            simpleEntry.setEntryId(entryId); // idem
        }
    }
}
