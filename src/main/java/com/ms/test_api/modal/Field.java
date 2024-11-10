package com.ms.test_api.modal;

import java.util.List;

import jakarta.persistence.CascadeType;
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
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "field")
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "field_id")
    private int fieldId;

    @Column(name = "field_type")
    private String fieldType;
    
    @Column(name = "price_per_hour")
    private double pricePerHour;
    
    private boolean status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    public Field(int fieldId, String fieldType, double pricePerHour, boolean status, Branch branch) {
        this.fieldId = fieldId;
        this.fieldType = fieldType;
        this.pricePerHour = pricePerHour;
        this.status = status;
        this.branch = branch;
    }

    @OneToMany(mappedBy = "field", cascade = CascadeType.MERGE)
    private List<Booking> bookings;

    
}
