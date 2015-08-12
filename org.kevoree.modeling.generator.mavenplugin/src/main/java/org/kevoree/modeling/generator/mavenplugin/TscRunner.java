package org.kevoree.modeling.generator.mavenplugin;

import de.flapdoodle.embed.nodejs.*;
import de.flapdoodle.embed.process.config.IRuntimeConfig;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by gregory.nain on 07/11/14.
 */
public class TscRunner {

    //runTsc(String tscPath, Path sourceDir, Path targetFile) throws Exception {
    public static void runTsc(File src, File target, File[] libraries, boolean copyLibDTs, boolean umd) throws Exception {

        Files.createDirectories(target.toPath());

        ArrayList<String> paramsCol = new ArrayList<String>();
        File[] files = src.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".ts")) {
                paramsCol.add(files[i].getAbsolutePath());
            }
        }
        if (libraries != null) {
            for (int i = 0; i < libraries.length; i++) {
                File[] lib = libraries[i].listFiles();
                for (int j = 0; j < lib.length; j++) {
                    if (lib[j].getName().endsWith(".ts")) {
                        paramsCol.add(lib[j].getAbsolutePath());
                    }
                }
            }
        }
        File targetTSCBIN = new File(src, "tsc.js");
/*
        File targetLIBD = new File(src, "lib.d.ts");
        Files.copy(TscRunner.class.getClassLoader().getResourceAsStream("tsc.js"), targetTSCBIN.toPath(), StandardCopyOption.REPLACE_EXISTING);

        File targetLIBD = null;
        if (copyLibDTs) {
            Files.copy(TscRunner.class.getClassLoader().getResourceAsStream("tsc/lib.d.ts"), targetLIBD.toPath(), StandardCopyOption.REPLACE_EXISTING);
            boolean founded = false;
            for (String alreadyAdded : paramsCol) {
                if (alreadyAdded.endsWith("lib.d.ts")) {
                    founded = true;
                }
            }
            if (!founded) {
                paramsCol.add(targetLIBD.getAbsolutePath());
            }
        }*/

        paramsCol.add("--outDir");
        paramsCol.add(target.getAbsolutePath());

        paramsCol.add("-d");

        if(umd){
            paramsCol.add("--module");
            paramsCol.add("umd");
        }


        if (testNativeNode()) {
            System.out.println("Native NodeJS installed on the machine, using it to compile to JS");
            String[] params = new String[paramsCol.size() + 2];
            for (int i = 0; i < paramsCol.size(); i++) {
                params[i + 2] = paramsCol.get(i);
            }
            if (getOS().equals(OSType.Windows)) {
                params[0] = "node.exe";
            } else {
                params[0] = "node";
            }
            params[1] = targetTSCBIN.getAbsolutePath();
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

            NodejsConfig nodejsConfig = new NodejsConfig(NodejsVersion.Main.V0_10, targetTSCBIN.getAbsolutePath(), paramsCol, target.getAbsolutePath());
            NodejsStarter runtime = new NodejsStarter(runtimeConfig);
            try {
                NodejsExecutable e = runtime.prepare(nodejsConfig);
                node = e.start();
                int retVal = node.waitFor();
                if (retVal != 0) {
                    throw new Exception("There were TypeScript compilation errors.");
                }
            } catch (InterruptedException var11) {
                var11.printStackTrace();
            } finally {
                if (node != null) {
                    node.stop();
                }
                /*
                if (targetTSCBIN != null) {
                    targetTSCBIN.delete();
                }
                if (targetLIBD != null) {
                    targetLIBD.delete();
                }*/
            }
        }
    }


    /*
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



        private void tsc(String tscPath, String... args) throws Exception {

        }
    */
    private static boolean testNativeNode() {
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

    public enum OSType {
        Windows, MacOS, Linux, Other
    }

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
