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
package org.structr.websocket.command;

import java.text.Collator;
import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.GraphObject;
import org.structr.core.Result;
import org.structr.core.entity.AbstractNode;
import org.structr.websocket.message.WebSocketMessage;
import org.structr.common.PagingHelper;
import org.structr.websocket.StructrWebSocket;
import org.structr.websocket.message.MessageBuilder;

//~--- JDK imports ------------------------------------------------------------

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.structr.core.app.StructrApp;
import org.structr.web.entity.dom.DOMNode;
import org.structr.web.entity.dom.Page;
import org.structr.web.entity.dom.ShadowDocument;
import org.structr.web.entity.dom.relationship.DOMChildren;

//~--- classes ----------------------------------------------------------------

/**
 * Websocket command to retrieve nodes which are in use on more than
 * one page.
 * 
 *
 */
public class ListComponentsCommand extends AbstractCommand {
	
	private static final Logger logger = LoggerFactory.getLogger(ListComponentsCommand.class.getName());

	static {

		StructrWebSocket.addCommand(ListComponentsCommand.class);

	}

	@Override
	public void processMessage(final WebSocketMessage webSocketData) {

		final SecurityContext securityContext  = getWebSocket().getSecurityContext();

		final int pageSize       = webSocketData.getPageSize();
		final int page           = webSocketData.getPage();

		try {

			// get shadow document
			Result result                      = StructrApp.getInstance(securityContext).nodeQuery(ShadowDocument.class).includeDeletedAndHidden().getResult();
			List<AbstractNode> filteredResults = new LinkedList();
			List<AbstractNode> resultList      = result.getResults();

			if (result.isEmpty()) {
				
				logger.warn("No shadow document found, creating one.");
				resultList.add(CreateComponentCommand.getOrCreateHiddenDocument());
			}
			
			ShadowDocument doc = (ShadowDocument) resultList.get(0);
			
			resultList.addAll(doc.getProperty(Page.elements));
			
			// Filter list and return only top level nodes
			for (GraphObject obj : resultList) {

				if (obj instanceof DOMNode) {

					DOMNode node = (DOMNode) obj;

					if (!doc.equals(node) && !node.hasIncomingRelationships(DOMChildren.class)) {
						
						filteredResults.add(node);

					}

				}

			}

			// Sort the components by name
			Collections.sort(filteredResults, new Comparator<AbstractNode>() {
				@Override
				public int compare(AbstractNode node1, AbstractNode node2) {
					final String nameNode1 = node1.getProperty(AbstractNode.name);
					final String nameNode2 = node2.getProperty(AbstractNode.name);

					if (nameNode1 != null && nameNode2 != null) {

						return nameNode1.compareTo(nameNode2);

					} else if (nameNode1 == null && nameNode2 == null) {

						return 0;

					} else if (nameNode1 == null) {

						return -1;

					} else {

						return 1;

					}

				}
			});

			// save raw result count
			int resultCountBeforePaging = filteredResults.size();
			
			// set full result list
			webSocketData.setResult(PagingHelper.subList(filteredResults, pageSize, page, null));
			webSocketData.setRawResultCount(resultCountBeforePaging);

			// send only over local connection
			getWebSocket().send(webSocketData, true);
			
		} catch (FrameworkException fex) {

			logger.warn("Exception occured", fex);
			getWebSocket().send(MessageBuilder.status().code(fex.getStatus()).message(fex.getMessage()).build(), true);

		}

	}

	//~--- get methods ----------------------------------------------------

	@Override
	public String getCommand() {

		return "LIST_COMPONENTS";

	}

}
