package easy.springboot.jpa.test.model;

import java.util.Date;

public record PersonModel(Long id, String firstName, String lastName, Date birthDate) {
}
