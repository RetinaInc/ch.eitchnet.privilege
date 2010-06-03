/*
 * Copyright (c) 2010
 * 
 * Robert von Burg
 * eitch@eitchnet.ch
 * 
 * All rights reserved.
 * 
 */

package ch.eitchnet.privilege.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import ch.eitchnet.privilege.base.PrivilegeContainer;
import ch.eitchnet.privilege.base.XmlConstants;
import ch.eitchnet.privilege.helper.ConfigurationHelper;
import ch.eitchnet.privilege.helper.XmlHelper;
import ch.eitchnet.privilege.i18n.PrivilegeException;
import ch.eitchnet.privilege.model.Certificate;
import ch.eitchnet.privilege.model.UserState;
import ch.eitchnet.privilege.model.internal.Privilege;
import ch.eitchnet.privilege.model.internal.Role;
import ch.eitchnet.privilege.model.internal.User;

/**
 * @author rvonburg
 * 
 */
public class DefaultPersistenceHandler implements PersistenceHandler {

	private static final Logger logger = Logger.getLogger(DefaultPersistenceHandler.class);

	private Map<String, User> userMap;
	private Map<String, Role> roleMap;
	private Map<String, Privilege> privilegesMap;

	private Map<String, User> transientUserMap;
	private Map<String, Role> transientRoleMap;
	private Map<String, Privilege> transientPrivilegesMap;

	/**
	 * @see ch.eitchnet.privilege.handler.PersistenceHandler#addPrivilege(ch.eitchnet.privilege.model.Certificate,
	 *      ch.eitchnet.privilege.model.internal.Privilege)
	 */
	@Override
	public void addPrivilege(Certificate certificate, Privilege privilege) {
		// TODO validate who is doing this

		privilegesMap.put(privilege.getName(), privilege);
		transientPrivilegesMap.put(privilege.getName(), privilege);
	}

	/**
	 * @see ch.eitchnet.privilege.handler.PersistenceHandler#addRole(ch.eitchnet.privilege.model.Certificate,
	 *      ch.eitchnet.privilege.model.internal.Role)
	 */
	@Override
	public void addRole(Certificate certificate, Role role) {
		// TODO validate who is doing this

		roleMap.put(role.getRoleName(), role);
		transientRoleMap.put(role.getRoleName(), role);
	}

	/**
	 * @see ch.eitchnet.privilege.handler.PersistenceHandler#addUser(ch.eitchnet.privilege.model.Certificate,
	 *      ch.eitchnet.privilege.model.internal.User)
	 */
	@Override
	public void addUser(Certificate certificate, User user) {
		// TODO validate who is doing this

		userMap.put(user.getUsername(), user);
		transientUserMap.put(user.getUsername(), user);
	}

	/**
	 * @see ch.eitchnet.privilege.handler.PersistenceHandler#getPrivilege(java.lang.String)
	 */
	@Override
	public Privilege getPrivilege(String privilegeName) {
		return privilegesMap.get(privilegeName);
	}

	/**
	 * @see ch.eitchnet.privilege.handler.PersistenceHandler#getRole(java.lang.String)
	 */
	@Override
	public Role getRole(String roleName) {
		return roleMap.get(roleName);
	}

	/**
	 * @see ch.eitchnet.privilege.handler.PersistenceHandler#getUser(java.lang.String)
	 */
	@Override
	public User getUser(String username) {
		return userMap.get(username);
	}

	/**
	 * @see ch.eitchnet.privilege.handler.PersistenceHandler#persist()
	 */
	@Override
	public void persist(Certificate certificate) {

		// TODO validate who is doing this

		// TODO Auto-generated method stub

	}

	/**
	 * @see ch.eitchnet.privilege.base.PrivilegeContainerObject#initialize(org.dom4j.Element)
	 */
	@Override
	public void initialize(Element element) {

		roleMap = new HashMap<String, Role>();
		userMap = new HashMap<String, User>();
		privilegesMap = new HashMap<String, Privilege>();

		// get parameters
		Element parameterElement = element.element(XmlConstants.XML_PARAMETERS);
		Map<String, String> parameterMap = ConfigurationHelper.convertToParameterMap(parameterElement);

		// get roles file name
		String rolesFileName = parameterMap.get(XmlConstants.XML_PARAM_ROLES_FILE);
		if (rolesFileName == null || rolesFileName.isEmpty()) {
			throw new PrivilegeException("[" + SessionHandler.class.getName() + "] Defined parameter "
					+ XmlConstants.XML_PARAM_ROLES_FILE + " is invalid");
		}

		// get roles file
		File rolesFile = new File(PrivilegeContainer.getInstance().getBasePath() + "/" + rolesFileName);
		if (!rolesFile.exists()) {
			throw new PrivilegeException("[" + SessionHandler.class.getName() + "] Defined parameter "
					+ XmlConstants.XML_PARAM_ROLES_FILE + " is invalid as roles file does not exist at path "
					+ rolesFile.getAbsolutePath());
		}

		// parse roles xml file to XML document
		Element rolesRootElement = XmlHelper.parseDocument(rolesFile).getRootElement();

		// read roles
		readRoles(rolesRootElement);

		// get users file name
		String usersFileName = parameterMap.get(XmlConstants.XML_PARAM_USERS_FILE);
		if (usersFileName == null || usersFileName.isEmpty()) {
			throw new PrivilegeException("[" + SessionHandler.class.getName() + "] Defined parameter "
					+ XmlConstants.XML_PARAM_USERS_FILE + " is invalid");
		}

		// get users file
		File usersFile = new File(PrivilegeContainer.getInstance().getBasePath() + "/" + usersFileName);
		if (!usersFile.exists()) {
			throw new PrivilegeException("[" + SessionHandler.class.getName() + "] Defined parameter "
					+ XmlConstants.XML_PARAM_USERS_FILE + " is invalid as users file does not exist at path "
					+ usersFile.getAbsolutePath());
		}

		// parse users xml file to XML document
		Element usersRootElement = XmlHelper.parseDocument(usersFile).getRootElement();

		// read users
		readUsers(usersRootElement);

		// get privileges file name
		String privilegesFileName = parameterMap.get(XmlConstants.XML_PARAM_PRIVILEGES_FILE);
		if (privilegesFileName == null || privilegesFileName.isEmpty()) {
			throw new PrivilegeException("[" + SessionHandler.class.getName() + "] Defined parameter "
					+ XmlConstants.XML_PARAM_PRIVILEGES_FILE + " is invalid");
		}

		// get privileges file
		File privilegesFile = new File(PrivilegeContainer.getInstance().getBasePath() + "/" + privilegesFileName);
		if (!privilegesFile.exists()) {
			throw new PrivilegeException("[" + SessionHandler.class.getName() + "] Defined parameter "
					+ XmlConstants.XML_PARAM_PRIVILEGES_FILE + " is invalid as privileges file does not exist at path "
					+ privilegesFile.getAbsolutePath());
		}

		// parse privileges xml file to XML document
		Element privilegesRootElement = XmlHelper.parseDocument(privilegesFile).getRootElement();

		// read privileges
		readPrivileges(privilegesRootElement);

		logger.info("Read " + userMap.size() + " Users");
		logger.info("Read " + roleMap.size() + " Roles");
		logger.info("Read " + privilegesMap.size() + " Privileges");
	}

