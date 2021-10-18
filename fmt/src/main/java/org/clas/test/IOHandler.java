package org.clas.test;

import java.lang.NumberFormatException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Handler of all input-output of the program. */
public final class IOHandler {
    private IOHandler() {}

    private static Set<Character> L1ARGS =
            new HashSet<>(Arrays.asList('c', 'n', 'v', 'V'));
    private static Set<Character> L2ARGS =
            new HashSet<>(Arrays.asList('i'));
    private static Set<Character> L3ARGS =
            new HashSet<>(Arrays.asList('s', 'x', 'y', 'z', 'X', 'Y', 'Z'));
    private static Map<String, Character> argmap;

    /** Associate char-indexed args with String-indexed args. */
    private static boolean initArgmap() {
        argmap = new HashMap<>();
        for (char argname : L1ARGS) {
            if      (argname == 'c') argmap.put("--cutsinfo",  'c');
            else if (argname == 'n') argmap.put("--nevents",   'n');
            else if (argname == 'v') argmap.put("--var",       'v');
            else if (argname == 'V') argmap.put("--variation", 'V');
            else return true; // Programmer error.
        }
        for (char argname : L2ARGS) {
            if      (argname == 'i') argmap.put("--inter",   'i');
            else return true; // Programmer error.
        }
        for (char argname : L3ARGS) {
            if      (argname == 's') argmap.put("--swim",    's');
            else if (argname == 'x') argmap.put("--dx",      'x');
            else if (argname == 'y') argmap.put("--dy",      'y');
            else if (argname == 'z') argmap.put("--dz",      'z');
            else if (argname == 'X') argmap.put("--rx",      'X');
            else if (argname == 'Y') argmap.put("--ry",      'Y');
            else if (argname == 'Z') argmap.put("--rz",      'Z');
            else return true; // Programmer error.
        }
        return false;
    }

    /** Print usage to stdout and exit. */
    private static boolean usage() {
        System.out.printf("\n");
        System.out.printf("Usage: alignment <file> [-n --nevents] [-v --var] [-i --inter]\n");
        System.out.printf("                        [-s --swim] [-c --cutsinfo] [-V --variation]\n");
        System.out.printf("                        [-x --dx] [-y --dy] [-z --dz]\n");
        System.out.printf("                        [-X --rx] [-Y --ry] [-Z --rz]\n");
        System.out.printf("  * file      : hipo input file.\n");
        System.out.printf("  * nevents   : number of events to run. If unspecified, runs all\n");
        System.out.printf("                events in input file.\n");
        System.out.printf("  * var       : variable to be aligned. Can be dXY, dZ, rXY, or rZ.\n");
        System.out.printf("  * inter (2) : [0] range between nominal position and position to\n");
        System.out.printf("                    be tested.\n");
        System.out.printf("                [1] step size for each tested value between\n");
        System.out.printf("                    <nominal - range> and <nominal + range>.\n");
        System.out.printf("  * swim  (3) : Setup for the Swim class. If unspecified, uses\n");
        System.out.printf("                default from RG-F data (-0.75, -1.0, 3.0).\n");
        System.out.printf("                [0] Solenoid magnet scale.\n");
        System.out.printf("                [1] Torus magnet scale.\n");
        System.out.printf("                [2] Torus magnet shift.\n");
        System.out.printf("  * cutsinfo  : int describing how much info on the cuts should be\n");
        System.out.printf("                printed. 0 is no info, 1 is minimal, 2 is detailed.\n");
        System.out.printf("                Default is 1.\n");
        System.out.printf("  * variation : CCDB variation to be used. Default is\n");
        System.out.printf("                ``rgf_spring2020''.\n");
        System.out.printf("  * dx    (3) : x shift for each FMT layer.\n");
        System.out.printf("  * dy    (3) : y shift for each FMT layer.\n");
        System.out.printf("  * dz    (3) : z shift for each FMT layer.\n");
        System.out.printf("  * rx    (3) : x rotation for each FMT layer.\n");
        System.out.printf("  * ry    (3) : y rotation for each FMT layer.\n");
        System.out.printf("  * rz    (3) : z rotation for each FMT layer.\n");
        System.out.printf("\n");
        System.out.printf("For example, if <var> == 'dZ', <inter> == '0.2 0.1', and\n");
        System.out.printf("<dz> == 0.5, then the values tested for z are:\n");
        System.out.printf("            (0.3, 0.4, 0.5, 0.6, 0.7).\n");
        System.out.printf("If a position or rotation is not specified, it is assumed to be 0\n");
        System.out.printf("for all FMT layers. If no argument is specified, a plot showing\n");
        System.out.printf("the residuals is shown.\n");
        System.out.printf("\n");
        System.out.printf("NOTE. All measurements are in cm, while the ccdb works in mm.\n");
        System.out.printf("\n");
        return true;
    }

