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
package org.openejb.nova.entity;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.lang.reflect.Method;

import javax.ejb.ObjectNotFoundException;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.hsqldb.jdbcDataSource;
import org.openejb.nova.MockTransactionManager;
import org.openejb.nova.transaction.TxnPolicy;
import org.openejb.nova.transaction.ContainerPolicy;
import org.openejb.nova.deployment.TransactionPolicySource;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.entity.cmp.CMPEntityContainer;
import org.openejb.nova.entity.cmp.CMPQuery;
import org.openejb.nova.entity.cmp.SimpleCommandFactory;
import org.openejb.nova.entity.cmp.CMRelation;
import org.openejb.nova.entity.cmp.CMPConfiguration;
import org.openejb.nova.persistence.jdbc.Binding;
import org.openejb.nova.persistence.jdbc.binding.IntBinding;
import org.openejb.nova.persistence.jdbc.binding.StringBinding;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class BasicCMPEntityContainerTest extends TestCase {
    private static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:ejb=Mock");
    static {
        new org.hsqldb.jdbcDriver();
    }

    private EntityContainerConfiguration config;
    private CMPEntityContainer container;
    private final jdbcDataSource ds = new jdbcDataSource();

    public void testLocalInvoke() throws Exception {
        Connection c = initDatabase();

        MockLocalHome home = (MockLocalHome) container.getEJBLocalHome();
        assertEquals(2, home.intMethod(1));

        MockLocal local = home.findByPrimaryKey(new Integer(1));
        assertEquals(3, local.intMethod(1));
        assertEquals(1, local.getIntField());
        c.close();
    }

    public void testFields() throws Exception {
        Connection c = initDatabase();
        Statement s = c.createStatement();
        MockLocalHome home = (MockLocalHome) container.getEJBLocalHome();
        MockLocal local = home.findByPrimaryKey(new Integer(1));
        assertEquals("Hello", local.getValue());
        local.setValue("World");
        ResultSet rs = s.executeQuery("SELECT VALUE FROM MOCK WHERE ID=1");
        assertTrue(rs.next());
        assertEquals("World", rs.getString(1));

        assertEquals("World", local.getValue());

        s.close();
        c.close();
    }

    public void testLifeCycle() throws Exception {
        Connection c = initDatabase();
        MockLocalHome home = (MockLocalHome) container.getEJBLocalHome();

        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT ID FROM MOCK WHERE ID=2");
        assertFalse(rs.next());
        rs.close();

        MockLocal local = home.create(new Integer(2), "Hello");
        rs = s.executeQuery("SELECT VALUE FROM MOCK WHERE ID=2");
        assertTrue(rs.next());
        assertEquals("Hello", rs.getString(1));
        rs.close();

        local = home.findByPrimaryKey(new Integer(2));
        assertEquals("Hello", local.getValue());

        local.remove();
        rs = s.executeQuery("SELECT ID FROM MOCK WHERE ID=2");
        assertFalse(rs.next());
        rs.close();
        s.close();
        c.close();
    }

    public void testSelect() throws Exception {
        Connection c = initDatabase();
        MockLocalHome home = (MockLocalHome) container.getEJBLocalHome();

        assertEquals("Hello", home.singleSelect(new Integer(1)));
        try {
            home.singleSelect(new Integer(2));
            fail("did not get ObjectNotFoundException");
        } catch (ObjectNotFoundException e) {
            // ok
        }

        Collection result = home.multiSelect(new Integer(1));
        assertEquals(1, result.size());
        assertEquals("Hello", result.iterator().next());

        result = home.multiSelect(new Integer(0));
        assertEquals(0, result.size());

        result = home.multiObject(new Integer(1));
        assertEquals(1, result.size());
        MockLocal local = (MockLocal) result.iterator().next();
        assertEquals(new Integer(1), local.getPrimaryKey());

        c.close();
    }

    protected void setUp() throws Exception {
        super.setUp();

        ds.setDatabase(".");
        ds.setUser("sa");
        ds.setPassword("");

        config = new EntityContainerConfiguration();
        config.uri = new URI("async", null, "localhost", 3434, "/JMX", null, CONTAINER_NAME.toString());
        config.beanClassName = MockCMPEJB.class.getName();
        config.homeInterfaceName = MockHome.class.getName();
        config.localHomeInterfaceName = MockLocalHome.class.getName();
        config.remoteInterfaceName = MockRemote.class.getName();
        config.localInterfaceName = MockLocal.class.getName();
        config.txnDemarcation = TransactionDemarcation.CONTAINER;
        config.txnManager = new MockTransactionManager();
        config.pkClassName = Integer.class.getName();
        config.trackedConnectionAssociator = new ConnectionTrackingCoordinator();
        config.unshareableResources = new HashSet();
        config.transactionPolicySource = new TransactionPolicySource() {
            public TxnPolicy getTransactionPolicy(String methodIntf, MethodSignature signature) {
                return ContainerPolicy.Required;
            }
            public TxnPolicy getTransactionPolicy(String methodIntf, String methodName, String[] parameterTypes) {
                return ContainerPolicy.Required;
            }
        };

        SimpleCommandFactory persistenceFactory = new SimpleCommandFactory(ds);
        ArrayList queries = new ArrayList();
        MethodSignature signature;

        signature = new MethodSignature("ejbFindByPrimaryKey", new String[]{"java.lang.Object"});
        persistenceFactory.defineQuery(signature, "SELECT ID FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)}, new Binding[]{new IntBinding(1, 0)});
        queries.add(new CMPQuery("Mock", false, signature, false, null));
        signature = new MethodSignature("ejbLoad", new String[]{});
        persistenceFactory.defineQuery(signature, "SELECT ID,VALUE FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)}, new Binding[]{new IntBinding(1, 0), new StringBinding(2, 1)});
        queries.add(new CMPQuery(signature, false, null));
        signature = new MethodSignature("ejbSelectSingleValue", new String[]{"java.lang.Integer"});
        persistenceFactory.defineQuery(signature, "SELECT VALUE FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)}, new Binding[]{new StringBinding(1, 0)});
        queries.add(new CMPQuery(signature, false, null));
        signature = new MethodSignature("ejbSelectMultiValue", new String[]{"java.lang.Integer"});
        persistenceFactory.defineQuery(signature, "SELECT VALUE FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)}, new Binding[]{new StringBinding(1, 0)});
        queries.add(new CMPQuery(signature, true, null));
        signature = new MethodSignature("ejbSelectMultiObject", new String[]{"java.lang.Integer"});
        persistenceFactory.defineQuery(signature, "SELECT ID FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)}, new Binding[]{new IntBinding(1, 0)});
        queries.add(new CMPQuery("Mock", true, signature, true, null));

        signature = new MethodSignature("ejbCreate", new String[]{"java.lang.Integer", "java.lang.String"});
        persistenceFactory.defineUpdate(signature, "INSERT INTO MOCK(ID, VALUE) VALUES(?,?)", new Binding[]{new IntBinding(1, 0), new StringBinding(2,1)});
        signature = new MethodSignature("ejbRemove", new String[0]);
        persistenceFactory.defineUpdate(signature, "DELETE FROM MOCK WHERE ID=?", new Binding[]{new IntBinding(1, 0)});
        signature = new MethodSignature("ejbStore", new String[0]);
        persistenceFactory.defineUpdate(signature, "UPDATE MOCK SET VALUE = ? WHERE ID=?", new Binding[]{new StringBinding(1,1), new IntBinding(2, 0)});

        CMPConfiguration cmpConfig = new CMPConfiguration();
        cmpConfig.persistenceFactory = persistenceFactory;
        cmpConfig.queries = (CMPQuery[]) queries.toArray(new CMPQuery[0]);
        cmpConfig.cmpFieldNames = new String[] { "id", "value" };
        cmpConfig.relations = new CMRelation[] {};
        container = new CMPEntityContainer(config, cmpConfig);

        persistenceFactory.defineContainer("Mock", container);
        container.doStart();
    }

    protected void tearDown() throws Exception {
        container.doStop();

        super.tearDown();
    }

    private Connection initDatabase() throws SQLException {
        Connection c = DriverManager.getConnection("jdbc:hsqldb:.", "sa", "");
        Statement s = c.createStatement();
        s.execute("CREATE TABLE MOCK(ID INTEGER, VALUE VARCHAR(50))");
        s.execute("INSERT INTO MOCK(ID, VALUE) VALUES(1, 'Hello')");
        s.close();
        return c;
    }

}
