package edu.ucmo.se4950.securestate.core;

/** Default page after login: read the document. */
class Viewing extends UserState {

    @Override
    void toViewing(UserFunctionsMachine m) { m.log("[User] already on the Viewing page"); }

    @Override
    String pageName() { return "Viewing"; }
}
