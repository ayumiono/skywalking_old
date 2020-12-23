package org.apache.skywalking.oap.server.analyzer.provider.trace.parser;

import java.util.List;

import org.apache.skywalking.oap.server.core.source.Endpoint;
import org.apache.skywalking.oap.server.library.module.Service;

/**
 * 端点分组服务
 * @author Administrator
 */
public interface IGroupParserService extends Service {
    public List<String> group(Endpoint endpoint);
}