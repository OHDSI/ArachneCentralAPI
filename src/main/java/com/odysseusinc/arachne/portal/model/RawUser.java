package com.odysseusinc.arachne.portal.model;

import javax.persistence.Entity;
import javax.persistence.NamedStoredProcedureQueries;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureParameter;
import javax.persistence.Table;

@Entity
@Table(name = "users_data")

@NamedStoredProcedureQueries({
        @NamedStoredProcedureQuery(name = "check_if_users_are_deletable",
                procedureName = "check_if_users_are_deletable",
                parameters = {
                        @StoredProcedureParameter(mode = ParameterMode.IN, name = "ids", type = String.class),
                        @StoredProcedureParameter(mode = ParameterMode.IN, name = "excluded_tables", type = String.class),
                        @StoredProcedureParameter(mode = ParameterMode.OUT, name = "filtered_ids", type = String.class)
                })
        })
public class RawUser extends BaseUser implements IUser {
}
