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
package org.openejb.entity.bmp;

import javax.ejb.EntityContext;
import javax.ejb.Timer;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.timer.PersistentTimer;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.UserTransactionImpl;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openejb.dispatch.DispatchInterceptor;
import org.openejb.entity.EntityInstanceInterceptor;
import org.openejb.entity.EntityCallbackInterceptor;
import org.openejb.naming.ComponentContextInterceptor;
import org.openejb.security.EJBIdentityInterceptor;
import org.openejb.security.EjbRunAsInterceptor;
import org.openejb.security.EjbSecurityInterceptor;
import org.openejb.security.PolicyContextHandlerEJBInterceptor;
import org.openejb.transaction.TransactionContextInterceptor;
import org.openejb.BmpEjbContainer;
import org.openejb.ConnectionTrackingInterceptor;
import org.openejb.SystemExceptionInterceptor;
import org.openejb.EJBInstanceContext;
import org.openejb.EjbCallbackInvocation;
import org.openejb.CallbackMethod;
import org.openejb.ExtendedEjbDeployment;
import org.openejb.EjbInvocation;
import org.openejb.EjbInvocationImpl;
import org.openejb.EJBInterfaceType;


/**
 * @version $Revision$ $Date$
 */
public class DefaultBmpEjbContainer implements BmpEjbContainer {
    private static final Log log = LogFactory.getLog(DefaultBmpEjbContainer.class);
    private final Interceptor invocationChain;
    private final Interceptor callbackChain;
    private final PersistentTimer transactedTimer;
    private final PersistentTimer nontransactionalTimer;
    private final TransactionContextManager transactionContextManager;
    private final UserTransactionImpl userTransaction;

    public DefaultBmpEjbContainer(
            TransactionContextManager transactionContextManager,
            TrackedConnectionAssociator trackedConnectionAssociator,
            PersistentTimer transactionalTimer,
            PersistentTimer nontransactionalTimer,
            boolean securityEnabled,
            boolean doAsCurrentCaller,
            boolean useContextHandler) throws Exception {

        this.transactionContextManager = transactionContextManager;
        this.userTransaction = new UserTransactionImpl(transactionContextManager, trackedConnectionAssociator);
        this.transactedTimer = transactionalTimer;
        this.nontransactionalTimer = nontransactionalTimer;

        //
        // build the normal invocation processing chain (built in reverse order)
        //

        Interceptor invocationChain;
        invocationChain = new DispatchInterceptor();
        if (doAsCurrentCaller) {
            invocationChain = new EJBIdentityInterceptor(invocationChain);
        }

        if (securityEnabled) {
            invocationChain = new EjbSecurityInterceptor(invocationChain);
        }
        invocationChain = new EjbRunAsInterceptor(invocationChain);
        if (useContextHandler) {
            invocationChain = new PolicyContextHandlerEJBInterceptor(invocationChain);
        }
        invocationChain = new ComponentContextInterceptor(invocationChain);
        if (trackedConnectionAssociator != null) {
            invocationChain = new ConnectionTrackingInterceptor(invocationChain, trackedConnectionAssociator);
        }
        invocationChain = new EntityInstanceInterceptor(invocationChain);
        invocationChain = new TransactionContextInterceptor(invocationChain, transactionContextManager);
        invocationChain = new SystemExceptionInterceptor(invocationChain);
        this.invocationChain = invocationChain;

        //
        // Callback chain is used for ejb state change callbacks
        //

        Interceptor callbackChain = new EntityCallbackInterceptor();
        if (doAsCurrentCaller) {
            callbackChain = new EJBIdentityInterceptor(callbackChain);
        }
        callbackChain = new ComponentContextInterceptor(callbackChain);
        this.callbackChain = callbackChain;
    }

    public TransactionContextManager getTransactionContextManager() {
        return transactionContextManager;
    }

    public UserTransactionImpl getUserTransaction() {
        return userTransaction;
    }

    public PersistentTimer getTransactedTimer() {
        return transactedTimer;
    }

    public PersistentTimer getNontransactedTimer() {
        return nontransactionalTimer;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        return invocationChain.invoke(invocation);
    }

    public void setContext(EJBInstanceContext instanceContext, EntityContext entityContext) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.SET_CONTEXT, instanceContext, new Object[]{entityContext});
        callbackChain.invoke(invocation);
    }

    public void unsetContext(EJBInstanceContext instanceContext) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.UNSET_CONTEXT, instanceContext);
        callbackChain.invoke(invocation);
    }

    public void ejbActivate(EJBInstanceContext instanceContext) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.ACTIVATE, instanceContext);
        callbackChain.invoke(invocation);
    }

    public void ejbPassivate(EJBInstanceContext instanceContext) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.PASSIVATE, instanceContext);
        callbackChain.invoke(invocation);
    }

    public void load(EJBInstanceContext instanceContext, TransactionContext transactionContext) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.LOAD, instanceContext);
        invocation.setTransactionContext(transactionContext);
        callbackChain.invoke(invocation);
    }

    public void store(EJBInstanceContext instanceContext, TransactionContext transactionContext) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.STORE, instanceContext);
        invocation.setTransactionContext(transactionContext);
        callbackChain.invoke(invocation);
    }

    public void timeout(ExtendedEjbDeployment deployment, Object id, Timer timer, int ejbTimeoutIndex) {
        EjbInvocation invocation = new EjbInvocationImpl(EJBInterfaceType.TIMEOUT, id, ejbTimeoutIndex, new Object[] {timer});
        invocation.setEjbDeployment(deployment);

        // set the transaction context into the invocation object
        TransactionContext transactionContext = transactionContextManager.getContext();
        if (transactionContext == null) {
            throw new IllegalStateException("Transaction context has not been set");
        }
        invocation.setTransactionContext(transactionContext);

        Thread currentThread = Thread.currentThread();
        ClassLoader oldClassLoader = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(deployment.getClassLoader());
            invoke(invocation);
        } catch (Throwable throwable) {
            log.warn("Timer invocation failed", throwable);
        } finally {
            currentThread.setContextClassLoader(oldClassLoader);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(DefaultBmpEjbContainer.class, "BmpEjbContainer");

        infoFactory.addReference("TransactionContextManager", TransactionContextManager.class, NameFactory.TRANSACTION_CONTEXT_MANAGER);
        infoFactory.addReference("TrackedConnectionAssociator", TrackedConnectionAssociator.class, NameFactory.JCA_CONNECTION_TRACKER);
        infoFactory.addReference("TransactedTimer", PersistentTimer.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addReference("NontransactedTimer", PersistentTimer.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addAttribute("securityEnabled", boolean.class, true);
        infoFactory.addAttribute("doAsCurrentCaller", boolean.class, true);
        infoFactory.addAttribute("useContextHandler", boolean.class, true);
        infoFactory.setConstructor(new String[]{
            "TransactionContextManager",
            "TrackedConnectionAssociator",
            "TransactedTimer",
            "NontransactedTimer",
            "securityEnabled",
            "doAsCurrentCaller",
            "useContextHandler"});

        infoFactory.addInterface(BmpEjbContainer.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
