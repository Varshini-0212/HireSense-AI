package com.hiresense.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "skills")
public class Skill extends BaseEntity {
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "category", nullable = false, length = 40)
    private String category;

    @Column(name = "description", length = 255)
    private String description;
}
