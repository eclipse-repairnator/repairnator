package fr.inria.spirals.repairnator.process.inspectors;

import fr.inria.jtravis.entities.Build;
import fr.inria.spirals.repairnator.BuildToBeInspected;
import fr.inria.spirals.repairnator.config.RepairnatorConfig;
import fr.inria.spirals.repairnator.notifier.AbstractNotifier;
import fr.inria.spirals.repairnator.notifier.PatchNotifier;
import fr.inria.spirals.repairnator.process.git.GitHelper;
import fr.inria.spirals.repairnator.process.inspectors.components.IRunInspector;
import fr.inria.spirals.repairnator.process.inspectors.properties.Properties;
import fr.inria.spirals.repairnator.process.inspectors.properties.machineInfo.MachineInfo;
import fr.inria.spirals.repairnator.process.step.AbstractStep;
import fr.inria.spirals.repairnator.process.step.checkoutrepository.CheckoutType;
import fr.inria.spirals.repairnator.serializer.AbstractDataSerializer;
import fr.inria.spirals.repairnator.utils.Utils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * This class initialize the pipelines by creating the steps:
 * it's the backbone of the pipeline.
 */
public class ProjectInspector {
    private final Logger logger = LoggerFactory.getLogger(ProjectInspector.class);

    private BuildToBeInspected buildToBeInspected;
    private PatchNotifier patchNotifier;
    private String gitUrl;
    private String gitBranch;
    private String gitCommit;
    private boolean pipelineEnding;

    protected GitHelper gitHelper;
    protected String repoLocalPath;
    protected String repoToPushLocalPath;
    protected String workspace;
    protected String m2LocalPath;
    protected List<AbstractDataSerializer> serializers;
    protected JobStatus jobStatus;
    protected List<AbstractNotifier> notifiers;
    protected CheckoutType checkoutType;
    protected List<AbstractStep> steps;
    protected AbstractStep finalStep;
    protected String gitSlug;
    protected IRunInspector iRunInspector;
    protected List<String> buildLog;

    public ProjectInspector() {}

    public ProjectInspector(BuildToBeInspected buildToBeInspected, String workspace, List<AbstractDataSerializer> serializers, List<AbstractNotifier> notifiers) {
        this.buildToBeInspected = buildToBeInspected;

        this.workspace = workspace;
        this.gitSlug = getRepoSlug();
        this.repoLocalPath = workspace + File.separator + this.gitSlug;
        long buildId = buildToBeInspected != null ? buildToBeInspected.getBuggyBuild().getId() : 0;
        this.repoToPushLocalPath = repoLocalPath+"_topush_" + buildId;
        this.m2LocalPath = new File(this.repoLocalPath + File.separator + ".m2").getAbsolutePath();
        this.serializers = serializers;
        this.gitHelper = new GitHelper();
        this.jobStatus = new JobStatus(repoLocalPath);
        this.notifiers = notifiers;
        this.checkoutType = CheckoutType.NO_CHECKOUT;
        this.steps = new ArrayList<>();
        this.buildLog = new ArrayList<>();
        this.initProperties();
    }

    public ProjectInspector(String workspace,String gitUrl,String gitBranch,String gitCommit,List<AbstractDataSerializer> serializers, List<AbstractNotifier> notifiers) {
        this.gitUrl = gitUrl;
        this.gitBranch = gitBranch;
        this.gitCommit = gitCommit;
        this.gitSlug = this.gitUrl.split("https://github.com/",2)[1].replace(".git","");
        this.workspace = workspace;
        this.repoLocalPath = workspace + File.separator + this.gitSlug;
        this.repoToPushLocalPath = repoLocalPath+"_topush";
        this.m2LocalPath = new File(this.repoLocalPath + File.separator + ".m2").getAbsolutePath();
        this.serializers = serializers;
        this.gitHelper = new GitHelper();
        this.jobStatus = new JobStatus(repoLocalPath);
        this.notifiers = notifiers;
        this.checkoutType = CheckoutType.NO_CHECKOUT;
        this.steps = new ArrayList<>();
        this.buildLog = new ArrayList<>();
        /* Skip initProperties*/
    }

    public ProjectInspector(String workspace, List<AbstractNotifier> notifiers) {
        this.workspace = workspace;
        this.repoLocalPath = workspace + File.separator;
        this.repoToPushLocalPath = repoLocalPath+"_topush";
        this.m2LocalPath = new File(this.repoLocalPath + File.separator + ".m2").getAbsolutePath();
        this.jobStatus = new JobStatus(repoLocalPath);
        this.notifiers = notifiers;
        this.checkoutType = CheckoutType.NO_CHECKOUT;
        this.steps = new ArrayList<>();
        this.buildLog = new ArrayList<>();
        /* Skip initProperties */
    }

