package org.kevoree.modeling.microframework.typescript;

import de.flapdoodle.embed.nodejs.NodejsConfig;
import de.flapdoodle.embed.nodejs.NodejsExecutable;
import de.flapdoodle.embed.nodejs.NodejsProcess;
import de.flapdoodle.embed.nodejs.NodejsRuntimeConfigBuilder;
import de.flapdoodle.embed.nodejs.NodejsStarter;
import de.flapdoodle.embed.nodejs.NodejsVersion;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.junit.Assert;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by gregory.nain on 28/11/14.
 */
public class NoJs_MainTest {

    private static int launchRunner(String file) {

        IRuntimeConfig runtimeConfig = (new NodejsRuntimeConfigBuilder()).defaults().build();
        NodejsProcess node = null;
        try {
            String basePath = NoJs_MainTest.class.getClassLoader().getResource(file).getFile().replaceAll("%20", " ");
            NodejsConfig nodejsConfig = new NodejsConfig(NodejsVersion.Main.V0_10, basePath, new ArrayList<String>(), basePath.toString().substring(0, basePath.toString().lastIndexOf("/")));
            NodejsStarter runtime = new NodejsStarter(runtimeConfig);
            NodejsExecutable e = (NodejsExecutable) runtime.prepare(nodejsConfig);
            node = (NodejsProcess) e.start();
            return node.waitFor();
        } catch (InterruptedException var11) {
            var11.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (node != null) {
                node.stop();
            }
        }
        return -1;
    }


    @Test
    public void baseTest() {
        Assert.assertEquals(0, launchRunner("MyTestRunner.js"));
    }


}
