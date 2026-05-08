package com.example.api.Repository;

import com.example.api.Entity.JetsonDeviceState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JetsonDeviceStateRepository extends JpaRepository<JetsonDeviceState, String> {
}
