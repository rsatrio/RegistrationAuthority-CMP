package com.rizky.ra.cmp;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.servlet.FilterRegistration;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import com.rizky.ra.cmp.auth.RaApiAuthorizer;
import com.rizky.ra.cmp.auth.RaAuthenticatorJWT;
import com.rizky.ra.cmp.auth.TokenClaims;
import com.rizky.ra.cmp.resources.CMPResource;
import com.rizky.ra.cmp.resources.RaResource;

//import org.apache.http.client.HttpClient;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

public class RaCmpApplication extends Application<RaCmpConfiguration> {

    Logger log1=LoggerFactory.getLogger(RaCmpApplication.class);

    public static void main(final String[] args) throws Exception {

        new RaCmpApplication().run(args);
    }

    @Override
    public String getName() {
        return "RA-CMP";
    }



    @Override
    public void run(final RaCmpConfiguration configuration,
            final Environment environment) {

        
        final CMPResource res2=new CMPResource();
        final RaResource res3=new RaResource();

        final JdbiFactory factory = new JdbiFactory();
        final Jdbi jdbi = factory.build(environment, configuration.getDataSourceFactory(), "h2mem");

        RaCmpSingleton.getSingleton().setJdbi(jdbi);
        RaCmpSingleton.getSingleton().setConfig(configuration);

        startLiquiBase(jdbi);

        FilterRegistration.Dynamic registration = environment.servlets()
                .addFilter("UrlRewriteFilter", new UrlRewriteFilter());

        registration.addMappingForUrlPatterns(null, true, "/*");
        registration.setInitParameter("confPath", "urlrewrite.xml");

        //Register Authentication&Authorize

        environment.jersey().register(new AuthDynamicFeature(new OAuthCredentialAuthFilter.Builder<TokenClaims>()
                .setAuthenticator(new RaAuthenticatorJWT(configuration))
                .setAuthorizer(new RaApiAuthorizer())
                .setPrefix("Bearer")
                .buildAuthFilter()));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(TokenClaims.class));

        
        environment.jersey().register(res2);
        environment.jersey().register(res3);

        //Load keystore
        KeyStore ks=null;
        try {
            ks=KeyStore.getInstance("pkcs12");
            ks.load(new FileInputStream(configuration.getKeyStorePath()), 
                    configuration.getKeyPass().toCharArray());
            RaCmpSingleton.getSingleton().setKeyStore(ks);
        }
        catch(Exception e2)  {
            log1.error("Failed to load Keystore. Shutting down...",e2);
            System.exit(10);
        }



    }

    @Override
    public void initialize(final Bootstrap<RaCmpConfiguration> bootstrap) {

        bootstrap.addBundle(new AssetsBundle("/ra-cmp/","/ra","index.html"));


        bootstrap.addBundle(new SwaggerBundle<RaCmpConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(RaCmpConfiguration configuration) {
                return configuration.getSwagger();
            }
        });
    }

    private void startLiquiBase(Jdbi jdbi) {

        Liquibase liq=null;

        try {
            liq=new Liquibase("rachangelog.xml", new ClassLoaderResourceAccessor(),
                    new JdbcConnection(jdbi.open().getConnection()));
            Handle handlejdbi=null;
            try {
                handlejdbi=jdbi.open();
                handlejdbi.createQuery("select count(1) from DATABASECHANGELOG")
                .mapTo(String.class).one();
            }
            catch(Exception e)  {                
                //                liq.changeLogSync("Initial");                
            }
            finally  {
                handlejdbi.close();
            }

            //Run LiquiBase Update

            liq.update("Initial");            

        }
        catch(Exception e)  {
            e.printStackTrace();
        }
        finally {
            try {
                liq.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


}
