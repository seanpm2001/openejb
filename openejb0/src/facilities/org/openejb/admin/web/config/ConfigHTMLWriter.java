/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 */
package org.openejb.admin.web.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Properties;

import org.openejb.alt.config.Bean;
import org.openejb.alt.config.sys.ConnectionManager;
import org.openejb.alt.config.sys.Connector;
import org.openejb.alt.config.sys.Container;
import org.openejb.alt.config.sys.Deployments;
import org.openejb.alt.config.sys.JndiProvider;
import org.openejb.alt.config.sys.Openejb;
import org.openejb.alt.config.sys.ProxyFactory;
import org.openejb.alt.config.sys.Resource;
import org.openejb.alt.config.sys.SecurityService;
import org.openejb.alt.config.sys.TransactionService;
import org.openejb.core.EnvProps;
import org.openejb.util.HtmlUtilities;
import org.openejb.util.StringUtilities;

/**
 * @author <a href="mailto:tim_urberg@yahoo.com">Tim Urberg</a>
 */
public class ConfigHTMLWriter implements Serializable {
	public static final String CREATE = "Create";
	public static final String EDIT = "Edit";
	public static final String DELETE = "Delete";

	public static final String TYPE_CONNECTOR = "connector";
	public static final String TYPE_CONTAINER = "container";
	public static final String TYPE_DEPLOYMENTS = "deployments";
	public static final String TYPE_JNDI_PROVIDER = "jndiProvider";
	public static final String TYPE_RESOURCE = "resource";
	public static final String TYPE_CONNECTION_MANAGER = "connectionManager";
	public static final String TYPE_OPENEJB_CONTENT = "openejbContent";
	public static final String TYPE_PROXY_FACTORY = "proxyFactory";
	public static final String TYPE_SECURITY_SERVICE = "securityService";
	public static final String TYPE_TRANSACTION_SERVICE = "transactionService";

	public static final String QUERY_PARAMETER_TYPE = "type";
	public static final String QUERY_PARAMETER_METHOD = "method";

	public static final String FORM_FIELD_HANDLE_FILE = "handleFile";
	public static final String FORM_FIELD_ID = "id";
	public static final String FORM_FIELD_JAR = "jar";
	public static final String FORM_FIELD_PROVIDER = "provider";
	public static final String FORM_FIELD_CONTAINER_TYPE = "containerType";
	public static final String FORM_FIELD_INDEX = "index";
	public static final String FORM_FIELD_DEPLOYMENT_TYPE = "deploymentType";
	public static final String FORM_FIELD_DEPLOYMENT_TEXT = "deploymentText";
	public static final String FORM_FIELD_JNDI_PARAMETERS = "jndiParameters";
	public static final String FORM_FIELD_CONTENT = "content";

	public static final String FORM_FIELD_SUBMIT_OPENEJB = "submitOpenejb";
	public static final String FORM_FIELD_SUBMIT_CONNECTOR = "submitConnector";
	public static final String FORM_FIELD_SUBMIT_CONTAINER = "submitContainer";
	public static final String FORM_FIELD_SUBMIT_DEPLOYMENTS = "submitDeployments";
	public static final String FORM_FIELD_SUBMIT_JNDI_PROVIDER = "submitJndiProvider";
	public static final String FORM_FIELD_SUBMIT_RESOURCE = "submitResource";
	public static final String FORM_FIELD_SUBMIT_CONNECTION_MANAGER = "submitConnectionManager";

	public static final String DEPLOYMENT_TYPE_JAR = "jar";
	public static final String DEPLOYMENT_TYPE_DIR = "dir";

