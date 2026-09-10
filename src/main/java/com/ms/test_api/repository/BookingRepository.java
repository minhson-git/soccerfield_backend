package com.ms.test_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.ms.test_api.entity.Booking;


public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking>{

}
