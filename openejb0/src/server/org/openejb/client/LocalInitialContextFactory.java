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
 * Copyright 2002 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.client;

import org.openejb.EnvProps;
import org.openejb.core.ivm.naming.NamingException;

import java.util.Hashtable;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;


/**
 * LocalInitialContextFactory
 * 
 * @author David Blevins
 * @author Richard Monson-Haefel
 * @since 10/5/2002
 */
public class LocalInitialContextFactory implements javax.naming.spi.InitialContextFactory {

    public Context getInitialContext( Hashtable env ) throws javax.naming.NamingException {

        if ( !org.openejb.OpenEJB.isInitialized() ) {
            initializeOpenEJB(env);
        }

        Context context = org.openejb.OpenEJB.getJNDIContext();
        context = (Context)context.lookup( "java:openejb/ejb" );

        return context;

    }

    private void initializeOpenEJB( Hashtable env ) throws javax.naming.NamingException {
        try { 
	    Properties props = new Properties();
    
	    //  Prepare defaults
	    /* DMB: We should get the defaults from the functionality 
	     *      Alan is working on.  This is temporary.
	     *      When that logic is finished, this block should
	     *      probably just be deleted.
	     */
	    props.put( EnvProps.ASSEMBLER, "org.openejb.alt.assembler.classic.Assembler" );
	    props.put( EnvProps.CONFIGURATION_FACTORY, "org.openejb.alt.config.ConfigurationFactory" );
	    props.put( EnvProps.CONFIGURATION, "conf/default.openejb.conf" );
    
	    //  Override defaults with System properties
	    props.putAll( System.getProperties() );
    
	    //  Override defauls again with JNDI Env properties
	    props.putAll( env );
    
	    org.openejb.OpenEJB.init( props );

        } catch( org.openejb.OpenEJBException e ) {
            throw new NamingException( "Cannot initailize OpenEJB", e );
        } catch( Exception e ) {
            throw new NamingException( "Cannot initailize OpenEJB", e );
        }
    }
}

 