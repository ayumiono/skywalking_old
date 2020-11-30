package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.query.input.DashboardSetting;
import org.apache.skywalking.oap.server.core.query.type.DashboardConfiguration;
import org.apache.skywalking.oap.server.core.query.type.TemplateChangeStatus;
import org.apache.skywalking.oap.server.core.storage.management.UITemplateManagementDAO;
import org.apache.skywalking.oap.server.storage.plugin.mixed.StorageModuleMixedConfig;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UITemplateManagementDAOMixedImpl implements UITemplateManagementDAO {

	private final StorageModuleMixedConfig config;
	
	private final Map<String/*provider*/, UITemplateManagementDAO> candidates;
	
	@Override
	public List<DashboardConfiguration> getAllTemplates(Boolean includingDisabled) throws IOException {
		return candidates.get(config.getManagement()).getAllTemplates(includingDisabled);
	}

	@Override
	public TemplateChangeStatus addTemplate(DashboardSetting setting) throws IOException {
		return candidates.get(config.getManagement()).addTemplate(setting);
	}

	@Override
	public TemplateChangeStatus changeTemplate(DashboardSetting setting) throws IOException {
		return candidates.get(config.getManagement()).changeTemplate(setting);
	}

	@Override
	public TemplateChangeStatus disableTemplate(String name) throws IOException {
		return candidates.get(config.getManagement()).disableTemplate(name);
	}

}
