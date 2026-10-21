package easy.springboot.web.controller;

import easy.springboot.jpa.domain.BaseEntity;
import easy.springboot.jpa.query.FilterModel;
import easy.springboot.jpa.query.QueryResult;
import easy.springboot.jpa.repository.BaseRepository;
import easy.springboot.jpa.service.BaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Optional;

public abstract class BaseController<E extends BaseEntity, ID, F extends FilterModel> {

    protected abstract <S extends BaseService<E, ID, BaseRepository<E, ID>>> S getService();

    @GetMapping(value = "find")
    public ResponseEntity<Map<String, Object>> find(@RequestParam ID id) {
        Optional<E> optional = getService().findById(id);
        return optional.isPresent() ?
                ResponseEntity.ok(getService().mutate(optional.get())) :
                ResponseEntity.notFound().build();
    }

    @PostMapping(value = "query")
    public ResponseEntity<QueryResult<Map<String, Object>>> query(@RequestBody F filter) {
        return ResponseEntity.ok(getService().mutate(getService().query(filter)));
    }
}
