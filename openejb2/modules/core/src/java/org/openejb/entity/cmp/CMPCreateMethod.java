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
package org.openejb.entity.cmp;

import java.lang.reflect.InvocationTargetException;
import javax.ejb.EntityBean;

import net.sf.cglib.reflect.FastClass;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocationResult;
import org.openejb.EJBContainer;
import org.openejb.EJBInterfaceType;
import org.openejb.EJBInvocation;
import org.openejb.EJBOperation;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.persistence.UpdateCommand;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class CMPCreateMethod implements VirtualOperation {
    private final EJBContainer container;
    private final FastClass beanClass;
    private final int createIndex;
    private final int postCreateIndex;
    private final UpdateCommand updateCommand;
    private final int slots;
    private final PrimaryKeyFactory pkFactory;

    public CMPCreateMethod(EJBContainer container, FastClass beanClass, int createIndex, int postCreateIndex, UpdateCommand updateCommand, int slots, PrimaryKeyFactory pkFactory) {
        this.container = container;
        this.beanClass = beanClass;
        this.createIndex = createIndex;
        this.postCreateIndex = postCreateIndex;
        this.updateCommand = updateCommand;
        this.slots = slots;
        this.pkFactory = pkFactory;
    }

    public InvocationResult execute(EJBInvocation invocation) throws Throwable {
        CMPInstanceContext ctx = (CMPInstanceContext) invocation.getEJBInstanceContext();
        InstanceData instanceData = new InstanceData(slots);
        ctx.setInstanceData(instanceData);

        EntityBean instance = (EntityBean) ctx.getInstance();
        Object[] args = invocation.getArguments();

        try {
            ctx.setOperation(EJBOperation.EJBCREATE);
            beanClass.invoke(createIndex, instance, args);
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof Exception && t instanceof RuntimeException == false) {
                // checked exception - which we simply include in the result
                return new SimpleInvocationResult(false, t);
            } else {
                // unchecked Exception - just throw it to indicate an abnormal completion
                throw t;
            }
        } finally {
            ctx.setOperation(EJBOperation.INACTIVE);
        }

        ctx.setId(pkFactory.getPrimaryKey(instanceData));
        Object[] values = new Object[slots];
        instanceData.store(values);
        updateCommand.executeUpdate(values);

        try {
            ctx.setOperation(EJBOperation.EJBPOSTCREATE);
            beanClass.invoke(postCreateIndex, instance, args);
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof Exception && t instanceof RuntimeException == false) {
                // checked exception - which we simply include in the result
                return new SimpleInvocationResult(false, t);
            } else {
                // unchecked Exception - just throw it to indicate an abnormal completion
                throw t;
            }
        } finally {
            ctx.setOperation(EJBOperation.INACTIVE);
        }

        EJBInterfaceType type = invocation.getType();
        return new SimpleInvocationResult(true, getReference(type.isLocal(), container, ctx.getId()));
    }

    private Object getReference(boolean local, EJBContainer container, Object id) {
        if (local) {
            return container.getEJBLocalObject(id);
        } else {
            return container.getEJBObject(id);
        }
    }
}