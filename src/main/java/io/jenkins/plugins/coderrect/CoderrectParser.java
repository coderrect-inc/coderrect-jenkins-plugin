package io.jenkins.plugins.coderrect;

import com.google.common.io.Files;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import jenkins.security.Roles;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jenkinsci.remoting.RoleChecker;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CoderrectParser implements FilePath.FileCallable<CoderrectStats>{
    /**
     * The subfolder holding raw race json file
     */
    private static final String CODERRECT_BUILD_PATH = ".coderrect/build";

    /**
     * index.json track binaries in this report
     */
    private static final String INDEX_JSON = "index.json";

    private final TaskListener listener;

    public CoderrectParser(TaskListener listener) {
        this.listener = listener;
    }

    public TaskListener getListener() {
        return listener;
    }

    /**
     * This method is executed from slave
     * @param baseDir - where .coderrect is
     */
    @Override
    public CoderrectStats invoke(File baseDir, VirtualChannel channel)
        throws IOException {
        listener.getLogger().println(String.format("[coderrect] parser is invoked. baseDir=%s", baseDir));

        // parse index.json and pick up the first binary with at least 1 data race
        JSONObject jpickedBinary = null;
        final File indexJsonFile = new File(baseDir + File.separator +
                CODERRECT_BUILD_PATH + File.separator + INDEX_JSON);
        JSONObject indexJson = JSONObject.fromObject(Files.toString(indexJsonFile, StandardCharsets.UTF_8));
        JSONArray executableJarr = indexJson.getJSONArray("Executables");
        for (int i = 0; i < executableJarr.size(); i++) {
            JSONObject j = executableJarr.getJSONObject(i);
            if (j.getInt("DataRaces") != 0) {
                jpickedBinary = j;
            }
        }
        if (jpickedBinary == null) {
            CoderrectStats stats = new CoderrectStats();
            stats.setParsedOk(false);
            stats.setErrmsg("No race found in any binary");
            return stats;
        }

        // parse race json of the picked binary
        String rawRaceJsonPath = String.format("%s/.coderrect/build/raw_%s.json", baseDir,
                jpickedBinary.getString("Name"));
        listener.getLogger().println(String.format("[coderrect] parse the raw race json file. rawRaceJsonPath=%s",
                rawRaceJsonPath));
        File rawRaceJsonFile = new File(rawRaceJsonPath);
        if (!rawRaceJsonFile.exists()) {
            CoderrectStats stats = new CoderrectStats();
            stats.setParsedOk(false);
            stats.setErrmsg(String.format("The raw race JSON file doesn't exist. file=%s", rawRaceJsonPath));
            return stats;
        }

        return parseRaceJson(rawRaceJsonFile, jpickedBinary.getString("Name"),
                indexJson.getString("CoderrectVer"));
    }


    private CoderrectStats parseRaceJson(File raceJsonFile, String projectName, String coderrectVer) throws IOException {
        JSONObject raceJson = JSONObject.fromObject(Files.toString(raceJsonFile, StandardCharsets.UTF_8));

        CoderrectStats stats = new CoderrectStats();

        JSONArray jarrDataRaces = raceJson.getJSONArray("dataRaces");
        stats.setDataRaces(jarrDataRaces.size());
        for (int i = 0; i < jarrDataRaces.size(); i++) {
            stats.getDataRaceList().add(new DataRace(jarrDataRaces.getJSONObject(i)));
        }

        JSONArray jarrDeadlocks = raceJson.getJSONArray("deadLocks");
        stats.setDeadlocks(jarrDeadlocks.size());

        JSONArray jarrMismatchedAPIs = raceJson.getJSONArray("mismatchedAPIs");
        stats.setMismatchedAPIs(jarrMismatchedAPIs.size());

        JSONArray jarrViolations = raceJson.getJSONArray("orderViolations");
        stats.setOrderViolations(jarrViolations.size());

        JSONArray jarrToctous = raceJson.getJSONArray("toctou");
        stats.setToctous(jarrToctous.size());

        stats.setParsedOk(true);
        stats.setErrmsg("OK");
        stats.setProjectName(projectName);
        stats.setReportGeneratedAt(raceJson.getString("generatedAt"));
        stats.setCoderrectVer(coderrectVer);
        return stats;
    }


    @Override
    public void checkRoles(RoleChecker checker) throws SecurityException {
        checker.check(this, Roles.SLAVE);
    }
}
