package wtf.alexhan.thousandlines.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wtf.alexhan.thousandlines.model.UsedInviteCode;

public interface UsedInviteCodeRepository extends JpaRepository<UsedInviteCode, Long> {
}
