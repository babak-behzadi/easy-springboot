package easy.springboot.jpa.model;

import java.util.Date;

public record PersonModel(Long id, String firstName, String lastName, Date birthDate) {
}
