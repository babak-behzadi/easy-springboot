package easy.springboot.web.test.controller;

import easy.springboot.security.restriction.BaseEndpoint;
import easy.springboot.security.restriction.BaseEndpointsAuthorityRestriction;
import easy.springboot.web.controller.BaseController;
import easy.springboot.web.test.domain.Person;
import easy.springboot.web.test.model.PersonModel;
import easy.springboot.web.test.query.PersonFilterModel;
import easy.springboot.web.test.service.PersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("person")
@BaseEndpointsAuthorityRestriction({
        @BaseEndpoint(value = "find", authorities = {"USER"}),
        @BaseEndpoint(value = "query", authorities = {"USER"})
})
public class PersonController extends BaseController<Person, Long, PersonFilterModel> {

    private final PersonService service;

    public PersonController(PersonService service) {
        this.service = service;
    }

    @Override
    protected PersonService getService() {
        return this.service;
    }

    @PostMapping("save")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<Person> save(@RequestBody PersonModel personModel) {
        return ResponseEntity.ok(getService().save(personModel));
    }
}
