/**
 * Веб-модуль приложения.
 */
module tariff.external.web {
    opens ru.sber.transport.web.api;

    exports ru.sber.transport.web.model;
    exports ru.sber.transport.tariff.external.web.impl;
    exports ru.sber.transport.web.api;

    requires com.fasterxml.jackson.annotation;
    requires io.swagger.v3.oas.annotations;
    requires jakarta.annotation;
    requires jakarta.servlet;
    requires jakarta.validation;
    requires org.hibernate.validator;
    requires spring.beans;
    requires spring.core;
    requires spring.context;
    requires spring.web;

    requires tariff.external.business;
    requires tariff.external.model;
    requires static lombok;
}