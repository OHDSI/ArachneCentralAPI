/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: May 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.repository.hibernate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SerializationException;
import org.hibernate.usertype.UserType;

public class JsonbType implements UserType {
    @Override
    public int[] sqlTypes() {

        return new int[]{Types.JAVA_OBJECT};
    }

    @Override
    public Class returnedClass() {

        return JsonObject.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {

        if (x == y) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        return x.equals(y);
    }

    @Override
    public int hashCode(Object o) throws HibernateException {

        if (o == null) {
            return 0;
        }
        return o.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] strings, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException, SQLException {
        final String json = resultSet.getString(strings[0]);
        if (json == null) {
            return null;
        }

        final JsonParser jsonParser = new JsonParser();
        return jsonParser.parse(json).getAsJsonObject();
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor sharedSessionContractImplementor) throws HibernateException, SQLException {

        if (value == null) {
            st.setNull(index, Types.OTHER);
            return;
        }

        st.setObject(index, value.toString(), Types.OTHER);
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {

        if (value == null) {
            return null;
        }
        final JsonParser jsonParser = new JsonParser();
        return jsonParser.parse(value.toString()).getAsJsonObject();
    }

    @Override
    public boolean isMutable() {

        return true;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {

        final Object deepCopy = deepCopy(value);

        if (!(deepCopy instanceof Serializable)) {
            throw new SerializationException(
                    String.format("deepCopy of %s is not serializable", value), null);
        }

        return (Serializable) deepCopy;
    }

    @Override
    public Object assemble(Serializable cached, final Object owner) throws HibernateException {

        return deepCopy(cached);
    }

    @Override
    public Object replace(Object original, final Object target, final Object owner) throws HibernateException {

        return deepCopy(original);
    }
}
