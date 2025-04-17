module sevenator {
    requires java.sql;
    requires batik.all;
    requires info.picocli;

    exports com.coniferproductions.sevenator;
    opens com.coniferproductions.sevenator.commands to info.picocli;
}