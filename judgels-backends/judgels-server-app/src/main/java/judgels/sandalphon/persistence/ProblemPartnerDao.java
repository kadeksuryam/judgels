package judgels.sandalphon.persistence;

import java.util.List;
import java.util.Optional;
import judgels.persistence.Dao;

public interface ProblemPartnerDao extends Dao<ProblemPartnerModel> {

    boolean existsByProblemJidAndPartnerJid(String problemJid, String partnerJid);

    Optional<ProblemPartnerModel> selectByProblemJidAndUserJid(String problemJid, String userJid);
    List<ProblemPartnerModel> selectAllByProblemJid(String problemJid);
}
