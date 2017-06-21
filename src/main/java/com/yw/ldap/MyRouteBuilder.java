/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.yw.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jetty.JettyHttpComponent;
import org.apache.camel.spring.Main;
import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;

/**
 * A Camel Router
 */
public class MyRouteBuilder extends RouteBuilder {

    com.yw.ldap.LdapService ldapService;

    private static final Logger log = LoggerFactory.getLogger(MyRouteBuilder.class);

    public LdapService getLdapService() {
        return ldapService;
    }

    public void setLdapService(LdapService ldapService) {
        this.ldapService = ldapService;
    }

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        Main.main(args);
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    private void setupHttpsRestService() throws Exception {
        //prepare one way SSL for fuse server
        KeyStoreParameters ksp = new KeyStoreParameters();
        ksp.setResource("<PATH TO KEYSTORE FILE (.jks)>");
        ksp.setPassword("<PASSWORD OF KEYSTORE>");
        KeyManagersParameters kmp = new KeyManagersParameters();
        kmp.setKeyStore(ksp);
        kmp.setKeyPassword("<PASSWORD OF KEY>");
        SSLContextParameters scp = new SSLContextParameters();
        scp.setKeyManagers(kmp);
        JettyHttpComponent jetty = this.getContext().getComponent("jetty", JettyHttpComponent.class);
        jetty.setSslContextParameters(scp);

        //set up route
        final Endpoint jettyEndpoint = jetty.createEndpoint("jetty:https://<FUSE>:<PORT>/service");
        from(jettyEndpoint).process(new MyBookService());
    }

    public void configure() {
        try {
            from("direct-vm:fromldapx").to("ldap:ldapserver?base=OU=xx,OU=xxx,DC=xx,DC=xx,DC=xx");
            setupHttpsRestService();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

    }
}
