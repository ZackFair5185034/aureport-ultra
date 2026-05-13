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
package com.aureport.ultra.core.provider.report.classpath;

import com.aureport.ultra.core.exception.ReportException;
import com.aureport.ultra.core.provider.report.ReportFile;
import com.aureport.ultra.core.provider.report.ReportProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Jacky.gao
 * @since 2016年12月4日
 */
public class ClasspathReportProvider implements ReportProvider, ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public InputStream loadReport(String file) {
        String resourcePath = file;
        if (file.startsWith("classpath*:")) {
            resourcePath = file.substring(11);
        } else if (file.startsWith("classpath:")) {
            resourcePath = file.substring(10);
        }

        InputStream inputStream = loadFromClassLoader(resourcePath);
        if (inputStream != null) {
            return inputStream;
        }

        // fallback: try Spring's resource resolution
        Resource resource = applicationContext.getResource(file);
        try {
            return resource.getInputStream();
        } catch (IOException e) {
            // try classpath*: prefix as fallback
            if (file.startsWith("classpath:")) {
                String newFileName = "classpath*:" + file.substring(10);
                try {
                    Resource fallbackResource = applicationContext.getResource(newFileName);
                    return fallbackResource.getInputStream();
                } catch (IOException ex) {
                    throw new ReportException("加载报表文件 " + file + " 失败");
                }
            }
            throw new ReportException("加载报表文件 " + file + " 失败: " + e.getMessage());
        }
    }

    private InputStream loadFromClassLoader(String resourcePath) {
        // Remove leading slash if present
        if (resourcePath.startsWith("/")) {
            resourcePath = resourcePath.substring(1);
        }
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        }
        if (is == null) {
            is = ClassLoader.getSystemResourceAsStream(resourcePath);
        }
        return is;
    }

    @Override
    public String getPrefix() {
        return "classpath:";
    }

    @Override
    public void deleteReport(String file) {
    }

    @Override
    public void saveReport(String file, String content) {
    }

    @Override
    public List<ReportFile> getReportFiles() {
        return null;
    }

    @Override
    public boolean disabled() {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
