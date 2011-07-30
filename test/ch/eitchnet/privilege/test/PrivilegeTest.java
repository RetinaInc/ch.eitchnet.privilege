/*
 * Copyright (c) 2010
 * 
 * Robert von Burg
 * eitch@eitchnet.ch
 * 
 * All rights reserved.
 * 
 */

package ch.eitchnet.privilege.test;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eitchnet.privilege.handler.PrivilegeHandler;
import ch.eitchnet.privilege.helper.InitializationHelper;
import ch.eitchnet.privilege.i18n.AccessDeniedException;
import ch.eitchnet.privilege.i18n.PrivilegeException;
import ch.eitchnet.privilege.model.Certificate;
import ch.eitchnet.privilege.model.PrivilegeRep;
import ch.eitchnet.privilege.model.Restrictable;
import ch.eitchnet.privilege.model.RoleRep;
import ch.eitchnet.privilege.model.UserRep;
import ch.eitchnet.privilege.model.UserState;

/**
 * JUnit for performing Privilege tests. This JUnit is by no means complete, but checks the bare minimum. TODO add more
 * tests, especially with deny and allow lists
 * 
 * @author rvonburg
 */
public class PrivilegeTest {

	private static final String ADMIN = "admin";
	private static final String PASS_ADMIN = "admin";
	private static final String BOB = "bob";
	private static final String TED = "ted";
	private static final String PASS_BOB = "admin1";
	private static final String ROLE_USER = "user";
	private static final String PASS_BAD = "123";

	private static final Logger logger = Logger.getLogger(PrivilegeTest.class);

	private static PrivilegeHandler privilegeHandler;

	/**
	 * @throws Exception
	 *             if something goes wrong
	 */
	@BeforeClass
	public static void init() throws Exception {

		try {
			// set up log4j
			BasicConfigurator.resetConfiguration();
			BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d %5p [%t] %C{1} %M - %m%n")));
			Logger.getRootLogger().setLevel(Level.INFO);

			// initialize container
			String pwd = System.getProperty("user.dir");
			File privilegeContainerXmlFile = new File(pwd + "/config/Privilege.xml");
			privilegeHandler = InitializationHelper.initializeFromXml(privilegeContainerXmlFile);
		} catch (Exception e) {
			logger.error(e, e);

			throw new RuntimeException("Initialization failed: " + e.getLocalizedMessage(), e);
		}
	}

	/**
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void testAuthenticationOk() throws Exception {

		Certificate certificate = privilegeHandler.authenticate(ADMIN, PASS_ADMIN);
		org.junit.Assert.assertTrue("Certificate is null!", certificate != null);
	}

	/**
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test(expected = AccessDeniedException.class)
	public void testFailAuthenticationNOk() throws Exception {

		Certificate certificate = privilegeHandler.authenticate(ADMIN, PASS_BAD);
		org.junit.Assert.assertTrue("Certificate is null!", certificate != null);
	}

	/**
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test(expected = PrivilegeException.class)
	public void testFailAuthenticationPWNull() throws Exception {

		Certificate certificate = privilegeHandler.authenticate(ADMIN, null);
		org.junit.Assert.assertTrue("Certificate is null!", certificate != null);
	}

	/**
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void testAddUserBobAsAdmin() throws Exception {

		Certificate certificate = privilegeHandler.authenticate(ADMIN, PASS_ADMIN);

		// let's add a new user bob
		UserRep userRep = new UserRep("1", BOB, "Bob", "Newman", UserState.NEW, new HashSet<String>(), null);
		privilegeHandler.addOrReplaceUser(certificate, userRep, null);
		logger.info("Added user " + BOB);

		// set bob's password
		privilegeHandler.setUserPassword(certificate, BOB, PASS_BOB);
		logger.info("Set Bob's password");
	}

	/**
	 * Will fail because user bob is not yet enabled
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test(expected = AccessDeniedException.class)
	public void testFailAuthAsBob() throws Exception {
		privilegeHandler.authenticate(BOB, PASS_BOB);
	}

	/**
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void testEnableUserBob() throws Exception {
		Certificate certificate = privilegeHandler.authenticate(ADMIN, PASS_ADMIN);
		privilegeHandler.setUserState(certificate, BOB, UserState.ENABLED);
	}

	/**
	 * Will fail as user bob has no role
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test(expected = PrivilegeException.class)
	public void testFailAuthUserBob() throws Exception {

		Certificate certificate = privilegeHandler.authenticate(BOB, PASS_BOB);
		org.junit.Assert.assertTrue("Certificate is null!", certificate != null);
	}

	/**
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void testAddRole() throws Exception {
		Certificate certificate = privilegeHandler.authenticate(ADMIN, PASS_ADMIN);

		Map<String, PrivilegeRep> privilegeMap = new HashMap<String, PrivilegeRep>();
		RoleRep roleRep = new RoleRep(ROLE_USER, privilegeMap);

		privilegeHandler.addOrReplaceRole(certificate, roleRep);
	}

	/**
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void testAddRoleToBob() throws Exception {
		Certificate certificate = privilegeHandler.authenticate(ADMIN, PASS_ADMIN);
		privilegeHandler.addRoleToUser(certificate, BOB, ROLE_USER);
	}

	/**
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void testAuthAsBob() throws Exception {
		privilegeHandler.authenticate(BOB, PASS_BOB);
	}

	/**
	 * Will fail because user bob does not have admin rights
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test(expected = AccessDeniedException.class)
	public void testFailAddUserTedAsBob() throws Exception {

		// auth as Bog
		Certificate certificate = privilegeHandler.authenticate(BOB, PASS_BOB);
		org.junit.Assert.assertTrue("Certificate is null!", certificate != null);

		// let's add a new user Ted
		UserRep userRep = new UserRep("1", TED, "Ted", "And then Some", UserState.NEW, new HashSet<String>(), null);
		privilegeHandler.addOrReplaceUser(certificate, userRep, null);
		logger.info("Added user " + TED);
	}

	/**
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void testAddAdminRoleToBob() throws Exception {

		Certificate certificate = privilegeHandler.authenticate(ADMIN, PASS_ADMIN);
		privilegeHandler.addRoleToUser(certificate, BOB, PrivilegeHandler.PRIVILEGE_ADMIN_ROLE);
		logger.info("Added " + PrivilegeHandler.PRIVILEGE_ADMIN_ROLE + " to " + ADMIN);
	}

	/**
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void testAddUserTedAsBob() throws Exception {

		Certificate certificate = privilegeHandler.authenticate(BOB, PASS_BOB);
		org.junit.Assert.assertTrue("Certificate is null!", certificate != null);

		// let's add a new user ted
		UserRep userRep = new UserRep("2", TED, "Ted", "Newman", UserState.NEW, new HashSet<String>(), null);
		privilegeHandler.addOrReplaceUser(certificate, userRep, null);
		logger.info("Added user " + TED);
	}

	/**
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	public void testPerformRestrictable() throws Exception {

		Certificate certificate = privilegeHandler.authenticate(ADMIN, PASS_ADMIN);
		org.junit.Assert.assertTrue("Certificate is null!", certificate != null);

		// see if eitch can perform restrictable
		Restrictable restrictable = new TestRestrictable();
		boolean actionAllowed = privilegeHandler.actionAllowed(certificate, restrictable);
		org.junit.Assert.assertTrue(ADMIN + " may not perform restrictable!", actionAllowed);
	}
}
