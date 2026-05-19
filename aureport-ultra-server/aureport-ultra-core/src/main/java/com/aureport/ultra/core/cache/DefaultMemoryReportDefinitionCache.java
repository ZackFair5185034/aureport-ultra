/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.aureport.ultra.core.cache;

import com.aureport.ultra.core.definition.ReportDefinition;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jacky.gao
 * @since 2016年12月4日
 */
public class DefaultMemoryReportDefinitionCache implements ReportDefinitionCache {
    private final Cache<String, ReportDefinition> cache;

    public DefaultMemoryReportDefinitionCache() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(30, java.util.concurrent.TimeUnit.MINUTES)
                .build();
    }

    @Override
    public ReportDefinition getReportDefinition(String file) {
        return cache.getIfPresent(file);
    }

    @Override
    public void cacheReportDefinition(String file, ReportDefinition reportDefinition) {
        cache.put(file, reportDefinition);
    }
}
