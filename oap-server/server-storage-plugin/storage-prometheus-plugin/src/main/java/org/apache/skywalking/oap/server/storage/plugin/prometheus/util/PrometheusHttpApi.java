package org.apache.skywalking.oap.server.storage.plugin.prometheus.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.skywalking.oap.server.core.analysis.metrics.HistogramMetrics;
import org.apache.skywalking.oap.server.core.analysis.metrics.PercentileMetrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMeterMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 基于apache http 开发
 * @author Administrator
 *
 */
public class PrometheusHttpApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusHttpApi.class);
	
	private final String prometheusAddress;
	public CloseableHttpClient client;
	
	final static DecimalFormat df = new DecimalFormat("#.000");
	
	private static final String RANGE = "/api/v1/query_range";
	private static final String QUERY = "/api/v1/query";
	
	public PrometheusHttpApi(String prometheusAddress) {
		this.prometheusAddress = prometheusAddress;
		client = HttpClientBuilder.create().build();//FIXME
	}
	
	public static String formatTimestamp(long timestamp) {
		String ts = df.format(new BigDecimal(timestamp+"").divide(new BigDecimal("1000")).doubleValue());
		return ts;
	}
	
	public static class PromQLBuilder {
		
		private String name;
		
		public String build() {
			return null;//TODO
		}
		
		public PromQLBuilder name(String name) {
			this.name = name;
			return this;
		}
		
		public PromQLBuilder offset() {
			return this;
		}
		
		public PromQLBuilder duration() {
			return this;
		}
		
		public PromQLBuilder labelEq() {
			return this;
		}
		
		public PromQLBuilder labelNotEq() {
			return this;
		}
		
		public PromQLBuilder labelRegMatch() {
			return this;
		}
		
		public PromQLBuilder labelNotRegMatch() {
			return this;
		}
		
		public PromQLBuilder func() {
			return this;
		}
		
		public PromQLBuilder operator() {
			return this;
		}
		
		public PromQLBuilder subquery() {
			return this;
		}
	}
	
	public PrometheusHttpAPIRespBody rangeQuery(String name, Map<String, String> labels, long start) {
		return rangeQuery(name, labels, start, System.currentTimeMillis());
	}
	
	public PrometheusHttpAPIRespBody rangeQuery(String name, Map<String, String> labels, long start, long end) {
		try {
			URIBuilder uriBuilder = new URIBuilder(prometheusAddress + RANGE);
			
			StringJoiner queryBuilder = new StringJoiner(",", name+"{", "}");
			
			if(labels != null) {
				for(Entry<String, String> label : labels.entrySet()) {
					queryBuilder.add(label.getKey()+"="+label.getValue());
				}
			}
			
			uriBuilder.addParameter("query", queryBuilder.toString());
			uriBuilder.addParameter("start", formatTimestamp(start));
			uriBuilder.addParameter("end", formatTimestamp(end));
			uriBuilder.addParameter("step", "60s");//FIXME
			
			HttpGet request = new HttpGet(uriBuilder.build());
			
			CloseableHttpResponse response = client.execute(request);
			
			String responseStr = EntityUtils.toString(response.getEntity());
			
			Gson gson = new Gson();
			
			return gson.fromJson(responseStr, PrometheusHttpAPIRespBody.class);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
		return null;
	}
	
	public PrometheusHttpAPIRespBody query(Model model, List<String> ids) {
		
		PrometheusHttpAPIRespBody body = null;
		
		try {
			URIBuilder uriBuilder = new URIBuilder(prometheusAddress + QUERY);
			
			Gson gson = new Gson();
			
			Map<String/*timestamp形如1606811026.446*/, Set<String>> tids = PrometheusMeterMapper.idAndTimestampTuple2(model, ids);
			
			for(Entry<String, Set<String>> entry : tids.entrySet()) {
				String timestampStr = entry.getKey();
				Set<String> _ids = entry.getValue();
				uriBuilder.clearParameters();
				//_bucket, _sum, _count
				boolean regx = HistogramMetrics.class.isAssignableFrom(model.getStorageModelClazz()) || PercentileMetrics.class.isAssignableFrom(model.getStorageModelClazz());
				String query = "";
				if(regx) {
					query = "{__name__=~\"" + model.getName() + ".*\",id=~\"" + StringUtils.join(_ids, "|") +"\"}";
				}else {
					query = "{__name__=\"" + model.getName() + ".*\",id=~\"" + StringUtils.join(_ids, "|") +"\"}";
				}
				
				uriBuilder.addParameter("query", query);
				uriBuilder.addParameter("time", timestampStr);
				
				HttpGet request = new HttpGet(uriBuilder.build());
				
				CloseableHttpResponse response = client.execute(request);
				
				String responseStr = EntityUtils.toString(response.getEntity());
				
				
				PrometheusHttpAPIRespBody child = gson.fromJson(responseStr, PrometheusHttpAPIRespBody.class);
				if(child.status.equals("success") && child.data != null && child.data.result != null) {
					if(body == null) {
						body = child;
					}else {
						body.append(child);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
		return body;
	}
	
	@Setter
	@Getter
	public static class PrometheusHttpAPIRespBody implements Serializable, Iterable<Metric> {
		private static final long serialVersionUID = -7567178683513936776L;
		private String status;
		private Data data;
		
		@Override
		public Iterator<Metric> iterator() {
			return data.getResult().iterator();
		}
		
		public void append(PrometheusHttpAPIRespBody source) {
			this.data.getResult().addAll(source.getData().getResult());
		}
	}

	@Setter
	@Getter
	public static class Data implements Serializable {

		private static final long serialVersionUID = 5604354581559044232L;
		
		private String resultType;
		
		private List<Metric> result;
	}
 	
	@Setter
	@Getter
	public static class Metric implements Serializable {
		private static final long serialVersionUID = -7079071092659601978L;
		private Map<String/*<label_name>*/, String/*<label_value>*/> metric;
		private List<Object[]> values;
		private Object[/*<unix_time>, <sample_value>*/] value;
	}
	
	
}
