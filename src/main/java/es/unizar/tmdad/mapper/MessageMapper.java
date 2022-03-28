package es.unizar.tmdad.mapper;

import es.unizar.tmdad.adt.Message;
import es.unizar.tmdad.adt.MessageIn;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    Message mapMessage(MessageIn msg);

}
