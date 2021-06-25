package judgels.jerahmeel.problem;

import com.google.common.collect.Lists;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import judgels.jerahmeel.api.problem.ProblemSetProblemInfo;
import judgels.jerahmeel.persistence.ProblemLevelDao;
import judgels.jerahmeel.persistence.ProblemSetDao;
import judgels.jerahmeel.persistence.ProblemSetModel;
import judgels.jerahmeel.persistence.ProblemSetProblemDao;
import judgels.jerahmeel.persistence.ProblemSetProblemModel;
import judgels.persistence.api.Page;
import judgels.persistence.api.SelectionOptions;

public class ProblemStore {
    private final ProblemSetDao problemSetDao;
    private final ProblemSetProblemDao problemDao;
    private final ProblemLevelDao problemLevelDao;

    @Inject
    public ProblemStore(
            ProblemSetDao problemSetDao,
            ProblemSetProblemDao problemDao,
            ProblemLevelDao problemLevelDao) {

        this.problemSetDao = problemSetDao;
        this.problemDao = problemDao;
        this.problemLevelDao = problemLevelDao;
    }

    public Page<ProblemSetProblemInfo> getProblems(Set<String> allowedProblemJids, Optional<Integer> page) {
        SelectionOptions.Builder selectionOptions = new SelectionOptions.Builder()
                .from(SelectionOptions.DEFAULT_PAGED);
        page.ifPresent(selectionOptions::page);

        Page<ProblemSetProblemModel> models =
                problemDao.selectPagedByDifficulty(allowedProblemJids, selectionOptions.build());
        Map<String, ProblemSetModel> problemSetsMap = problemSetDao.selectByJids(models.getPage()
                .stream()
                .map(m -> m.problemSetJid)
                .collect(Collectors.toSet()));
        Map<String, Integer> problemLevelsMap = problemLevelDao.selectAllAverageByProblemJids(models.getPage()
                .stream()
                .map(m -> m.problemJid)
                .collect(Collectors.toSet()));
        return models.mapPage(p -> Lists.transform(p, m -> new ProblemSetProblemInfo.Builder()
                .problemSetSlug(problemSetsMap.get(m.problemSetJid).slug)
                .problemAlias(m.alias)
                .problemJid(m.problemJid)
                .problemLevel(Optional.ofNullable(problemLevelsMap.get(m.problemJid)))
                .build()));
    }
}
