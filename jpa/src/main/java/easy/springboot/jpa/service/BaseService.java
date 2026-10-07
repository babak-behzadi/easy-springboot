package easy.springboot.jpa.service;

import easy.springboot.jpa.domain.BaseEntity;
import easy.springboot.jpa.query.FilterModel;
import easy.springboot.jpa.query.QueryResult;
import easy.springboot.jpa.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public abstract class BaseService<E extends BaseEntity, ID, R extends BaseRepository<E, ID>> {

    private final R repository;

    public R getRepository() {
        return repository;
    }

    protected BaseService(R repository) {
        this.repository = repository;
    }

    public <F extends FilterModel> QueryResult<E> query(F filterModel) {
        Page<E> page = repository.findAll(filterModel.toSpecification(),
                PageRequest.of(filterModel.getPage(), filterModel.getPageSize() <= 0 ? 10 : filterModel.getPageSize()));
        long total = repository.count(filterModel.toSpecification());
        return new QueryResult<>(page, total);
    }

    public E create(E entity) {
        return repository.save(entity);
    }

    public E update(E entity) {
        return repository.save(entity);
    }

    public void delete(E entity) {
        repository.delete(entity);
    }
}
