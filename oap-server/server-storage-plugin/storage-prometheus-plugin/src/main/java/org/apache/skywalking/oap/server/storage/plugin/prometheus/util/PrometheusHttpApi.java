package org.apache.skywalking.oap.server.storage.plugin.prometheus.util;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.skywalking.oap.server.core.analysis.metrics.HistogramMetrics;
import org.apache.skywalking.oap.server.core.analysis.metrics.PercentileMetrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMeterMapper;

import com.google.gson.Gson;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 基于apache http 开发
 * @author Administrator
 *
 */
@Slf4j
public class PrometheusHttpApi {

	private final String prometheusAddress;
	public CloseableHttpClient client;
	Gson gson = new Gson();
	
	final static DecimalFormat df = new DecimalFormat("#.000");
	
	private static final String RANGE = "/api/v1/query_range";
	private static final String QUERY = "/api/v1/query";
	private static final String DELETE = "/api/v1/admin/tsdb/delete_series";
	private static final String CLEAN_TOMBSTONES = "/api/v1/admin/tsdb/clean_tombstones";
	
	public PrometheusHttpApi(String prometheusAddress) {
		this.prometheusAddress = prometheusAddress;
		client = HttpClientBuilder.create().build();//FIXME
	}
	
	public static String formatTimestamp(long timestamp) {
		String ts = df.format(new BigDecimal(timestamp+"").divide(new BigDecimal("1000")).doubleValue());
		return ts;
	}
	
