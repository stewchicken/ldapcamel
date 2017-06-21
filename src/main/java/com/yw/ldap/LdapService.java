/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yw.ldap;

/**
 *
 * @author Y.W
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Y.W
 */
public class LdapService {

    private CamelContext ctx;
    private static final Logger log = LoggerFactory.getLogger(LdapService.class);

    public CamelContext getCtx() {
        return ctx;
    }

    public void setCtx(CamelContext ctx) {
        this.ctx = ctx;
    }

    // using Sun LDAP 
    public boolean checkACL(String username, String acl) {
        boolean isOk = false;
        try {
            String url = "CN=xx,OU=xx,OU=xx,OU=xx,DC=xx,DC=xx,DC=xx";
            // String userurl = "CN=" + username + suffix;
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            props.setProperty(Context.PROVIDER_URL, "ldap://<ldapserver>:<port>");
            props.setProperty(Context.URL_PKG_PREFIXES, "com.sun.jndi.url");
            props.setProperty(Context.REFERRAL, "ignore");
            props.setProperty(Context.SECURITY_AUTHENTICATION, "simple");
            props.setProperty(Context.SECURITY_PRINCIPAL, url);
            props.setProperty(Context.SECURITY_CREDENTIALS, "password");
            InitialLdapContext context = new InitialLdapContext(props, null);

            String searchFilter = "(&(objectClass=user)(sAMAccountName=" + username + "))";
            NamingEnumeration<SearchResult> answer = context.search("OU=xx,OU=xx,DC=xx,DC=xx,DC=xx", searchFilter, getSimpleSearchControls());
            List<String> ldapaclList = new ArrayList<String>();
            while (answer.hasMoreElements()) {
                SearchResult searchResult = (SearchResult) answer.next();
                String objectName = searchResult.getName();
                System.out.println(objectName);
                Attributes attrs = searchResult.getAttributes();
                Attribute attr = attrs.get("memberOf");
                javax.naming.NamingEnumeration<String> namingenum = (javax.naming.NamingEnumeration<String>) attr.getAll();
                while (namingenum.hasMore()) {
                    String acl_in_ldap = (String) namingenum.next();
                    ldapaclList.add(acl_in_ldap.toLowerCase());
                }
            }
            for (String result : ldapaclList) {

                if (result.toLowerCase().contains(acl.toLowerCase())) {
                    isOk = true;
                    break;
                }
            }
            context.close();

        } catch (Exception e) {
            isOk = false;
        }
        return isOk;
    }

    private SearchControls getSimpleSearchControls() {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setTimeLimit(30000);
        //String[] attrIDs = {"objectGUID"};
        //searchControls.setReturningAttributes(attrIDs);
        return searchControls;
    }

    // using SUN LDAP
    public boolean checkPassword(String username, String password) {
        boolean isOk = false;
        try {
            String suffix = ",OU=xx,OU=xx,OU=xx,DC=xx,DC=xx,DC=xx";
            String userurl = "CN=" + username + suffix;
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            props.setProperty(Context.PROVIDER_URL, "ldap://<ldapserver>:<port>");
            props.setProperty(Context.URL_PKG_PREFIXES, "com.sun.jndi.url");
            props.setProperty(Context.REFERRAL, "ignore");
            props.setProperty(Context.SECURITY_AUTHENTICATION, "simple");
            props.setProperty(Context.SECURITY_PRINCIPAL, userurl);
            props.setProperty(Context.SECURITY_CREDENTIALS, password);
            InitialLdapContext context = new InitialLdapContext(props, null);
            context.close();
            isOk = true;

        } catch (Exception e) {

            isOk = false;
        }
        return isOk;
    }

    // Using Camel LDAP which does not work very stable on jboss fuse 620
    public boolean containACLs(String username, Collection<String> acls) throws Exception {
        boolean contained = false;

        ProducerTemplate template = getTemplate();
        // from("activemq:queue:ldapqueue") ;
        Endpoint endpoint = ctx.getEndpoint("direct-vm:fromldapx");
        //Endpoint endpoint =  ctx.getEndpoint("activemq:queue:ldapqueue"); 
        Exchange exchange = endpoint.createExchange();
        String cnstr = "(CN=" + username + ")";
        exchange.getIn().setBody(cnstr);
        Exchange out = template.send(endpoint, exchange);
        @SuppressWarnings("unchecked")
        Collection<SearchResult> data = out.getOut().getBody(Collection.class);
        @SuppressWarnings("rawtypes")
        Iterator itr = data.iterator();
        List<String> ldapaclList = new ArrayList<String>();
        while (itr.hasNext()) {
            javax.naming.directory.SearchResult result = (javax.naming.directory.SearchResult) itr.next();
            Attributes attrs = result.getAttributes();
            Attribute attr = attrs.get("memberOf");
            @SuppressWarnings("unchecked")
            javax.naming.NamingEnumeration<String> namingenum = (javax.naming.NamingEnumeration<String>) attr.getAll();
            while (namingenum.hasMore()) {
                String acl_in_ldap = (String) namingenum.next();
                ldapaclList.add(acl_in_ldap);
            }
        }

        for (String check_acl : acls) {
            for (String acl_ldap : ldapaclList) {
                check_acl = check_acl.trim();
                if (!acl_ldap.toUpperCase().contains(check_acl.toUpperCase())) {
                    contained = false;
                } else {
                    contained = true;
                    break;
                }
            }
            if (contained == false) {
                return contained;
            }
        }

        return contained;
    }

    public boolean containACLs(String username, String password, Collection<String> acls) throws Exception {

        if (!checkPassword(username, password)) {
            throw new Exception("entered wrong password for user : " + username);
        }
        boolean contained = false;
        ProducerTemplate template = getTemplate();
        Endpoint endpoint = ctx.getEndpoint("direct:startldap");
        Exchange exchange = endpoint.createExchange();
        String cnstr = "(CN=" + username + ")";
        exchange.getIn().setBody(cnstr);
        Exchange out = template.send(endpoint, exchange);
        @SuppressWarnings("unchecked")
        Collection<SearchResult> data = out.getOut().getBody(Collection.class);
        @SuppressWarnings("rawtypes")
        Iterator itr = data.iterator();
        List<String> ldapaclList = new ArrayList<String>();
        while (itr.hasNext()) {
            javax.naming.directory.SearchResult result = (javax.naming.directory.SearchResult) itr.next();
            Attributes attrs = result.getAttributes();
            Attribute attr = attrs.get("memberOf");
            @SuppressWarnings("unchecked")
            javax.naming.NamingEnumeration<String> namingenum = (javax.naming.NamingEnumeration<String>) attr.getAll();
            while (namingenum.hasMore()) {
                String acl_in_ldap = (String) namingenum.next();
                ldapaclList.add(acl_in_ldap);
            }
        }

        for (String check_acl : acls) {
            for (String acl_ldap : ldapaclList) {
                check_acl = check_acl.trim();
                if (!acl_ldap.toUpperCase().contains(check_acl.toUpperCase())) {
                    contained = false;
                } else {
                    contained = true;
                    break;
                }
            }
            if (contained == false) {
                return contained;
            }
        }
        return contained;
    }

    protected ProducerTemplate getTemplate() {
        if (this.ctx != null) {
            return (ProducerTemplate) this.ctx.getRegistry().lookup("producer");
        } else {
            log.warn("No context!");
            return null;
        }
    }
}
