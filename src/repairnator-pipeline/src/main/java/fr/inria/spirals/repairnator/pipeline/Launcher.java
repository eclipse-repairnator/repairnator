package fr.inria.spirals.repairnator.pipeline;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.stringparsers.EnumeratedStringParser;
import com.martiansoftware.jsap.stringparsers.FileStringParser;
import com.martiansoftware.jsap.Switch;

import fr.inria.spirals.repairnator.process.inspectors.ProjectInspector;
import fr.inria.spirals.repairnator.config.RepairnatorConfig;
import fr.inria.spirals.repairnator.notifier.PatchNotifier;

public class Launcher implements LauncherAPI {
    private static LauncherAPI launcher;

    public Launcher() {}
    
    public Launcher(String[] args) throws JSAPException{
        init(args);
    }

    public static void init(String[] args) throws JSAPException{
        JSAP jsap = defineBasicArgs();
        JSAPResult res = jsap.parse(args);
        String choice = res.getString("launcherChoice");
        if (choice.equals("OLD")) {
            launcher = new LegacyLauncher(args);
        } else {
            launcher = new BranchLauncher(args);
        }
    }

    public static JSAP defineBasicArgs() throws JSAPException {
        JSAP jsap = new JSAP();

        FlaggedOption opt2 = new FlaggedOption("launcherChoice");
        opt2.setLongFlag("launcherChoice");
        opt2.setStringParser(JSAP.STRING_PARSER);
        opt2.setDefault("OLD");
        opt2.setHelp("OLD: Original Launcher. NEW: new launcher.");
        jsap.registerParameter(opt2);

        return jsap;
    }

    public static void main(String[] args) throws JSAPException{
        Launcher main = new Launcher(args);
        main.launch();
    }

    @Override
    public boolean mainProcess() {
        return launcher.mainProcess();
    }

    @Override
    public void launch() {}

    @Override
    public JSAP defineArgs() throws JSAPException{
        return launcher.defineArgs();
    }

    @Override
    public RepairnatorConfig getConfig() {
        return launcher.getConfig();
    }

    @Override
    public ProjectInspector getInspector() {
        return launcher.getInspector();
    }

    @Override
    public void setPatchNotifier(PatchNotifier patchNotifier) {
        launcher.setPatchNotifier(patchNotifier);
    }
}