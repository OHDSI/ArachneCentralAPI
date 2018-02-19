package com.odysseusinc.arachne.portal.model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "users_data")
public class RawUser extends BaseUser implements IUser {
}
