package easy.springboot.jpa.service;

import easy.springboot.jpa.domain.BaseEntity;
import easy.springboot.jpa.repository.BaseRepository;

public abstract class BaseService<E extends BaseEntity<ID>, ID, R extends BaseRepository<E, ID>> {

    private final R repository;

    public R getRepository() {
        return repository;
    }

    protected BaseService(R repository) {
        this.repository = repository;
    }
}
