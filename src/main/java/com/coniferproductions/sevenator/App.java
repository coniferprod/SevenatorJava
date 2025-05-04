package com.coniferproductions.sevenator;

import com.coniferproductions.sevenator.commands.*;
import com.coniferproductions.sevenator.datamodel.Cartridge;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "sevenator", subcommands = { Dump.class, Export.class, Import.class, Generate.class }, description = "Manages Yamaha DX7 cartridges")
public class App {
    public static final String LOGGER_NAME = "com.coniferproductions.sevenator";
    private static System.Logger logger = System.getLogger(LOGGER_NAME);

    public App() {
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}
