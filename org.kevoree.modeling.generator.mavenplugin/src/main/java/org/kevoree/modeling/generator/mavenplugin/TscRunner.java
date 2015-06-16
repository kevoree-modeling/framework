package org.kevoree.modeling.generator.mavenplugin;

import de.flapdoodle.embed.nodejs.*;
import de.flapdoodle.embed.process.config.IRuntimeConfig;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by gregory.nain on 07/11/14.
 */
public class TscRunner {


    public void runTsc(String tscPath, Path sourceDir, Path targetFile) throws Exception {
        List<String> params = new ArrayList<String>();
        //params.add("--sourcemap");
        params.add("-d");
        params.add("--out");
        params.add(targetFile.toFile().getAbsolutePath());

        File[] selected = sourceDir.toFile().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".ts")) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        HashMap<String, File> filteredHeaders = new HashMap<String, File>();
        for (int i = 0; i < selected.length; i++) {
            if (!selected[i].getName().endsWith(".d.ts")) {
                filteredHeaders.put(selected[i].getName().replace(".ts", ""), selected[i]);
            }
        }
        for (int i = 0; i < selected.length; i++) {
            if (selected[i].getName().endsWith(".d.ts")) {
                if(filteredHeaders.get(selected[i].getName().replace(".d.ts",""))==null){
                    params.add(selected[i].getAbsolutePath());
                }
            } else {
                params.add(selected[i].getAbsolutePath());
            }
        }
        tsc(tscPath, params.toArray(new String[params.size()]));
    }

    private boolean testNativeNode() {
        String[] params = new String[2];
        if (getOS().equals(OSType.Windows)) {
            params[0] = "node.exe";
        } else {
            params[0] = "node";
        }
        params[1] = "-v";
        ProcessBuilder pb = new ProcessBuilder(params);
        pb.redirectError();
        pb.redirectOutput();
        try {
            int res = pb.start().waitFor();
            return res == 0;
        } catch (InterruptedException e) {
        } catch (IOException e) {
        }
        return false;
    }

    private void tsc(String tscPath, String... args) throws Exception {
        if (testNativeNode()) {
            System.out.println("Native NodeJS installed on the machine, using it to compile to JS");
            String[] params = new String[args.length + 2];
            for (int i = 0; i < args.length; i++) {
                params[i + 2] = args[i];
            }
            if (getOS().equals(OSType.Windows)) {
                params[0] = "node.exe";
            } else {
                params[0] = "node";
            }
            params[1] = tscPath;
            ProcessBuilder pb = new ProcessBuilder(params);
            pb.redirectError();
            pb.redirectOutput();
            int res = pb.start().waitFor();
            if (res != 0) {
                StringBuilder builder = new StringBuilder();
                for (String s : params) {
                    builder.append(" " + s);
                }
                throw new Exception("Compilation error, please check your console " + builder.toString());
            }
        } else {
            IRuntimeConfig runtimeConfig = (new NodejsRuntimeConfigBuilder()).defaults().build();
            NodejsProcess node = null;
            ArrayList<String> paramsCol = new ArrayList<String>();
            for (int i = 0; i < args.length; i++) {
                paramsCol.add(args[i]);
            }
            NodejsConfig nodejsConfig = new NodejsConfig(NodejsVersion.Main.V0_10, tscPath, paramsCol, System.getProperty("java.io.tmpdir"));
            NodejsStarter runtime = new NodejsStarter(runtimeConfig);
            try {
                NodejsExecutable e = (NodejsExecutable) runtime.prepare(nodejsConfig);
                node = (NodejsProcess) e.start();
                node.waitFor();
            } catch (InterruptedException var11) {
                var11.printStackTrace();
            } finally {
                if (node != null) {
                    node.stop();
                }
            }
        }

        /*
        if (res != 0) {
            System.err.println("NodeJS not found, try Java embedded version ...");
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            engine.eval(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("node.js")));
            StringBuilder param = new StringBuilder();
            param.append("process.argv = [\"node\",\"node\"");
            for (String p : args) {
                param.append(",\"" + p + "\"");
            }
            param.append("];\n");
            engine.eval(param.toString());
            engine.eval(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("tsc.js")));
        }*/
    }

    public enum OSType {
        Windows, MacOS, Linux, Other
    }

    ;

    public static OSType getOS() {
        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
            return OSType.MacOS;
        } else if (OS.indexOf("win") >= 0) {
            return OSType.Windows;
        } else if (OS.indexOf("nux") >= 0) {
            return OSType.Linux;
        } else {
            return OSType.Other;
        }
    }


}
