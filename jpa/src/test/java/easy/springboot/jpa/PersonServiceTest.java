package easy.springboot.jpa;

import easy.springboot.jpa.domain.Person;
import easy.springboot.jpa.model.PersonModel;
import easy.springboot.jpa.service.PersonService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Calendar;

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
    }
}
