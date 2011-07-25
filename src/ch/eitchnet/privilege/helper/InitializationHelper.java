/*
 * Copyright (c) 2010
 * 
 * Robert von Burg
 * eitch@eitchnet.ch
 * 
 * All rights reserved.
 * 
 */

package ch.eitchnet.privilege.helper;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import ch.eitchnet.privilege.handler.EncryptionHandler;
import ch.eitchnet.privilege.handler.PersistenceHandler;
import ch.eitchnet.privilege.handler.PrivilegeHandler;
import ch.eitchnet.privilege.i18n.PrivilegeException;

/**
 * This class implements the initializing of the {@link PrivilegeHandler} by loading an XML file containing the
 * configuration
 * 
 * @author rvonburg
 */
public class InitializationHelper {

	private static final Logger logger = Logger.getLogger(InitializationHelper.class);

	/**
	 * Initializes the {@link PrivilegeHandler} from the configuration file
	 * 
	 * @param privilegeContainerXmlFile
	 *            a {@link File} reference to the XML file containing the configuration for Privilege
	 * 
	 * @return the {@link PrivilegeHandler} instance loaded from the configuration file
	 */
	public static PrivilegeHandler initializeFromXml(File privilegeContainerXmlFile) {

		// make sure file exists
		if (!privilegeContainerXmlFile.exists()) {
			throw new PrivilegeException("Privilige file does not exist at path "
					+ privilegeContainerXmlFile.getAbsolutePath());
		}

		// parse container xml file to XML document
		Element containerRootElement = XmlHelper.parseDocument(privilegeContainerXmlFile).getRootElement();

		// instantiate encryption handler
		Element encryptionHandlerElement = containerRootElement.element(XmlConstants.XML_HANDLER_ENCRYPTION);
		String encryptionHandlerClassName = encryptionHandlerElement.attributeValue(XmlConstants.XML_ATTR_CLASS);
		EncryptionHandler encryptionHandler = ClassHelper.instantiateClass(encryptionHandlerClassName);

		// instantiate persistence handler
		Element persistenceHandlerElement = containerRootElement.element(XmlConstants.XML_HANDLER_PERSISTENCE);
		String persistenceHandlerClassName = persistenceHandlerElement.attributeValue(XmlConstants.XML_ATTR_CLASS);
		PersistenceHandler persistenceHandler = ClassHelper.instantiateClass(persistenceHandlerClassName);

		// instantiate privilege handler
		Element privilegeHandlerElement = containerRootElement.element(XmlConstants.XML_HANDLER_PRIVILEGE);
		String privilegeHandlerClassName = privilegeHandlerElement.attributeValue(XmlConstants.XML_ATTR_CLASS);
		PrivilegeHandler privilegeHandler = ClassHelper.instantiateClass(privilegeHandlerClassName);

		try {

			// get parameters
			Element parameterElement = encryptionHandlerElement.element(XmlConstants.XML_PARAMETERS);
			Map<String, String> parameterMap = convertToParameterMap(parameterElement);

			// initialize encryption handler
			encryptionHandler.initialize(parameterMap);

		} catch (Exception e) {
			logger.error(e, e);
			throw new PrivilegeException("EncryptionHandler " + encryptionHandlerClassName
					+ " could not be initialized");
		}

		try {

			// get parameters
			Element parameterElement = persistenceHandlerElement.element(XmlConstants.XML_PARAMETERS);
			Map<String, String> parameterMap = convertToParameterMap(parameterElement);

			// initialize persistence handler
			persistenceHandler.initialize(parameterMap);

		} catch (Exception e) {
			logger.error(e, e);
			throw new PrivilegeException("PersistenceHandler " + persistenceHandlerElement
					+ " could not be initialized");
		}

		try {

			// get parameters
			Element parameterElement = privilegeHandlerElement.element(XmlConstants.XML_PARAMETERS);
			Map<String, String> parameterMap = convertToParameterMap(parameterElement);

			// initialize privilege handler
			privilegeHandler.initialize(parameterMap, encryptionHandler, persistenceHandler);

		} catch (Exception e) {
			logger.error(e, e);
			throw new PrivilegeException("PrivilegeHandler " + privilegeHandlerClassName + " could not be initialized");
		}

		return privilegeHandler;
	}

	/**
	 * Converts an {@link XmlConstants#XML_PARAMETERS} element containing {@link XmlConstants#XML_PARAMETER} elements to
	 * a {@link Map} of String key/value pairs
	 * 
	 * @param element
	 *            the XML {@link Element} with name {@link XmlConstants#XML_PARAMETERS} containing
	 *            {@link XmlConstants#XML_PARAMETER} elements
	 * 
	 * @return the {@link Map} of the parameter name/value combinations from the given {@link Element}
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> convertToParameterMap(Element element) {

		Map<String, String> parameterMap = new HashMap<String, String>();

		// if element is null then there are no parameters, so return empty map
		if (element == null)
			return parameterMap;

		List<Element> elements = element.elements(XmlConstants.XML_PARAMETER);

		// if elements is null or empty then there are no parameters, so return empty map
		if (elements == null || elements.isEmpty())
			return parameterMap;

		for (Element parameter : elements) {
			String name = parameter.attributeValue(XmlConstants.XML_ATTR_NAME);
			String value = parameter.attributeValue(XmlConstants.XML_ATTR_VALUE);
			parameterMap.put(name, value);
		}

		return parameterMap;
	}
}