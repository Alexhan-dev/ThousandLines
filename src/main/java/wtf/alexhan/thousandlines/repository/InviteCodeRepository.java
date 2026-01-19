package wtf.alexhan.thousandlines.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import wtf.alexhan.thousandlines.model.InviteCode;

import java.util.Optional;

public interface InviteCodeRepository extends JpaRepository<InviteCode, String> {
    Optional<InviteCode> findByCode(String code);

}
