package org.structr.files.cmis;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.BulkUpdateObjectIdAndChangeToken;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService;
import org.apache.chemistry.opencmis.commons.server.ObjectInfo;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.commons.io.FilenameUtils;
import org.structr.cmis.CMISInfo;
import org.structr.common.SecurityContext;
import org.structr.files.cmis.repository.CMISRootFolder;

/**
 * The Structr CMIS service.
 *
 * @author Christian Morgner
 */


public class StructrCmisService extends AbstractCmisService {

	private static final Logger logger = Logger.getLogger(StructrCmisService.class.getName());

	private CMISPolicyService policyService             = null;
	private CMISAclService aclService                   = null;
	private CMISRelationshipService relationshipService = null;
	private CMISMultiFilingService multiFilingService   = null;
	private CMISDiscoveryService discoveryService       = null;
	private CMISVersioningService versioningService     = null;
	private CMISObjectService objectService             = null;
	private CMISNavigationService navigationService     = null;
	private CMISRepositoryService repositoryService     = null;
	private SecurityContext securityContext             = null;

	public StructrCmisService(final SecurityContext securityContext) {

		repositoryService      = new CMISRepositoryService(securityContext);
		navigationService      = new CMISNavigationService(securityContext);
		objectService          = new CMISObjectService(securityContext);
		versioningService      = new CMISVersioningService(securityContext);
		discoveryService       = new CMISDiscoveryService(securityContext);
		multiFilingService     = new CMISMultiFilingService(securityContext);
		relationshipService    = new CMISRelationshipService(securityContext);
		aclService             = new CMISAclService(securityContext);
		policyService          = new CMISPolicyService(securityContext);

		this.securityContext   = securityContext;
	}

	// ----- interface CmisService -----
	@Override
	public String create(String repositoryId, Properties properties, String folderId, ContentStream contentStream, VersioningState versioningState, List<String> policies, ExtensionsData extension) {
		logger.log(Level.INFO, "{0}", repositoryId);
		return null;
	}

	@Override
	public void deleteObjectOrCancelCheckOut(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension) {

		logger.log(Level.INFO, "{0}", repositoryId);
	}

	@Override
	public Acl applyAcl(String repositoryId, String objectId, Acl aces, AclPropagation aclPropagation) {

		logger.log(Level.INFO, "{0}", repositoryId);
		return null;
	}

	@Override
	public ObjectInfo getObjectInfo(final String repositoryId, final String objectId) {

		ObjectData obj = null;

		if (CMISInfo.ROOT_FOLDER_ID.equals(objectId)) {

			obj = new CMISRootFolder(false);

		} else {

			obj = getObject(repositoryId, objectId, null, Boolean.FALSE, IncludeRelationships.NONE, null, Boolean.FALSE, Boolean.FALSE, null);
		}

		return getObjectInfoIntern(repositoryId, obj);
	}

	@Override
	public void close() {
	}

