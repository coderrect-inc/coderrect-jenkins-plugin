function getProjectPath(bcfilePath) {
    var idx = bcfilePath.lastIndexOf('/');
    return bcfilePath.substring(0, idx);
}


/**
 * Returns number of spaces between "22|" and the code
 *
 * @param line - it has the format of "22|        int a = 23;"
 */
function findLeadingSpaces(line) {
    if (line.length == 0)
        return 10000;

    let num = 0;
    for (let i = 1; i < line.length; i++) {
        let ch = line.charAt(i);
        if (ch == ' ' || ch == '\t') {
            num ++;
        }
        else {
            if (num != 0) break;
        }
    }

    return num;
}


/**
 * Returns a string whith 'numOfRemovedSpaces" of leading spaces are removed
 *
 * @param line - like '22|       int a = b;'
 */
function removeSpaces(line, numOfRemovedSpaces) {
    if (line.length == 0)
        return line;

    let loc = line.indexOf(' ', 1) + 1;
    let str = line.substring(0, loc);
    loc += numOfRemovedSpaces;
    str += line.substring(loc);

    return str;
}


/**
 * @param snippet - a mutli-line string. each line has the format
 *                  22|         int a = 2
 *                  where "22" is the line number
 * @return a string removed all unnecessary identions
 */
function removeUnnecessaryIdentions(snippet) {
    const lines = snippet.split(/\r?\n/);
    let minSpaces = 10000;
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        const leadingSpaces = findLeadingSpaces(line);

        // if a line doesn't have enough leading spaces, we
        // don't need to optimize the whole snippet
        if (leadingSpaces <= 1)
            return snippet;

        if (leadingSpaces < minSpaces)
            minSpaces = leadingSpaces;
    }

    // keep one space betweek "22|" and the code
    const removedSpaces = minSpaces - 1;

    let buf = '';
    for (let i = 0; i < lines.length; i++) {
        const line2 = lines[i];
        buf += removeSpaces(line2, removedSpaces) + "\n";
    }

    return buf;
}



/**
 * @param race - json
 */
function setupOneRace(ord, race) {
    var selector = "#race" + ord + " .raceNoSpan";
    $(selector).html(""+(ord+1));

    // set href to the source file
    selector = "#race" + ord + " .raceSharedVarDiv .srcFileLink";
    var el = $(selector);
    el.href = "file://" + race.sharedObj.dir + "/" + race.sharedObj.filename;
    el.html(race.sharedObj.dir + "/" + race.sharedObj.filename);

    // set line number
    selector = "#race" + ord + " .raceSharedVarSrc .lineNumberSpan";
    $(selector).html(race.sharedObj.line);

    // set the source code define the shared variable
    selector = "#race" + ord + " .raceSharedVarDiv .codeSnippet code";
    $(selector).html(race.sharedObj.sourceLine);

    // set the field if there is
    if (race.sharedObj.field) {
        selector = "#race" + ord + " .raceSharedVarDiv .structFieldDiv code";
        $(selector).html(" Please check the field " + race.sharedObj.field);
    }
    else {
        selector = "#race" + ord + " .raceSharedVarDiv .structFieldDiv";
        $(selector).hide();
    }

    // set the openmp section
    selector = "#race" + ord + " .raceOpenMPDiv";
    if (race.isOmpRace) {
        selector = selector + " .codeSnippet code";
        $(selector).html(removeUnnecessaryIdentions(race.ompInfo.snippet));
    }
    else {
        $(selector).hide();
    }

    setThread(ord, 1, race.access1, race.isOmpRace);
    setThread(ord, 2, race.access2, race.isOmpRace);
}


/**
 * @param raceOrd start from 0
 */
