package io.jenkins.plugins.coderrect;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class CoderrectPublisher extends Recorder implements SimpleBuildStep {
    private static final String DIR_REPORT = ".coderrect";

    private String buildDirectory;

    @DataBoundConstructor()
    public CoderrectPublisher(final String buildDirectory) {
        this.buildDirectory = buildDirectory;
    }

    public String getBuildDirectory() {
        return this.buildDirectory;
    }

    /**
     * This method is execuated on master
     */
    @Override
    public void perform(@Nonnull Run<?, ?> run,
                        @Nonnull FilePath ws,
                        @Nonnull Launcher launcher,
                        @Nonnull TaskListener listener)
            throws InterruptedException, IOException {
        listener.getLogger().println(String.format("[coderrect] Starting. workspace=%s, run=%s, buildDirectory=%s",
                ws.getBaseName(), run.getId(), this.buildDirectory));

        if (Result.ABORTED.equals(run.getResult())) {
            listener.getLogger().println("Skipping publishing race reports because build aborted");
            return;
        }

        CoderrectParser parser = new CoderrectParser(listener, this.buildDirectory);
        // ws remotely executes 'parser' on the slave machine by sending jar file to
        // the slave and ser/deser its return value to the master
        CoderrectStats stats = ws.act(parser);
        Run<?,?> prevRun = run.getPreviousBuild();
        CoderrectStats prevStats = null;
        if (prevRun != null) {
            CoderrectAction prevAction = prevRun.getAction(CoderrectAction.class);
            if (prevAction != null)
                prevStats = prevAction.getStats();
        }
        logRaceList(listener.getLogger(), "current", stats.getDataRaceList());
        if (prevStats != null)
            logRaceList(listener.getLogger(), "previous", prevStats.getDataRaceList());
        CoderrectAction action = new CoderrectAction(run, stats, prevStats);
        listener.getLogger().printf("[coderrect] newAdded=%d, disappeared=%d\n",
                action.getNewAdded().size(), action.getDisappeared().size());
        run.addAction(action);

        copyFilesToBuildDirectory(run, ws, listener, run.getRootDir(), launcher.getChannel());

        listener.getLogger().println("[coderrect] done");
    }

    private void logRaceList(PrintStream logger, String prefix, List<DataRace> races) {
        logger.println("[coderrect] " + prefix);
        for (DataRace race : races) {
            logger.printf("signature - %s\n", race.getSignature());
        }
        logger.println("\n\n");
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    @Symbol("publishCoderrect")
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        //@Extension
        //public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

        public DescriptorImpl(Class<? extends Publisher> clazz) {
            super(clazz);
            load();
        }

        public DescriptorImpl() {
            this(CoderrectPublisher.class);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Publish the race report to Jenkins";
        }

        @Override
        public String getHelpFile(String fieldName) {
            return "/plugin/coderrect/help.html";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }
    }


    /**
     * This method is executed on master. It copies .coderrect/report to thee build directory
     *
     * @param rootDir - the build directory on master such as "$jenkins/jobs/your_proj/branches/master/build/27
     * @param channel - communicate with the slave
     */
    private void copyFilesToBuildDirectory(final Run<?,?> run,
                                           final FilePath ws,
                                           final TaskListener listener,
                                           final File rootDir,
                                           final VirtualChannel channel)
            throws IOException, InterruptedException {

        File directory = new File(rootDir, DIR_REPORT);
        listener.getLogger().println(String.format("[coderrect] Copy report from agent. serverTargetDir=" + directory.getAbsolutePath()));
        if (!directory.exists() && !directory.mkdir()) {
            throw new IOException("Can't create directory for copy of workspace files: "
                    + directory.getAbsolutePath());
        }

        final FilePath coderrectReportPath = ws.child(this.buildDirectory + File.separator + ".coderrect/report");
        if (!coderrectReportPath.exists()) {
            listener.getLogger().println("[coderrect] the report folder doesn't exist. dir=" + coderrectReportPath.getName());
            return;
        }
        final FilePath buildTarget = new FilePath(run.getRootDir()).child(DIR_REPORT);
        listener.getLogger().printf("[coderrect] copy report artifacts from slave. target=%s, source=%s",
                buildTarget.absolutize().getName(), coderrectReportPath.absolutize().getName());
        coderrectReportPath.copyRecursiveTo("**/*", buildTarget);
   }
}
