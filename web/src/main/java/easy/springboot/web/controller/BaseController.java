package easy.springboot.web.controller;

import easy.springboot.jpa.domain.BaseEntity;
import easy.springboot.jpa.query.FilterModel;
import easy.springboot.jpa.query.QueryResult;
import easy.springboot.jpa.service.BaseService;
import easy.springboot.web.mutation.EntityMutator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Optional;

public class BaseController<S extends BaseService, F extends FilterModel> {

    private final S service;

    public BaseController(S service) {
        this.service = service;
    }

    @GetMapping(value = "find")
    public <E extends BaseEntity, ID> ResponseEntity<Map<String, Object>> find(@RequestParam ID id) {
        Optional<E> optional = service.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new EntityMutator<E>().apply(optional.get()));
    }

    @PostMapping(value = "query")
    public <R> ResponseEntity<QueryResult<R>> query(@RequestBody F filter) {
        return ResponseEntity.ok(service.query(filter));
    }
}
