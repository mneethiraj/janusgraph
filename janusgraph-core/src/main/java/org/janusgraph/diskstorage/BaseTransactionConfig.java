package org.janusgraph.diskstorage;

import org.janusgraph.diskstorage.util.time.*;
import org.janusgraph.diskstorage.configuration.ConfigOption;
import org.janusgraph.diskstorage.configuration.Configuration;

import java.time.Instant;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 * @author Dan LaRocque <dalaro@hopcount.org>
 */
public interface BaseTransactionConfig {

    /**
     * Returns the commit time of this transaction which is either a custom timestamp provided
     * by the user, the commit time as set by the enclosing operation, or the first time this method is called.
     *
     * @return commit timestamp for this transaction
     */
    public Instant getCommitTime();

    /**
     * Sets the commit time of this transaction. If a commit time has already been set, this method throws
     * an exception. Use {@link #hasCommitTime()} to check prior to setting.
     *
     * @param time
     */
    public void setCommitTime(Instant time);

    /**
     * Returns true if a commit time has been set on this transaction.
     *
     * @return
     */
    public boolean hasCommitTime();

    /**
     * Returns the timestamp provider of this transaction.
     */
    public TimestampProvider getTimestampProvider();

    /**
     * Returns the (possibly null) group name for this transaction.
     * Transactions are grouped under this name for reporting and error tracking purposes.
     *
     * @return group name prefix string or null
     */
    public String getGroupName();

    /**
     * True when {@link #getGroupName()} is non-null, false when null.
     */
    public boolean hasGroupName();

    /**
     * Get an arbitrary transaction-specific option.
     *
     * @param opt option for which to return a value
     * @return value of the option
     */
    public <V> V getCustomOption(ConfigOption<V> opt);

    /**
     * Return any transaction-specific options.
     *
     * @see #getCustomOption(ConfigOption)
     * @return options for this tx
     */
    public Configuration getCustomOptions();
}