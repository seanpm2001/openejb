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
package org.openejb;

import java.util.HashMap;


/**
 * This class is a bit crufty.  Need something like this, but not static
 * and more along the lines of a collection of containers registered as gbeans
 */
public class ContainerIndex {


    // TODO: Should be an array list or something
    EJBContainer[] containers = new EJBContainer[1];
    
    HashMap index = new HashMap();

    private static final ContainerIndex containerIndex = new ContainerIndex();
    
    public static ContainerIndex getInstance(){
        return containerIndex;
    }
    
    private ContainerIndex(){}

    
    public void addContainer(EJBContainer container){
        int i = containers.length;

        EJBContainer[] newArray = new EJBContainer[i+1];
        System.arraycopy(containers, 0, newArray, 0, i);
        containers = newArray;
        
        containers[i] =  container;
        index.put( container.getContainerID(), new Integer(i));
    }

    public int length(){
        return containers.length;
    }

    public int getContainerIndex(Object containerID){
        return getContainerIndex( (String)containerID);
    }
    
    public int getContainerIndex(String containerID){
        Integer idCode = (Integer)index.get( containerID );
        
        return ( idCode == null )? -1: idCode.intValue();
    }
    
    public EJBContainer getContainer(String containerID){
        return getContainer(getContainerIndex(containerID));
    }
    
    public EJBContainer getContainer(Integer index){
        return (index == null)? null: getContainer(index.intValue());
    }
    
    public EJBContainer getContainer(int index){
        return containers[index];
    }
}

