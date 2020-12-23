package org.apache.skywalking.oap.server.analyzer.provider.trace.parser.listener;

import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.oap.server.analyzer.provider.AnalyzerModuleConfig;
import org.apache.skywalking.oap.server.analyzer.provider.trace.parser.IGroupParserService;
import org.apache.skywalking.oap.server.core.source.Endpoint;
import org.apache.skywalking.oap.server.library.util.ResourceUtils;
import org.yaml.snakeyaml.Yaml;

public class GroupProcessor implements IGroupParserService {

	private Map<String/* service_name */, List<GroupRule>> groupRules;

	abstract class GroupRule {
		public abstract String group(Endpoint endpoint);
	}

	public GroupProcessor(AnalyzerModuleConfig config) {
		this.readGroupRules(config);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void readGroupRules(AnalyzerModuleConfig config) {
		Reader applicationReader;
		try {
			applicationReader = ResourceUtils.read("group-settings.yml");
		} catch (FileNotFoundException e) {
			throw new RuntimeException("can't load group-settings.yml", e);
		}
		Map yamlData = new Yaml().loadAs(applicationReader, Map.class);
		List<Map<String, Object>> property = (List<Map<String, Object>>) yamlData.get("groupRules");
		if (property == null) {
			return;
		}
		groupRules = new HashMap<String, List<GroupRule>>();
		property.forEach((item) -> {
			String group = item.get("group").toString();
			List<Map<String, Object>> services = (List<Map<String, Object>>) item.get("service");
			services.forEach((service) -> {
				service.entrySet().forEach((e) -> {
					String serviceName = e.getKey();
					if (!groupRules.containsKey(serviceName)) {
						groupRules.put(serviceName, new ArrayList<>());
					}
					if(group.startsWith("~")) {
						GroupRule groupRule = new GroupRule() {
							Pattern pattern = Pattern.compile(StringUtils.substringAfter(group, "~"));
							@Override
							public String group(Endpoint endpoint) {
								Matcher matcher = pattern.matcher(endpoint.getName());
								if (matcher.find()) {
									return matcher.group(1);
								}
								return null;
							}
						};
						groupRules.get(serviceName).add(groupRule);
					} else if(e.getValue() != null) {
						List<String> ematchers = (List<String>) ((Map) e.getValue()).get("endpoint");
						GroupRule groupRule = new GroupRule() {
							private List<Pattern> patterns = new ArrayList<>();
							private List<String> equals = new ArrayList<>();
							{
								for(String ematcher : ematchers) {
									if (ematcher.startsWith("~")) {
										Pattern pattern = Pattern.compile(StringUtils.substringAfter(ematcher, "~"));
										patterns.add(pattern);
									}else {
										equals.add(ematcher);
									}
								}
							}
							@Override
							public String group(Endpoint endpoint) {
								for(String ematcher : ematchers) {
									if (ematcher.startsWith("~")) {
										Pattern pattern = Pattern.compile(StringUtils.substringAfter(ematcher, "~"));
										Matcher matcher = pattern.matcher(endpoint.getName());
										if (matcher.find()) {
											return group;
										}
									} else if (ematcher.equals(endpoint.getName())) {
										return group;
									}
								}
								return null;
							}
						};
						groupRules.get(serviceName).add(groupRule);
					}
				});
			});
		});
	}
	
	@Override
	public List<String> group(Endpoint endpoint) {
		List<String> groupLabels = new ArrayList<String>();
		if (groupRules.containsKey(endpoint.getServiceName())) {
			groupRules.get(endpoint.getServiceName()).forEach(groupRule -> {
				String label = groupRule.group(endpoint);
				if(label != null) {
					groupLabels.add(label);
				}
			});
		}
		return groupLabels;
	}
}
