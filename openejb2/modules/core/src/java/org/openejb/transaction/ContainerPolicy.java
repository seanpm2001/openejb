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
package org.openejb.transaction;

import javax.ejb.TransactionRequiredLocalException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionRequiredException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocationResult;
import org.apache.geronimo.transaction.TransactionContext;
import org.apache.geronimo.transaction.UnspecifiedTransactionContext;
import org.apache.geronimo.transaction.InheritableTransactionContext;
import org.apache.geronimo.transaction.ContainerTransactionContext;

import org.openejb.EJBInvocation;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class ContainerPolicy {
    private static final Log log = LogFactory.getLog(ContainerPolicy.class);

    public static final TransactionPolicy NotSupported = new TxNotSupported(log);
    public static final TransactionPolicy Required = new TxRequired(log);
    public static final TransactionPolicy Supports = new TxSupports(log);
    public static final TransactionPolicy RequiresNew = new TxRequiresNew(log);
    public static final TransactionPolicy Mandatory = new TxMandatory();
    public static final TransactionPolicy Never = new TxNever(log);
    public static final TransactionPolicy BeforeDelivery = new TxBeforeDelivery();
    public static final TransactionPolicy AfterDelivery = new TxAfterDelivery(log);
    
    private static final class TxNotSupported implements TransactionPolicy {
        private final Log log;
        private TxNotSupported(Log log) {
            super();
            this.log = log;
        }
        public InvocationResult invoke(Interceptor interceptor, EJBInvocation ejbInvocation, TransactionManager txnManager) throws Throwable {
            TransactionContext clientContext = TransactionContext.getContext();
            if (clientContext != null) {
                clientContext.suspend();
            }
            try {
                TransactionContext beanContext = new UnspecifiedTransactionContext();
                TransactionContext.setContext(beanContext);
                beanContext.begin();
                ejbInvocation.setTransactionContext(beanContext);
                try {
                    InvocationResult result = interceptor.invoke(ejbInvocation);
                    beanContext.commit();
                    return result;
                } catch (Throwable t) {
                    try {
                        beanContext.rollback();
                    } catch (Exception e) {
                        log.warn("Unable to roll back", e);
                    }
                    throw t;
                }
            } finally {
                ejbInvocation.setTransactionContext(clientContext);
                TransactionContext.setContext(clientContext);
                if (clientContext != null) {
                    clientContext.resume();
                }
            }
        }
        public String toString() {
            return "NotSupported";
        }
    }
    private static final class TxRequired implements TransactionPolicy {
        private final Log log;
        private TxRequired(Log log) {
            super();
            this.log = log;
        }
        public InvocationResult invoke(Interceptor interceptor, EJBInvocation ejbInvocation, TransactionManager txnManager) throws Throwable {
            TransactionContext clientContext = TransactionContext.getContext();
            if (clientContext instanceof InheritableTransactionContext) {
                return interceptor.invoke(ejbInvocation);
            }

            if (clientContext != null) {
                clientContext.suspend();
            }
            try {
                TransactionContext beanContext = new ContainerTransactionContext(txnManager);
                TransactionContext.setContext(beanContext);
                beanContext.begin();
                ejbInvocation.setTransactionContext(beanContext);
                try {
                    InvocationResult result = interceptor.invoke(ejbInvocation);
                    beanContext.commit();
                    return result;
                } catch (Throwable t) {
                    try {
                        beanContext.rollback();
                    } catch (Exception e) {
                        log.warn("Unable to roll back", e);
                    }
                    throw t;
                }
            } finally {
                ejbInvocation.setTransactionContext(clientContext);
                TransactionContext.setContext(clientContext);
                if (clientContext != null) {
                    clientContext.resume();
                }
            }
        }
        public String toString() {
            return "Required";
        }
    }
    private static final class TxSupports implements TransactionPolicy {
        private final Log log;
        private TxSupports(Log log) {
            super();
            this.log = log;
        }
        public InvocationResult invoke(Interceptor interceptor, EJBInvocation ejbInvocation, TransactionManager txnManager) throws Throwable {
            TransactionContext clientContext = TransactionContext.getContext();
            if (clientContext != null) {
                return interceptor.invoke(ejbInvocation);
            }

            try {
                TransactionContext beanContext = new UnspecifiedTransactionContext();
                TransactionContext.setContext(beanContext);
                beanContext.begin();
                ejbInvocation.setTransactionContext(beanContext);
                try {
                    InvocationResult result = interceptor.invoke(ejbInvocation);
                    beanContext.commit();
                    return result;
                } catch (Throwable t) {
                    try {
                        beanContext.rollback();
                    } catch (Exception e) {
                        log.warn("Unable to roll back", e);
                    }
                    throw t;
                }
            } finally {
                ejbInvocation.setTransactionContext(null);
                TransactionContext.setContext(null);
            }
        }
        public String toString() {
            return "Supports";
        }
    }
    private static final class TxRequiresNew implements TransactionPolicy {
        private final Log log;
        private TxRequiresNew(Log log) {
            super();
            this.log = log;
        }
        public InvocationResult invoke(Interceptor interceptor, EJBInvocation ejbInvocation, TransactionManager txnManager) throws Throwable {
            TransactionContext clientContext = TransactionContext.getContext();

            if (clientContext != null) {
                clientContext.suspend();
            }
            try {
                TransactionContext beanContext = new ContainerTransactionContext(txnManager);
                TransactionContext.setContext(beanContext);
                beanContext.begin();
                ejbInvocation.setTransactionContext(beanContext);
                try {
                    InvocationResult result = interceptor.invoke(ejbInvocation);
                    beanContext.commit();
                    return result;
                } catch (Throwable t) {
                    try {
                        beanContext.rollback();
                    } catch (Exception e) {
                        log.warn("Unable to roll back", e);
                    }
                    throw t;
                }
            } finally {
                ejbInvocation.setTransactionContext(clientContext);
                TransactionContext.setContext(clientContext);
                if (clientContext != null) {
                    clientContext.resume();
                }
            }
        }
        public String toString() {
            return "RequiresNew";
        }
    }
    private static final class TxMandatory implements TransactionPolicy {
        public InvocationResult invoke(Interceptor interceptor, EJBInvocation ejbInvocation, TransactionManager txnManager) throws Throwable {
            TransactionContext clientContext = TransactionContext.getContext();
            if (clientContext instanceof InheritableTransactionContext) {
                return interceptor.invoke(ejbInvocation);
            }

            if (ejbInvocation.getType().isLocal()) {
                throw new TransactionRequiredLocalException();
            } else {
                throw new TransactionRequiredException();
            }
        }
        public String toString() {
            return "Mandatory";
        }
    }
    private static final class TxNever implements TransactionPolicy {
        private final Log log;
        private TxNever(Log log) {
            super();
            this.log = log;
        }
        public InvocationResult invoke(Interceptor interceptor, EJBInvocation ejbInvocation, TransactionManager txnManager) throws Throwable {
            TransactionContext clientContext = TransactionContext.getContext();

            if (clientContext instanceof InheritableTransactionContext) {
                throw new TransactionNotSupportedException();
            }

            if (clientContext != null) {
                return interceptor.invoke(ejbInvocation);
            }

            try {
                TransactionContext beanContext = new UnspecifiedTransactionContext();
                TransactionContext.setContext(beanContext);
                beanContext.begin();
                ejbInvocation.setTransactionContext(beanContext);
                try {
                    InvocationResult result = interceptor.invoke(ejbInvocation);
                    beanContext.commit();
                    return result;
                } catch (Throwable t) {
                    try {
                        beanContext.rollback();
                    } catch (Exception e) {
                        log.warn("Unable to roll back", e);
                    }
                    throw t;
                }
            } finally {
                ejbInvocation.setTransactionContext(null);
                TransactionContext.setContext(null);
            }
        }
        public String toString() {
            return "Never";
        }
    }
    //TODO INCOMPLETE: XAResource is not enlisted in new tx. Method tx attr. is not checked. clientContext is not saved.
    private static final class TxBeforeDelivery implements TransactionPolicy {
        public InvocationResult invoke(Interceptor interceptor, EJBInvocation ejbInvocation, TransactionManager txnManager) throws Throwable {
            TransactionContext clientContext = TransactionContext.getContext();
            if (clientContext instanceof InheritableTransactionContext) {
                return interceptor.invoke(ejbInvocation);
            }

            if (clientContext != null) {
                clientContext.suspend();
            }
            try {
                TransactionContext beanContext = new ContainerTransactionContext(txnManager);
                TransactionContext.setContext(beanContext);
                beanContext.begin();
                ejbInvocation.setTransactionContext(beanContext);
                return new SimpleInvocationResult(true, null);
            } catch (Exception e) {
                return new SimpleInvocationResult(false, e);
            }
        }
        public String toString() {
            return "BeforeDelivery";
        }
    }
    //TODO really broken. possible (imported) tx context is not restored.  XAResource is not delisted.
    private static final class TxAfterDelivery implements TransactionPolicy {
        private final Log log;
        private TxAfterDelivery(Log log) {
            super();
            this.log = log;
        }
        public InvocationResult invoke(Interceptor interceptor, EJBInvocation ejbInvocation, TransactionManager txnManager) throws Throwable {
            TransactionContext beanContext = TransactionContext.getContext();
            try {
                try {
                    beanContext.commit();
                    return new SimpleInvocationResult(true, null);
                } catch (Throwable t) {
                    try {
                        beanContext.rollback();
                    } catch (Exception e) {
                        log.warn("Unable to roll back", e);
                    }
                    throw t;
                }
            } finally {
                ejbInvocation.setTransactionContext(null);
                TransactionContext.setContext(null);
            }
        }
        public String toString() {
            return "AfterDelivery";
        }
    }
}