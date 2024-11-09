package com.ms.test_api.reponsitory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ms.test_api.modal.Booking;


@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>{

    Page<Booking> findByUser_UserId(int user_UserId, Pageable pageable);

}
