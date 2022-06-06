package es.unizar.tmdad.repository.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "users")
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "superuser")
    private Boolean superuser;

    @ManyToMany(mappedBy = "users")
    @ToString.Exclude
    private Set<RoomEntity> rooms = new HashSet<>();
}
