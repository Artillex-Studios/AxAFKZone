package com.artillexstudios.axafkzone.libraries;

import com.artillexstudios.axapi.libs.libby.Library;
import com.artillexstudios.axapi.libs.libby.relocation.Relocation;

public enum Libraries {

    MATH3("org{}apache{}commons:commons-math3:3.6.1");

    private final Library library;

    public Library getLibrary() {
        return library;
    }

    Libraries(String lib, Relocation relocation) {
        this.library = createLibrary(lib, relocation);
    }

    Libraries(String lib) {
        this.library = createLibrary(lib, null);
    }

    private Library createLibrary(String lib, Relocation relocation) {
        String[] split = lib.split(":");
        Library.Builder builder = Library.builder()
                .groupId(split[0])
                .artifactId(split[1])
                .version(split[2]);

        if (relocation != null) {
            builder.relocate(relocation);
        }

        return builder.build();
    }
}
