package com.marklogic.appdeployer.command.security;

import com.marklogic.appdeployer.command.AbstractResourceCommand;
import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.appdeployer.command.SortOrderConstants;
import com.marklogic.mgmt.SaveReceipt;
import com.marklogic.mgmt.api.API;
import com.marklogic.mgmt.api.security.Role;
import com.marklogic.mgmt.mapper.DefaultResourceMapper;
import com.marklogic.mgmt.mapper.ResourceMapper;
import com.marklogic.mgmt.resource.ResourceManager;
import com.marklogic.mgmt.resource.security.RoleManager;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class DeployRolesCommand extends AbstractResourceCommand {

	// Used internally
	private boolean removeRolesAndPermissionsDuringDeployment = false;
	private boolean secondPass = false;
	private ResourceMapper resourceMapper;
	private Set<String> roleNamesThatDontNeedToBeRedeployed;

	public DeployRolesCommand() {
		setExecuteSortOrder(SortOrderConstants.DEPLOY_ROLES);
		setUndoSortOrder(SortOrderConstants.DELETE_ROLES);
	}

	/**
	 * The set of roles is processed twice. The first time, the roles are saved without any default permissions or references to other roles.
	 * This is to avoid issues where the roles refer to each other or to themselves (via default permissions). The second time, the roles are
	 * saved with permissions and references to other roles, which is guaranteed to work now that the roles have all been created.
	 *
	 * @param context
	 */
	@Override
	public void execute(CommandContext context) {
		removeRolesAndPermissionsDuringDeployment = true;
		secondPass = false;
		if (logger.isInfoEnabled()) {
			logger.info("Deploying roles minus their default permissions and references to roles");
		}
		roleNamesThatDontNeedToBeRedeployed = new HashSet<>();
		super.execute(context);
		if (logger.isInfoEnabled()) {
			logger.info("Redeploying roles that have default permissions and/or references to roles");
		}
		removeRolesAndPermissionsDuringDeployment = false;
		secondPass = true;
		super.execute(context);
	}

	/**
	 * Any file deployed in the first pass, must be a candidate for deployment in the second phase.
	 * We need to ignore the hashes for these files during the second phase.
	 */
	@Override
	protected void afterResourceSaved(ResourceManager mgr, CommandContext context, File resourceFile, SaveReceipt receipt) {
		if (!secondPass) {
			ignoreHashForFilename(resourceFile.getAbsolutePath());
		}
		super.afterResourceSaved(mgr, context, resourceFile, receipt);
	}

	/**
	 * If this is the first time roles are being deployed by this command - indicated by the removeRolesAndPermissionsDuringDeployment
	 * class variable - then each payload is modified so that default permissions and role references are not included,
	 * thus ensuring that the role can be created successfully.
	 *
	 * If this is the second time that roles are being deployed by this command, then the entire payload is sent. However,
	 * if the role doesn't have any default permissions or role references, it will not be deployed a second time, as
	 * there was nothing missing from the first deployment of the role.
	 *
	 * @param mgr
	 * @param context
	 * @param f
	 * @param payload
	 * @return
	 */
	@Override
	protected String adjustPayloadBeforeSavingResource(ResourceManager mgr, CommandContext context, File f, String payload) {
		payload = super.adjustPayloadBeforeSavingResource(mgr, context, f, payload);

		if (resourceMapper == null) {
			API api = new API(context.getManageClient(), context.getAdminManager());
			resourceMapper = new DefaultResourceMapper(api);
		}

		Role role = resourceMapper.readResource(payload, Role.class);

		// Is this the first time the roles are being deployed?
		if (removeRolesAndPermissionsDuringDeployment) {
			if (role.hasPermissionsOrRoles()) {
				role.clearPermissionsAndRoles();
				return role.getJson();
			} else {
				roleNamesThatDontNeedToBeRedeployed.add(role.getRoleName());
				return payload;
			}
		}
		// Else it's the second time roles are being deployed, but no need to deploy a role if it doesn't have any default permissions or role references
		else if (roleNamesThatDontNeedToBeRedeployed.contains(role.getRoleName())) {
			if (logger.isInfoEnabled()) {
				logger.info("Not redeploying role " + role.getRoleName() + ", as it does not have any default permissions or references to other roles");
			}
			return null;
		}
		// Else log a message to indicate that the role is being redeployed
		else if (logger.isInfoEnabled()) {
			logger.info("Redeploying role " + role.getRoleName() + " with default permissions and references to other roles included");
		}

		return payload;
	}

	protected File[] getResourceDirs(CommandContext context) {
		return findResourceDirs(context, configDir -> configDir.getRolesDir());
	}

	@Override
	protected ResourceManager getResourceManager(CommandContext context) {
		return new RoleManager(context.getManageClient());
	}
}