    public ProjectInspector(BuildToBeInspected buildToBeInspected, String workspace, List<AbstractNotifier> notifiers) {
        this.buildToBeInspected = buildToBeInspected;
        this.workspace = workspace;
        this.gitSlug = getRepoSlug();
        this.repoLocalPath = workspace + File.separator + getRepoSlug();
        long buildId = buildToBeInspected != null ? buildToBeInspected.getBuggyBuild().getId() : 0;
        this.repoToPushLocalPath = repoLocalPath + "_topush_" + buildId;
        this.m2LocalPath = new File(this.repoLocalPath + File.separator + ".m2").getAbsolutePath();
        this.serializers = new ArrayList<AbstractDataSerializer>();
        this.gitHelper = new GitHelper();
        this.jobStatus = new JobStatus(repoLocalPath);
        this.notifiers = notifiers;
        this.checkoutType = CheckoutType.NO_CHECKOUT;
        this.steps = new ArrayList<>();
        this.buildLog = new ArrayList<>();
        this.initProperties();
    }

    public ProjectInspector setIRunInspector(IRunInspector iRunInspector) {
        this.iRunInspector = iRunInspector;
        return this;
    }

    public void setSkipPreSteps(boolean skipPreSteps) {
        this.iRunInspector.setSkipPreSteps(skipPreSteps);
    }

    public Logger getLogger() {
        return this.logger;
    }

    public String getCheckoutBranchName() {
        return this.gitBranch;
    }

    public String getGitCommit() {
        return this.gitCommit;
    }

    public String getGitRepositoryBranch() {
        return this.getBuggyBuild().getBranch().getName();
    }

    protected void initProperties() {
        try {
            Properties properties = this.jobStatus.getProperties();

            Build build = this.getBuggyBuild();
            long id = build.getId();
            String url = Utils.getTravisUrl(build.getId(), this.getRepoSlug());
            Date date = build.getFinishedAt();
            fr.inria.spirals.repairnator.process.inspectors.properties.builds.Build buggyBuild = new fr.inria.spirals.repairnator.process.inspectors.properties.builds.Build(id, url, date);
            properties.getBuilds().setBuggyBuild(buggyBuild);

            build = this.getPatchedBuild();
            if (build != null) {
                id = build.getId();
                url = Utils.getTravisUrl(build.getId(), this.getRepoSlug());
                date = build.getFinishedAt();
                fr.inria.spirals.repairnator.process.inspectors.properties.builds.Build patchedBuild = new fr.inria.spirals.repairnator.process.inspectors.properties.builds.Build(id, url, date);
                properties.getBuilds().setFixerBuild(patchedBuild);
            }

            MachineInfo machineInfo = properties.getReproductionBuggyBuild().getMachineInfo();
            machineInfo.setHostName(Utils.getHostname());

            fr.inria.spirals.repairnator.process.inspectors.properties.repository.Repository repository = properties.getRepository();
            repository.setName(this.getRepoSlug());
            repository.setUrl(Utils.getSimpleGithubRepoUrl(this.getRepoSlug()));

            if (this.getBuggyBuild().isPullRequest()) {
                repository.setIsPullRequest(true);
                repository.setPullRequestId(this.getBuggyBuild().getPullRequestNumber());
            }

            GitHub gitHub;
            try {
                gitHub = RepairnatorConfig.getInstance().getGithub();
                GHRepository repo = gitHub.getRepository(this.getRepoSlug());
                repository.setGithubId(repo.getId());
                if (repo.isFork()) {
                    repository.setIsFork(true);
                    repository.getOriginal().setName(repo.getParent().getFullName());
                    repository.getOriginal().setGithubId(repo.getParent().getId());
                    repository.getOriginal().setUrl(Utils.getSimpleGithubRepoUrl(repo.getParent().getFullName()));
                }
            } catch (IOException e) {
                this.logger.warn("It was not possible to retrieve information to check if " + this.getRepoSlug() + " is a fork.");
                this.logger.debug(e.toString());
            }

            switch (this.getBuildToBeInspected().getStatus()) {
                case ONLY_FAIL:
                    properties.setType("only_fail");
                    break;

                case FAILING_AND_PASSING:
                    properties.setType("failing_passing");
                    break;

                case PASSING_AND_PASSING_WITH_TEST_CHANGES:
                    properties.setType("passing_passing");
                    break;
            }
        } catch (Exception e) {
            this.logger.error("Error while initializing metrics.", e);
        }
    }

    public String getGitUrl() {
        return this.gitUrl;
    }

