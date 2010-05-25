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
import java.util.Map;

import org.apache.log4j.Logger;

import ch.eitchnet.privilege.base.PrivilegeContainer;
import ch.eitchnet.privilege.i18n.AccessDeniedException;
import ch.eitchnet.privilege.i18n.PrivilegeException;
import ch.eitchnet.privilege.model.Certificate;
import ch.eitchnet.privilege.model.Restrictable;
import ch.eitchnet.privilege.model.Session;
import ch.eitchnet.privilege.model.User;
import ch.eitchnet.privilege.model.UserState;

/**
 * @author rvonburg
 * 
 */
public class DefaultSessionHandler implements SessionHandler {

	private static final Logger logger = Logger.getLogger(DefaultSessionHandler.class);

	private static long lastSessionId;

	private Map<String, User> userMap;
	private Map<String, CertificateSessionPair> sessionMap;

	/**
	 * @see ch.eitchnet.privilege.handler.SessionHandler#actionAllowed(ch.eitchnet.privilege.model.Certificate,
	 *      ch.eitchnet.privilege.model.Restrictable)
	 * 
	 * @throws AccessDeniedException
	 *             if the {@link Certificate} is not for a currently logged in {@link User} or if the user may not
	 *             perform the action defined by the {@link Restrictable} implementation
	 */
	@Override
	public boolean actionAllowed(Certificate certificate, Restrictable restrictable) {

		if (certificate == null)
			throw new PrivilegeException("Certificate may not be null!");
		else if (restrictable == null)
			throw new PrivilegeException("Restrictable may not be null!");

		// first see if a session exists for this certificate
		CertificateSessionPair certificateSessionPair = sessionMap.get(certificate.getSessionId());
		if (certificateSessionPair == null)
			throw new AccessDeniedException("There is no session information for " + certificate.toString());

		// validate certificate has not been tampered with
		Certificate sessionCertificate = certificateSessionPair.certificate;
		if (!sessionCertificate.equals(certificate))
			throw new PrivilegeException("Received illegal certificate for session id " + certificate.getSessionId());

		// TODO is this overkill?
		// validate authToken from certificate using the sessions authPassword
		String authToken = certificate.getAuthToken(certificateSessionPair.session.getAuthPassword());
		if (authToken == null || !authToken.equals(certificateSessionPair.session.getAuthToken()))
			throw new PrivilegeException("Received illegal certificate data for session id "
					+ certificate.getSessionId());

		// get user object
		User user = userMap.get(certificateSessionPair.session.getUsername());
		if (user == null) {
			throw new PrivilegeException(
					"Oh now, how did this happen: No User in user map although certificate is valid!");
		}

		// now validate on policy handler
		return PrivilegeContainer.getInstance().getPolicyHandler().actionAllowed(user, restrictable);
	}

	/**
	 * @see ch.eitchnet.privilege.handler.SessionHandler#authenticate(java.lang.String, java.lang.String)
	 * 
	 * @throws AccessDeniedException
	 *             if the user credentials are not valid
	 */
	@Override
	public Certificate authenticate(String username, String password) {

		// both username and password must at least have 3 characters in length
		if (username == null || username.length() < 3)
			throw new PrivilegeException("The given username is shorter than 3 characters");
		else if (password == null || password.length() < 3)
			throw new PrivilegeException("The given password is shorter than 3 characters");

		EncryptionHandler encryptionHandler = PrivilegeContainer.getInstance().getEncryptionHandler();

		// we only work with hashed passwords
		String passwordHash = encryptionHandler.convertToHash(password);

		// get user object
		User user = userMap.get(username);
		// no user means no authentication
		if (user == null)
			throw new AccessDeniedException("There is no user defined with the credentials: " + username + " / ***...");

		// validate password
		if (!user.getPassword().equals(passwordHash))
			throw new AccessDeniedException("Password is incorrect for " + username + " / ***...");

		// validate if user is allowed to login
		if (user.getState() != UserState.ENABLED)
			throw new AccessDeniedException("User " + username + " is not ENABLED. State is: " + user.getState());

		// get 2 auth tokens
		String authToken = encryptionHandler.nextToken();
		String authPassword = encryptionHandler.nextToken();

		// get next session id
		String sessionId = nextSessionId();

		// create certificate
		Certificate certificate = new Certificate(sessionId, username, authToken, authPassword, user.getLocale());

		// create and save a new session
		Session session = new Session(sessionId, authToken, authPassword, user.getUsername(), System
				.currentTimeMillis());
		sessionMap.put(sessionId, new CertificateSessionPair(session, certificate));

		// log
		logger.info("Authenticated: " + session);

		// return the certificate
		return certificate;
	}

	private String nextSessionId() {
		return Long.toString(++lastSessionId % Long.MAX_VALUE);
	}

	public void initialize(File userFile) {
		// TODO implement
	}

	private class CertificateSessionPair {
		private Session session;
		private Certificate certificate;

		public CertificateSessionPair(Session session, Certificate certificate) {
			this.session = session;
			this.certificate = certificate;
		}
	}
}