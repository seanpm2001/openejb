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
package org.openejb.persistence.jdbc.binding;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.CallableStatement;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.Reader;
import java.io.StringWriter;

import org.openejb.persistence.jdbc.Binding;
import org.openejb.persistence.Tuple;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public final class CharacterStreamBinding implements Binding {
    private final int index;
    private final int slot;

    public CharacterStreamBinding(int index, int slot) {
        this.index = index;
        this.slot = slot;
    }

    public void bind(PreparedStatement ps, Object[] args) throws SQLException {
        String str = (String) args[slot];
        ps.setCharacterStream(index, new StringReader(str), str.length());
    }

    public void unbind(ResultSet rs, Tuple tuple) throws SQLException {
        Object[] values = tuple.getValues();
        Reader reader = rs.getCharacterStream(index);
        try {
            if (rs.wasNull()) {
                values[slot] = null;
            } else {
                char[] buffer = new char[4096];
                StringWriter writer = new StringWriter(4096);
                int len;
                try {
                    while ((len = reader.read(buffer)) != -1) {
                        writer.write(buffer, 0, len);
                    }
                } catch (IOException e) {
                    SQLException sqlException = new SQLException("Unable to read Character stream from server");
                    sqlException.initCause(e);
                    throw sqlException;
                }
                values[slot] = writer.toString();
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    SQLException sqlException = new SQLException("Unable to close Character stream");
                    sqlException.initCause(e);
                    throw sqlException;
                }
            }
        }
    }

    public void unbind(CallableStatement cs, Tuple tuple) throws SQLException {
        //TODO implement this
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public int getLength() {
        return 1;
    }
}