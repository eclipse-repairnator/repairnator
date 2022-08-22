package fr.inria.spirals.repairnator.realtime;

import fr.inria.spirals.repairnator.InputBuild;
import fr.inria.spirals.repairnator.realtime.githubapi.commits.GithubAPICommitAdapter;
import fr.inria.spirals.repairnator.realtime.githubapi.commits.models.SelectedCommit;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;

import org.mockito.internal.util.reflection.Whitebox;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestGithubScanner {


    @Test
    public void testBuildSubmission(){
        DockerPipelineRunner runner = mock(DockerPipelineRunner.class);

        GithubScanner scanner = new GithubScanner();
        Whitebox.setInternalState(scanner, "runner", runner);

        boolean isGithubActionsFailed = false;
        String commitId = "fda5d6161a5602a76e810209491d04cf91f4803b";
        String repoName = "repairnator/failingProject";
        SelectedCommit commit = new SelectedCommit(isGithubActionsFailed, commitId, repoName);

        scanner.process(commit);
        verify(runner, times(1)).submitBuild(any(InputBuild.class));
    }

    @Test
    public void testFetchingAll() throws Exception {

        Set<String> repos = new HashSet<String>(FileUtils.readLines(new File(getClass()
                        .getResource("/GithubScannerTest_repos.txt").getFile()), "UTF-8"));
        GithubScanner scanner = new GithubScanner();
        scanner.setup();

        List<SelectedCommit> commits =
                scanner.fetch(new SimpleDateFormat("dd/MM/yyyy").parse("01/12/2020").getTime(),
                        new SimpleDateFormat("dd/MM/yyyy").parse("27/01/2021").getTime(),
                        GithubScanner.FetchMode.ALL, repos);
        assertTrue(commits.stream().anyMatch(x -> x.getCommitId().equals("290bdc01884f7c9bf2140a7c66d22ca72fe50fbb")));
        assertTrue(commits.stream().anyMatch(x -> x.getCommitId().equals("e3456813fdcca1ed5075c0fb72b0bcbc9524e791")));
    }

    @Test
    public void testFetchingFailed() throws Exception {

        Set<String> repos = new HashSet<String>(FileUtils.readLines(new File(getClass()
                .getResource("/GithubScannerTest_repos.txt").getFile()), "UTF-8"));
        GithubScanner scanner = new GithubScanner();
        scanner.setup();

        List<SelectedCommit> commits =
                scanner.fetch(new SimpleDateFormat("dd/MM/yyyy").parse("01/12/2020").getTime(),
                        new SimpleDateFormat("dd/MM/yyyy").parse("27/01/2021").getTime(),
                        GithubScanner.FetchMode.FAILED, repos);
        assertFalse(commits.stream().anyMatch(x -> x.getCommitId().equals("290bdc01884f7c9bf2140a7c66d22ca72fe50fbb")));
        assertTrue(commits.stream().anyMatch(x -> x.getCommitId().equals("e3456813fdcca1ed5075c0fb72b0bcbc9524e791")));
    }

    @Test
    public void testFetchingPassing() throws Exception {

        Set<String> repos = new HashSet<String>(FileUtils.readLines(new File(getClass()
                .getResource("/GithubScannerTest_repos.txt").getFile()), "UTF-8"));
        GithubScanner scanner = new GithubScanner();
        scanner.setup();

        List<SelectedCommit> commits =
                scanner.fetch(new SimpleDateFormat("dd/MM/yyyy").parse("01/12/2020").getTime(),
                        new SimpleDateFormat("dd/MM/yyyy").parse("27/01/2021").getTime(),
                        GithubScanner.FetchMode.PASSING, repos);
        assertTrue(commits.stream().anyMatch(x -> x.getCommitId().equals("290bdc01884f7c9bf2140a7c66d22ca72fe50fbb")));
        assertFalse(commits.stream().anyMatch(x -> x.getCommitId().equals("e3456813fdcca1ed5075c0fb72b0bcbc9524e791")));
    }

}
