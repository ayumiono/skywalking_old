package org.apache.skywalking.oap.server.storage.plugin.prometheus.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.skywalking.oap.server.core.storage.model.Model;
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
	
	private static final String RANGE = "/api/v1/query_range";
	
	public PrometheusHttpApi(String prometheusAddress) {
		this.prometheusAddress = prometheusAddress;
		client = HttpClientBuilder.create().build();//FIXME
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
	
	public PrometheusHttpAPIRespBody rangeQuery(String name, Map<String, String> labels, long start, long end) {
		try {
			URIBuilder uriBuilder = new URIBuilder(prometheusAddress+RANGE);
			
			StringJoiner queryBuilder = new StringJoiner(",", name+"{", "}");
			
			for(Entry<String, String> label : labels.entrySet()) {
				queryBuilder.add(label.getKey()+"="+label.getValue());
			}
			
			uriBuilder.addParameter("query", queryBuilder.toString());
			uriBuilder.addParameter("start", start+"");
			uriBuilder.addParameter("end", end+"");
			
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
	
	public PrometheusHttpAPIRespBody rangeQuery(Model model, List<String> ids, long start, long end) {
		try {
			URIBuilder uriBuilder = new URIBuilder(prometheusAddress+RANGE);
			
			String query = "{__name__=~\"" + model.getName() + ".*\",id=~\"" + StringUtils.join(ids, "|") +"\"}";
			
			
			uriBuilder.addParameter("query", query);
			uriBuilder.addParameter("start", start+"");
			uriBuilder.addParameter("end", end+"");
			
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
