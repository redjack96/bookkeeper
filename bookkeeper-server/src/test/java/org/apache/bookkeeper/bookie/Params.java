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

import lombok.Data;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * Classe che possiede al suo interno delle classi interne
 * utili per inizializzare i parametri dei test JUnit implementati.
 * Implementata per non confondere i diversi parametri, che spesso sono tutti long.
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

    @Data
    public static class NewLocation {
        private File file;
        private long size;
        private boolean error;

        public NewLocation(File file, long size, boolean error) {
            this.file = file;
            this.size = size;
            this.error = error;
        }

        public String toString() {
            if(file == null){
                return "FILE null SIZE " + size + " ERROR " + error;
            }
            return "FILE " + file.getPath() + " SIZE " + size + " ERROR " + error;
        }
    }
}
