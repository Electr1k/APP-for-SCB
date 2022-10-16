package com.example.appforsb.model;

import org.w3c.dom.Text;

import java.lang.reflect.Array;

public class Patent {


    String inventorCard;
    String number, name, publication_date, filing_date;
    String ipc;
    String inventor;
    String patente;
    String description;
    String priority;
    String id;

    public Patent(String id,String inventorCard, String number, String name, String publication_date, String filing_date, String ipc, String inventor, String patente, String description, String priority) {
        this.id = id;
        this.inventorCard = inventorCard;
        this.number = number;
        this.name = name;
        this.publication_date = publication_date;
        this.filing_date = filing_date;
        this.ipc = ipc;
        this.inventor = inventor;
        this.patente = patente;
        this.description = description;
        this.priority = priority;
    }
    public String getId() {
        return id;
    }


    public String getPriority() {
        return priority;
    }

    public String getInventorCard() {
        return inventorCard;
    }

    public void setInventorCard(String inventorCard) {
        this.inventorCard = inventorCard;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublication_date() {
        return publication_date;
    }

    public void setPublication_date(String publication_date) {
        this.publication_date = publication_date;
    }

    public String getFiling_date() {
        return filing_date;
    }

    public void setFiling_date(String filing_date) {
        this.filing_date = filing_date;
    }

    public String getIpc() {
        return ipc;
    }

    public void setIpc(String ipc) {
        this.ipc = ipc;
    }

    public String getInventor() {
        return inventor;
    }

    public void setInventor(String inventor) {
        this.inventor = inventor;
    }

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