    /** Check if an argument can be parsed into an int. */
    private static boolean checkInt(String arg) {
        try {Integer.parseInt(arg);}
        catch (NumberFormatException e) {return true;}
        return false;
    }

    /** Check if an argument can be parsed into a double. */
    private static boolean checkDouble(String arg) {
        try {Double.parseDouble(arg);}
        catch (NumberFormatException e) {return true;}
        return false;
    }

    /** Arguments parser. Arguments are detailed in usage(). */
    public static boolean parseArgs(String[] args, Map<Character, List<String>> params) {
        // NOTE. Better error messages here would be cool, but not strictly necessary at the moment.
        if (initArgmap()) return true;
        if (args.length < 1) return usage();

        // Get args. Can't believe that Java doesn't have a standard method for this.
        for (int i = 0; i < args.length; ++i) {
            final String argS = args[i];
            char argC;
            if (argS.charAt(0) == '-') {
                int count = 0;
                if (argS.length() < 2) return usage();
                if (argS.charAt(1) == '-') { // string-indexed argument.
                    if (argS.length() < 3) return usage();
                    if (argmap.get(argS) == null) return usage();
                    argC = argmap.get(argS);
                }
                else { // char-indexed argument.
                    if (argS.length() > 2) return usage();
                    argC = argS.charAt(1);
                }
                params.put(argC, new ArrayList<String>());
                if      (L1ARGS.contains(argC)) count = 1;
                else if (L2ARGS.contains(argC)) count = 2;
                else if (L3ARGS.contains(argC)) count = 3;
                else return usage(); // argument is not in list of accepted arguments.
                for (int j = 0; j < count; ++j) params.get(argC).add(args[++i]);
            }
            else { // positional argument.
                if (params.get('f') != null) return usage();
                params.put('f', new ArrayList<String>());
                params.get('f').add(argS);
            }
        }

        // Check that args are of correct type.
        if (params.get('f') == null) return usage();
        if (!params.get('f').get(0).endsWith(".hipo")) return usage();
        for (Map.Entry<Character, List<String>> entry : params.entrySet()) {
            Character    key = entry.getKey();
            List<String> vals = entry.getValue();
            if (vals.size() == 0) return usage();
            for (String val : vals) {
                if (val == null) return usage();
                if (key.equals('c') && checkInt(val)) return usage();
                if (key.equals('n') && checkInt(val)) return usage();
                if (key.equals('v') && (!val.equals("dXY") && !val.equals("dZ")
                                     && !val.equals("rXY") && !val.equals("rZ"))) return usage();
                if ((L2ARGS.contains(key) || L3ARGS.contains(key))
                        && checkDouble(val)) return usage();
            }
        }
        if (!((params.get('v') == null) == (params.get('i') == null))) return usage();

        return false;
    }

    /** Prints all parameters. Useful for debugging. */
    private boolean printParams(Map<Character, List<String>> params) {
        System.out.printf("\n");
        for (Character key : params.keySet()) {
            System.out.printf("\n%c :", key);
            for (String val : params.get(key)) System.out.printf(" %s", val);
        }
        System.out.printf("\n\n");

        return false;
    }
}
