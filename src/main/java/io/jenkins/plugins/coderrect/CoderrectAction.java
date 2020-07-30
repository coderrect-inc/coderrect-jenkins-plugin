package io.jenkins.plugins.coderrect;

import hudson.model.Run;
import jenkins.model.RunAction2;

public class CoderrectAction implements RunAction2 {
    private transient Run run;
    private final CoderrectStats stats;

    public CoderrectAction(Run<?, ?> run, CoderrectStats stats) {
        this.run = run;
        this.stats = stats;
    }

    public CoderrectStats getStats() {
        return stats;
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