	public int queryAge(String name, Map<String, String> labels, long time) {
		try {
			URIBuilder uriBuilder = new URIBuilder(prometheusAddress + QUERY);
			
			StringJoiner queryBuilder = new StringJoiner(",", name+"{", "}");
			
			if(labels != null) {
				for(Entry<String, String> label : labels.entrySet()) {
					queryBuilder.add(label.getKey()+"='"+label.getValue()+"'");
				}
			}
			
			uriBuilder.addParameter("query", queryBuilder.toString());
			uriBuilder.addParameter("time", formatTimestamp(time));
			
			HttpPost post = new HttpPost(uriBuilder.build());
			
			CloseableHttpResponse response = client.execute(post);
			String responseStr = EntityUtils.toString(response.getEntity());
			PrometheusHttpAPIRespBody body = gson.fromJson(responseStr, PrometheusHttpAPIRespBody.class);
			if(body.getData() != null && body.getData().getResult().size() > 0) {
				return Integer.parseInt(body.getData().getResult().get(0).metric.get("age"));
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return 0;
	}
	
	public boolean delete(Model model, Map<String, String> labels, long start, long end) {
		try {
			URIBuilder uriBuilder = new URIBuilder(prometheusAddress + DELETE);
			
			boolean regx = HistogramMetrics.class.isAssignableFrom(model.getStorageModelClazz()) || PercentileMetrics.class.isAssignableFrom(model.getStorageModelClazz());
			StringJoiner queryBuilder = new StringJoiner(",","{","}");
			if(regx) {
				queryBuilder.add("__name__=~\"" + model.getName() + ".*\"");
			}else {
				queryBuilder.add("__name__=\"" + model.getName() + "\"");
			}
			if(labels != null) {
				for(Entry<String, String> label : labels.entrySet()) {
					queryBuilder.add(label.getKey()+"='"+label.getValue()+"'");
				}
			}
			
			uriBuilder.addParameter("match[]", queryBuilder.toString());
			uriBuilder.addParameter("start", formatTimestamp(start));
			uriBuilder.addParameter("end", formatTimestamp(end));
			
			HttpPost post = new HttpPost(uriBuilder.build());
			
			CloseableHttpResponse response = client.execute(post);
			
			if(response.getStatusLine().getStatusCode() == 204) {
				post = new HttpPost(uriBuilder.build());
				uriBuilder = new URIBuilder(prometheusAddress + CLEAN_TOMBSTONES);
				response = client.execute(post);
				if(response.getStatusLine().getStatusCode() == 204) {
					return true;
				}else {
					String responseStr = EntityUtils.toString(response.getEntity());
					log.error(responseStr);
				}
			}else {
				String responseStr = EntityUtils.toString(response.getEntity());
				log.error(responseStr);
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return false;
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
					queryBuilder.add(label.getKey()+"='"+label.getValue()+"'");
				}
			}
			
			uriBuilder.addParameter("query", queryBuilder.toString());
			uriBuilder.addParameter("start", formatTimestamp(start));
			uriBuilder.addParameter("end", formatTimestamp(end));
			uriBuilder.addParameter("step", "60s");//FIXME
			
			HttpGet request = new HttpGet(uriBuilder.build());
			
			CloseableHttpResponse response = client.execute(request);
			
			String responseStr = EntityUtils.toString(response.getEntity());
			
			return gson.fromJson(responseStr, PrometheusHttpAPIRespBody.class);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return null;
	}
	
	public boolean delete(Model model, PromeMetricFamily hisotryMetric) {
		try {
			boolean regx = HistogramMetrics.class.isAssignableFrom(model.getStorageModelClazz()) || PercentileMetrics.class.isAssignableFrom(model.getStorageModelClazz());
			String nameMatch = regx ? "__name__=~\"" + model.getName() + ".*\"" : "__name__=\"" + model.getName() + "\"";
			hisotryMetric.getMetrics().stream().collect(Collectors.groupingBy((metric)->{
				return metric.getTimestamp();
			})).entrySet().forEach((entry)->{//group by timestamp
				long timestamp = entry.getKey();
				List<PromeMetric> metrics = entry.getValue();
				metrics.stream().collect(Collectors.groupingBy((m)->{//group by age
					return m.getAge();
				})).entrySet().forEach(e->{
					int age = e.getKey();
					List<PromeMetric> ms = entry.getValue();
					String idMatch = "id=\"" + StringUtils.join(ms.stream().map(f->f.getId()).collect(Collectors.toList()), "|") + "\"";
					String ageMatch = "age=\"" + age + "\"";
					String match = "{" + nameMatch + "," + idMatch + "," + ageMatch + "}";
					try {
						URIBuilder uriBuilder = new URIBuilder(prometheusAddress + DELETE);
						
						uriBuilder.addParameter("match[]", match);
						uriBuilder.addParameter("start", formatTimestamp(timestamp));
						uriBuilder.addParameter("end", formatTimestamp(timestamp));
						HttpPost request = new HttpPost(uriBuilder.build());
						CloseableHttpResponse response = client.execute(request);
						if(response.getStatusLine().getStatusCode() == 204) {
							log.error("delete prometheus history data failed:" + response.getStatusLine().getStatusCode());
						}else {
							String responseStr = EntityUtils.toString(response.getEntity());
							log.error("delete by ids failed:{}", responseStr);
						}
					} catch (URISyntaxException | IOException e1) {
						log.error(e1.getMessage(), e1);
					}
				});
			});;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}
	
	public boolean delete(Model model, List<String> ids) {
		try {
			URIBuilder uriBuilder = new URIBuilder(prometheusAddress + DELETE);
			
			Map<String/*timestamp形如1606811026.446*/, Set<String>> tids = PrometheusMeterMapper.idAndTimestampTuple2(model, ids);
			
			for(Entry<String, Set<String>> entry : tids.entrySet()) {
				String timestampStr = entry.getKey();
				Set<String> _ids = entry.getValue();
				uriBuilder.clearParameters();
				//_bucket, _sum, _count
				boolean regx = HistogramMetrics.class.isAssignableFrom(model.getStorageModelClazz()) || PercentileMetrics.class.isAssignableFrom(model.getStorageModelClazz());
				String query = "";
				if(regx) {
					query = "{__name__=~\"" + model.getName() + ".*\",id=\"" + StringUtils.join(_ids, "|") +"\"}";
				}else {
					query = "{__name__=\"" + model.getName() + "\",id=\"" + StringUtils.join(_ids, "|") +"\"}";
				}
				
				uriBuilder.addParameter("match[]", query);
				uriBuilder.addParameter("start", timestampStr);
				uriBuilder.addParameter("end", timestampStr);
				
				HttpPost request = new HttpPost(uriBuilder.build());
				
				CloseableHttpResponse response = client.execute(request);
				
				if(response.getStatusLine().getStatusCode() == 204) {
					return true;
				}else {
					String responseStr = EntityUtils.toString(response.getEntity());
					log.error("delete by ids failed:{}", responseStr);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return false;
	}
	
	public PrometheusHttpAPIRespBody query(Model model, List<String> ids) {
		
		PrometheusHttpAPIRespBody body = null;
		
		try {
			URIBuilder uriBuilder = new URIBuilder(prometheusAddress + QUERY);
			
			Map<String/*timestamp形如1606811026.446*/, Set<String>> tids = PrometheusMeterMapper.idAndTimestampTuple2(model, ids);
			
			for(Entry<String, Set<String>> entry : tids.entrySet()) {
				String timestampStr = entry.getKey();
				Set<String> _ids = entry.getValue();
				uriBuilder.clearParameters();
				//_bucket, _sum, _count
				boolean regx = HistogramMetrics.class.isAssignableFrom(model.getStorageModelClazz()) || PercentileMetrics.class.isAssignableFrom(model.getStorageModelClazz());
				String query = "";
				if(regx) {
					query = "{__name__=~\"" + model.getName() + ".*\",id=\"" + StringUtils.join(_ids, "|") +"\"}";
				}else {
					query = "{__name__=\"" + model.getName() + "\",id=\"" + StringUtils.join(_ids, "|") +"\"}";
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
			log.error(e.getMessage(),e);
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
