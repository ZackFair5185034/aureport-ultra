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
package com.aureport.ultra.core.definition.datasource;

import com.aureport.ultra.core.build.Dataset;
import com.aureport.ultra.core.definition.dataset.DatasetDefinition;
import com.aureport.ultra.core.definition.dataset.SqlDatasetDefinition;
import com.aureport.ultra.core.exception.ReportComputeException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jacky.gao
 * @since 2017年2月9日
 */
public class BuildinDatasourceDefinition implements DatasourceDefinition {
    private String name;
    private List<DatasetDefinition> datasets;

    public List<Dataset> buildDatasets(Connection conn, Map<String, Object> parameters, int queryTimeout) {
        if (datasets == null || datasets.size() == 0) {
            return null;
        }
        boolean callerSuppliedConnection = (conn != null);
        List<Dataset> list = new ArrayList<Dataset>();
        try {
            for (DatasetDefinition dsDef : datasets) {
                SqlDatasetDefinition sqlDataset = (SqlDatasetDefinition) dsDef;
                sqlDataset.setQueryTimeout(queryTimeout);
                Dataset ds = sqlDataset.buildDataset(parameters, conn);
                list.add(ds);
            }
            return list;
        } finally {
            if (!callerSuppliedConnection && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // ignore close error
                }
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<DatasetDefinition> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<DatasetDefinition> datasets) {
        this.datasets = datasets;
    }

    @Override
    public DatasourceType getType() {
        return DatasourceType.buildin;
    }
}
