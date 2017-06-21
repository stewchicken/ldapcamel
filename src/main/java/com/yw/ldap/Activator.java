/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yw.ldap;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger log = LoggerFactory.getLogger(Activator.class);
    private static String bundleName;
    private static String bundleVersion;

    public void start(BundleContext context) throws Exception {
        bundleName = context.getBundle().getSymbolicName();
        bundleVersion = context.getBundle().getVersion().toString();
        //log.info(bundleName + " " + bundleVersion + " is starting...");
        System.out.println(bundleName + " " + bundleVersion + " is starting with CAMEL ldap...");
    }

    public void stop(BundleContext context) throws Exception {
       // log.info(bundleName + " " + bundleVersion + " is ending...");
        System.out.println(bundleName + " " + bundleVersion + " is ending...");
    }

}