	public static void writeOpenejb(PrintWriter body, Openejb openejbConfig, String handle, String configLocation) {
		//get all the parts of the configuration
		Connector[] connectors = openejbConfig.getConnector();
		Container[] containers = openejbConfig.getContainer();
		Deployments[] deploymentsArray = openejbConfig.getDeployments();
		JndiProvider[] jndiProviders = openejbConfig.getJndiProvider();
		Resource[] resources = openejbConfig.getResource();
		ConnectionManager connectionManager = openejbConfig.getConnectionManager();
		ProxyFactory proxyFactory = openejbConfig.getProxyFactory();
		SecurityService securityService = openejbConfig.getSecurityService();
		TransactionService transactionService = openejbConfig.getTransactionService();

		//print instructions
		body.println("This page allows you to configure your system.  The configuration file being used is:");
		body.print(configLocation);
		body.println(". Please pick one of the fields below to continue.  If you need help, click on the");
		body.println("question mark (?) next to the field.<br>");

		body.println(createTableHTMLDecleration());
		body.println(createTableHTML("Connector", "Container", true));

		//create the connectors
		if (connectors != null && connectors.length > 0) {
			body.println(HtmlUtilities.createSelectFormField(TYPE_CONNECTOR, null));
			for (int i = 0; i < connectors.length; i++) {
				body.println(HtmlUtilities.createSelectOption(connectors[i].getId(), connectors[i].getId(), false));
			}
			body.println("</select>");
		} else {
			body.println("No Connectors");
		}
		body.println("</td>\n<td>");

		//create the container list
		if (containers != null && containers.length > 0) {
			body.println(HtmlUtilities.createSelectFormField(TYPE_CONTAINER, null));
			for (int i = 0; i < containers.length; i++) {
				body.println(HtmlUtilities.createSelectOption(containers[i].getId(), containers[i].getId(), false));
			}
			body.println("</select>");
		} else {
			body.println("No Containers");
		}

		body.println("</td>\n</tr>\n<tr>\n<td>");

		body.println(createCEDUrl(TYPE_CONNECTOR, CREATE));
		if (connectors != null && connectors.length > 0) {
			body.println(createCEDUrl(TYPE_CONNECTOR, EDIT));
			body.println(createCEDUrl(TYPE_CONNECTOR, DELETE));
		}

		body.println("</td>\n<td>");

		body.println(createCEDUrl(TYPE_CONTAINER, CREATE));
		if (containers != null && containers.length > 0) {
			body.println(createCEDUrl(TYPE_CONTAINER, EDIT));
			body.println(createCEDUrl(TYPE_CONTAINER, DELETE));
		}

		body.println(createTableHTML("Deployments", "JNDI Provider", false));

		//print the deployments
		if (deploymentsArray != null & deploymentsArray.length > 0) {
			body.println(HtmlUtilities.createSelectFormField(TYPE_DEPLOYMENTS, null));
			String deployment;
			for (int i = 0; i < deploymentsArray.length; i++) {
				if (deploymentsArray[i].getDir() != null) {
					deployment = deploymentsArray[i].getDir();
				} else {
					deployment = deploymentsArray[i].getJar();
				}
				body.println(HtmlUtilities.createSelectOption(deployment, deployment, false));
			}
			body.println("</select>");
		} else {
			body.println("No Deployments");
		}
		body.println("</td>\n<td>");

		//print the jndi provider list
		if (jndiProviders != null && jndiProviders.length > 0) {
			body.println(HtmlUtilities.createSelectFormField(TYPE_JNDI_PROVIDER, null));
			for (int i = 0; i < jndiProviders.length; i++) {
				body.println(HtmlUtilities.createSelectOption(jndiProviders[i].getId(), jndiProviders[i].getId(), false));
			}
		} else {
			body.println("No JNDI Providers");
		}

		body.println("</td>\n</tr>\n<tr>\n<td>");

		body.println(createCEDUrl(TYPE_DEPLOYMENTS, CREATE));
		if (deploymentsArray != null & deploymentsArray.length > 0) {
			body.println(createCEDUrl(TYPE_DEPLOYMENTS, EDIT));
			body.println(createCEDUrl(TYPE_DEPLOYMENTS, DELETE));
		}

		body.println("</td>\n<td>");

		body.println(createCEDUrl(TYPE_JNDI_PROVIDER, CREATE));
		if (jndiProviders != null && jndiProviders.length > 0) {
			body.println(createCEDUrl(TYPE_JNDI_PROVIDER, EDIT));
			body.println(createCEDUrl(TYPE_JNDI_PROVIDER, DELETE));
		}

		body.println(createTableHTML("Resource", "Connection Manager", false));

		//print the resources
		if (resources != null && resources.length > 0) {
			body.println(HtmlUtilities.createSelectFormField(TYPE_RESOURCE, null));
			for (int i = 0; i < resources.length; i++) {
				body.println(HtmlUtilities.createSelectOption(resources[i].getId(), resources[i].getId(), false));
			}
			body.println("</select>");
		} else {
			body.println("No Resources");
		}
		body.println("</td>\n<td>");

		if (connectionManager != null) {
			body.println(connectionManager.getId());
		} else {
			body.println("No Connection Manager");
		}

		body.println("</td>\n</tr>\n<tr>\n<td>");
		body.println(createCEDUrl(TYPE_RESOURCE, CREATE));
		if (resources != null && resources.length > 0) {
			body.println(createCEDUrl(TYPE_RESOURCE, EDIT));
			body.println(createCEDUrl(TYPE_RESOURCE, DELETE));
		}

		body.println("</td>\n<td>");

		if (connectionManager == null) {
			body.println(createCEDUrl(TYPE_CONNECTION_MANAGER, CREATE));
		} else {
			body.println(createCEDUrl(TYPE_CONNECTION_MANAGER, EDIT));
			body.println(createCEDUrl(TYPE_CONNECTION_MANAGER, DELETE));
		}

		body.println(createTableHTML("Proxy Factory", "Security Service", false));

		if (proxyFactory != null) {
			body.println(proxyFactory.getId());
		} else {
			body.println("No Proxy Factory");
		}

		body.println("</td>\n<td>");

		if (securityService != null) {
			body.println(securityService.getId());
		} else {
			body.println("No Security Service");
		}

		body.println("</td>\n</tr>\n<tr>\n<td>");

		if (proxyFactory == null) {
			body.println(createCEDUrl(TYPE_PROXY_FACTORY, CREATE));
		} else {
			body.println(createCEDUrl(TYPE_PROXY_FACTORY, EDIT));
			body.println(createCEDUrl(TYPE_PROXY_FACTORY, DELETE));
		}

		body.println("</td>\n<td>");
		if (securityService == null) {
			body.println(createCEDUrl(TYPE_SECURITY_SERVICE, CREATE));
		} else {
			body.println(createCEDUrl(TYPE_SECURITY_SERVICE, EDIT));
			body.println(createCEDUrl(TYPE_SECURITY_SERVICE, DELETE));
		}

		body.println(createTableHTML("Transaction Service", "&nbsp;", false));

		if (transactionService != null) {
			body.println(transactionService.getId());
		} else {
			body.println("No Transaction Service");
		}

		body.println("</td>\n<td>&nbsp;</td>\n</tr>\n<tr>\n<td>");
		if (transactionService == null) {
			body.println(createCEDUrl(TYPE_TRANSACTION_SERVICE, CREATE));
		} else {
			body.println(createCEDUrl(TYPE_TRANSACTION_SERVICE, EDIT));
			body.println(createCEDUrl(TYPE_TRANSACTION_SERVICE, DELETE));
		}

		body.println("</td><td>&nbsp;</td></tr>\n<tr>\n<td colspan=\"2\">&nbsp;</td>\n</tr>");
		body.println("<tr>\n<td colspan=\"2\">");
		body.println(HtmlUtilities.createSubmitFormButton(FORM_FIELD_SUBMIT_OPENEJB, "Write Changes"));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_HANDLE_FILE, handle));
		body.println("</td>\n</tr>\n</table>\n</form>");
	}

	public static void writeConnector(PrintWriter body, Connector connector, String handle, int index) throws IOException {
		String id = "";
		String jar = "";
		String provider = "";
		String jdbcDriver = "";
		String jdbcUrl = "";
		String username = "";
		String password = "";
		Properties contentProps = new Properties();

		if (connector != null) {
			id = StringUtilities.nullToBlankString(connector.getId());
			jar = StringUtilities.nullToBlankString(connector.getJar());
			provider = StringUtilities.nullToBlankString(connector.getProvider());

			if (connector.getContent() != null) {
				ByteArrayInputStream in =
					new ByteArrayInputStream(StringUtilities.nullToBlankString(connector.getContent()).getBytes());
				contentProps.load(in);
			}

			jdbcDriver = contentProps.getProperty(EnvProps.JDBC_DRIVER, "");
			jdbcUrl = contentProps.getProperty(EnvProps.JDBC_URL, "");
			username = contentProps.getProperty(EnvProps.USER_NAME, "");
			password = contentProps.getProperty(EnvProps.PASSWORD, "");
		}

		//print instructions
		body.println("Please enter the fields below for a connector.  If you need help, click on the question");
		body.println("mark beside the field.  The bold fields are required.<br>");

		body.println(createTableHTMLDecleration());
		body.println(printFormRow("Id", FORM_FIELD_ID, id, 30, true));
		body.println(printFormRow("Jar", FORM_FIELD_JAR, jar, 30, false));
		body.println(printFormRow("Provider", FORM_FIELD_PROVIDER, provider, 30, false));
		body.println(printFormRow("JDBC Driver", EnvProps.JDBC_DRIVER, jdbcDriver, 30, false));
		body.println(printFormRow("JDBC URL", EnvProps.JDBC_URL, jdbcUrl, 30, false));
		body.println(printFormRow("Username", EnvProps.USER_NAME, username, 30, false));
		body.println(printFormRow("Password", EnvProps.PASSWORD, password, 30, false));

		body.println("<tr>\n<td colspan=\"2\">&nbsp;</td>\n</tr>");
		body.print("<tr>\n<td colspan=\"2\">");
		body.println(HtmlUtilities.createSubmitFormButton(FORM_FIELD_SUBMIT_CONNECTOR, "Finished"));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_HANDLE_FILE, handle));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_INDEX, String.valueOf(index)));
		body.println("</td>\n</tr>\n</table>\n</form>");
	}

	public static void writeContainer(PrintWriter body, ContainerData containerData, String handle) throws IOException {
		Properties properties = new Properties();
		String[] containerTypes = { Bean.CMP_ENTITY, Bean.BMP_ENTITY, Bean.STATEFUL, Bean.STATELESS };
		String containerType = containerData.getContainerType();

		//print instructions
		body.println("Please enter the fields below for a container.  If you need help, click on the question");
		body.println("mark beside the field.  The bold fields are required.<br>");

		body.println(createTableHTMLDecleration());
		body.println("<tr>\n<td><b>Container</b> <a href=\"javascript:void(0)\">(?)</a></td>\n<td>");
		if (containerData.isEdit()) {
			body.println(containerType);
			body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_CONTAINER_TYPE, containerType));
		} else {
			body.println(
				HtmlUtilities.createSelectFormField(FORM_FIELD_CONTAINER_TYPE, "submitForm(this.form, 'Configuration')"));
			for (int i = 0; i < containerTypes.length; i++) {
				body.println(
					HtmlUtilities.createSelectOption(
						containerTypes[i],
						containerTypes[i],
						containerTypes[i].equals(containerType)));
			}
			body.println("</select>");
			if ("".equals(containerType))
				containerType = Bean.CMP_ENTITY;
		}
		body.println("</td>\n</tr>");

		body.println(printFormRow("Id", FORM_FIELD_ID, containerData.getId(), 30, true));
		body.println(printFormRow("Jar", FORM_FIELD_JAR, containerData.getJar(), 30, false));
		body.println(printFormRow("Provider", FORM_FIELD_PROVIDER, containerData.getProvider(), 30, false));

		//check for which type of container we're writing
		if (Bean.CMP_ENTITY.equals(containerType)) {
			body.println(
				printFormRow(
					"Global Database File",
					EnvProps.GLOBAL_TX_DATABASE,
					containerData.getGlobalTxDatabase(),
					35,
					true));
			body.println(
				printFormRow(
					"Local Database File",
					EnvProps.LOCAL_TX_DATABASE,
					containerData.getLocalTxDatabase(),
					35,
					true));
			body.println(printFormRow("Pool Size", EnvProps.IM_POOL_SIZE, containerData.getPoolSize(), 5, false));
		} else if (Bean.STATEFUL.equals(containerType)) {
			body.println(printFormRow("Passivator", EnvProps.IM_PASSIVATOR, containerData.getPassivator(), 35, false));
			body.println(printFormRow("Time Out", EnvProps.IM_TIME_OUT, containerData.getTimeOut(), 5, false));
			body.println(printFormRow("Pool Size", EnvProps.IM_POOL_SIZE, containerData.getPoolSize(), 5, false));
			body.println(
				printFormRow("Bulk Passivate", EnvProps.IM_PASSIVATE_SIZE, containerData.getBulkPassivate(), 5, false));
		} else if (Bean.STATELESS.equals(containerType)) {
			body.println(printFormRow("Time Out", EnvProps.IM_TIME_OUT, containerData.getTimeOut(), 5, false));
			body.println(printFormRow("Pool Size", EnvProps.IM_POOL_SIZE, containerData.getPoolSize(), 5, false));
			body.println(
				"<tr>\n<td>Strict Pooling <a href=\"javascript:popUpHelp('help/config/help.html')\">(?)</a></td>\n<td>");
			body.println(HtmlUtilities.createSelectFormField(EnvProps.IM_STRICT_POOLING, null));
			body.println(HtmlUtilities.createSelectOption("true", "true", "true".equals(containerData.getStrictPooling())));
			body.println(
				HtmlUtilities.createSelectOption("false", "false", "false".equals(containerData.getStrictPooling())));
			body.println("</select>\n</td>\n</tr>");
		}

		body.println("<tr>\n<td colspan=\"2\">&nbsp;</td>\n</tr>");
		body.print("<tr>\n<td colspan=\"2\">");
		body.println(HtmlUtilities.createSubmitFormButton(FORM_FIELD_SUBMIT_CONTAINER, "Finished"));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_HANDLE_FILE, handle));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_INDEX, String.valueOf(containerData.getIndex())));
		body.println("</table>\n</form>");
	}

	public static void writeJNDIProvider(PrintWriter body, JndiProvider jndiProvider, String handle, int index) {
		String id = "";
		String jar = "";
		String provider = "";
		String content = "";

		if (jndiProvider != null) {
			id = StringUtilities.nullToBlankString(jndiProvider.getId());
			jar = StringUtilities.nullToBlankString(jndiProvider.getJar());
			provider = StringUtilities.nullToBlankString(jndiProvider.getProvider());
			content = StringUtilities.nullToBlankString(jndiProvider.getContent());
		}

		//print instructions
		body.println("Please enter the fields below for a JNDI provider.  If you need help, click on the question");
		body.println("mark beside the field.  The bold fields are required.<br>");
		body.println(createTableHTMLDecleration());
		body.println(printFormRow("Id", FORM_FIELD_ID, id, 30, true));
		body.println(printFormRow("Jar", FORM_FIELD_JAR, jar, 30, false));
		body.println(printFormRow("Provider", FORM_FIELD_PROVIDER, provider, 30, false));
		body.println(
			"<tr>\n<td valign=\"top\"><b>JNDI Parameters</b> <a href=\"javascript:popUpHelp('help/config/help.html')\">(?)</a></td>\n<td>");
		body.println(HtmlUtilities.createTextArea(FORM_FIELD_JNDI_PARAMETERS, content, 5, 40, null, null, null));
		body.println("</td>\n</tr>\n<tr>\n<td colspan\"2\">&nbsp;</td>\n</tr>\n<tr>\n<td colspan=\"2\">");
		body.println(HtmlUtilities.createSubmitFormButton(FORM_FIELD_SUBMIT_JNDI_PROVIDER, "Finished"));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_HANDLE_FILE, handle));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_INDEX, String.valueOf(index)));
		body.println("</table>\n</form>");

	}

	public static void writeResource(PrintWriter body, Resource resource, String handle, int index) {
		String id = "";
		String jar = "";
		String provider = "";
		String jndi = "";
		String content = "";

		if (resource != null) {
			id = StringUtilities.nullToBlankString(resource.getId());
			jar = StringUtilities.nullToBlankString(resource.getJar());
			provider = StringUtilities.nullToBlankString(resource.getProvider());
			jndi = StringUtilities.nullToBlankString(resource.getJndi());
			content = StringUtilities.nullToBlankString(resource.getContent());
		}

		//print instructions
		body.println("Please enter the fields below for a resource.  If you need help, click on the question");
		body.println("mark beside the field.  The bold fields are required.<br>");
		body.println(createTableHTMLDecleration());
		body.println(printFormRow("Id", FORM_FIELD_ID, id, 30, true));
		body.println(printFormRow("Jar", FORM_FIELD_JAR, jar, 30, false));
		body.println(printFormRow("Provider", FORM_FIELD_PROVIDER, provider, 30, false));
		body.println(printFormRow("JNDI", FORM_FIELD_JNDI_PARAMETERS, jndi, 30, false));
		body.println(
			"<tr>\n<td valign=\"top\">Content <a href=\"javascript:popUpHelp('help/config/help.html')\">(?)</a></td>\n<td>");
		body.println(HtmlUtilities.createTextArea(FORM_FIELD_CONTENT, content, 5, 40, null, null, null));
		body.println("</td>\n</tr>\n<tr>\n<td colspan\"2\">&nbsp;</td>\n</tr>\n<tr>\n<td colspan=\"2\">");
		body.println(HtmlUtilities.createSubmitFormButton(FORM_FIELD_SUBMIT_RESOURCE, "Finished"));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_HANDLE_FILE, handle));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_INDEX, String.valueOf(index)));
		body.println("</table>\n</form>");

	}

	public static void writeDeployments(PrintWriter body, Deployments deployments, String handle, int index)
		throws IOException {
		String jarOrDir = null;
		boolean isDir = true;

		if (deployments != null) {
			if (deployments.getDir() != null) {
				jarOrDir = deployments.getDir();
				isDir = true;
			} else if (deployments.getJar() != null) {
				jarOrDir = deployments.getJar();
				isDir = false;
			}
		}

		jarOrDir = StringUtilities.nullToBlankString(jarOrDir);

		//print instructions
		body.println("Please select a Jar or Directory below.  This field is required.<br><br>");
		body.println(createTableHTMLDecleration());

		body.println("<tr>\n<td>");
		body.println(HtmlUtilities.createSelectFormField(FORM_FIELD_DEPLOYMENT_TYPE, null));
		body.println(HtmlUtilities.createSelectOption(DEPLOYMENT_TYPE_JAR, "Jar File", !isDir));
		body.println(HtmlUtilities.createSelectOption(DEPLOYMENT_TYPE_DIR, "Directory", isDir));
		body.println("</select>\n</td>\n<td>");
		body.println(HtmlUtilities.createTextFormField(FORM_FIELD_DEPLOYMENT_TEXT, jarOrDir, 30, 0));
		body.println("</td>\n<tr>\n<td colspan=\"2\">&nbsp;</td>\n</tr>");
		body.println("</td>\n<tr>\n<td colspan=\"2\">");
		body.println(HtmlUtilities.createSubmitFormButton(FORM_FIELD_SUBMIT_DEPLOYMENTS, "Finshed"));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_HANDLE_FILE, handle));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_INDEX, String.valueOf(index)));
		body.println("</td>\n</tr>\n</table>\n</form>");
	}

	public static void writeConnectionManager(PrintWriter body, ConnectionManager connectionManager, String handle){
		String id = "";
		String jar = "";
		String provider = "";
		String content = "";
		if(connectionManager != null) {
			id = StringUtilities.nullToBlankString(connectionManager.getId());
			jar = StringUtilities.nullToBlankString(connectionManager.getJar());
			provider = StringUtilities.nullToBlankString(connectionManager.getProvider());
			content = StringUtilities.nullToBlankString(connectionManager.getContent());
		}
		
		//print instructions
		body.println("Please enter the fields below for a connection manager.  If you need help, click on the question");
		body.println("mark beside the field.  The bold fields are required.<br>");
		body.println(createTableHTMLDecleration());
		body.println(printFormRow("Id", FORM_FIELD_ID, id, 30, true));
		body.println(printFormRow("Jar", FORM_FIELD_JAR, jar, 30, false));
		body.println(printFormRow("Provider", FORM_FIELD_PROVIDER, provider, 30, false));
		body.println(
			"<tr>\n<td valign=\"top\">Content <a href=\"javascript:popUpHelp('help/config/help.html')\">(?)</a></td>\n<td>");
		body.println(HtmlUtilities.createTextArea(FORM_FIELD_CONTENT, content, 5, 40, null, null, null));
		body.println("</td>\n</tr>\n<tr>\n<td colspan\"2\">&nbsp;</td>\n</tr>\n<tr>\n<td colspan=\"2\">");
		body.println(HtmlUtilities.createSubmitFormButton(FORM_FIELD_SUBMIT_CONNECTION_MANAGER, "Finished"));
		body.println(HtmlUtilities.createHiddenFormField(FORM_FIELD_HANDLE_FILE, handle));
		body.println("</table>\n</form>");
	}

	private static String printFormRow(String display, String name, String value, int size, boolean required) {
		StringBuffer temp = new StringBuffer(125).append("<tr>\n<td>");

		if (required) {
			temp.append("<b>").append(display).append("</b>");
		} else {
			temp.append(display);
		}

		return temp
			.append(" <a href=\"javascript:popUpHelp('help/config/help.html')\">(?)</a>")
			.append("</td>\n<td>")
			.append(HtmlUtilities.createTextFormField(name, value, size, 0))
			.append("</td>\n</tr>\n")
			.toString();
	}

	private static String createCEDUrl(String type, String method) {
		StringBuffer temp = new StringBuffer(150);

		if (DELETE.equals(method)) {
			temp
				.append("<a href=\"javascript:confirmSubmitForm(document.configForm, 'Configuration?")
				.append(QUERY_PARAMETER_TYPE)
				.append("=")
				.append(type)
				.append("&")
				.append(QUERY_PARAMETER_METHOD)
				.append("=")
				.append(method)
				.append("', 'Are you sure you want to delete this ")
				.append(type)
				.append("?\\nNote: changes will not be written until you click the Write Changes button.')\">");
		} else {
			temp
				.append("<a href=\"javascript:submitForm(document.configForm, 'Configuration?")
				.append(QUERY_PARAMETER_TYPE)
				.append("=")
				.append(type)
				.append("&")
				.append(QUERY_PARAMETER_METHOD)
				.append("=")
				.append(method)
				.append("')\">");
		}

		return temp.append(method).append("</a>").toString();
	}

	private static String createTableHTML(String label1, String label2, boolean isTop) {
		StringBuffer temp = new StringBuffer(225);

		if (!isTop) {
			temp.append("</td>\n</tr>\n");
		}

		temp.append("<tr>\n<td colspan=\"2\">&nbsp;</td>\n</tr>\n<tr>\n<td>");
		if ("&nbsp;".equals(label1)) {
			temp.append(label1);
		} else {
			temp.append("<b>").append(label1).append("</b>").append(
				" <a href=\"javascript:popUpHelp('help/config/help.html')\">(?)</a>");
		}

		temp.append("</td>\n<td>");
		if ("&nbsp;".equals(label2)) {
			temp.append(label2);
		} else {
			temp.append("<b>").append(label2).append("</b>").append(
				" <a href=\"javascript:popUpHelp('help/config/help.html')\">(?)</a>");
		}
		temp.append("</td>\n</tr>\n<tr>\n<td>");

		return temp.toString();
	}

	private static String createTableHTMLDecleration() {
		return "<form action=\"Configuration\" method=\"post\" name=\"configForm\">\n"
			+ "<table border=\"0\" cellpadding=\"1\" cellspacing=\"1\" width=\"430\">";
	}
}