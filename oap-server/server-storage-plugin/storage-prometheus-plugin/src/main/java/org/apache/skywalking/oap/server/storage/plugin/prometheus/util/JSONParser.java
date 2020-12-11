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
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.MetricType;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PromeSummary.PromeSummaryBuilder;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PrometheusHttpApi.PrometheusHttpAPIRespBody;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class JSONParser {

	private final PrometheusHttpAPIRespBody json;

	public PromeMetricFamily metricFamily;

	public MetricType type = MetricType.GAUGE;
	public List<PromeTextSample> samples = new ArrayList<>();
	private boolean _bucket_flag = false;
	private boolean _sum_flag = false;
	private boolean _count_flag = false;
	private boolean _quantile_flag = false;

	public PromeMetricFamily parse() throws IOException {
		json.getData().getResult().forEach(metric->{
			String name = metric.getMetric().get("__name__").toString();
			Map<String, String> labels = metric.getMetric();
			_bucket_flag = _bucket_flag ? _bucket_flag : name.endsWith("_bucket") && labels.containsKey("le");
			_sum_flag = _sum_flag ? _sum_flag : name.endsWith("_sum");
			_count_flag = _count_flag ? _count_flag : name.endsWith("_count");
			_quantile_flag = _quantile_flag ? _quantile_flag : labels.containsKey("quantile");
			if(metric.getValue() == null) {
				metric.getValues().stream().forEach(value->{
					samples.add(new PromeTextSample(name, labels, value[1].toString(), 
							new BigDecimal((double)value[0]).multiply(new BigDecimal("1000")).longValue()));
				});
			}else {
				samples.add(new PromeTextSample(name, labels, metric.getValue()[1].toString(), 
						new BigDecimal((double)metric.getValue()[0]).multiply(new BigDecimal("1000")).longValue()));
			}
		});
		if(_bucket_flag && _sum_flag && _count_flag) {
			if(_quantile_flag) {
				type = MetricType.SUMMARY;
			}else {
				type = MetricType.HISTOGRAM;
			}
		}
		if(json.getData() != null && json.getData().getResult() != null && json.getData().getResult().size() > 0) {
			log.info("{} type---------------------->{}", new Gson().toJson(json), type);
		}
		end();
		return metricFamily;
	}

	private void end() {
		if (metricFamily != null) {
			return;
		}

		PromeMetricFamily.Builder metricFamilyBuilder = new PromeMetricFamily.Builder();
		metricFamilyBuilder.setName("");
		metricFamilyBuilder.setType(type);

		if (samples.size() < 1) {
			return;
		}
		switch (type) {
		case GAUGE:
			samples.stream().map(sample -> {
				Map<String, String> labels = Maps.newHashMap(sample.getLabels());
				labels.remove("annotation");
				return Pair.of(labels, sample);
			}).collect(groupingBy(Pair::getLeft, mapping(Pair::getRight, toList()))).forEach((labels, samples)->{
				samples.forEach((textSample) -> {
					int age = textSample.getLabels().get("age") == null ? 0 : Integer.parseInt(textSample.getLabels().get("age"));
					metricFamilyBuilder
					.addMetric(PromeGauge.builder().name(textSample.getName()).value(convertStringToDouble(textSample.getValue()))
					.labels(textSample.getLabels()).timestamp(textSample.timestamp).age(age).build());
				});
			});
			break;
		case HISTOGRAM:
			samples.stream().map(sample -> {
				Map<String, String> labels = Maps.newHashMap(sample.getLabels());
				labels.remove("le");
				labels.remove("__name__");//_bucket的name不一样
				return Pair.of(labels, sample);
			}).collect(groupingBy(Pair::getLeft, mapping(Pair::getRight, toList()))).forEach((labels, samples) -> {
				long timestamp = samples.get(0).timestamp;
				PromeHistogram.PromeHistogramBuilder hBuilder = PromeHistogram.builder();
	            hBuilder.name("").timestamp(timestamp);
	            int age = samples.get(0).getLabels().get("age") == null ? 0 : Integer.parseInt(samples.get(0).getLabels().get("age"));
	            samples.forEach(textSample -> {
	                hBuilder.labels(textSample.getLabels());
	                if (textSample.getName().endsWith("_count")) {
	                    hBuilder.sampleCount((long) convertStringToDouble(textSample.getValue()));
	                } else if (textSample.getName().endsWith("_sum")) {
	                    hBuilder.sampleSum(convertStringToDouble(textSample.getValue()));
	                } else if (textSample.getLabels().containsKey("le")) {
	                    hBuilder.bucket(textSample.getLabels().remove("le"),
	                        (long) convertStringToDouble(textSample.getValue())
	                    );
	                }
	            });
	            metricFamilyBuilder.addMetric(hBuilder.age(age).build());
			});
			break;
		case SUMMARY:
			samples.stream().map(sample -> {
				Map<String, String> labels = Maps.newHashMap(sample.getLabels());
				labels.remove("quantile");
				labels.remove("le");
				labels.remove("__name__");//_bucket的name不一样
				return Pair.of(labels, sample);
			}).collect(groupingBy(Pair::getLeft, mapping(Pair::getRight, toList()))).forEach((labels, samples) -> {
				PromeSummaryBuilder sBuilder = PromeSummary.builder();
				String precision = labels.remove("precision");
				int _age = samples.get(0).getLabels().get("age") == null ? 0 : Integer.parseInt(samples.get(0).getLabels().get("age")); 
				sBuilder.name("").timestamp(samples.get(0).timestamp).labels(labels).precision(Integer.parseInt(precision));
				samples.forEach(textSample -> {
					if (textSample.getName().endsWith("_count")) {
						sBuilder.sampleCount((long) convertStringToDouble(textSample.getValue()));
					} else if (textSample.getName().endsWith("_sum")) {
						sBuilder.sampleSum(convertStringToDouble(textSample.getValue()));
					} else if (textSample.getLabels().containsKey("quantile")) {
						sBuilder.quantile(textSample.getLabels().remove("quantile"),
								convertStringToLong(textSample.getValue()));
					} else if (textSample.getLabels().containsKey("le") && textSample.getLabels().get("__name__").endsWith("_bucket")) {
						sBuilder.dataset(textSample.getLabels().remove("le"),
								convertStringToLong(textSample.getValue()));
					}
				});
				metricFamilyBuilder.addMetric(sBuilder.age(_age).build());
			});
			break;
		default:
			
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
