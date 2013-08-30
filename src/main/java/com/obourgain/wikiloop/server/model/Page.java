package com.obourgain.wikiloop.server.model;

public class Page {
    String name;
    boolean inLoop;

    public Page(String name) {
        this.name = name;
        inLoop = false;
    }

    public Page(String name, boolean inLoop) {
        this.name = name;
        this.inLoop = inLoop;
    }

    public String getName() {
        return name;
    }

    public boolean isInLoop() {
        return inLoop;
    }

    @Override
    public String toString() {
        return name;
    }

}
