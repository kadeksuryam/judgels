package judgels.uriel.contest.scoreboard.gcj;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import judgels.gabriel.api.Verdict;
import judgels.sandalphon.api.submission.programming.Submission;
import judgels.uriel.api.contest.Contest;
import judgels.uriel.api.contest.ContestStyle;
import judgels.uriel.api.contest.contestant.ContestContestant;
import judgels.uriel.api.contest.module.GcjStyleModuleConfig;
import judgels.uriel.api.contest.module.StyleModuleConfig;
import judgels.uriel.api.contest.scoreboard.GcjScoreboard.GcjScoreboardEntry;
import judgels.uriel.api.contest.scoreboard.GcjScoreboard.GcjScoreboardProblemState;
import judgels.uriel.api.contest.scoreboard.ScoreboardState;
import judgels.uriel.contest.scoreboard.AbstractProgrammingScoreboardProcessorTests;
import judgels.uriel.contest.scoreboard.ScoreboardProcessResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GcjScoreboardProcessorTests extends AbstractProgrammingScoreboardProcessorTests {
    private GcjScoreboardProcessor scoreboardProcessor = new GcjScoreboardProcessor();

    @Nested
    class Process {
        private ScoreboardState state = new ScoreboardState.Builder()
                .addProblemJids("p1", "p2")
                .addProblemAliases("A", "B")
                .problemPoints(ImmutableList.of(1, 10))
                .build();

        private Contest contest = new Contest.Builder()
                .beginTime(Instant.ofEpochSecond(60))
                .duration(Duration.ofMinutes(100))
                .id(1)
                .jid("JIDC")
                .name("contest-name")
                .slug("contest-slug")
                .style(ContestStyle.GCJ)
                .build();

        private StyleModuleConfig styleModuleConfig = new GcjStyleModuleConfig.Builder()
                .wrongSubmissionPenalty(10)
                .build();

        private Set<ContestContestant> contestants = ImmutableSet.of(
                new ContestContestant.Builder().userJid("c1").build(),
                new ContestContestant.Builder().userJid("c2").contestStartTime(Instant.ofEpochSecond(300)).build());

        @Test
        void time_calculation() {
            List<Submission> submissions = ImmutableList.of(
                    createSubmission(1, 300, "c1", "p1", 100, Verdict.ACCEPTED),
                    createSubmission(2, 360, "c1", "p1", 0, Verdict.TIME_LIMIT_EXCEEDED),
                    createSubmission(3, 400, "c1", "p2", 0, Verdict.TIME_LIMIT_EXCEEDED),
                    createSubmission(4, 410, "c1", "p2", 100, Verdict.ACCEPTED),
                    createSubmission(5, 900, "c2", "p1", 100, Verdict.ACCEPTED));

            ScoreboardProcessResult result = scoreboardProcessor.process(
                    contest,
                    state,
                    Optional.empty(),
                    styleModuleConfig,
                    contestants,
                    submissions,
                    ImmutableList.of(),
                    Optional.empty());

            assertThat(Lists.transform(result.getEntries(), e -> (GcjScoreboardEntry) e)).containsExactly(
                    new GcjScoreboardEntry.Builder()
                            .rank(1)
                            .contestantJid("c1")
                            .totalPoints(11)
                            .totalPenalties(26)
                            .addAttemptsList(2, 2)
                            .addPenaltyList(4, 6)
                            .addProblemStateList(
                                    GcjScoreboardProblemState.ACCEPTED,
                                    GcjScoreboardProblemState.ACCEPTED
                            )
                            .build(),
                    new GcjScoreboardEntry.Builder()
                            .rank(2)
                            .contestantJid("c2")
                            .totalPoints(1)
                            .totalPenalties(10)
                            .addAttemptsList(1, 0)
                            .addPenaltyList(10, 0)
                            .addProblemStateList(
                                    GcjScoreboardProblemState.ACCEPTED,
                                    GcjScoreboardProblemState.NOT_ACCEPTED
                            )
                            .build());
        }

        @Nested
        class ProblemOrdering {
            private List<Submission> submissions = ImmutableList.of(
                    createSubmission(1, 350, "c2", "p1", 100, Verdict.ACCEPTED),
                    createSubmission(2, 400, "c1", "p2", 100, Verdict.ACCEPTED));

            @Test
            void base_case() {
                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.empty(),
                        styleModuleConfig,
                        contestants,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                assertThat(Lists.transform(result.getEntries(), e -> (GcjScoreboardEntry) e)).containsExactly(
                        new GcjScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c1")
                                .totalPoints(10)
                                .totalPenalties(6)
                                .addAttemptsList(0, 1)
                                .addPenaltyList(0, 6)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.NOT_ACCEPTED,
                                        GcjScoreboardProblemState.ACCEPTED
                                )
                                .build(),
                        new GcjScoreboardEntry.Builder()
                                .rank(2)
                                .contestantJid("c2")
                                .totalPoints(1)
                                .totalPenalties(1)
                                .addAttemptsList(1, 0)
                                .addPenaltyList(1, 0)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.ACCEPTED,
                                        GcjScoreboardProblemState.NOT_ACCEPTED
                                )
                                .build());
            }

            @Test
            void reversed_case() {
                state = new ScoreboardState.Builder()
                        .addProblemJids("p2", "p1")
                        .addProblemAliases("B", "A")
                        .problemPoints(ImmutableList.of(10, 1))
                        .build();

                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.empty(),
                        styleModuleConfig,
                        contestants,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                assertThat(Lists.transform(result.getEntries(), e -> (GcjScoreboardEntry) e)).containsExactly(
                        new GcjScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c1")
                                .totalPoints(10)
                                .totalPenalties(6)
                                .addAttemptsList(1, 0)
                                .addPenaltyList(6, 0)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.ACCEPTED,
                                        GcjScoreboardProblemState.NOT_ACCEPTED
                                )
                                .build(),
                        new GcjScoreboardEntry.Builder()
                                .rank(2)
                                .contestantJid("c2")
                                .totalPoints(1)
                                .totalPenalties(1)
                                .addAttemptsList(0, 1)
                                .addPenaltyList(0, 1)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.NOT_ACCEPTED,
                                        GcjScoreboardProblemState.ACCEPTED
                                )
                                .build());
            }
        }

        @Nested
        class Sorting {
            @Test
            void points_over_penalty() {
                List<Submission> submissions = ImmutableList.of(
                        createSubmission(1, 300, "c1", "p1", 100, Verdict.ACCEPTED),
                        createSubmission(2, 900, "c2", "p2", 100, Verdict.ACCEPTED));

                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.empty(),
                        styleModuleConfig,
                        contestants,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                assertThat(Lists.transform(result.getEntries(), e -> (GcjScoreboardEntry) e)).containsExactly(
                        new GcjScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c2")
                                .totalPoints(10)
                                .totalPenalties(10)
                                .addAttemptsList(0, 1)
                                .addPenaltyList(0, 10)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.NOT_ACCEPTED,
                                        GcjScoreboardProblemState.ACCEPTED
                                )
                                .build(),
                        new GcjScoreboardEntry.Builder()
                                .rank(2)
                                .contestantJid("c1")
                                .totalPoints(1)
                                .totalPenalties(4)
                                .addAttemptsList(1, 0)
                                .addPenaltyList(4, 0)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.ACCEPTED,
                                        GcjScoreboardProblemState.NOT_ACCEPTED
                                )
                                .build());
            }

            @Test
            void penalty_as_tiebreaker() {
                List<Submission> submissions = ImmutableList.of(
                        createSubmission(1, 900, "c2", "p1", 100, Verdict.ACCEPTED),
                        createSubmission(2, 900, "c1", "p1", 100, Verdict.ACCEPTED));

                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.empty(),
                        styleModuleConfig,
                        contestants,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                assertThat(Lists.transform(result.getEntries(), e -> (GcjScoreboardEntry) e)).containsExactly(
                        new GcjScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c2")
                                .totalPoints(1)
                                .totalPenalties(10)
                                .addAttemptsList(1, 0)
                                .addPenaltyList(10, 0)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.ACCEPTED,
                                        GcjScoreboardProblemState.NOT_ACCEPTED
                                )
                                .build(),
                        new GcjScoreboardEntry.Builder()
                                .rank(2)
                                .contestantJid("c1")
                                .totalPoints(1)
                                .totalPenalties(14)
                                .addAttemptsList(1, 0)
                                .addPenaltyList(14, 0)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.ACCEPTED,
                                        GcjScoreboardProblemState.NOT_ACCEPTED
                                )
                                .build());
            }

            @Test
            void same_rank_if_equal() {
                contestants = ImmutableSet.of(
                        new ContestContestant.Builder().userJid("c1").build(),
                        new ContestContestant.Builder()
                                .userJid("c2")
                                .contestStartTime(Instant.ofEpochSecond(300))
                                .build(),
                        new ContestContestant.Builder().userJid("c3").build());

                List<Submission> submissions = ImmutableList.of(
                        createSubmission(1, 660, "c1", "p1", 100, Verdict.ACCEPTED),
                        createSubmission(2, 900, "c2", "p1", 100, Verdict.ACCEPTED));

                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.empty(),
                        styleModuleConfig,
                        contestants,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                assertThat(Lists.transform(result.getEntries(), e -> (GcjScoreboardEntry) e)).containsExactly(
                        new GcjScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c1")
                                .totalPoints(1)
                                .totalPenalties(10)
                                .addAttemptsList(1, 0)
                                .addPenaltyList(10, 0)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.ACCEPTED,
                                        GcjScoreboardProblemState.NOT_ACCEPTED
                                )
                                .build(),
                        new GcjScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c2")
                                .totalPoints(1)
                                .totalPenalties(10)
                                .addAttemptsList(1, 0)
                                .addPenaltyList(10, 0)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.ACCEPTED,
                                        GcjScoreboardProblemState.NOT_ACCEPTED
                                )
                                .build(),
                        new GcjScoreboardEntry.Builder()
                                .rank(3)
                                .contestantJid("c3")
                                .totalPoints(0)
                                .totalPenalties(0)
                                .addAttemptsList(0, 0)
                                .addPenaltyList(0, 0)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.NOT_ACCEPTED,
                                        GcjScoreboardProblemState.NOT_ACCEPTED
                                )
                                .build());
            }

            @Test
            void zero_points_ordering() {
                contestants = ImmutableSet.of(
                        new ContestContestant.Builder().userJid("c1").build(),
                        new ContestContestant.Builder().userJid("c2").build(),
                        new ContestContestant.Builder().userJid("c3").build());

                List<Submission> submissions = ImmutableList.of(
                        createSubmission(1, 660, "c1", "p1", 0, Verdict.WRONG_ANSWER),
                        createSubmission(2, 900, "c1", "p2", 0, Verdict.WRONG_ANSWER),
                        createSubmission(3, 900, "c3", "p2", 0, Verdict.WRONG_ANSWER));

                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.empty(),
                        styleModuleConfig,
                        contestants,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                assertThat(Lists.transform(result.getEntries(), e -> (GcjScoreboardEntry) e)).containsExactly(
                        new GcjScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c1")
                                .totalPoints(0)
                                .totalPenalties(0)
                                .addAttemptsList(1, 1)
                                .addPenaltyList(0, 0)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.NOT_ACCEPTED,
                                        GcjScoreboardProblemState.NOT_ACCEPTED
                                )
                                .build(),
                        new GcjScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c3")
                                .totalPoints(0)
                                .totalPenalties(0)
                                .addAttemptsList(0, 1)
                                .addPenaltyList(0, 0)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.NOT_ACCEPTED,
                                        GcjScoreboardProblemState.NOT_ACCEPTED
                                )
                                .build(),
                        new GcjScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c2")
                                .totalPoints(0)
                                .totalPenalties(0)
                                .addAttemptsList(0, 0)
                                .addPenaltyList(0, 0)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.NOT_ACCEPTED,
                                        GcjScoreboardProblemState.NOT_ACCEPTED
                                )
                                .build());
            }
        }

        @Nested
        class PendingAfterFreeze {
            private Optional<Instant> freezeTime = Optional.of(Instant.ofEpochSecond(500));

            private List<Submission> baseSubmissions = ImmutableList.of(
                    createSubmission(1, 100, "c1", "p1", 100, Verdict.ACCEPTED),
                    createSubmission(2, 400, "c2", "p2", 100, Verdict.ACCEPTED),
                    createSubmission(3, 450, "c2", "p1", 100, Verdict.ACCEPTED));

            @Test
            void no_pending() {
                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.empty(),
                        styleModuleConfig,
                        contestants,
                        baseSubmissions,
                        ImmutableList.of(),
                        freezeTime);

                assertThat(Lists.transform(result.getEntries(), e -> (GcjScoreboardEntry) e)).containsExactly(
                        new GcjScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c2")
                                .totalPoints(11)
                                .totalPenalties(3)
                                .addAttemptsList(1, 1)
                                .addPenaltyList(3, 2)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.ACCEPTED,
                                        GcjScoreboardProblemState.ACCEPTED
                                )
                                .build(),
                        new GcjScoreboardEntry.Builder()
                                .rank(2)
                                .contestantJid("c1")
                                .totalPoints(1)
                                .totalPenalties(1)
                                .addAttemptsList(1, 0)
                                .addPenaltyList(1, 0)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.ACCEPTED,
                                        GcjScoreboardProblemState.NOT_ACCEPTED
                                )
                                .build());
            }

            @Test
            void pending_does_not_overwrite_accepted() {
                List<Submission> submissions = new ImmutableList.Builder<Submission>()
                        .addAll(baseSubmissions)
                        .add(createSubmission(4, 501, "c1", "p1", 100, Verdict.PENDING))
                        .build();

                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.empty(),
                        styleModuleConfig,
                        contestants,
                        submissions,
                        ImmutableList.of(),
                        freezeTime);

                assertThat(Lists.transform(result.getEntries(), e -> (GcjScoreboardEntry) e)).containsExactly(
                        new GcjScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c2")
                                .totalPoints(11)
                                .totalPenalties(3)
                                .addAttemptsList(1, 1)
                                .addPenaltyList(3, 2)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.ACCEPTED,
                                        GcjScoreboardProblemState.ACCEPTED
                                )
                                .build(),
                        new GcjScoreboardEntry.Builder()
                                .rank(2)
                                .contestantJid("c1")
                                .totalPoints(1)
                                .totalPenalties(1)
                                .addAttemptsList(1, 0)
                                .addPenaltyList(1, 0)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.ACCEPTED,
                                        GcjScoreboardProblemState.NOT_ACCEPTED
                                )
                                .build());
            }

            @Test
            void pending_does_overwrite_not_accepted() {
                List<Submission> submissions = new ImmutableList.Builder<Submission>()
                        .addAll(baseSubmissions)
                        .add(createSubmission(4, 501, "c1", "p2", 0, Verdict.PENDING))
                        .build();

                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.empty(),
                        styleModuleConfig,
                        contestants,
                        submissions,
                        ImmutableList.of(),
                        freezeTime);

                assertThat(Lists.transform(result.getEntries(), e -> (GcjScoreboardEntry) e)).containsExactly(
                        new GcjScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c2")
                                .totalPoints(11)
                                .totalPenalties(3)
                                .addAttemptsList(1, 1)
                                .addPenaltyList(3, 2)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.ACCEPTED,
                                        GcjScoreboardProblemState.ACCEPTED
                                )
                                .build(),
                        new GcjScoreboardEntry.Builder()
                                .rank(2)
                                .contestantJid("c1")
                                .totalPoints(1)
                                .totalPenalties(1)
                                .addAttemptsList(1, 0)
                                .addPenaltyList(1, 0)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.ACCEPTED,
                                        GcjScoreboardProblemState.FROZEN
                                )
                                .build());
            }

            @Test
            void pending_counts_on_freeze_time() {
                List<Submission> submissions = new ImmutableList.Builder<Submission>()
                        .addAll(baseSubmissions)
                        .add(createSubmission(4, 500, "c1", "p2", 0, Verdict.PENDING))
                        .build();

                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.empty(),
                        styleModuleConfig,
                        contestants,
                        submissions,
                        ImmutableList.of(),
                        freezeTime);

                assertThat(Lists.transform(result.getEntries(), e -> (GcjScoreboardEntry) e)).containsExactly(
                        new GcjScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c2")
                                .totalPoints(11)
                                .totalPenalties(3)
                                .addAttemptsList(1, 1)
                                .addPenaltyList(3, 2)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.ACCEPTED,
                                        GcjScoreboardProblemState.ACCEPTED
                                )
                                .build(),
                        new GcjScoreboardEntry.Builder()
                                .rank(2)
                                .contestantJid("c1")
                                .totalPoints(1)
                                .totalPenalties(1)
                                .addAttemptsList(1, 0)
                                .addPenaltyList(1, 0)
                                .addProblemStateList(
                                        GcjScoreboardProblemState.ACCEPTED,
                                        GcjScoreboardProblemState.FROZEN
                                )
                                .build());
            }
        }

        @Nested
        class IncrementalProcess {
            List<Submission> submissions = ImmutableList.of(
                    createSubmission(5, 100, "c1", "p1", 0, Verdict.WRONG_ANSWER),
                    createSubmission(6, 200, "c2", "p1", 0, Verdict.WRONG_ANSWER),
                    createSubmission(7, 300, "c1", "p1", 0, Verdict.WRONG_ANSWER),
                    createSubmission(8, 400, "c1", "p1", 100, Verdict.ACCEPTED),
                    createSubmission(9, 500, "c2", "p2", 100, Verdict.ACCEPTED),
                    createSubmission(10, 600, "c2", "p1", 0, Verdict.PENDING),
                    createSubmission(11, 700, "c1", "p2", 100, Verdict.ACCEPTED));

            Set<ContestContestant> contestants = ImmutableSet.of(
                    new ContestContestant.Builder().userJid("c1").build(),
                    new ContestContestant.Builder().userJid("c2").contestStartTime(Instant.ofEpochSecond(300)).build(),
                    new ContestContestant.Builder().userJid("c3").build());

            GcjScoreboardIncrementalContent incrementalContent = new GcjScoreboardIncrementalContent.Builder()
                    .lastSubmissionId(3)
                    .putAttemptsMapsByContestantJid("c2", ImmutableMap.of("p1", 0, "p2", 2))
                    .putAttemptsMapsByContestantJid("c3", ImmutableMap.of("p1", 1, "p2", 0))
                    .putPenaltyMapsByContestantJid("c2", ImmutableMap.of("p1", 0L, "p2", 3L))
                    .putPenaltyMapsByContestantJid("c3", ImmutableMap.of("p1", 3L, "p2", 0L))
                    .putProblemStateMapsByContestantJid("c2", ImmutableMap.of(
                            "p1", GcjScoreboardProblemState.NOT_ACCEPTED,
                            "p2", GcjScoreboardProblemState.NOT_ACCEPTED))
                    .putProblemStateMapsByContestantJid("c3", ImmutableMap.of(
                            "p1", GcjScoreboardProblemState.ACCEPTED,
                            "p2", GcjScoreboardProblemState.NOT_ACCEPTED))
                    .build();

            @Test
            void empty_initial_incremental_content() {
                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.empty(),
                        styleModuleConfig,
                        contestants,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                assertThat(result.getIncrementalContent()).isEqualTo(new GcjScoreboardIncrementalContent.Builder()
                        .lastSubmissionId(9)
                        .putAttemptsMapsByContestantJid("c1", ImmutableMap.of("p1", 3, "p2", 0))
                        .putAttemptsMapsByContestantJid("c2", ImmutableMap.of("p1", 1, "p2", 1))
                        .putPenaltyMapsByContestantJid("c1", ImmutableMap.of("p1", 6L, "p2", 0L))
                        .putPenaltyMapsByContestantJid("c2", ImmutableMap.of("p1", 0L, "p2", 4L))
                        .putProblemStateMapsByContestantJid("c1", ImmutableMap.of(
                                "p1", GcjScoreboardProblemState.ACCEPTED,
                                "p2", GcjScoreboardProblemState.NOT_ACCEPTED))
                        .putProblemStateMapsByContestantJid("c2", ImmutableMap.of(
                                "p1", GcjScoreboardProblemState.NOT_ACCEPTED,
                                "p2", GcjScoreboardProblemState.ACCEPTED))
                        .build());
            }

            @Test
            void empty_new_submissions() {
                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.of(incrementalContent),
                        styleModuleConfig,
                        contestants,
                        ImmutableList.of(),
                        ImmutableList.of(),
                        Optional.empty());

                assertThat(result.getIncrementalContent()).isEqualTo(new GcjScoreboardIncrementalContent.Builder()
                        .from(incrementalContent)
                        .lastSubmissionId(3)
                        .build());
            }

            @Test
            void existing_incremental_content() {
                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.of(incrementalContent),
                        styleModuleConfig,
                        contestants,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                assertThat(result.getIncrementalContent()).isEqualTo(new GcjScoreboardIncrementalContent.Builder()
                        .lastSubmissionId(9)
                        .putAttemptsMapsByContestantJid("c1", ImmutableMap.of("p1", 3, "p2", 0))
                        .putAttemptsMapsByContestantJid("c2", ImmutableMap.of("p1", 1, "p2", 3))
                        .putAttemptsMapsByContestantJid("c3", ImmutableMap.of("p1", 1, "p2", 0))
                        .putPenaltyMapsByContestantJid("c1", ImmutableMap.of("p1", 6L, "p2", 0L))
                        .putPenaltyMapsByContestantJid("c2", ImmutableMap.of("p1", 0L, "p2", 4L))
                        .putPenaltyMapsByContestantJid("c3", ImmutableMap.of("p1", 3L, "p2", 0L))
                        .putProblemStateMapsByContestantJid("c1", ImmutableMap.of(
                                "p1", GcjScoreboardProblemState.ACCEPTED,
                                "p2", GcjScoreboardProblemState.NOT_ACCEPTED))
                        .putProblemStateMapsByContestantJid("c2", ImmutableMap.of(
                                "p1", GcjScoreboardProblemState.NOT_ACCEPTED,
                                "p2", GcjScoreboardProblemState.ACCEPTED))
                        .putProblemStateMapsByContestantJid("c3", ImmutableMap.of(
                                "p1", GcjScoreboardProblemState.ACCEPTED,
                                "p2", GcjScoreboardProblemState.NOT_ACCEPTED))
                        .build());
            }
        }
    }
}
