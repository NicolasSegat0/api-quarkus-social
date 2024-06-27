package io.github.NicolasSegat0.quarkussocial.domain.model;


import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "USERS")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "age")
    private Integer age;


}
