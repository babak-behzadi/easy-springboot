package easy.springboot.jpa;

import easy.springboot.jpa.domain.Person;
import easy.springboot.jpa.model.PersonModel;
import easy.springboot.jpa.query.PersonFilterModel;
import easy.springboot.jpa.query.QueryResult;
import easy.springboot.jpa.service.PersonService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Calendar;
import java.util.Optional;

@SpringBootTest(classes = TestConfig.class)
public class PersonServiceTest {

    @Autowired
    PersonService personService;

    @Test
    void save_person() {
        Person person = personService.save(
                new PersonModel(null, "Babak", "Behzadi", Calendar.getInstance().getTime()));
        Assertions.assertNotNull(person);
        Assertions.assertEquals("Babak", person.getFirstName());
        Assertions.assertEquals("Behzadi", person.getLastName());
    }

    @Test
    void search_person() {
        PersonFilterModel filterModel = new PersonFilterModel();
        filterModel.setFirstName("bab");
        filterModel.setPage(0);
        filterModel.setPageSize(1);

        QueryResult<Person> queryResult = personService.query(filterModel);
        Assertions.assertNotNull(queryResult);
        Assertions.assertEquals(1, queryResult.getRows().size());
        Assertions.assertEquals("Babak", queryResult.getRows().get(0).getFirstName());
    }

    @Test
    void find_person() {
        Optional<Person> person = personService.findById(1L);
        Assertions.assertTrue(person.isPresent());
        Assertions.assertEquals("Babak", person.get().getFirstName());
    }
}
