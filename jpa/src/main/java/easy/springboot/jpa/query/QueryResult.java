/*
 * Copyright 2026 Babak Behzadi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package easy.springboot.jpa.query;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QueryResult<R> {

    private final int page;
    private final int pageSize;
    private final long total;
    private final List<R> rows;

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotal() {
        return total;
    }

    public List<R> getRows() {
        return rows;
    }

    public QueryResult(Page<R> page, long total) {
        this.page = page.getNumber();
        this.pageSize = page.getSize();
        this.total = total;
        this.rows = page.getContent().stream().toList();
    }

    public QueryResult(List<R> rows, int page, int pageSize, long total) {
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.rows = rows;
    }

    public QueryResult(List<R> rows, long total) {
        this(rows, 0, Integer.MAX_VALUE, total);
    }

    /**
     * mutate query result list objects
     *
     * @param mutator mutation function
     * @param <M>     input and output class
     * @return {@link QueryResult<M>}
     */
    public <M> QueryResult<M> mutate(Function<R, M> mutator) {
        return new QueryResult<>(
                this.rows.stream().map(mutator).collect(Collectors.toList()),
                this.page,
                this.pageSize,
                this.total);
    }
}
