package ru.sber.transport.tariff_fleet.annotation;


import ru.sber.transport.tariff_fleet.constant.DocumentType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ContractType {
    DocumentType value();
}