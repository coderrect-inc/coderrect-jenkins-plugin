package io.jenkins.plugins.coderrect;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * An example of data race
 *
 *     {
 *       "access1": {
 *         "col": 5,
 *         "dir": "/home/jsong/source/jenkins/public_ci_test",
 *         "filename": "race.cpp",
 *         "line": 13,
 *         "snippet": " 11|void* worker(void* unused) {\n 12|    *z = 20;\n>13|    x = rand() + (*z);\n 14|    std::cout << x << \"\\n\";\n 15|    return nullptr;\n",
 *         "sourceLine": " 13|    x = rand() + (*z);\n",
 *         "stacktrace": [
 *           "pthread_create [race.cpp:30]",
 *           "worker(void*) [race.cpp:30]"
 *         ]
 *       },
 *       "access2": {
 *         "col": 5,
 *         "dir": "/home/jsong/source/jenkins/public_ci_test",
 *         "filename": "race.cpp",
 *         "line": 13,
 *         "snippet": " 11|void* worker(void* unused) {\n 12|    *z = 20;\n>13|    x = rand() + (*z);\n 14|    std::cout << x << \"\\n\";\n 15|    return nullptr;\n",
 *         "sourceLine": " 13|    x = rand() + (*z);\n",
 *         "stacktrace": [
 *           "pthread_create [race.cpp:31]",
 *           "worker(void*) [race.cpp:31]"
 *         ]
 *       },
 *       "isOmpRace": false,
 *       "priority": 2,
 *       "sharedObj": {
 *         "dir": "/home/jsong/source/jenkins/public_ci_test",
 *         "field": "",
 *         "filename": "race.cpp",
 *         "line": 5,
 *         "name": "x",
 *         "sourceLine": " 5|int x;\n"
 *       }
 *     }
 */
public class DataRace extends AbstractRace {
    private String srcFilePath;
    private int lineNum;
    private String varCodeSnippet;

    private String thread1SrcFilePath;
    private String thread1CodeSnippet;
    private String thread1StackTrace;

    private String thread2SrcFilePath;
    private String thread2CodeSnippet;
    private String thread2StackTrace;

    private boolean isOpenMPRace;

    /**
     * Used to compare if two races are logical equal
     */
    private String signature;

    public DataRace(JSONObject jrace) {
        StringBuilder builder = new StringBuilder();

        JSONObject jshObj = jrace.getJSONObject("sharedObj");
        this.srcFilePath = jshObj.getString("dir") + "/" + jshObj.getString("filename");
        this.lineNum = jshObj.getInt("line");
        this.varCodeSnippet = jshObj.getString("sourceLine");

        builder.append(jshObj.getString("filename")).append('\n');
        builder.append(removeLineNumber(jshObj.getString("sourceLine")).trim()).append('\n');

        JSONObject jth1 = jrace.getJSONObject("access1");
        JSONObject jth2 = jrace.getJSONObject("access2");
        this.thread1SrcFilePath = jth1.getString("dir") + "/" + jth1.getString("filename");
        this.thread2SrcFilePath = jth2.getString("dir") + "/" + jth2.getString("filename");
        this.thread1CodeSnippet = jth1.getString("snippet");
        this.thread2CodeSnippet = jth2.getString("snippet");

        this.thread1StackTrace = parseStackTrace(jth1);
        this.thread2StackTrace = parseStackTrace(jth2);

        this.isOpenMPRace = jrace.getBoolean("isOmpRace");

        addThreadIntoSignature(builder, jth1);
        addThreadIntoSignature(builder, jth2);
        signature = builder.toString();
    }


    private void addThreadIntoSignature(StringBuilder builder, JSONObject jth) {
        builder.append(jth.getString("filename"));
        builder.append(normalizeCode(jth.getString("snippet")));
        builder.append(normalizeStackTrace(jth.getJSONArray("stacktrace")));
    }


    /**
     * For each line of code in the snippet, get rid of the line number,
     * and trim the spaces wrapping the line
     */
    private String normalizeCode(String codeSnippet) {
        StringBuilder builder = new StringBuilder();

        String[] lines = codeSnippet.split("\\n");
        for (String line : lines) {
            String s = removeLineNumber(line);
            s = s.trim();
            if (s.length() > 0)
                builder.append(s).append('\n');
        }

        return builder.toString();
    }


    /**
     * A line has the format
     *   12|   fdf dfdfd
     *
     * This method removes the line number 12 and the vertical bar.
     */
    private String removeLineNumber(String line) {
        int idx = line.indexOf('|');
        if (idx >= 0)
            return line.substring(idx+1);
        return line;
    }


    /**
     * Remove leading and trailing spaces from each line of the stack trace
     * and then concat them together
     */
    private String normalizeStackTrace(JSONArray jarr) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < jarr.size(); i++) {
            String s = jarr.getString(i).trim();
            if (s.charAt(s.length()-1) == ']') {
                for (int k = s.length()-1; k > 0; k--) {
                    if (s.charAt(k) == '[') {
                        s = s.substring(0, k);
                        break;
                    }
                }
            }
            builder.append(s).append('\n');
        }

        return builder.toString();
    }


    private String parseStackTrace(JSONObject j) {
        final String ident = "  ";
        JSONArray jstacktrace = j.getJSONArray("stacktrace");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < jstacktrace.size(); i++) {
            for (int k = 0; k < i; k++) {
                sb.append(ident);
            }
            sb.append(jstacktrace.getString(i)).append('\n');
        }

        return sb.toString();
    }


    public String getSrcFilePath() {
        return this.srcFilePath;
    }

    public int getLineNum() {
        return this.lineNum;
    }

    public String getVarCodeSnippet() {
        return varCodeSnippet;
    }

    public String getThread1SrcFilePath() {
        return this.thread1SrcFilePath;
    }

    public String getThread2SrcFilePath() {
        return this.thread2SrcFilePath;
    }

    public String getThread1CodeSnippet() {
        return this.thread1CodeSnippet;
    }

    public String getThread2CodeSnippet() {
        return this.thread2CodeSnippet;
    }

    public String getThread1StackTrace() {
        return this.thread1StackTrace;
    }

    public String getThread2StackTrace() {
        return this.thread2StackTrace;
    }

    public boolean getIsOpenMPRace() {
        return this.isOpenMPRace;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public boolean isLogicalEqualTo(AbstractRace race) {
        if (!(race instanceof DataRace))
            return false;

        DataRace tmpRace = (DataRace) race;
        if (isOpenMPRace != tmpRace.isOpenMPRace)
            return false;

        return signature.equalsIgnoreCase(((DataRace) race).signature);
    }
}
