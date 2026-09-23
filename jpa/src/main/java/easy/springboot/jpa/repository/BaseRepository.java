package easy.springboot.jpa.repository;

import easy.springboot.jpa.domain.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BaseRepository<E extends BaseEntity<ID>, ID> extends JpaRepository<E, ID>, JpaSpecificationExecutor<E> {
}
