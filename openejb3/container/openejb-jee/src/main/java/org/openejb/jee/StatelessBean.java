/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.jee;

import java.util.Collections;

/**
 * @version $Revision$ $Date$
 */
public class StatelessBean extends SessionBean {
    public StatelessBean(String ejbName, String ejbClass) {
        super(ejbName, ejbClass, SessionType.STATELESS);
        postActivate = Collections.EMPTY_LIST;
        prePassivate = Collections.EMPTY_LIST;
    }

    public StatelessBean() {
        this(null, null);
    }

    public void setSessionType(SessionType value) {
    }

}