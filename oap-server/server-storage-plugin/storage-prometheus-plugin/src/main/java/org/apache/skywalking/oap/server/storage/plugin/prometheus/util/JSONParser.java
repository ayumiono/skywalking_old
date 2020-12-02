package org.apache.skywalking.oap.server.storage.plugin.prometheus.util;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Counter;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Gauge;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.MetricFamily;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.MetricType;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PromeSummary.PromeSummaryBuilder;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PrometheusHttpApi.PrometheusHttpAPIRespBody;

import com.google.common.collect.Maps;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class JSONParser {

	private final PrometheusHttpAPIRespBody json;

	public MetricFamily metricFamily;

	public MetricType type = null;
	public List<PromeTextSample> samples = new ArrayList<>();

	public MetricFamily parse() throws IOException {
		json.getData().getResult().forEach(metric->{
			if(metric.getValue() == null) {
				metric.getValues().stream().forEach(value->{
					samples.add(new PromeTextSample(metric.getMetric().get("__name__").toString(), metric.getMetric(), value[1].toString(), new BigDecimal((double)value[0]).multiply(new BigDecimal("1000")).longValue()));
				});
			}else {
				samples.add(new PromeTextSample(metric.getMetric().get("__name__").toString(), metric.getMetric(), metric.getValue()[1].toString(), new BigDecimal((double)metric.getValue()[0]).multiply(new BigDecimal("1000")).longValue()));
			}
		});
		end();
		return metricFamily;
	}

	private void end() {
		if (metricFamily != null) {
			return;
		}

		MetricFamily.Builder metricFamilyBuilder = new MetricFamily.Builder();
		metricFamilyBuilder.setName("");
		metricFamilyBuilder.setType(type);

		if (samples.size() < 1) {
			return;
		}
		switch (type) {
		case GAUGE:
			samples.forEach(textSample -> metricFamilyBuilder
					.addMetric(Gauge.builder().name(textSample.getName()).value(convertStringToDouble(textSample.getValue()))
							.labels(textSample.getLabels()).timestamp(textSample.timestamp).build()));
			break;
		case COUNTER:
			samples.forEach(textSample -> metricFamilyBuilder
					.addMetric(Counter.builder().name(textSample.getName()).value(convertStringToDouble(textSample.getValue()))
							.labels(textSample.getLabels()).timestamp(textSample.timestamp).build()));
			break;
		case HISTOGRAM:
			samples.stream().map(sample -> {
				Map<String, String> labels = Maps.newHashMap(sample.getLabels());
				labels.remove("le");
				return Pair.of(labels, sample);
			}).collect(groupingBy(Pair::getLeft, mapping(Pair::getRight, toList()))).forEach((labels, samples) -> {
				PromeHistogram.PromeHistogramBuilder hBuilder = PromeHistogram.builder();
				hBuilder.name(samples.get(0).getName()).timestamp(samples.get(0).timestamp);
				hBuilder.labels(labels);
				samples.forEach(textSample -> {
					if (textSample.getName().endsWith("_count")) {
						hBuilder.sampleCount((long) convertStringToDouble(textSample.getValue()));
					} else if (textSample.getName().endsWith("_sum")) {
						hBuilder.sampleSum(convertStringToDouble(textSample.getValue()));
					} else if (textSample.getLabels().containsKey("le")) {
						hBuilder.bucket(textSample.getLabels().remove("le"),
								(long) convertStringToDouble(textSample.getValue()));
					}
				});
				metricFamilyBuilder.addMetric(hBuilder.build());
			});
			break;
		case SUMMARY:
			samples.stream().map(sample -> {
				Map<String, String> labels = Maps.newHashMap(sample.getLabels());
				labels.remove("quantile");
				labels.remove("le");
				return Pair.of(labels, sample);
			}).collect(groupingBy(Pair::getLeft, mapping(Pair::getRight, toList()))).forEach((labels, samples) -> {
				PromeSummaryBuilder sBuilder = PromeSummary.builder();
				sBuilder.name(name).timestamp(samples.get(0).timestamp);
				String precision = labels.remove("precision");
				sBuilder.labels(labels);
				sBuilder.precision(Integer.parseInt(precision));
				samples.forEach(textSample -> {
					if (textSample.getName().endsWith("_count")) {
						sBuilder.sampleCount((long) convertStringToDouble(textSample.getValue()));
					} else if (textSample.getName().endsWith("_sum")) {
						sBuilder.sampleSum(convertStringToDouble(textSample.getValue()));
					} else if (textSample.getLabels().containsKey("quantile")) {
						sBuilder.quantile(textSample.getLabels().remove("quantile"),
								convertStringToLong(textSample.getValue()));
					} else if (textSample.getLabels().containsKey("le")) {
						sBuilder.dataset(textSample.getLabels().remove("le"),
								convertStringToLong(textSample.getValue()));
					}
				});
				metricFamilyBuilder.addMetric(sBuilder.build());
			});

			break;
		}
		metricFamily = metricFamilyBuilder.build();
	}

	private static double convertStringToDouble(String valueString) {
		double doubleValue;
		if (valueString.equalsIgnoreCase("NaN")) {
			doubleValue = Double.NaN;
		} else if (valueString.equalsIgnoreCase("+Inf")) {
			doubleValue = Double.POSITIVE_INFINITY;
		} else if (valueString.equalsIgnoreCase("-Inf")) {
			doubleValue = Double.NEGATIVE_INFINITY;
		} else {
			doubleValue = Double.parseDouble(valueString);
		}
		return doubleValue;
	}
	
	private static long convertStringToLong(String valueString) {
		long doubleValue;
		if (valueString.equalsIgnoreCase("NaN")) {
			doubleValue = Long.MIN_VALUE;
		} else if (valueString.equalsIgnoreCase("+Inf")) {
			doubleValue = Long.MAX_VALUE;
		} else if (valueString.equalsIgnoreCase("-Inf")) {
			doubleValue = Long.MIN_VALUE;
		} else {
			doubleValue = Long.parseLong(valueString);
		}
		return doubleValue;
	}

	@Setter
	@Getter
	@AllArgsConstructor
	public static class PromeTextSample {
		private final String name;
		private final Map<String, String> labels;
		private final String value;
		public final Long timestamp;
	}
}
