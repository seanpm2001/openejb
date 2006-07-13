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
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.admin.web.cmp.mapping;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.exolab.castor.jdo.conf.Database;
import org.exolab.castor.mapping.xml.MappingRoot;

/**
 * @author <a href="mailto:tim_urberg@yahoo.com">Tim Urberg</a>
 */
public class CMPMappingDataBean implements SessionBean {
	private Database globalDatabase;
	private Database localDatabase;
	private String globalDatabaseFileName;
	private String localDatabaseFileName;
	private MappingRoot mappingRoot;
	private String mappingRootFileName;
	
	public void ejbCreate() {}

	public void ejbActivate() {}

	public void ejbPassivate() {}

	public void ejbRemove() {}

	public void setSessionContext(SessionContext arg0) {}

	public Database getGlobalDatabase() {
		return globalDatabase;
	}

	public String getGlobalDatabaseFileName() {
		return globalDatabaseFileName;
	}

	public Database getLocalDatabase() {
		return localDatabase;
	}

	public String getLocalDatabaseFileName() {
		return localDatabaseFileName;
	}

	public MappingRoot getMappingRoot() {
		return mappingRoot;
	}

	public String getMappingRootFileName() {
		return mappingRootFileName;
	}

	public void setGlobalDatabase(Database database) {
		globalDatabase = database;
	}

	public void setGlobalDatabaseFileName(String string) {
		globalDatabaseFileName = string;
	}

	public void setLocalDatabase(Database database) {
		localDatabase = database;
	}

	public void setLocalDatabaseFileName(String string) {
		localDatabaseFileName = string;
	}

	public void setMappingRoot(MappingRoot root) {
		mappingRoot = root;
	}

	public void setMappingRootFileName(String string) {
		mappingRootFileName = string;
	}

}