/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */

package org.openejb.resource.jdbc;

import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ResourceAdapter;
import javax.security.auth.Subject;
import java.sql.DriverManager;
import java.sql.Connection;

/**
 * @version $Revision$ $Date$
 */
public class BasicManagedConnectionFactory implements javax.resource.spi.ManagedConnectionFactory, java.io.Serializable {
    private final String jdbcDriver;
    private final String jdbcUrl;
    private final String defaultUserName;
    private final String defaultPassword;
    private java.io.PrintWriter logWriter;
    private final int hashCode;

    public BasicManagedConnectionFactory(String jdbcDriver, String jdbcUrl, String defaultUserName, String defaultPassword) {
        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
        this.defaultUserName = defaultUserName;
        this.defaultPassword = defaultPassword;
        hashCode = jdbcDriver.hashCode() ^ jdbcUrl.hashCode() ^ defaultUserName.hashCode() ^ defaultPassword.hashCode();
    }

    public Object createConnectionFactory() throws javax.resource.ResourceException {
        throw new javax.resource.NotSupportedException("This connector must be used with an application server connection manager");
    }

    public Object createConnectionFactory(ConnectionManager cxManager) throws javax.resource.ResourceException {
        return new JdbcConnectionFactory(this, cxManager, jdbcUrl, jdbcDriver, defaultPassword, defaultUserName);
    }

    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws javax.resource.ResourceException {
        try {
            JdbcConnectionRequestInfo request = (JdbcConnectionRequestInfo) connectionRequestInfo;
            Connection connection = DriverManager.getConnection(jdbcUrl, request.getUserName(), request.getPassword());
            return new JdbcManagedConnection(this, connection, request);
        } catch (java.sql.SQLException e) {
            throw (EISSystemException)new EISSystemException("Could not obtain a physical JDBC connection from the DriverManager").initCause(e);
        }
    }

    public boolean equals(Object object) {
        if (!(object instanceof BasicManagedConnectionFactory)) {
            return false;
        }
        BasicManagedConnectionFactory that = (BasicManagedConnectionFactory) object;
        return jdbcDriver.equals(that.jdbcDriver) && jdbcUrl.equals(that.jdbcUrl) && defaultUserName.equals(that.defaultUserName) && defaultPassword.equals(that.defaultPassword);
    }

    public java.io.PrintWriter getLogWriter() {
        return logWriter;
    }

    public int hashCode() {
        return hashCode;
    }

    public ManagedConnection matchManagedConnections(java.util.Set connectionSet, javax.security.auth.Subject subject, ConnectionRequestInfo connectionInfo) throws javax.resource.ResourceException {
        if (!(connectionInfo instanceof JdbcConnectionRequestInfo)) {
            return null;
        }

        JdbcManagedConnection[] connections = (JdbcManagedConnection[]) connectionSet.toArray(new JdbcManagedConnection[]{});
        int i = 0;
        for (; i < connections.length && !connections[i].getRequestInfo().equals(connectionInfo); i++) {
        }
        return (i < connections.length) ? connections[i] : null;
    }

    public void setLogWriter(java.io.PrintWriter out) {
        logWriter = out;
    }

    public ResourceAdapter getResourceAdapter() {
        return null; //TODO: implement this
    }

    public void setResourceAdapter(ResourceAdapter ra) {
        //TODO: implement this
    }


}