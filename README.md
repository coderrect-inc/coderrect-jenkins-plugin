# coderrect-jenkins-plugin

Coderrect is a static analysis tool for C/C++ code. Unlike popular tools such as Cppcheck and Clang static analyzer, Coderrect primarily focuses on the detection of concurrency races caused by code using explicit multi-thread API (e.g. PThread, std::thread) or implicit parallel API (e.g. OpenMP).

Besides running Coderrect to scan the local source code via the command line, you integrate it with Jenkins easily to be part of your CI/CD pipeline so that Coderrect can be triggered automatically by a pull request, a code check-in, or a scheduled nightly build.

To learn how to use Coderrect in your Jeknins's based CI/CD pipeline, check [this tutorial](https://coderrect.com/coderrect-and-jenkins-integration/)

For more information about Coderrect, please visit [here](http://coderrect.com).

## Build
```
$ mvn package
```
