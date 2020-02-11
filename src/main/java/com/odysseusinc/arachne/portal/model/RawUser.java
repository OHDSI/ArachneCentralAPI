package com.odysseusinc.arachne.portal.model;

import static com.odysseusinc.arachne.portal.model.RawUser.CHECK_IF_USERS_ARE_DELETABLE;

import javax.persistence.Entity;
import javax.persistence.NamedStoredProcedureQueries;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureParameter;
import javax.persistence.Table;

@Entity
@Table(name = "users_data")

@NamedStoredProcedureQueries({
        @NamedStoredProcedureQuery(name = CHECK_IF_USERS_ARE_DELETABLE,
                procedureName = CHECK_IF_USERS_ARE_DELETABLE,
                parameters = {
                        @StoredProcedureParameter(mode = ParameterMode.IN, name = "ids", type = String.class),
                        @StoredProcedureParameter(mode = ParameterMode.IN, name = "excluded_tables", type = String.class),
                        @StoredProcedureParameter(mode = ParameterMode.OUT, name = "filtered_ids", type = String.class)
                })
})
public class RawUser extends BaseUser implements IUser {

    public static final String CHECK_IF_USERS_ARE_DELETABLE = "check_if_users_are_deletable";
}

