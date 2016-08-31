/**
 * Copyright (C) 2010-2016 Structr GmbH
 *
 * This file is part of Structr <http://structr.org>.
 *
 * Structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.files.ftp;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.structr.common.AccessMode;
import org.structr.common.PathHelper;
import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.app.StructrApp;
import org.structr.core.entity.AbstractUser;
import org.structr.core.graph.Tx;
import org.structr.rest.auth.AuthHelper;
import org.structr.web.common.FileHelper;
import org.structr.web.entity.AbstractFile;
import org.structr.web.entity.FileBase;
import org.structr.web.entity.Folder;
import org.structr.web.entity.dom.Page;

/**
 *
 *
 */
public class StructrFileSystemView implements FileSystemView {

	private static final Logger logger = Logger.getLogger(StructrFileSystemView.class.getName());
	private StructrFtpUser user = null;
	private SecurityContext securityContext = null;

	private String workingDir = "/";

	public StructrFileSystemView(final User user) {
		
		try (Tx tx = StructrApp.getInstance().tx()) {
		
			org.structr.web.entity.User structrUser = (org.structr.web.entity.User) AuthHelper.getPrincipalForCredential(AbstractUser.name, user.getName());
			
			securityContext = SecurityContext.getInstance(structrUser, AccessMode.Backend);
			
			this.user = new StructrFtpUser(securityContext, structrUser);
			
			tx.success();
		
		} catch (FrameworkException fex) {
			logger.log(Level.SEVERE, "Error while initializing file system view", fex);
		}
	}

	@Override
	public FtpFile getHomeDirectory() throws FtpException {

		try (Tx tx = StructrApp.getInstance(securityContext).tx()) {

			org.structr.web.entity.User structrUser = (org.structr.web.entity.User) AuthHelper.getPrincipalForCredential(AbstractUser.name, user.getName());

			final Folder homeDir = structrUser.getProperty(org.structr.web.entity.User.homeDirectory);

			tx.success();

			return new StructrFtpFolder(securityContext, homeDir);

		} catch (FrameworkException fex) {
			logger.log(Level.SEVERE, "Error while getting home directory", fex);
		}

		return null;
	}

	@Override
	public FtpFile getWorkingDirectory() throws FtpException {

		try (Tx tx = StructrApp.getInstance(securityContext).tx()) {

			AbstractFile structrWorkingDir = FileHelper.getFileByAbsolutePath(SecurityContext.getSuperUserInstance(), workingDir);
			
			tx.success();

			if (structrWorkingDir == null || structrWorkingDir instanceof FileBase) {
				return new StructrFtpFolder(securityContext, null);
			}

			return new StructrFtpFolder(securityContext, (Folder) structrWorkingDir);
			
		} catch (FrameworkException fex) {
			logger.log(Level.SEVERE, "Error in changeWorkingDirectory()", fex);
		}
		
		return null;
	}

	@Override
	public boolean changeWorkingDirectory(String requestedPath) throws FtpException {

		try (Tx tx = StructrApp.getInstance(securityContext).tx()) {
			
			final StructrFtpFolder newWorkingDirectory = (StructrFtpFolder) getFile(requestedPath);

			workingDir = newWorkingDirectory.getAbsolutePath();

			tx.success();

			return true;

		} catch (FrameworkException fex) {
			logger.log(Level.SEVERE, "Error in changeWorkingDirectory()", fex);
		}

		return false;
	}

	@Override
	public FtpFile getFile(String requestedPath) throws FtpException {

		logger.log(Level.INFO, "Requested path: {0}", requestedPath);

		try (Tx tx = StructrApp.getInstance(securityContext).tx()) {

			if (StringUtils.isBlank(requestedPath) || "/".equals(requestedPath)) {
				return getHomeDirectory();
			}

			StructrFtpFolder cur = (StructrFtpFolder) getWorkingDirectory();

			if (".".equals(requestedPath) || "./".equals(requestedPath)) {
				return cur;
			}

			if ("..".equals(requestedPath) || "../".equals(requestedPath)) {
				return new StructrFtpFolder(securityContext, cur.getStructrFile().getProperty(AbstractFile.parent));
			}

			// If relative path requested, prepend base path
			if (!requestedPath.startsWith("/")) {

				String basePath = cur.getAbsolutePath();

				logger.log(Level.INFO, "Base path: {0}", basePath);

				while (requestedPath.startsWith("..")) {
					requestedPath = StringUtils.stripStart(StringUtils.stripStart(requestedPath, ".."), "/");
					basePath = StringUtils.substringBeforeLast(basePath, "/");
				}

				requestedPath = StringUtils.stripEnd(basePath.equals("/") ? "/".concat(requestedPath) : basePath.concat("/").concat(requestedPath), "/");

				logger.log(Level.INFO, "Base path: {0}, requestedPath: {1}", new Object[]{basePath, requestedPath});

			}

			AbstractFile file = FileHelper.getFileByAbsolutePath(SecurityContext.getSuperUserInstance(), requestedPath);


			if (file != null) {

				if (file instanceof Folder) {
					tx.success();
					return new StructrFtpFolder(securityContext, (Folder) file);
				} else {
					tx.success();
					return new StructrFtpFile(securityContext, (FileBase) file);
				}
			}

			// Look up a page by its name
			Page page = StructrApp.getInstance(securityContext).nodeQuery(Page.class).andName(PathHelper.getName(requestedPath)).getFirst();
			if (page != null) {

				tx.success();

				return new FtpFilePageWrapper(page);
			}

			logger.log(Level.WARNING, "No existing file found: {0}", requestedPath);

			tx.success();
			return new FileOrFolder(requestedPath, user);

		} catch (FrameworkException fex) {
			logger.log(Level.SEVERE, "Error in getFile()", fex);
		}

		return null;

	}

	@Override
	public boolean isRandomAccessible() throws FtpException {
		logger.log(Level.INFO, "isRandomAccessible(), returning true");
		return true;
	}

	@Override
	public void dispose() {
		logger.log(Level.INFO, "dispose() does nothing");
	}

}
