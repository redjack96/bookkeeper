package org.apache.bookkeeper.bookie;

import lombok.Data;

import java.nio.ByteBuffer;

/**
 * Classe che possiede al suo interno delle classi interne
 * utili per inizializzare i parametri dei test JUnit implementati.
 */
public class Params {
    @Data // Permette di creare a runtime getter e setter.
    public static class Entry {
        private final EntryLogger entryLogger;
        private final long ledgerId;
        private final long entryId;
        private long offset;
        private final boolean isExpectedSuccess;
        private EntryLoggerUtil.SimpleLedgerEntry simpleEntry;

        /**
         * Crea un set di parametri da passare al test TestCheckEntry o TestReadEntry
         *
         * @param ledgerId          un qualsiasi long che rappresenta l'id del ledger (o che non lo rappresenti)
         * @param entryId           un qualsiasi long che rappresenta l'id della entry (o che non lo rappresenti)
         * @param offset            posizione ricavata con EntryLoggerUtil.getPositionInEntryLog()
         * @param entryLogger       una istanza valida di entryLogger, non nulla. Può essere vuoto o contenere dei ledger.
         * @param isExpectedSuccess false se il metodo checkEntry deve causare un' eccezione, true se deve avere successo
         */
        public Entry(long ledgerId, long entryId, long offset, EntryLogger entryLogger, boolean isExpectedSuccess) {
            this.entryLogger = entryLogger;
            this.ledgerId = ledgerId;
            this.entryId = entryId;
            this.offset = offset; // possibilmente usare il metodo getPositionOfEntryInLog, quando implementato.
            this.isExpectedSuccess = isExpectedSuccess;
        }

        public String toString() {
            return "LEDGER " + ledgerId + " ENTRY " + entryId + " OFFSET " + offset;
        }
    }

    @Data
    public static class FileInfoWrite {
        private ByteBuffer[] buffs;
        private long position;
        private long expectedWrittenBytes;
        private boolean error;

        public FileInfoWrite(ByteBuffer[] buffs, long position, long expectedWrittenBytes, boolean error) {
            this.buffs = buffs;
            this.position = position;
            this.expectedWrittenBytes = expectedWrittenBytes;
            this.error = error;
        }

        public String toString() {
            return "BUFFERS " + buffs.length + " POSITION " + position + " EXPECTED " + expectedWrittenBytes;
        }
    }
}
