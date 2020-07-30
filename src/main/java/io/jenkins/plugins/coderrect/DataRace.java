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
public class DataRace {
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

    public DataRace(JSONObject jrace) {
        JSONObject jshObj = jrace.getJSONObject("sharedObj");
        this.srcFilePath = jshObj.getString("dir") + "/" + jshObj.getString("filename");
        this.lineNum = jshObj.getInt("line");
        this.varCodeSnippet = jshObj.getString("sourceLine");

        JSONObject jth1 = jrace.getJSONObject("access1");
        JSONObject jth2 = jrace.getJSONObject("access2");
        this.thread1SrcFilePath = jth1.getString("dir") + "/" + jth1.getString("filename");
        this.thread2SrcFilePath = jth2.getString("dir") + "/" + jth2.getString("filename");
        this.thread1CodeSnippet = jth1.getString("snippet");
        this.thread2CodeSnippet = jth2.getString("snippet");

        this.thread1StackTrace = parseStackTrace(jth1);
        this.thread2StackTrace = parseStackTrace(jth2);

        this.isOpenMPRace = jrace.getBoolean("isOmpRace");
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
}
