package com.ms.test_api.reponsitory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ms.test_api.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>{

}