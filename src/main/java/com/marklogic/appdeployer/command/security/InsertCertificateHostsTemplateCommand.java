package com.marklogic.appdeployer.command.security;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.marklogic.appdeployer.ConfigDir;
import com.marklogic.appdeployer.command.AbstractResourceCommand;
import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.appdeployer.command.SortOrderConstants;
import com.marklogic.mgmt.PayloadParser;
import com.marklogic.mgmt.resource.ResourceManager;
import com.marklogic.mgmt.resource.security.CertificateTemplateManager;

import org.springframework.http.ResponseEntity;

/**
 * Used to insert (externally signed) host certificates for a template... by calling POST manage/v2/certificate-templates/[id-or-name] service. 
 *    Place PEM formatted files under:  ml-config/security/certificate-templates/hostcertificates/YOUR-TEMPLATE
 * 
 * PEM Files MUST end with .crt AND .key
 * 
 * This command only inserts certificate hosts for templates that are defined in certificate-templates directory
 */
public class InsertCertificateHostsTemplateCommand extends AbstractResourceCommand {
	/**
	 * The public certificate file to process
	 */
	private File publicCertFile;
	/**
	 * The private certificate key to match the above public cert
	 */
	private File privateCertFile;

	/**
	 * The name of the template that is being processed.
	 */
	private String superTemplateName;


	public InsertCertificateHostsTemplateCommand() {
		setExecuteSortOrder(SortOrderConstants.INSERT_HOST_CERTIFICATES);
	}

	public InsertCertificateHostsTemplateCommand(String superTemplateName) {
		this();
		this.superTemplateName = superTemplateName;
	}

	@Override
	public String toString() {
		if (publicCertFile != null) {
			return publicCertFile.getAbsolutePath();
		}
		return this.superTemplateName;
	}

	/**
	 * This execute method supports two primary modes:
	 *  - Build Commands to insert the hosts
	 * 	- IF a templateName was specified when constructing the Command, will process only that template
	 *  - Process a single host certificate/key pair (publicCertFile)
	 */
	@Override
	public void execute(CommandContext context) {
		if (this.publicCertFile != null) {
			// implies private key exists
			insertHostCertificate(context);
		} else {
			// PULL template names to search the hostcertificates directory
			for (File resourceDir : getResourceDirs(context)) {
				List<String> subTemplateNames = getTemplateNamesFromResourceDir(context, resourceDir);

				for (String tn: subTemplateNames) {
					// Check to only process template ID specified (if Command was constructed using Constructor)
					if (superTemplateName == null || tn.equalsIgnoreCase(superTemplateName)) {
						processHostCertificatesDir(context, tn);
					}
				}
			}
		}
	}

	protected List<String> getTemplateNamesFromResourceDir(CommandContext context, File resourceDir) {
		List<String> subTemplateNames = new ArrayList<String>();
		if (resourceDir.exists()) {
			if (logger.isInfoEnabled()) {
				logger.info("Using Template Names from directory: " + resourceDir.getAbsolutePath());
			}
			for (File f : listFilesInDirectory(resourceDir, context)) {
				PayloadParser pp = new PayloadParser();
				subTemplateNames.add(pp.getPayloadFieldValue(copyFileToString(f), "template-name"));
			}
		} else {
			logResourceDirectoryNotFound(resourceDir);
		}
		return subTemplateNames;
	}

	public void insertHostCertificate(CommandContext context) {
		CertificateTemplateManager certMgr = (CertificateTemplateManager) getResourceManager(context);
		// Only insert host if a certificate does not exist
		if (!certMgr.certificateExists(this.getSuperTemplateName())) {
			String pubCertString = copyFileToString(this.publicCertFile);
			String privateKeyString = copyFileToString(this.privateCertFile);

			ResponseEntity<String> response = certMgr.insertHostCertificate(this.getSuperTemplateName(), pubCertString, privateKeyString);
			if (logger.isDebugEnabled()) {
				logger.debug("Response: " + response);
			}
		}
	}

    /**
     * Processes all CRT files in the template directory
     * NOTE: hostnames MUST end in crt and key.   Recommend:  myhost.marklogic.com.pem and myhost.marklogic.com.key
	  * 
     * @param context
     * @param superTemplateName Name of the template the host certificates are related to
     */
    protected void processHostCertificatesDir(CommandContext context, String superTemplateName) {
		for (ConfigDir configDir : context.getAppConfig().getConfigDirs()) {
			File hostCertDir = new File(configDir.getCertificateTemplatesDir() + File.separator + "hostcertificates" + File.separator + superTemplateName);
			logger.info(format("Checking for host certificates in: %s", hostCertDir.getAbsolutePath()));

			if(hostCertDir.exists()){
				for (File f : hostCertDir.listFiles()) {
					// Files must end in CRT
					if (f.getName().endsWith("crt")) {
						String keyFileString = f.getAbsolutePath().replace(".crt", ".key");
						File pFile = new File(keyFileString);

						if (pFile.exists()) {
							logger.info("Found Public and Private key files for : " + f.getAbsolutePath());
							this.privateCertFile = f;
							this.publicCertFile = pFile;

							// Create COMMAND here
							logger.info(format("About to insert host certificate for %s found in file: %s", superTemplateName, f.getAbsolutePath()));
							InsertCertificateHostsTemplateCommand insertHostCommand = new InsertCertificateHostsTemplateCommand();
							insertHostCommand.setPublicCertFile(f);
							insertHostCommand.setPrivateCertFile(pFile);
							insertHostCommand.setSuperTemplateName(superTemplateName);
							insertHostCommand.execute(context);
							logger.info(format("Inserting host certificate %s for template %s", f.getAbsolutePath(), superTemplateName));
						}
					}
				}
			}
		}
	 }
	 
	@Override
	protected ResourceManager getResourceManager(CommandContext context) {
		return new CertificateTemplateManager(context.getManageClient());
	}

	@Override
	protected File[] getResourceDirs(CommandContext context) {
		return findResourceDirs(context, configDir -> configDir.getCertificateTemplatesDir());
	}

	public void setSuperTemplateName(String name){
		this.superTemplateName = name;
	}

	public String getSuperTemplateName() {
		return this.superTemplateName;
	}

	public void setPublicCertFile(File publicCert) {
		this.publicCertFile = publicCert;
	}
	public File getPublicCertFile() {
		return this.publicCertFile;
	}
	public void setPrivateCertFile(File privateKey) {
		this.privateCertFile = privateKey;
	}
}