    public String getGitSlug() {
        return this.gitSlug;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public GitHelper getGitHelper() {
        return this.gitHelper;
    }

    public List<AbstractDataSerializer> getSerializers() {
        return serializers;
    }

    public void setSerializers(List<AbstractDataSerializer> serializers) {
        this.serializers = serializers;
    }

    public String getWorkspace() {
        return workspace;
    }

    public String getM2LocalPath() {
        return m2LocalPath;
    }

    public BuildToBeInspected getBuildToBeInspected() {
        if (this.buildToBeInspected == null) {
            return null;
        }
        return this.buildToBeInspected;
    }

    public Build getPatchedBuild() {
        if (this.buildToBeInspected == null) {
            return null;
        }
        return this.buildToBeInspected.getPatchedBuild();
    }

    public Build getBuggyBuild() {
        if (this.buildToBeInspected == null) {
            return null;
        }
        return this.buildToBeInspected.getBuggyBuild();
    }

    public String getRepoSlug() {
        if (this.buildToBeInspected == null) {
            return null;
        }
        return this.buildToBeInspected.getBuggyBuild().getRepository().getSlug();
    }

    public String getRepoLocalPath() {
        return repoLocalPath;
    }

    public Git openAndGetGitObject() throws IOException {
        return new Git(new FileRepository(repoLocalPath + File.separatorChar + ".git"));
    }

    public String getRepoToPushLocalPath() {
        return repoToPushLocalPath;
    }

    public String getRemoteBranchName() {
        String formattedDate;
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYYMMdd-HHmmss");
        if(this.getBuggyBuild() != null){
            formattedDate = this.getBuggyBuild().getId() + "-" + dateFormat.format(this.getBuggyBuild().getFinishedAt());
        }else {
            formattedDate = dateFormat.format(new Date());
        }
        return this.getGitSlug().replace('/', '-') + '-' + formattedDate;
    }

    public String getProjectIdToBeInspected() {
        if (this.buildToBeInspected == null) {
            return null;
        }
        return String.valueOf(buildToBeInspected.getBuggyBuild().getId());
    }

    public void run() {
        this.iRunInspector.run(this);
    }

    public CheckoutType getCheckoutType() {
        return checkoutType;
    }

    public void setCheckoutType(CheckoutType checkoutType) {
        this.checkoutType = checkoutType;
    }

    public List<AbstractNotifier> getNotifiers() {
        return notifiers;
    }

    public PatchNotifier getPatchNotifier() {
        return patchNotifier;
    }

    public void setPatchNotifier(PatchNotifier patchNotifier) {
        this.patchNotifier = patchNotifier;
    }

    public AbstractStep getFinalStep() {
        return finalStep;
    }

    public void setFinalStep(AbstractStep finalStep) {
        this.finalStep = finalStep;
    }

    public boolean isPipelineEnding() {
        return pipelineEnding;
    }

    public void setPipelineEnding(boolean pipelineEnding) {
        this.pipelineEnding = pipelineEnding;
    }

    public void registerStep(AbstractStep step) {
        this.steps.add(this.steps.size(), step);
    }

    public List<AbstractStep> getSteps() {
        return steps;
    }

    public void printPipeline() {
        this.logger.info("----------------------------------------------------------------------");
        this.logger.info("PIPELINE STEPS");
        this.logger.info("----------------------------------------------------------------------");
        for (int i = 0; i < this.steps.size(); i++) {
            this.logger.info(this.steps.get(i).getName());
        }
    }

    public void printPipelineEnd() {
        this.logger.info("----------------------------------------------------------------------");
        this.logger.info("PIPELINE EXECUTION SUMMARY");
        this.logger.info("----------------------------------------------------------------------");
        int higherDuration = 0;
        for (int i = 0; i < this.steps.size(); i++) {
            AbstractStep step = this.steps.get(i);
            int stepDuration = step.getDuration();
            if (stepDuration > higherDuration) {
                higherDuration = stepDuration;
            }
        }
        for (int i = 0; i < this.steps.size(); i++) {
            AbstractStep step = this.steps.get(i);
            String stepName = step.getName();
            String stepStatus = (step.getStepStatus() != null) ? step.getStepStatus().getStatus().name() : "NOT RUN";
            String stepDuration = String.valueOf(step.getDuration());

            StringBuilder stepDurationFormatted = new StringBuilder();
            if (!stepStatus.equals("SKIPPED") && !stepStatus.equals("NOT RUN")) {
                stepDurationFormatted.append(" [ ");
                for (int j = 0; j < (String.valueOf(higherDuration).length() - stepDuration.length()); j++) {
                    stepDurationFormatted.append(" ");
                }
                stepDurationFormatted.append(stepDuration + " s ]");
            } else {
                for (int j = 0; j < (String.valueOf(higherDuration).length() + 7); j++) {
                    stepDurationFormatted.append(" ");
                }
            }

            int stringSize = stepName.length() + stepStatus.length() + stepDurationFormatted.length();
            int nbDot = 70 - stringSize;
            StringBuilder stepNameFormatted = new StringBuilder(stepName);
            for (int j = 0; j < nbDot; j++) {
                stepNameFormatted.append(".");
            }
            this.logger.info(stepNameFormatted + stepStatus + stepDurationFormatted);
        }
        String finding = getFinding();
        finding = (finding.equals("UNKNOWN")) ? "-" : finding;
        this.logger.info("----------------------------------------------------------------------");
        this.logger.info("PIPELINE FINDING: "+finding);
        this.logger.info("----------------------------------------------------------------------");
    }

    public String getFinding() {
        return AbstractDataSerializer.getPrettyPrintState(this).toUpperCase();
    }

    public List<String> getBuildLog() {
        return buildLog;
    }

    public void printToBuildLog(String s){
        buildLog.add(s);
    }

}
