package com.marklogic.appdeployer.command.forests;

import com.marklogic.appdeployer.AppConfig;
import com.marklogic.appdeployer.DefaultAppConfigFactory;
import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.mgmt.util.SimplePropertySource;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

public class CalculateForestHostsTest extends Assert {

	@Test
	public void test() {
		AppConfig appConfig = new AppConfig();
		CommandContext context = new CommandContext(appConfig, null, null);

		TestHostNameProvider hostNameProvider = new TestHostNameProvider("name1", "name2", "name3", "name4", "name5");
		hostNameProvider.addGroupHostNames("group1", "name1", "name2");
		hostNameProvider.addGroupHostNames("group2", "name3");
		hostNameProvider.addGroupHostNames("group3", "name4", "name5");

		DefaultHostCalculator hostCalculator = new DefaultHostCalculator(hostNameProvider);

		// Verify we get all 5 hosts back when nothing special is configured
		List<String> hostNames = hostCalculator.calculateHostNames("test-db", context);
		assertEquals(5, hostNames.size());

		// Select 2 of the 3 hosts for test-db
		Properties props = new Properties();
		props.setProperty("mlDatabaseGroups", "test-db,group1|group2");
		DefaultAppConfigFactory factory = new DefaultAppConfigFactory(new SimplePropertySource(props));
		appConfig = factory.newAppConfig();
		context = new CommandContext(appConfig, null, null);

		hostNames = hostCalculator.calculateHostNames("test-db", context);
		assertEquals(3, hostNames.size());
		assertTrue(hostNames.contains("name1"));
		assertTrue(hostNames.contains("name2"));
		assertTrue(hostNames.contains("name3"));

		props.setProperty("mlDatabaseGroups", "test-db,group3");
		appConfig = factory.newAppConfig();
		context = new CommandContext(appConfig, null, null);
		hostNames = hostCalculator.calculateHostNames("test-db", context);
		assertEquals(2, hostNames.size());
		assertTrue(hostNames.contains("name4"));
		assertTrue(hostNames.contains("name5"));
	}
}
