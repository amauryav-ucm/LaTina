open module latina {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires javafx.web;
    requires jdk.jsobject;
    requires org.hibernate.orm.core;
    requires jdk.jfr;

    exports latina.negocio.rol to org.hibernate.orm.core;
    exports latina;
}
