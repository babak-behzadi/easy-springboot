package easy.springboot.jpa.service;

import easy.springboot.jpa.domain.BaseEntity;
import easy.springboot.jpa.mutation.EntityMutator;
import easy.springboot.jpa.query.FilterModel;
import easy.springboot.jpa.query.QueryResult;
import easy.springboot.jpa.repository.BaseRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class BaseService<E extends BaseEntity, ID, R extends BaseRepository<E, ID>> {

    private final R repository;

    protected R getRepository() {
        return repository;
    }

    @Bean
    protected EntityMutator getMutator() {
        return new EntityMutator();
    }

    protected BaseService(R repository) {
        this.repository = repository;
    }

    /**
     * queries database based on filterModel
     * returns {@link QueryResult} object containing result page and total count
     *
     * @param filterModel
     * @param <F>
     * @return {@link QueryResult<E>}
     */
    public <F extends FilterModel> QueryResult<E> query(F filterModel) {
        int p = Math.max(filterModel.getPage(), 0);
        int ps = Math.max(1, filterModel.getPageSize());
        ps = Math.min(ps, 10);
        Page<E> page = repository.findAll(filterModel.toSpecification(), PageRequest.of(p, ps));
        long total = repository.count(filterModel.toSpecification());
        return new QueryResult<>(page, total);
    }

    /**
     * queries database based on filterModel
     * returns {@link QueryResult} object containing all result and total count
     *
     * @param filterModel
     * @param <F>
     * @return {@link QueryResult<E>}
     */
    public <F extends FilterModel> QueryResult<E> queryAll(F filterModel) {
        List<E> list = repository.findAll(filterModel.toSpecification());
        long total = repository.count(filterModel.toSpecification());
        return new QueryResult<>(list, total);
    }

    /**
     * finds entity object by id
     *
     * @param id
     * @return {@link Optional<E>}
     */
    public Optional<E> findById(ID id) {
        return repository.findById(id);
    }

    /**
     * saves entity object
     *
     * @param entity
     * @return <E>
     */
    public E save(E entity) {
        return repository.save(entity);
    }

    /**
     * deletes entity object
     *
     * @param entity
     */
    public void delete(E entity) {
        repository.delete(entity);
    }

    public Map<String, Object> mutate(E e) {
        return getMutator().mutate(e, null);
    }

    public QueryResult<Map<String, Object>> mutate(QueryResult<E> queryResult) {
        return queryResult.mutate(e -> getMutator().mutate(e, null));
    }
}
