package edu.ucmo.se4950.securestate.core;

/** The single shared document that users view and edit. */
public class DocumentStore {

    private String text = "Quarterly report draft.\nEdit this text on the Editing page.";

    synchronized String getText() { return text; }

    synchronized void setText(String text) { this.text = text == null ? "" : text; }
}
