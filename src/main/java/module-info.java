module org.andrei.sample_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    // --- DB & Security Dependencies ---
    requires java.sql;
    requires org.postgresql.jdbc;
    requires jbcrypt;  // For BCrypt

    // --- Exports ---
    exports org.andrei.sample_project;
    exports org.andrei.sample_project.connection;
    exports org.andrei.sample_project.repository;

    // --- Opens (Allowing reflection) ---
    opens org.andrei.sample_project.connection to java.sql;
    opens org.andrei.sample_project.repository to java.sql;

    // Open the main package to FXML and reflection
    opens org.andrei.sample_project to java.sql, javafx.base, javafx.fxml;
}