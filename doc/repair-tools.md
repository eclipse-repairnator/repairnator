# Program Repair Tools

This page describes the program repair tools that have been implemented in Repairnator and the different strategies that can be used for each of them.

## NPEFix

[NPEFix](https://github.com/Spirals-Team/npefix) is a program repair tool developed to fix NullPointerException.
There is no specific strategy for it in Repairnator: if a NPE is detected, NPEFix is called directly to try fixing it.

It can be used [in the configuration](repairnator-config.md#REPAIR_TOOLS) with this value: `NPEFix`.

## Nopol

[Nopol](https://github.com/SpoonLabs/nopol) is a program repair tool developed to fix conditional statements.
Repairnator supports different strategies to run Nopol.
Nopol is currently configured in Repairnator to find a patch in a maximum of 4 hours.

**NopolAllTests**  With this strategy, Nopol will try to find a patch that fix all the failing tests, from all tests classes of the project. This strategy can be used [in the configuration](repairnator-config.md#REPAIR_TOOLS) with this value: `NopolAllTests`.

**NopolSingleTest** With this strategy, Nopol will be launched back for each test class with a failing test: it will try to find a patch for each test class in failure. This strategy can be used [in the configuration](repairnator-config.md#REPAIR_TOOLS) with this value: `NopolSingleTest`.

**NopolTestExclusionStrategy** In this strategy, we consider that there is more chance to find a patch for a failing test (i.e., a test with an `AssertionError`) than for an erroring test (i.e., a test which fails with any other exception).
This strategy will launch Nopol for each test class, but it will try to ignore erroring tests first to find a patch, and then it will look for a patch ignoring failing tests.

This strategy can be used [in the configuration](repairnator-config.md#REPAIR_TOOLS) with this value: `NopolTestExclusionStrategy`.

## Astor

[Astor](https://github.com/SpoonLabs/astor) is a program repair tool that uses mutation techniques and genetic programming to obtain patches.
Repairnator supports different strategies to run Astor.
Astor is currently configured in Repairnator to find a patch in a maximum of 100 minutes.

**AstorJGenProg** In this strategy, we are using Astor [with the mode JGenProg](https://github.com/SpoonLabs/astor#jgenprog), an implementation of GenProg. This strategy can be used [in the configuration](repairnator-config.md#REPAIR_TOOLS) with this value: `AstorJGenProg`.

**AstorJKali** In this strategy, we are using Astor [with the mode JKali](https://github.com/SpoonLabs/astor#jkali), an implementation of Kali. This strategy can be used [in the configuration](repairnator-config.md#REPAIR_TOOLS) with this value: `AstorJKali`.

**AstorJMut** In this strategy, we are using Astor [with the mode JMutRepair](https://github.com/SpoonLabs/astor#jmutrepair), an implementation of mutation-based repair. This strategy can be used [in the configuration](repairnator-config.md#REPAIR_TOOLS) with this value: `AstorJMut`.

## AssertFixer

[AssertFixer](https://github.com/STAMP-project/AssertFixer) is a program repair tool developed to fix the tests instead of the program.
Repairnator currently supports only one strategy for AssertFixer.

It can be used [in the configuration](repairnator-config.md#REPAIR_TOOLS) with this value: `AssertFixer`.

## Sorald
[Sorald](https://github.com/kth-tcs/sonarqube-repair) is a repair tool dedicted to fix static bugs by analyzing source codes with [Spoon](https://github.com/INRIA/spoon) 

Currently this tool supports the following [rules](https://github.com/kth-tcs/sonarqube-repair/blob/master/docs/HANDLED_RULES.md) on SonarQube 

It can be used [in the configuration](repairnator-config.md#REPAIR_TOOLS) with this value: `SoraldBot` with the corresponding handled rulenumber [in the configuration](repairnator-config.md#REPAIR_TOOLS). [Current supported ruleNumbers](https://github.com/kth-tcs/sonarqube-repair/blob/master/docs/HANDLED_RULES.md)
Sorald resolves violations of `--sonarRules` in the files changed in a specified commit. 

Moreover, the launcher mode should be set to `--launcherMode GIT_REPOSITORY` to use this tool.

Additional parameters:
* `--sonarRules`: the rules Sorald should analyze for warnings after input. Input format: 2116,1656... .
* `--gitrepourl`: the url of the target repo that should be fixed. Required only in the new version.
* `--gitcommithash`: the hash number of the commit that Sorald will fix its changed files. Required only in the new version.

## Sequencer

[Sequencer](http://arxiv.org/pdf/1901.01808) is a repair tool based on machine learning with sequence-to-sequence. You run it as follows:

    docker run -e BUILD_ID=<TRAVIS_BUILD_ID> -e REPAIR_TOOLS=SequencerRepair repairnator/pipeline

The main class is [SequencerRepair](https://github.com/eclipse/repairnator/blob/master/src/repairnator-pipeline/src/main/java/fr/inria/spirals/repairnator/process/step/repair/sequencer/SequencerRepair.java)
