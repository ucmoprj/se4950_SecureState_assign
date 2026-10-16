package edu.ucmo.se4950.securestate.core;

/** Edit and save the document. */
class Editing extends UserState {

    @Override
    void toEditing(UserFunctionsMachine m) { m.log("[User] already on the Editing page"); }

    @Override
    void saveDocument(UserFunctionsMachine m, String text) {
        m.documents().setText(text);
        m.log("[User] document saved");
    }

    @Override
    String pageName() { return "Editing"; }
}
