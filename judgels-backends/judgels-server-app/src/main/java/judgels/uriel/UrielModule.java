package judgels.uriel;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.inject.Singleton;
import judgels.jophiel.api.session.Session;
import judgels.jophiel.session.SessionStore;
import judgels.sandalphon.submission.bundle.BaseItemSubmissionStore;
import judgels.sandalphon.submission.bundle.ItemSubmissionStore;
import judgels.sandalphon.submission.programming.BaseSubmissionStore;
import judgels.sandalphon.submission.programming.SubmissionStore;
import judgels.service.actor.ActorChecker;
import judgels.uriel.persistence.ContestBundleItemSubmissionDao;
import judgels.uriel.persistence.ContestProgrammingGradingDao;
import judgels.uriel.persistence.ContestProgrammingSubmissionDao;

@Module
public class UrielModule {
    private final UrielConfiguration config;

    public UrielModule(UrielConfiguration config) {
        this.config = config;
    }

    @Provides
    @UrielBaseDataDir
    Path urielBaseDataDir() {
        return Paths.get(config.getBaseDataDir());
    }

    @Provides
    @Singleton
    static ActorChecker actorChecker(SessionStore sessionStore) {
        return new ActorChecker(authHeader ->
                sessionStore.getSessionByToken(authHeader.getBearerToken()).map(Session::getUserJid));
    }

    @Provides
    @Singleton
    static SubmissionStore submissionStore(
            ContestProgrammingSubmissionDao submissionDao,
            ContestProgrammingGradingDao gradingDao,
            ObjectMapper mapper) {

        return new BaseSubmissionStore<>(submissionDao, gradingDao, mapper);
    }

    @Provides
    @Singleton
    static ItemSubmissionStore itemSubmissionStore(ContestBundleItemSubmissionDao submissionDao) {
        return new BaseItemSubmissionStore<>(submissionDao);
    }
}
