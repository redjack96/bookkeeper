/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.apache.bookkeeper.bookie;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.testutils.BkConfigurationUtil;
import org.apache.bookkeeper.util.DiskChecker;
import org.apache.bookkeeper.util.IOUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class EntryLoggerUtil {
    private static final List<File> tempDirs = new ArrayList<>();
    private static File rootDir;
    private static File currentDir; // la directory "current" dentro la quale vengono salvati i file entry log
    private static final Map<Integer, Long> LEDGER_POSITION_MAP = new HashMap<>(); // dato l'hash formato da ledger id e entry id, viene restituita la posizione
    private final Random rand = new Random();
    /**
     * @return crea e restituisce la directory in cui bookkeeper crea i log entry files
     */
    public static File createTempDir(String prefix, String suffix) {
        try {
            File dir = IOUtils.createTempDir(prefix, suffix);

            // restituisce una directory "current" figlia della directory passata in input
            File currentDir = BookieImpl.getCurrentDirectory(dir);

            // crea la directory current, se tutto è ok.
            BookieImpl.checkDirectoryStructure(currentDir);

            EntryLoggerUtil.tempDirs.add(dir);
            EntryLoggerUtil.rootDir = dir;
            EntryLoggerUtil.currentDir = currentDir;
            return dir;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Impossibile creare le cartelle temporanee");
        }
    }

    /**
     * Elimina le directory temporanee e pulisce la lista.
     */
    public static void deleteDirs() {
        Exception e1 = null;
        try {
            for (File dir : tempDirs) {
                FileUtils.deleteDirectory(dir);
            }
        } catch (Exception e) {
            e.printStackTrace();
            e1 = e;
        } finally {
            tempDirs.clear();
        }
        if (e1 != null) throw new IllegalStateException("Impossibile creare le cartelle temporanee");
    }


    public static EntryLogger createEmptyEntryLogger() {
        try {
            return new EntryLogger(BkConfigurationUtil.newServerConfiguration(), getLedgerDirsManager());
        } catch (Exception e) {
            deleteDirs();
            throw new IllegalStateException("impossibile creare un EntryLogger vuoto: " + e.getMessage());
        }
    }

    public static EntryLogger createNonEmptyEntryLogger(long ledgerId, long entryId) {
        try {
            EntryLogger entryLogger = createEmptyEntryLogger();
            long positionInEntryLog = addEntryToLogger(entryLogger, ledgerId, entryId);
            LEDGER_POSITION_MAP.put(getHash(ledgerId, entryId), positionInEntryLog);
            return entryLogger;
        } catch (Exception e) {
            deleteDirs();
            throw new IllegalStateException("impossibile creare un EntryLogger con una entry: " + e.getMessage());
        }
    }

    public static LedgerDirsManager getLedgerDirsManager() {
        ServerConfiguration conf = BkConfigurationUtil.newServerConfiguration();
        DiskChecker diskChecker = new DiskChecker(conf.getDiskUsageThreshold(), conf.getDiskUsageWarnThreshold());
        return new LedgerDirsManager(conf, new File[]{rootDir}, diskChecker);
    }

    /**
     * Genera una entry con contenuti casuali.
     * @param ledger id del ledger a cui la entry deve appartenere
     * @param entry id della entry
     * @param length lunghezza della entry in byte, comprensiva dei 16 byte che rappresentano i precedenti 2 parametri
     * @return il buffer di byte che rappresenta la entry
     */
    private ByteBuf generateRandomEntry(long ledger, long entry, int length) {
        ByteBuf bb = Unpooled.buffer(length);
        bb.writeLong(ledger);
        bb.writeLong(entry);
        byte[] randByteArray = new byte[length - 8 - 8]; // quando scrivo una entry, bisogna scrivere anche i metadati che la identificano: ledgerId (8byte) e entryId(8byte)
        rand.nextBytes(randByteArray);
        bb.writeBytes(randByteArray);
        return bb;
    }

    /**
     * Genera una entry nel formato: "ledger-[ledgerId]-entry[entryId]". Affinché
     * la entry sia valida, prima dei dati vengono preposti nell' entrylog [ledgerId][entryId]
     * @param ledger id del ledger
     * @param entry id della entry
     * @return ByteBuf che rappresenta la entry completo di metadati e dati.
     */
    private static ByteBuf generateEntry(long ledger, long entry) {
        byte[] data = generateDataString(ledger, entry).getBytes();
        ByteBuf bb = Unpooled.buffer(8 + 8 + data.length);
        bb.writeLong(ledger);
        bb.writeLong(entry);
        bb.writeBytes(data);
        return bb;
    }

    private static String generateDataString(long ledger, long entry) {
        return ("ledger-" + ledger + "-" + entry);
    }

    /**
     * Aggiunge una entry al ledger richiesto per mezzo dell'entryLogger fornito.
     * @param entryLogger una istanza valida di entryLogger
     * @param ledgerId un ledger id. L'id potrebbe essere già utilizzato: in tal caso la entry viene aggiunta alla fine dello stream del ledger.
     * @return se ha successo restituisce la location della entry
     */
    public static long addEntryToLogger(EntryLogger entryLogger, long ledgerId, long entryId){
        try {
            return entryLogger.addEntry(ledgerId, generateEntry(ledgerId, entryId).nioBuffer());
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     * Metodo che a partire da ledgerId e entryId ricava la posizione corretta nell'entryLog
     * @param ledgerId id del ledger
     * @param entryId id della entry nel ledger
     * @return -1 se non trova nulla, altrimenti l'offset.
     */
    public static long getPositionInEntryLog(long ledgerId, long entryId) {
        int hash = getHash(ledgerId, entryId); // ricavo l'hash univoco generato da ledgerId e entryId

        Long l = EntryLoggerUtil.LEDGER_POSITION_MAP.get(hash);
        if (l != null) return l;
        return -1;
    }

    private static int getHash(long a, long b){
        return Objects.hash(1L, a, 2L, b);
    }
}