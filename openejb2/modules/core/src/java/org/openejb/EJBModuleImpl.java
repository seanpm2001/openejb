/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb;

import java.util.Hashtable;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.gbean.jmx.GBeanMBeanContext;
import org.apache.geronimo.j2ee.management.J2EEApplication;
import org.apache.geronimo.j2ee.management.J2EEServer;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.j2ee.management.impl.Util;
import org.openejb.entity.cmp.ConnectionProxyFactory;
import org.tranql.query.ConnectionFactoryDelegate;

/**
 * @version $Revision$ $Date$
 */
public class EJBModuleImpl implements GBean {
    private final J2EEServer server;
    private final J2EEApplication application;
    private final String deploymentDescriptor;
    private final ConnectionFactoryDelegate delegate;
    private final ConnectionProxyFactory connectionFactory;
    private GBeanContext context;
    private String baseName;

    public EJBModuleImpl(J2EEServer server, J2EEApplication application, String deploymentDescriptor, ConnectionFactoryDelegate delegate, ConnectionProxyFactory connectionFactory) {
        this.server = server;
        this.application = application;
        this.deploymentDescriptor = deploymentDescriptor;
        this.delegate = delegate;
        this.connectionFactory = connectionFactory;
    }

    public String getDeploymentDescriptor() {
        return deploymentDescriptor;
    }

    public String getServer() {
        return server.getobjectName();
    }

    public String getApplication() {
        if (application == null) {
            return null;
        }
        return application.getobjectName();
    }

    public String[] getJavaVMs() {
        return server.getjavaVMs();
    }

    public String[] getEJBs() throws MalformedObjectNameException {
        return Util.getObjectNames(((GBeanMBeanContext) context).getServer(),
                baseName,
                new String[]{"EntityBean", "StatelessSessionBean", "StatefulSessionBean", "MessageDrivenBean"});
    }

    public void setGBeanContext(GBeanContext context) {
        this.context = context;
        if (context != null) {
            ObjectName objectName = context.getObjectName();
            verifyObjectName(objectName);

            // build the base name used to query the server for child modules
            Hashtable keyPropertyList = objectName.getKeyPropertyList();
            String name = (String) keyPropertyList.get("name");
            String j2eeServerName = (String) keyPropertyList.get("J2EEServer");
            String j2eeApplicationName = (String) keyPropertyList.get("J2EEServer");
            baseName = objectName.getDomain() + ":J2EEServer=" + j2eeServerName + ",J2EEApplication=" + j2eeApplicationName + ",EJBModule=" + name + ",";
        } else {
            baseName = null;
        }
    }

    /**
     * ObjectName must match this pattern:
     * <p/>
     * domain:j2eeType=EJBModule,name=MyName,J2EEServer=MyServer,J2EEApplication=MyApplication
     */
    private void verifyObjectName(ObjectName objectName) {
        if (objectName.isPattern()) {
            throw new InvalidObjectNameException("ObjectName can not be a pattern", objectName);
        }
        Hashtable keyPropertyList = objectName.getKeyPropertyList();
        if (!"EJBModule".equals(keyPropertyList.get("j2eeType"))) {
            throw new InvalidObjectNameException("EJBModule object name j2eeType property must be 'EJBModule'", objectName);
        }
        if (!keyPropertyList.containsKey("name")) {
            throw new InvalidObjectNameException("EJBModule object must contain a name property", objectName);
        }
        if (!keyPropertyList.containsKey("J2EEServer")) {
            throw new InvalidObjectNameException("EJBModule object name must contain a J2EEServer property", objectName);
        }
        if (!keyPropertyList.containsKey("J2EEApplication")) {
            throw new InvalidObjectNameException("EJBModule object name must contain a J2EEApplication property", objectName);
        }
        if (keyPropertyList.size() != 4) {
            throw new InvalidObjectNameException("EJBModule object name can only have j2eeType, name, J2EEApplication, and J2EEServer properties", objectName);
        }
    }

    public void doStart() throws WaitingException, Exception {
        if (delegate != null) {
            delegate.setConnectionFactory(connectionFactory.getProxy());
        }
    }

    public void doStop() throws WaitingException, Exception {
        if (delegate != null) {
            delegate.setConnectionFactory(null);
        }
    }

    public void doFail() {
        if (delegate != null) {
            delegate.setConnectionFactory(null);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(EJBModuleImpl.class);
        infoFactory.addReference("J2EEServer", J2EEServer.class);
        infoFactory.addReference("J2EEApplication", J2EEApplication.class);
        infoFactory.addAttribute("deploymentDescriptor", true);
        infoFactory.addReference("ConnectionFactory", ConnectionProxyFactory.class);
        infoFactory.addAttribute("Delegate", true);

        infoFactory.addAttribute("server", false);
        infoFactory.addAttribute("application", false);
        infoFactory.addAttribute("javaVMs", false);
        infoFactory.addAttribute("ejbs", false);

        infoFactory.setConstructor(
                new String[]{"J2EEServer", "J2EEApplication", "deploymentDescriptor", "Delegate", "ConnectionFactory"},
                new Class[]{J2EEServer.class, J2EEApplication.class, String.class, ConnectionFactoryDelegate.class, ConnectionProxyFactory.class});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
