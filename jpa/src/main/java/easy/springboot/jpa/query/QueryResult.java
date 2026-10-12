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

    @SuppressWarnings("unchecked")
    public QueryResult(Page<?> page, long total) {
        this.page = page.getNumber();
        this.pageSize = page.getSize();
        this.total = total;
        this.rows = (List<R>) page.getContent().stream().toList();
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

    public <M> QueryResult<M> mutate(Function<R, M> mutator) {
        return new QueryResult<>(
                this.rows.stream().map(mutator).collect(Collectors.toList()),
                this.page,
                this.pageSize,
                this.total);
    }
}
