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
package com.aureport.ultra.core.export;

import com.aureport.ultra.core.build.ReportBuilder;
import com.aureport.ultra.core.cache.CacheUtils;
import com.aureport.ultra.core.definition.CellDefinition;
import com.aureport.ultra.core.definition.Expand;
import com.aureport.ultra.core.definition.ReportDefinition;
import com.aureport.ultra.core.exception.ReportException;
import com.aureport.ultra.core.exception.ReportParseException;
import com.aureport.ultra.core.export.builder.down.DownCellbuilder;
import com.aureport.ultra.core.export.builder.right.RightCellbuilder;
import com.aureport.ultra.core.model.Report;
import com.aureport.ultra.core.parser.ReportParser;
import com.aureport.ultra.core.provider.report.ReportProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jacky.gao
 * @since 2016年12月4日
 */
public class ReportRender implements ApplicationContextAware {
    private ReportParser reportParser;
    private ReportBuilder reportBuilder;
    private Collection<ReportProvider> reportProviders;
    private DownCellbuilder downCellParentbuilder = new DownCellbuilder();
    private RightCellbuilder rightCellParentbuilder = new RightCellbuilder();

    public Report render(String file, Map<String, Object> parameters) {
        ReportDefinition reportDefinition = getReportDefinition(file);
        return reportBuilder.buildReport(reportDefinition, parameters);
    }

    public Report render(ReportDefinition reportDefinition, Map<String, Object> parameters) {
        return reportBuilder.buildReport(reportDefinition, parameters);
    }

    public ReportDefinition getReportDefinition(String file) {
        ReportDefinition reportDefinition = CacheUtils.getReportDefinition(file);
        if (reportDefinition == null) {
            reportDefinition = parseReport(file);
            rebuildReportDefinition(reportDefinition);
            CacheUtils.cacheReportDefinition(file, reportDefinition);
        }
        return reportDefinition;
    }

    public void rebuildReportDefinition(ReportDefinition reportDefinition) {
        List<CellDefinition> cells = reportDefinition.getCells();
        for (CellDefinition cell : cells) {
            addRowChildCell(cell, cell, new HashSet<>());
            addColumnChildCell(cell, cell, new HashSet<>());
        }
        for (CellDefinition cell : cells) {
            Expand expand = cell.getExpand();
            if (expand.equals(Expand.Down)) {
                downCellParentbuilder.buildParentCell(cell, cells);
            } else if (expand.equals(Expand.Right)) {
                rightCellParentbuilder.buildParentCell(cell, cells);
            }
        }
    }

    public ReportDefinition parseReport(String file) {
        InputStream inputStream = null;
        try {
            inputStream = buildReportFile(file);
            return reportParser.parse(inputStream, file);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                throw new ReportParseException(e);
            }
        }
    }

    private InputStream buildReportFile(String file) {
        InputStream inputStream = null;
        for (ReportProvider provider : reportProviders) {
            if (file.startsWith(provider.getPrefix())) {
                inputStream = provider.loadReport(file);
            }
        }
        if (inputStream == null) {
            throw new ReportException("Report [" + file + "] not support.");
        }
        return inputStream;
    }

    private void addRowChildCell(CellDefinition cell, CellDefinition childCell, Set<CellDefinition> visited) {
        CellDefinition leftCell = cell.getLeftParentCell();
        if (leftCell == null) {
            return;
        }
        if (!visited.add(leftCell)) {
            return;
        }
        List<CellDefinition> childrenCells = leftCell.getRowChildrenCells();
        childrenCells.add(childCell);
        addRowChildCell(leftCell, childCell, visited);
    }

    private void addColumnChildCell(CellDefinition cell, CellDefinition childCell, Set<CellDefinition> visited) {
        CellDefinition topCell = cell.getTopParentCell();
        if (topCell == null) {
            return;
        }
        if (!visited.add(topCell)) {
            return;
        }
        List<CellDefinition> childrenCells = topCell.getColumnChildrenCells();
        childrenCells.add(childCell);
        addColumnChildCell(topCell, childCell, visited);
    }

    public void setReportParser(ReportParser reportParser) {
        this.reportParser = reportParser;
    }

    public void setReportBuilder(ReportBuilder reportBuilder) {
        this.reportBuilder = reportBuilder;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        reportProviders = applicationContext.getBeansOfType(ReportProvider.class).values();
    }
}
