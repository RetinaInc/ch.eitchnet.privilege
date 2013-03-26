/*
 * Copyright (c) 2012
 *
 * This file is part of ???????????????
 *
 * ?????????????? is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Privilege is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ????????????????.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package ch.eitchnet.privilege.helper;

import ch.eitchnet.privilege.model.Certificate;

/**
 * This {@link ThreadLocal} holds a reference to the {@link Certificate} which allows any code segment to perform
 * further authorization before executing
 * 
 * @author Robert von Burg <eitch@eitchnet.ch>
 */
public class CertificateThreadLocal extends ThreadLocal<Certificate> {

	private static final CertificateThreadLocal instance;
	static {
		instance = new CertificateThreadLocal();
	}

	public static CertificateThreadLocal getInstance() {
		return CertificateThreadLocal.instance;
	}

	public static Certificate getCertificate() {
		return CertificateThreadLocal.instance.get();
	}

	public static void setCertificate(Certificate certificate) {
		CertificateThreadLocal.instance.set(certificate);
	}
}