function setThread(raceOrd, threadOrd, jthread, isOmpRace) {
    // set source file path
    var selector = "#race" + raceOrd + " .raceThreadsDiv .raceThreadSrcDiv" + threadOrd + " .srcFileLink";
    var el = $(selector);
    el.href = "file://" + jthread.dir + "/" + jthread.filename;
    el.html(jthread.dir + "/" + jthread.filename);

    // set code snippet
    selector = "#race" + raceOrd + " .raceThreadsDiv .raceThreadCodeSnippet" + threadOrd + " code";
    $(selector).html(removeUnnecessaryIdentions(jthread.snippet));

    // set stack trace - not very useful. may ask Yanze to generate more deeper trace
    selector = "#race" + raceOrd + " .raceThreadsDiv .raceThreadStacktrace" + threadOrd + " code";
    if (jthread.stacktrace.length > 0 && !isOmpRace) {
        var str = "";
        var ident = "";
        jthread.stacktrace.forEach(function (line) {
            str += ident + line + "\n";
            ident += "  ";
        });
        $(selector).html(str);
    }
    else {
        $("#race" + raceOrd + " .raceThreadStacktrace" + threadOrd).hide();
    }
}



function setupOneMismatchedAPI(ord, baseOrd, race) {
    var selector = "#mismatchedAPI" + ord + " .raceNoSpan";
    $(selector).html(""+(ord+baseOrd+1));

    // set href to the source file
    selector = "#mismatchedAPI" + ord + " .raceSharedVarDiv .srcFileLink";
    var el = $(selector);
    el.href = "file://" + race.inst.dir + "/" + race.inst.filename;
    el.html(race.inst.dir + "/" + race.inst.filename);

    // set line number
    selector = "#mismatchedAPI" + ord + " .raceSharedVarSrc .lineNumberSpan";
    $(selector).html(race.inst.line);

    // set the error message
    selector = "#mismatchedAPI" + ord + " .raceSharedVarSrc .explanationSpan";
    $(selector).html(race.errorMsg);

    // set the source code causing the trouble
    selector = "#mismatchedAPI" + ord + " .sourceLineDiv code";
    $(selector).html(race.inst.sourceLine);

    // set the code snippet
    selector = "#mismatchedAPI" + ord + " .codeSnippetDiv code";
    $(selector).html(race.inst.snippet);

    // set the stacktrace
    selector = "#mismatchedAPI" + ord + " .stacktraceDiv code";
    if (race.inst.stacktrace.length > 0) {
        var str = "";
        var ident = "";
        race.inst.stacktrace.forEach(function (line) {
            str += ident + line + "\n";
            ident += "  ";
        });
        $(selector).html(str);
    }
    else {
        $("#mismatchedAPI" + ord + " .stacktraceDiv").hide();
    }
}



function setupBody() {
    var i = 0;

    var html = $("#races").html();

    for ( i = 0; i < raceData.dataRaces.length; i++) {
        if (i != 0) {
            var html2 = html.replace('race0', 'race'+i);
            $("#races").append(html2);
        }
        setupOneRace(i, raceData.dataRaces[i]);
    }

    if (verData.enableMismatchedAPIs) {
        html = $("#mismatchedAPIs").html();
        for (i = 0; i < raceData.mismatchedAPIs.length; i++) {
            if (i != 0) {
                var html2 = html.replace('mismatchedAPI0', 'mismatchedAPI' + i);
                $("#mismatchedAPIs").append(html2);
            }
            setupOneMismatchedAPI(i, raceData.dataRaces.length, raceData.mismatchedAPIs[i]);
        }
    }
    else {
        $("#mismatchedAPIs").hide();
    }
}


$(document).ready(function() {
    // set up the report header
    //
    $("#projectName").html(getProjectPath(raceData.bcfile));
    $("#reportGeneratedAt").html(raceData.generatedAt);
    $("#numOfRaces").html(raceData.dataRaces.length);
    $("#coderrectVer").html(verData.Version);

    // set up the report body
    setupBody();

    $("#feedbackForm").submit(function(){
        $.modal.close();
    });
});
