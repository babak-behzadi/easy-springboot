package easy.springboot.jpa.query;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

    public QueryResult<R> mutate(Consumer<R> consumer) {
        this.rows.forEach(consumer);
        return this;
    }
}
