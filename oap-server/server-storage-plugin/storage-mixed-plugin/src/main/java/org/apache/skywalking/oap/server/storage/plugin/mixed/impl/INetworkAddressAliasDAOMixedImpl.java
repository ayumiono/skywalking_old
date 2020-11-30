package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.manual.networkalias.NetworkAddressAlias;
import org.apache.skywalking.oap.server.core.storage.cache.INetworkAddressAliasDAO;
import org.apache.skywalking.oap.server.storage.plugin.mixed.StorageModuleMixedConfig;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class INetworkAddressAliasDAOMixedImpl implements INetworkAddressAliasDAO {

	private final StorageModuleMixedConfig config;
	
	final Map<String, INetworkAddressAliasDAO> candidates;
	
	@Override
	public List<NetworkAddressAlias> loadLastUpdate(long timeBucket) {
		return candidates.get(config.getMetrics()).loadLastUpdate(timeBucket);
	}

}
