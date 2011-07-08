/**
 * Copyright (C) 2010-2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license a copy of which has been included with this distribution
 * in the license.txt file.
 */

package org.fusesource.fabric.apollo.amqp.codec;

import org.fusesource.fabric.apollo.amqp.codec.types.ApplicationProperties;
import org.fusesource.fabric.apollo.amqp.codec.types.Properties;
import org.fusesource.hawtbuf.DataByteArrayOutputStream;

import java.io.DataOutput;

/**
 *
 */
public abstract class BareMessage<K> {

    protected Properties properties;
    protected ApplicationProperties applicationProperties;
    protected K data;

    public K getData() {
        return data;
    }

    public void setData(K data) {
        this.data = data;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public ApplicationProperties getApplicationProperties() {
        return applicationProperties;
    }

    public void setApplicationProperties(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public abstract long size();
    public abstract void write(DataOutput out) throws Exception;

    public String toString() {
        StringBuffer buf = new StringBuffer();

        if (properties != null) {
            buf.append("\n");
            buf.append(properties.toString());
        }

        if (data != null) {
            buf.append("\n");
            buf.append(data.toString());
        }

        if (applicationProperties != null) {
            buf.append("\n");
            buf.append(applicationProperties.toString());
        }

        return buf.toString();
    }

}