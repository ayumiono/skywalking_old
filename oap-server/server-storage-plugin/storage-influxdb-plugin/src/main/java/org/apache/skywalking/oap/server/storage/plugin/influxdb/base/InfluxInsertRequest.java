/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.oap.server.storage.plugin.influxdb.base;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.skywalking.oap.server.core.analysis.metrics.DataTable;
import org.apache.skywalking.oap.server.core.analysis.metrics.HistogramMetrics;
import org.apache.skywalking.oap.server.core.analysis.metrics.PercentileMetrics;
import org.apache.skywalking.oap.server.core.storage.StorageBuilder;
import org.apache.skywalking.oap.server.core.storage.StorageData;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.core.storage.model.ModelColumn;
import org.apache.skywalking.oap.server.core.storage.type.StorageDataComplexObject;
import org.apache.skywalking.oap.server.library.client.request.InsertRequest;
import org.apache.skywalking.oap.server.library.client.request.UpdateRequest;
import org.apache.skywalking.oap.server.storage.plugin.influxdb.InfluxConstants;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import com.google.common.collect.Maps;

/**
 * InfluxDB Point wrapper.
 */
public class InfluxInsertRequest implements InsertRequest, UpdateRequest {
    private BatchPoints.Builder builder;
    private Map<String, Object> fields = Maps.newHashMap();
    private List<Point.Builder> pointBuilders;
    
    private static final int[] RANKS = {
        50,
        75,
        90,
        95,
        99
    };

    public InfluxInsertRequest(Model model, StorageData storageData, StorageBuilder storageBuilder) {
        Map<String, Object> objectMap = storageBuilder.data2Map(storageData);
        pointBuilders = new ArrayList<Point.Builder>();
        for (ModelColumn column : model.getColumns()) {
            Object value = objectMap.get(column.getColumnName().getName());

            if (value instanceof StorageDataComplexObject) {
                fields.put(
                    column.getColumnName().getStorageName(),
                    ((StorageDataComplexObject) value).toStorageData()
                );
            } else {
                fields.put(column.getColumnName().getStorageName(), value);
            }
        }
        
        if(storageData instanceof PercentileMetrics) {
        	PercentileMetrics _storageData = (PercentileMetrics) storageData;
        	DataTable percentileVaules = _storageData.getPercentileValues();
        	/*DataTable dataset = _storageData.getDataset(); PercentileMetrics中的dataset没有HistogramMetrics这样有maxNumOfSteps和step，导致bucket太离散不适合做tag
        	List<String> sortedKeys = dataset.sortedKeys(Comparator.comparingInt(Integer::parseInt));
        	sortedKeys.forEach(key->{
        		Long value = dataset.get(key);
        		Point.Builder pb = Point.measurement(model.getName() + "_bucket")
                .addField(InfluxConstants.ID_COLUMN, storageData.id())
                .addField("value", value)
                .tag("_le", key);
        		pointBuilders.add(pb);
        	});*/
        	
        	List<String> sortedKeys = percentileVaules.sortedKeys(Comparator.comparingInt(Integer::parseInt));
        	sortedKeys.forEach(key->{
        		Long value = percentileVaules.get(key);
        		Point.Builder pb = Point.measurement(model.getName() + "_quantile")
                .addField(InfluxConstants.ID_COLUMN, storageData.id())
                .addField("value", value)
                .tag("quantile", "p" + RANKS[Integer.parseInt(key)]);
        		pointBuilders.add(pb);
        	});
        } else if(storageData instanceof HistogramMetrics) {
        	HistogramMetrics _storageData = (HistogramMetrics) storageData;
        	DataTable dataset = _storageData.getDataset();
        	final List<String> sortedKeys = dataset.sortedKeys(Comparator.comparingInt(Integer::parseInt));
        	sortedKeys.forEach(key->{
        		Long value = dataset.get(key);
        		Point.Builder pb = Point.measurement(model.getName() + "_bucket")
                .addField(InfluxConstants.ID_COLUMN, storageData.id())
                .addField("value", value)
                .tag("le", key);
        		pointBuilders.add(pb);
        	});
        }
        Point.Builder pb = Point.measurement(model.getName())
                .addField(InfluxConstants.ID_COLUMN, storageData.id())
                .fields(fields);
    	pointBuilders.add(pb);
        builder = BatchPoints.builder();
    }

    public InfluxInsertRequest time(long time, TimeUnit unit) {
    	pointBuilders.forEach(pb->pb.time(time, unit));
        return this;
    }

    public InfluxInsertRequest addFieldAsTag(String fieldName, String tagName) {
        builder.tag(tagName, String.valueOf(fields.get(fieldName)));
        return this;
    }

    public BatchPoints getPoint() {
    	pointBuilders.forEach(pb->{
    		builder.point(pb.build());
    	});
        return builder.build();
    }
}