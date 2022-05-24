package es.unizar.tmdad.service.impl;

import es.unizar.tmdad.repository.RoomRepository;
import es.unizar.tmdad.repository.entity.RoomEntity;
import es.unizar.tmdad.repository.entity.UserEntity;
import es.unizar.tmdad.service.RoomService;
import es.unizar.tmdad.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final UserService userService;

    public RoomServiceImpl(RoomRepository roomRepository,UserService userService){
        this.roomRepository = roomRepository;
        this.userService = userService;
    }

    @Override
    public boolean isUserInTheRoom(String userId, Long roomId) {
        RoomEntity room = this.roomRepository.findById(roomId).orElseThrow();
        UserEntity user = this.userService.getUser(userId);
        return room.getUsers().contains(user);
    }
}
