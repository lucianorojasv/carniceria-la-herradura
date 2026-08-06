package pe.laherradura.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.laherradura.entity.StoreLocation;

import java.util.List;
import java.util.Optional;

public interface StoreLocationRepository extends JpaRepository<StoreLocation, Long> {
    @EntityGraph(attributePaths = "images")
    List<StoreLocation> findAllByOrderByMainDescNameAsc();

    @EntityGraph(attributePaths = "images")
    List<StoreLocation> findByActiveTrueOrderByMainDescNameAsc();

    @EntityGraph(attributePaths = "images")
    Optional<StoreLocation> findFirstByMainTrueAndActiveTrue();
}