	/**
	 * @param usersRootElement
	 */
	private void readUsers(Element usersRootElement) {

		List<Element> userElements = usersRootElement.elements(XmlConstants.XML_USER);
		for (Element userElement : userElements) {

			String username = userElement.attributeValue(XmlConstants.XML_ATTR_USERNAME);
			String password = userElement.attributeValue(XmlConstants.XML_ATTR_PASSWORD);

			String firstname = userElement.element(XmlConstants.XML_FIRSTNAME).getTextTrim();
			String surname = userElement.element(XmlConstants.XML_SURNAME).getTextTrim();

			UserState userState = UserState.valueOf(userElement.element(XmlConstants.XML_STATE).getTextTrim());

			// TODO better handling needed
			String localeName = userElement.element(XmlConstants.XML_LOCALE).getTextTrim();
			Locale locale = new Locale(localeName);

			Element rolesElement = userElement.element(XmlConstants.XML_ROLES);
			List<Element> rolesElementList = rolesElement.elements(XmlConstants.XML_ROLE);
			Set<String> roles = new HashSet<String>();
			for (Element roleElement : rolesElementList) {
				String roleName = roleElement.getTextTrim();
				if (roleName.isEmpty()) {
					logger.warn("User " + username + " has a role defined with no name, Skipped.");
				} else {
					roles.add(roleName);
				}
			}

			// create user
			User user = User.buildUser(username, password, firstname, surname, userState, roles, locale);

			// put user in map
			userMap.put(username, user);
		}
	}

	/**
	 * @param rolesRootElement
	 */
	private void readRoles(Element rolesRootElement) {

		List<Element> roleElements = rolesRootElement.elements(XmlConstants.XML_ROLE);
		for (Element roleElement : roleElements) {

			String roleName = roleElement.attributeValue(XmlConstants.XML_ATTR_NAME);

			List<Element> privilegeElements = roleElement.elements(XmlConstants.XML_PRIVILEGE);
			Set<String> privileges = new HashSet<String>();
			for (Element privilegeElement : privilegeElements) {

				String privilegeName = privilegeElement.attributeValue(XmlConstants.XML_ATTR_NAME);
				privileges.add(privilegeName);
			}

			Role role = new Role(roleName, privileges);
			roleMap.put(roleName, role);
		}
	}

	/**
	 * @param rolesRootElement
	 */
	private void readPrivileges(Element privilegesRootElement) {

		List<Element> privilegeElements = privilegesRootElement.elements(XmlConstants.XML_PRIVILEGE);
		for (Element privilegeElement : privilegeElements) {

			String privilegeName = privilegeElement.attributeValue(XmlConstants.XML_ATTR_NAME);
			String privilegePolicy = privilegeElement.attributeValue(XmlConstants.XML_ATTR_POLICY);

			String allAllowedS = privilegeElement.element(XmlConstants.XML_ALL_ALLOWED).getTextTrim();
			boolean allAllowed = Boolean.valueOf(allAllowedS);

			List<Element> denyElements = privilegeElement.elements(XmlConstants.XML_DENY);
			List<String> denyList = new ArrayList<String>(denyElements.size());
			for (Element denyElement : denyElements) {
				String denyValue = denyElement.getTextTrim();
				if (denyValue.isEmpty()) {
					logger.error("Privilege " + privilegeName + " has an empty deny value!");
				} else {
					denyList.add(denyValue);
				}
			}

			List<Element> allowElements = privilegeElement.elements(XmlConstants.XML_ALLOW);
			List<String> allowList = new ArrayList<String>(allowElements.size());
			for (Element allowElement : allowElements) {
				String allowValue = allowElement.getTextTrim();
				if (allowValue.isEmpty()) {
					logger.error("Privilege " + privilegeName + " has an empty allow value!");
				} else {
					allowList.add(allowValue);
				}
			}

			Privilege privilege = new Privilege(privilegeName, privilegePolicy, allAllowed, denyList, allowList);
			privilegesMap.put(privilegeName, privilege);
		}
	}
}
