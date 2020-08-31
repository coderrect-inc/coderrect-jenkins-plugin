package io.jenkins.plugins.coderrect;

import hudson.model.Run;
import jenkins.model.RunAction2;

public class CoderrectAction implements RunAction2 {
    private transient Run run;
    private final CoderrectStats stats;
    private final CoderrectStats prevStats;

    public CoderrectAction(Run<?, ?> run, CoderrectStats stats, CoderrectStats prevStats) {
        this.run = run;
        this.stats = stats;
        this.prevStats = prevStats;
    }

    public CoderrectStats getStats() {
        return stats;
    }

    public CoderrectStats getPrevStats() {
        return prevStats;
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.run = run;
    }

    public Run getRun(){
        return this.run;
    }

    @Override
    public String getIconFileName() {
        // return the path to the icon file
        return "/plugin/coderrect/images/coderrectlogo.svg";
    }

    @Override
    public String getDisplayName() {
        return "Coderrect";
    }

    @Override
    public String getUrlName() {
        return "coderrect";
    }
}
