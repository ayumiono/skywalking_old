package org.apache.skywalking.oap.server.storage.plugin.prometheus.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * 不做name排重; 每次prometheus 从 httpserver 拉取完后，清空注册的 metrics collections; 
 * LRU //TODO
 * 
 * @author Administrator
 */
@Slf4j
public class CustomCollectorRegistry extends CollectorRegistry {

	/**
	 * The default registry.
	 */
	public static final CustomCollectorRegistry defaultRegistry = new CustomCollectorRegistry(true);

	private final Object namesCollectorsLock = new Object();
	private final Map<Collector, List<String>> collectorsToNames = new HashMap<Collector, List<String>>();
	private final Map<String, List<Collector>> namesToCollectors = new HashMap<String, List<Collector>>();

	private final boolean autoDescribe;

	public CustomCollectorRegistry() {
		this(false);
	}

	public CustomCollectorRegistry(boolean autoDescribe) {
		this.autoDescribe = autoDescribe;
	}

	/**
	 * Register a Collector.
	 * <p>
	 * A collector can be registered to multiple CollectorRegistries.
	 */
	public void register(Collector m) {
		List<String> names = collectorNames(m);
		synchronized (namesCollectorsLock) {
			for (String name : names) {
				if (!namesToCollectors.containsKey(name)) {
					namesToCollectors.put(name, new ArrayList<>());
				}
				namesToCollectors.get(name).add(m);
			}
			collectorsToNames.put(m, names);
		}
	}

	/**
	 * Unregister a Collector.
	 */
	public void unregister(Collector m) {
		synchronized (namesCollectorsLock) {
			List<String> names = collectorsToNames.remove(m);
			for (String name : names) {
				namesToCollectors.remove(name);
			}
		}
	}

	/**
	 * Unregister all Collectors.
	 */
	public void clear() {
		synchronized (namesCollectorsLock) {
			collectorsToNames.clear();
			namesToCollectors.clear();
		}
	}

	/**
	 * A snapshot of the current collectors.
	 */
	private Set<Collector> collectors() {
		synchronized (namesCollectorsLock) {
			Set<Collector> result = new HashSet<Collector>(collectorsToNames.keySet());
			clear();
			log.debug("all metrics pulled, clear cache.");
			return result;
		}
	}

	private List<String> collectorNames(Collector m) {
		List<Collector.MetricFamilySamples> mfs;
		if (m instanceof Collector.Describable) {
			mfs = ((Collector.Describable) m).describe();
		} else if (autoDescribe) {
			mfs = m.collect();
		} else {
			mfs = Collections.emptyList();
		}

		List<String> names = new ArrayList<String>();
		for (Collector.MetricFamilySamples family : mfs) {
			switch (family.type) {
			case SUMMARY:
				names.add(family.name + "_count");
				names.add(family.name + "_sum");
				names.add(family.name);
				break;
			case HISTOGRAM:
				names.add(family.name + "_count");
				names.add(family.name + "_sum");
				names.add(family.name + "_bucket");
				names.add(family.name);
				break;
			default:
				names.add(family.name);
			}
		}
		return names;
	}

	/**
	 * Enumeration of metrics of all registered collectors.
	 */
	public Enumeration<Collector.MetricFamilySamples> metricFamilySamples() {
		throw new UnsupportedOperationException("不支持的操作");
	}

	/**
	 * Enumeration of metrics matching the specified names.
	 * <p>
	 * Note that the provided set of names will be matched against the time series
	 * name and not the metric name. For instance, to retrieve all samples from a
	 * histogram, you must include the '_count', '_sum' and '_bucket' names.
	 */
	public Enumeration<Collector.MetricFamilySamples> filteredMetricFamilySamples(Set<String> includedNames) {
		return new MetricFamilySamplesEnumeration(includedNames);
	}

	class MetricFamilySamplesEnumeration implements Enumeration<Collector.MetricFamilySamples> {

		private final Iterator<Collector> collectorIter;
		private Iterator<Collector.MetricFamilySamples> metricFamilySamples;
		private Collector.MetricFamilySamples next;
		private Set<String> includedNames;

		MetricFamilySamplesEnumeration(Set<String> includedNames) {
			this.includedNames = includedNames;
			collectorIter = includedCollectorIterator(includedNames);
			findNextElement();
		}

		private Iterator<Collector> includedCollectorIterator(Set<String> includedNames) {
			if (includedNames.isEmpty()) {
				Set<Collector> collector = collectors();
				return collector.iterator();
			} else {
				HashSet<Collector> collectors = new HashSet<Collector>();
				synchronized (namesCollectorsLock) {
					final Set<String> pulledMetricNames = new HashSet<>();
					for (Map.Entry<String, List<Collector>> entry : namesToCollectors.entrySet()) {
						if (includedNames.contains(entry.getKey())) {
							pulledMetricNames.add(entry.getKey());
							collectors.addAll(entry.getValue());
							for(Collector pulledCollector : entry.getValue()) {
								collectorsToNames.remove(pulledCollector);
							}
						}
					}
					namesToCollectors.entrySet().removeIf(e->{
						return pulledMetricNames.contains(e.getKey());
					});
				}

				return collectors.iterator();
			}
		}

		MetricFamilySamplesEnumeration() {
			this(Collections.<String>emptySet());
		}

		private void findNextElement() {
			try {
				next = null;
				
				while (metricFamilySamples != null && metricFamilySamples.hasNext()) {
					next = filter(metricFamilySamples.next());
					if (next != null) {
						return;
					}
				}

				if (next == null) {
					while (collectorIter.hasNext()) {
						metricFamilySamples = collectorIter.next().collect().iterator();
						while (metricFamilySamples.hasNext()) {
							next = filter(metricFamilySamples.next());
							if (next != null) {
								return;
							}
						}
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		private Collector.MetricFamilySamples filter(Collector.MetricFamilySamples next) {
			if (includedNames.isEmpty()) {
				return next;
			} else {
				Iterator<Collector.MetricFamilySamples.Sample> it = next.samples.iterator();
				while (it.hasNext()) {
					if (!includedNames.contains(it.next().name)) {
						it.remove();
					}
				}
				if (next.samples.size() == 0) {
					return null;
				}
				return next;
			}
		}

		public Collector.MetricFamilySamples nextElement() {
			Collector.MetricFamilySamples current = next;
			if (current == null) {
				throw new NoSuchElementException();
			}
			findNextElement();
			return current;
		}

		public boolean hasMoreElements() {
			return next != null;
		}
	}

	public Double getSampleValue(String name) {
		throw new UnsupportedOperationException("不支持的操作");
	}

	public Double getSampleValue(String name, String[] labelNames, String[] labelValues) {
		throw new UnsupportedOperationException("不支持的操作");
	}
}
