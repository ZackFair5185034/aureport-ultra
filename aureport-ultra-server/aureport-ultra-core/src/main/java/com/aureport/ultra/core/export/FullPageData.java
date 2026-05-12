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

import com.aureport.ultra.core.build.paging.Page;

import java.util.List;

/**
 * @author Jacky.gao
 * @since 2017年3月23日
 */
public class FullPageData {
    private int totalPages;
    private int columnMargin;
    private List<List<Page>> pageList;

    public FullPageData(int totalPages, int columnMargin, List<List<Page>> pageList) {
        this.totalPages = totalPages;
        this.columnMargin = columnMargin;
        this.pageList = pageList;
    }

    public int getColumnMargin() {
        return columnMargin;
    }

    public List<List<Page>> getPageList() {
        return pageList;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
