package es.unizar.tmdad.repository;

import es.unizar.tmdad.repository.entity.MessageTimestampEntity;
import org.springframework.data.repository.CrudRepository;

public interface MessageTimestampRepository extends CrudRepository<MessageTimestampEntity, MessageTimestampEntity.MessageTimestampCompositeKey> {
}
