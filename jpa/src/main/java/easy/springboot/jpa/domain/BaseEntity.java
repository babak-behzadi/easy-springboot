package easy.springboot.jpa.domain;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@MappedSuperclass
public class BaseEntity<ID> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private ID id;

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BaseEntity<?> that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
