/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.controller.CheckController;
import hexlet.code.controller.RootController;
import hexlet.code.controller.UrlController;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

import static hexlet.code.repository.BaseRepository.dataSource;

/**
 * Main class with getApp function.
 */
public class App {

    public static Logger logger = LoggerFactory.getLogger(App.class);

    /**
     * Starting app.
     * @param args
     */
    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }

    /**
     * @return port number.
     */
    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "8080");

        logger.info("Received port {}", port);

        return Integer.parseInt(port);
    }

    /**
     * @return data source which needs to get db connection.
     */
    public static @NotNull HikariDataSource getDataSource() throws IOException, SQLException {
        String jdbc = System
                .getenv()
                .getOrDefault("JDBC_DATABASE_URL", "jdbc:h2:mem:hexlet_project;DB_CLOSE_DELAY=-1;");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbc);
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        logger.info("database url " + jdbc);

        return dataSource;
    }

    /**
     * @return customized app with prepared db.
     */
    public static @Nullable Javalin getApp() {
        try {
            dataSource = getDataSource();
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return null;
        }

        InputStream schemaStream = App.class.getClassLoader().getResourceAsStream("schema.sql");
        if (schemaStream == null) {
            logger.warn("schema.sql not found");
            return null;
        }
        String sql = new BufferedReader(new InputStreamReader(schemaStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            logger.warn(e.getSQLState());
            return null;
        }

        JavalinJte.init(createTemplateEngine());

        Javalin app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
        });

        addRoutes(app);

        return app;
    }

    /**
     * @return templates handler
     */
    private static @NotNull TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        return TemplateEngine.create(codeResolver, ContentType.Html);
    }

    /**
     * routes handler.
     * @param app
     */
    private static void addRoutes(@NotNull Javalin app) {
        app.get(NamedRoutes.rootPath(), RootController::getRootPage);
        app.get(NamedRoutes.urlsPath(), UrlController::showUrls);
        app.post(NamedRoutes.urlsPath(), UrlController::addUrl);
        app.get(NamedRoutes.urlPath("{id}"), UrlController::showUrl);
        app.post(NamedRoutes.checkUrl("{id}"), CheckController::addCheck);
    }
}
