package com.Mini_ERP.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "module_permissions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "module"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModulePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ErpModule module;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessLevel accessLevel;

    @Column(nullable = false)
    @Builder.Default
    private boolean canRead = false;
}