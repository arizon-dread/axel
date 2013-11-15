/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.broker.messagestore;

import se.inera.axel.shs.client.MessageListConditions;
import se.inera.axel.shs.mime.ShsMessage;

import java.io.InputStream;

/**
 * The broker's interface to the message database log/queue.
 * <p/>
 *
 * This log is used to store the incoming message (with {@link #saveMessage(se.inera.axel.shs.mime.ShsMessage)}
 * and get a reference to an {@link ShsMessageEntry}-object. (Claim-Check-pattern).<br/>
 * That object is then processed and dispatched inside the broker until the message is to
 * be sent to a remote destination or handed to a client. Then the original message is
 * fetched from the database with {@link #loadMessage(ShsMessageEntry)}. <br/>
 *
 * An implementation of this interface would typically interact with {@link MessageStoreService} to handle the actual store/fetch of the real {@link ShsMessage}.
 */
public interface MessageLogService {

    /**
     * Saves a message to the physical message store and returns a log entry
     * containing header values and routing status of the message.
     * A txId only be used once (in one transaction), use the other methods to make updates to the entry.
     *
     * @param message The message as it enters the server.
     * @return A log entry that should be used during message routing.
     * @throws MessageAlreadyExistsException if the same txId is reused in another transaction.
     */
	ShsMessageEntry saveMessage(ShsMessage message);

    /**
     * Loads a message from the physical message store given a message log entry.
     * @param entry Entry representing the message.
     * @return An instance of ShsMessage that can be used to send to recipients.
     */
    ShsMessage loadMessage(ShsMessageEntry entry);

    /**
     * Loads a log entry from the transaction database
     * given an shs address and transaction id of the message.
     *
     * @param shsTo An shs address, usually an org. nbr.
     * @param txId Transaction id in the format of an uuid
     * @return The log entry, if found.
     * @throws MessageNotFoundException if the message cannot be found.
     */
    ShsMessageEntry loadEntry(String shsTo, String txId);

    /**
     * Loads a log entry from the transaction database and marks it
     * for fetching by a client.
     *
     * @param shsTo An shs address, usually an org. nbr.
     * @param txId Transaction id in the format of an uuid
     * @return The log entry with status set to {@link MessageState#FETCHING_IN_PROGRESS}, if found.
     * @throws MessageNotFoundException if the message cannot be found.
     */
    ShsMessageEntry loadEntryAndLockForFetching(String shsTo, String txId);

    /**
     * Updates a log entry with state {@link MessageState#RECEIVED}
     *
     * @param entry Entry to update
     * @return The log entry with new status.
     */
    ShsMessageEntry messageReceived(ShsMessageEntry entry);

    /**
     * Updates a log entry with state {@link MessageState#QUARANTINED}
     *
     * @param entry Entry to update
     * @return The log entry with new status.
     */
    ShsMessageEntry messageQuarantined(ShsMessageEntry entry, Exception exception);

    /**
     * Updates a log entry with state {@link MessageState#SENT}
     *
     * @param entry Entry to update
     * @return The log entry with new status.
     */
    ShsMessageEntry messageSent(ShsMessageEntry entry);

    /**
     * Updates a log entry with state {@link MessageState#FETCHED}
     *
     * @param entry Entry to update
     * @return The log entry with new status.
     */
    ShsMessageEntry messageFetched(ShsMessageEntry entry);

    /**
     * Updates a log entry with acknowledge flag set to true.
     *
     * @param entry Entry to update
     * @return The acknowledged log entry
     */
    ShsMessageEntry messageAcknowledged(ShsMessageEntry entry);

    /**
     * Updates a log entry with state {@link MessageState#ONE_TO_MANY}
     *
     * @param entry Entry to update
     * @return The log entry with new status.
     */
    ShsMessageEntry messageOneToMany(ShsMessageEntry entry);
    
    /**
     * Puts related messages into QUARANTINE, given information in the error message.
     *
     * @param message The SHS error message.
     * @return
     */
    ShsMessage quarantineCorrelatedMessages(ShsMessage message);

    /**
     * Acknowledges related messages, given information in the confirm message.
     *
     * @param message The SHS confirm message.
     * @return
     */
    ShsMessage acknowledgeCorrelatedMessages(ShsMessage message);

    /**
     * Issues a transaction log database update.
     *
     * @param entry Entry to save.
     * @return The updated entry.
     */
	ShsMessageEntry update(ShsMessageEntry entry);


    /**
     * Can be used to reset messages that have been 'stuck' in state {@link MessageState#FETCHING_IN_PROGRESS}
     * for more than an hour. These messages are set to state {@link MessageState#RECEIVED} again.
     */
    void releaseStaleFetchingInProgress();


    /**
     * Lists messages according to the filter criteria.
     *
     * @param shsTo An shs address, usually an org. nbr.
     * @param filter A criteria object.
     * @return The asynchronous messages matching the criteria.
     */
    Iterable<ShsMessageEntry> listMessages(String shsTo, MessageListConditions filter);

    ShsMessageEntry saveMessageStream(InputStream mimeMessageStream);


}
