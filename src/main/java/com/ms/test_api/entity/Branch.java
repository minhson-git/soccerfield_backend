package com.ms.test_api.entity;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "branches")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 100)
    String name;

    @Column(nullable = false, length = 255)
    String address;

    @Column(length = 50)
    String district;

    @Column(length = 20)
    String phone;

    @Column(name = "opening_time")
    LocalTime openingTime;

    @Column(name = "closing_time")
    LocalTime closingTime;

    @OneToMany(mappedBy = "branch")
    List<Field> fields = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    User owner;

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    List<PricingRule> pricingRules = new ArrayList<>();
}
