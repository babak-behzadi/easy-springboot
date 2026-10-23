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

package easy.springboot.web.controller;

import easy.springboot.jpa.domain.BaseEntity;
import easy.springboot.jpa.query.FilterModel;
import easy.springboot.jpa.query.QueryResult;
import easy.springboot.jpa.repository.BaseRepository;
import easy.springboot.jpa.service.BaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Optional;

/**
 * Provides generic find and query endpoints for applications using the JPA and Security modules.
 * Consuming applications must enable Spring method security and configure authentication.
 */
public abstract class BaseController<E extends BaseEntity, ID, F extends FilterModel> {

    protected abstract <S extends BaseService<E, ID, BaseRepository<E, ID>>> S getService();

    /**
     * Returns the mapped entity for the supplied ID, or HTTP 404 if it does not exist.
     *
     * @param id ID to look up
     * @return mapped entity response
     */
    @GetMapping(value = "find")
    @PreAuthorize("@authorityRestrictionService.hasAccess(this)")
    public ResponseEntity<Map<String, Object>> find(@RequestParam ID id) {
        Optional<E> optional = getService().findById(id);
        return optional.isPresent() ?
                ResponseEntity.ok(getService().mutate(optional.get())) :
                ResponseEntity.notFound().build();
    }

    /**
     * Searches entities using the supplied filter model.
     *
     * @param filter filter model
     * @return mapped query result
     */
    @PostMapping(value = "query")
    @PreAuthorize("@authorityRestrictionService.hasAccess(this)")
    public ResponseEntity<QueryResult<Map<String, Object>>> query(@RequestBody F filter) {
        return ResponseEntity.ok(getService().mutate(getService().query(filter)));
    }
}
