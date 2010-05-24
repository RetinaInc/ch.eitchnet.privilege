/*
 * Copyright (c) 2010
 * 
 * Robert von Burg
 * eitch@eitchnet.ch
 * 
 * All rights reserved.
 * 
 */

package ch.eitchnet.privilege.model;

/**
 * @author rvonburg
 * 
 */
public interface RestrictionPolicy {

	public boolean actionAllowed(User user, Restrictable restrictable);
}