	// ----- interface RepositoryService -----
	@Override
	public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
		return repositoryService.getRepositoryInfos(extension);
	}

	@Override
	public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
		return repositoryService.getRepositoryInfo(repositoryId, extension);
	}

	@Override
	public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		return repositoryService.getTypeChildren(repositoryId, typeId, includePropertyDefinitions, maxItems, skipCount, extension);
	}

	@Override
	public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth, Boolean includePropertyDefinitions, ExtensionsData extension) {
		return repositoryService.getTypeDescendants(repositoryId, typeId, depth, includePropertyDefinitions, extension);
	}

	@Override
	public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
		return repositoryService.getTypeDefinition(repositoryId, typeId, extension);
	}

	@Override
	public TypeDefinition createType(String repositoryId, TypeDefinition type, ExtensionsData extension) {
		return repositoryService.createType(repositoryId, type, extension);
	}

	@Override
	public TypeDefinition updateType(String repositoryId, TypeDefinition type, ExtensionsData extension) {
		return repositoryService.updateType(repositoryId, type, extension);
	}

	@Override
	public void deleteType(String repositoryId, String typeId, ExtensionsData extension) {
		repositoryService.deleteType(repositoryId, typeId, extension);
	}

	// ----- interface NaviagationService -----
	@Override
	public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		return navigationService.getChildren(repositoryId, folderId, filter, orderBy, includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, maxItems, skipCount, extension);
	}

	@Override
	public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
		return navigationService.getDescendants(repositoryId, folderId, depth, filter, includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, extension);
	}

	@Override
	public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId, BigInteger depth, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
		return navigationService.getFolderTree(repositoryId, folderId, depth, filter, includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, extension);
	}

	@Override
	public List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, Boolean includeRelativePathSegment, ExtensionsData extension) {
		return navigationService.getObjectParents(repositoryId, objectId, filter, includeAllowableActions, includeRelationships, renditionFilter, includeRelativePathSegment, extension);
	}

	@Override
	public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {

		final ObjectData parent = navigationService.getFolderParent(repositoryId, folderId, filter, extension);
		if (parent != null) {

			return parent;
		}

		return new CMISRootFolder(false);
	}

	@Override
	public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		return navigationService.getCheckedOutDocs(repositoryId, folderId, filter, orderBy, includeAllowableActions, includeRelationships, renditionFilter, maxItems, skipCount, extension);
	}

	// ----- interface ObjectService -----
	@Override
	public String createDocument(String repositoryId, Properties properties, String folderId, ContentStream contentStream, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		return objectService.createDocument(repositoryId, properties, folderId, contentStream, versioningState, policies, addAces, removeAces, extension);
	}

	@Override
	public String createDocumentFromSource(String repositoryId, String sourceId, Properties properties, String folderId, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		return objectService.createDocumentFromSource(repositoryId, sourceId, properties, folderId, versioningState, policies, addAces, removeAces, extension);
	}

	@Override
	public String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		return objectService.createFolder(repositoryId, properties, folderId, policies, addAces, removeAces, extension);
	}

	@Override
	public String createRelationship(String repositoryId, Properties properties, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		return objectService.createRelationship(repositoryId, properties, policies, addAces, removeAces, extension);
	}

	@Override
	public String createPolicy(String repositoryId, Properties properties, String folderId, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		return objectService.createPolicy(repositoryId, properties, folderId, policies, addAces, removeAces, extension);
	}

	@Override
	public String createItem(String repositoryId, Properties properties, String folderId, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		return objectService.createItem(repositoryId, properties, folderId, policies, addAces, removeAces, extension);
	}

	@Override
	public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {

		if (CMISInfo.ROOT_FOLDER_ID.equals(objectId)) {
			return new CMISRootFolder(true).getAllowableActions();
		}

		return objectService.getAllowableActions(repositoryId, objectId, extension);
	}

	@Override
	public ObjectData getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {

		if (CMISInfo.ROOT_FOLDER_ID.equals(objectId)) {
			return new CMISRootFolder(includeAllowableActions);
		}

		return objectService.getObject(repositoryId, objectId, filter, includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds, includeAcl, extension);
	}

	@Override
	public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {

		if (CMISInfo.ROOT_FOLDER_ID.equals(objectId)) {
			return new CMISRootFolder(false).getProperties();
		}

		return objectService.getProperties(repositoryId, objectId, filter, extension);
	}

	@Override
	public List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {

		if (CMISInfo.ROOT_FOLDER_ID.equals(objectId)) {
			return new CMISRootFolder(false).getRenditions();
		}

		return objectService.getRenditions(repositoryId, objectId, renditionFilter, maxItems, skipCount, extension);
	}

	@Override
	public ObjectData getObjectByPath(final String repositoryId, final String path, final String filter, final Boolean includeAllowableActions, final IncludeRelationships includeRelationships, final String renditionFilter, final Boolean includePolicyIds, final Boolean includeAcl, final ExtensionsData extension) {

		final String cleanPath = FilenameUtils.normalize(path);

		if (CMISInfo.ROOT_FOLDER_ID.equals(cleanPath) || path == null) {
			return new CMISRootFolder(includeAllowableActions);
		}

		return objectService.getObjectByPath(repositoryId, cleanPath, filter, includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds, includeAcl, extension);
	}

	@Override
	public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset, BigInteger length, ExtensionsData extension) {
		return objectService.getContentStream(repositoryId, objectId, streamId, offset, length, extension);
	}

	@Override
	public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken, Properties properties, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		objectService.updateProperties(repositoryId, objectId, changeToken, properties, extension);
	}

	@Override
	public List<BulkUpdateObjectIdAndChangeToken> bulkUpdateProperties(String repositoryId, List<BulkUpdateObjectIdAndChangeToken> objectIdsAndChangeTokens, Properties properties, List<String> addSecondaryTypeIds, List<String> removeSecondaryTypeIds, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		return objectService.bulkUpdateProperties(repositoryId, objectIdsAndChangeTokens, properties, addSecondaryTypeIds, removeSecondaryTypeIds, extension);
	}

	@Override
	public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		objectService.moveObject(repositoryId, objectId, targetFolderId, sourceFolderId, extension);
	}

	@Override
	public void deleteObject(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		objectService.deleteObject(repositoryId, objectId, allVersions, extension);
	}

	@Override
	public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions, UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		return objectService.deleteTree(repositoryId, folderId, allVersions, unfileObjects, continueOnFailure, extension);
	}

	@Override
	public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag, Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		objectService.setContentStream(repositoryId, objectId, overwriteFlag, changeToken, contentStream, extension);
	}

	@Override
	public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		objectService.deleteContentStream(repositoryId, objectId, changeToken, extension);
	}

	@Override
	public void appendContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken, ContentStream contentStream, boolean isLastChunk, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		objectService.appendContentStream(repositoryId, objectId, changeToken, contentStream, isLastChunk, extension);
	}

	// ----- interface VersioningService -----
	@Override
	public void checkOut(String repositoryId, Holder<String> objectId, ExtensionsData extension, Holder<Boolean> contentCopied) {
		logger.log(Level.INFO, "...");
		versioningService.checkOut(repositoryId, objectId, extension, contentCopied);
	}

	@Override
	public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		versioningService.cancelCheckOut(repositoryId, objectId, extension);
	}

	@Override
	public void checkIn(String repositoryId, Holder<String> objectId, Boolean major, Properties properties, ContentStream contentStream, String checkinComment, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		versioningService.checkIn(repositoryId, objectId, major, properties, contentStream, checkinComment, policies, addAces, removeAces, extension);
	}

	@Override
	public ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId, Boolean major, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		return versioningService.getObjectOfLatestVersion(repositoryId, objectId, versionSeriesId, major, filter, includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds, includeAcl, extension);
	}

	@Override
	public Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId, Boolean major, String filter, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		return versioningService.getPropertiesOfLatestVersion(repositoryId, objectId, versionSeriesId, major, filter, extension);
	}

	@Override
	public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter, Boolean includeAllowableActions, ExtensionsData extension) {

		if (objectId != null && objectId.equals(CMISInfo.ROOT_FOLDER_ID)) {
			return Arrays.asList(new ObjectData[] { new CMISRootFolder(includeAllowableActions) });
		}

		logger.log(Level.INFO, "...");
		return versioningService.getAllVersions(repositoryId, objectId, versionSeriesId, filter, includeAllowableActions, extension);
	}

	// ----- interface DiscoveryService -----
	@Override
	public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		return discoveryService.query(repositoryId, statement, searchAllVersions, includeAllowableActions, includeRelationships, renditionFilter, maxItems, skipCount, extension);
	}

	@Override
	public ObjectList getContentChanges(String repositoryId, Holder<String> changeLogToken, Boolean includeProperties, String filter, Boolean includePolicyIds, Boolean includeAcl, BigInteger maxItems, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		return discoveryService.getContentChanges(repositoryId, changeLogToken, includeProperties, filter, includePolicyIds, includeAcl, maxItems, extension);
	}

	// ----- interface MultiFilingService -----
	@Override
	public void addObjectToFolder(String repositoryId, String objectId, String folderId, Boolean allVersions, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		multiFilingService.addObjectToFolder(repositoryId, objectId, folderId, allVersions, extension);
	}

	@Override
	public void removeObjectFromFolder(String repositoryId, String objectId, String folderId, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		multiFilingService.removeObjectFromFolder(repositoryId, objectId, folderId, extension);
	}

	// ----- interface RelationshipService -----
	@Override
	public ObjectList getObjectRelationships(String repositoryId, String objectId, Boolean includeSubRelationshipTypes, RelationshipDirection relationshipDirection, String typeId, String filter, Boolean includeAllowableActions, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		return relationshipService.getObjectRelationships(repositoryId, objectId, includeSubRelationshipTypes, relationshipDirection, typeId, filter, includeAllowableActions, maxItems, skipCount, extension);
	}

	// ----- interface AclService -----
	@Override
	public Acl getAcl(String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		return aclService.getAcl(repositoryId, objectId, onlyBasicPermissions, extension);
	}

	@Override
	public Acl applyAcl(String repositoryId, String objectId, Acl addAces, Acl removeAces, AclPropagation aclPropagation, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		return aclService.applyAcl(repositoryId, objectId, addAces, removeAces, aclPropagation, extension);
	}

	// ----- interface PolicyService -----
	@Override
	public void applyPolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		policyService.applyPolicy(repositoryId, policyId, objectId, extension);
	}

	@Override
	public void removePolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
		logger.log(Level.INFO, "...");
		policyService.removePolicy(repositoryId, policyId, objectId, extension);
	}

	@Override
	public List<ObjectData> getAppliedPolicies(String repositoryId, String objectId, String filter, ExtensionsData extension) {
		return policyService.getAppliedPolicies(repositoryId, objectId, filter, extension);
	}
}
