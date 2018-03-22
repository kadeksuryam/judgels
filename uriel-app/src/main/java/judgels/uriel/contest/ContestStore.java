package judgels.uriel.contest;

import com.google.common.collect.Lists;
import java.util.Optional;
import javax.inject.Inject;
import judgels.persistence.JudgelsDao;
import judgels.persistence.api.Page;
import judgels.uriel.api.contest.Contest;
import judgels.uriel.api.contest.ContestData;
import judgels.uriel.api.contest.ContestStyle;
import judgels.uriel.persistence.ContestModel;

public class ContestStore {
    private final JudgelsDao<ContestModel> contestDao;
    private final ContestRawDao contestRawDao;

    @Inject
    public ContestStore(JudgelsDao<ContestModel> contestDao, ContestRawDao contestRawDao) {
        this.contestDao = contestDao;
        this.contestRawDao = contestRawDao;
    }

    public Optional<Contest> findContestByJid(String contestJid) {
        return contestDao.selectByJid(contestJid).map(ContestStore::fromModel);
    }

    public Page<Contest> getContests(String userJid, int page, int pageSize) {
        Page<ContestModel> models = contestRawDao.selectAllByUserJid(userJid, page, pageSize);
        return models.mapData(data -> Lists.transform(data, ContestStore::fromModel));
    }

    public Contest createContest(ContestData contestData) {
        ContestModel model = new ContestModel();
        toModel(contestData, model);
        return fromModel(contestDao.insert(model));
    }

    private static Contest fromModel(ContestModel model) {
        return new Contest.Builder()
                .jid(model.jid)
                .name(model.name)
                .description(model.description)
                .style(ContestStyle.valueOf(model.style))
                .build();
    }

    private static void toModel(ContestData data, ContestModel model) {
        model.name = data.getName();
        model.description = data.getDescription();
        model.style = data.getStyle().name();
    }
}
