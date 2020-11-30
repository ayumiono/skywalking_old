package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PrometheusMetricsMapper {

	/**
	 * skywalking metrics type
	 * @return
	 */
	Class<?> value();
	
}
