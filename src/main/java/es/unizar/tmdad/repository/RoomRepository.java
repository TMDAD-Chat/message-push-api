package es.unizar.tmdad.repository;

import es.unizar.tmdad.repository.entity.RoomEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends CrudRepository<RoomEntity,Long> {
}
