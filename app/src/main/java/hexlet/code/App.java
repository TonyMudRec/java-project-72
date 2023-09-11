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
import hexlet.code.repository.BaseRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class App {

    public static void main(String[] args) throws SQLException, IOException {
        Javalin app = getApp();
        app.start(getPort());
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "8080");
        return Integer.valueOf(port);
    }

    public static Javalin getApp() throws IOException, SQLException {
        HikariConfig hikariConfig = new HikariConfig();
        String jdbc = System
                .getenv()
                .getOrDefault("JDBC_DATABASE_URL", "jdbc:h2:mem:hexlet_project;DB_CLOSE_DELAY=-1;");
        hikariConfig.setJdbcUrl(jdbc);
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        InputStream schemaStream = App.class.getClassLoader().getResourceAsStream("schema.sql");
        if (schemaStream == null) {
            throw new FileNotFoundException();
        }
        String sql = new BufferedReader(new InputStreamReader(schemaStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
        BaseRepository.dataSource = dataSource;

        JavalinJte.init(createTemplateEngine());

        Javalin app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
        });

        addRoutes(app);

        return app;
    }

    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        return TemplateEngine.create(codeResolver, ContentType.Html);
    }

    private static void addRoutes(Javalin app) {
        app.get(NamedRoutes.rootPath(), RootController::getRootPage);
        app.get(NamedRoutes.urlsPath(), UrlController::showUrls);
        app.post(NamedRoutes.urlsPath(), UrlController::addUrl);
        app.get(NamedRoutes.urlPath("{id}"), UrlController::showUrl);
        app.post(NamedRoutes.checkUrl("{id}"), CheckController::addCheck);
    }
}
