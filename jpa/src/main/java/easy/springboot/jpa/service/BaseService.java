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

package easy.springboot.jpa.service;

import easy.springboot.jpa.domain.BaseEntity;
import easy.springboot.jpa.mutation.EntityMutator;
import easy.springboot.jpa.query.FilterModel;
import easy.springboot.jpa.query.QueryResult;
import easy.springboot.jpa.query.QueryResultField;
import easy.springboot.jpa.repository.BaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class BaseService<E extends BaseEntity, ID, R extends BaseRepository<E, ID>> {

    private final R repository;
    private EntityMutator entityMutator;

    protected R getRepository() {
        return repository;
    }

    @Autowired
    public void setEntityMutator(EntityMutator entityMutator) {
        this.entityMutator = entityMutator;
    }

    protected BaseService(R repository) {
        this.repository = repository;
    }

    /**
     * queries database based on filterModel
     * returns {@link QueryResult} object containing result page and total count
     *
     * @param filterModel filter model object
     * @param <F> the class extends filter model
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
     * @param filterModel filter model object
     * @param <F> the class extends filter model
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
     * @param id id
     * @return {@link Optional<E>}
     */
    public Optional<E> findById(ID id) {
        return repository.findById(id);
    }

    /**
     * saves entity object
     *
     * @param entity entity object to be saved
     * @return <E>
     */
    public E save(E entity) {
        return repository.save(entity);
    }

    /**
     * deletes entity object
     *
     * @param entity enty object to be deleted
     */
    public void delete(E entity) {
        repository.delete(entity);
    }

    /**
     * changes entity object to a {@link Map} object, regarding fields annotated by {@link QueryResultField}
     *
     * @param entity Entity object
     * @return {@link Map}
     */
    public Map<String, Object> mutate(E entity) {
        return entityMutator.mutate(entity, null);
    }

    /**
     * changes all query result rows to {@link Map<String, Object>} object, regarding fields annotated by {@link QueryResultField}
     *
     * @param queryResult query result object to mutate
     * @return {@link QueryResult<Map>}
     */
    public QueryResult<Map<String, Object>> mutate(QueryResult<E> queryResult) {
        return queryResult.mutate(e -> entityMutator.mutate(e, null));
    }
}
