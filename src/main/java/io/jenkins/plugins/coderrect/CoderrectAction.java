package io.jenkins.plugins.coderrect;

import hudson.model.Run;
import jenkins.model.RunAction2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CoderrectAction implements RunAction2 {
    private transient Run run;
    private final CoderrectStats stats;
    private final CoderrectStats prevStats;
    private final List<DataRace> newAdded;
    private final List<DataRace> disappeared;

    public CoderrectAction(Run<?, ?> run, CoderrectStats stats, CoderrectStats prevStats) {
        this.run = run;
        this.stats = stats;
        this.prevStats = prevStats;
        this.newAdded = new ArrayList<>();
        this.disappeared = new ArrayList<>();

        if (this.prevStats != null) {
            findDifference(this.stats.getDataRaceList(), this.prevStats.getDataRaceList(), newAdded, disappeared);
        }
        else {
            this.newAdded.addAll(this.stats.getDataRaceList());
        }
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


    private void findDifference(List<DataRace> currRaces,
                                List<DataRace> prevRaces,
                                List<DataRace> newAdded,
                                List<DataRace> disappeared) {
        Set<Integer> oldFounded = new HashSet<>();
        for (int k = 0; k < currRaces.size(); k++) {
            DataRace race = currRaces.get(k);
            boolean found = false;

            for (int i = 0; i < prevRaces.size(); i++) {
                DataRace tmpRace = prevRaces.get(i);
                if (race.isLogicalEqualTo(tmpRace)) {
                    found = true;
                    oldFounded.add(i);
                    break;
                }
            }

            if (!found) {
                newAdded.add(race);
            }
        }

        for (int i = 0; i < prevRaces.size(); i++) {
            if (!oldFounded.contains(i)) {
                disappeared.add(prevRaces.get(i));
            }
        }
    }

    public List<DataRace> getNewAdded() {
        return newAdded;
    }

    public List<DataRace> getDisappeared() {
        return disappeared;
    }
}
