/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package sshd.shell.springboot.autoconfiguration;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import static java.util.concurrent.TimeUnit.SECONDS;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import static org.awaitility.Awaitility.await;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author anand
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ConfigTest.class, properties = {"sshd.shell.publicKeyFile=src/test/resources/id_rsa.pub",
    "banner.image.location=banner.png"})
public class SshdShellAutoConfigurationWithPublicKeyAndBannerImageTest {

    @Autowired
    private SshdShellProperties properties;

    @Test
    public void testTestCommand() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession("admin", "localhost", properties.getShell().getPort());
        jsch.addIdentity("src/test/resources/id_rsa");
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("test run bob\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        await().atMost(2, SECONDS).until(() -> os.toString().contains("Enter 'help' for a list of supported commands\n"
                + "\rapp> test run bob\r\ntest run bob\n\rapp> ")); 
        channel.disconnect();
        session.disconnect();
    }
}
