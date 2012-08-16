package polygonsSWP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.UIManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import polygonsSWP.analysis.AlgorithmRunner;
import polygonsSWP.analysis.CsvLog;
import polygonsSWP.analysis.PolygonLog;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;
import polygonsSWP.generators.heuristics.IncrementalConstructionAndBacktrackingFactory;
import polygonsSWP.generators.heuristics.SpacePartitioningFactory;
import polygonsSWP.generators.heuristics.SteadyGrowthFactory;
import polygonsSWP.generators.heuristics.TwoOptMovesFactory;
import polygonsSWP.generators.heuristics.VelocityVirmaniFactory;
import polygonsSWP.generators.other.ConvexHullGeneratorFactory;
import polygonsSWP.generators.other.EnumeratingPermuteAndRejectFactory;
import polygonsSWP.generators.other.PermuteAndRejectFactory;
import polygonsSWP.generators.other.TwoPeasantsGeneratorFactory;
import polygonsSWP.generators.rpa.RandomPolygonAlgorithmFactory;
import polygonsSWP.gui.MainFrame;
import polygonsSWP.util.Random;

/**
* (c) 2011-2012
* @author Steve Dierker <dierker.steve@fu-berlin.de>
* @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
* @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
* @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
* @author Sebastian Thobe <s.thobe@fu-berlin.de>
* @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class PolygonsMain
{
  /*
   * This is the place to add new PolygonGenerators.
   */
  private static PolygonGeneratorFactory[] factories = {
      new SpacePartitioningFactory(), new PermuteAndRejectFactory(),
      new TwoOptMovesFactory(), new RandomPolygonAlgorithmFactory(),
      new IncrementalConstructionAndBacktrackingFactory(),
      new ConvexHullGeneratorFactory(), new VelocityVirmaniFactory(),
      new SteadyGrowthFactory(), new TwoPeasantsGeneratorFactory(),
      new EnumeratingPermuteAndRejectFactory() };

  public static void main(String[] args) throws Exception {
    // Process command line.
    CommandLineParser clp = new CommandLineParser();
    if (!clp.parseCommandLine(args)) {
      clp.help();
      return;
    }

    /*
     * 1. If --help or --usage is specified, display help. 2. If any parameters
     * are specified, run the AlgoRunner, using ConsoleWriter if --output is not
     * given, DataWriter for --output. 3. If none of the above is specified,
     * show the GUI, disregarding any other option. + In case a user-supplied
     * value is inapplicable or has a wrong format, tell the user and display
     * help.
     */

    if (clp.isHelp()) {
      clp.help();
      return;
    }

    Random.pseudoRandom(false, 1322691L);

    // Show GUI if no parameters.
    if (clp.getParameterNumber() == 0) {
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new MainFrame(factories);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      return;
    }

    // Read parameters & perform sanity checks.
    int algorithm = clp.getAlgorithm();
    if (algorithm < 0 || algorithm >= factories.length) {
      System.err.println("Algorithm ID out of range.");
      clp.help();
      return;
    }

    boolean header = !clp.getNoHeader();
    boolean statistics = !clp.getNoStatistics();
    int number = clp.getNumber();
    int threads = clp.getThreads();
    String database = clp.getDatabase();
    PolygonGeneratorFactory factory = factories[clp.getAlgorithm()];
    
    PolygonLog log = null;
    if(database != null) {
    
    } else {
      String output = clp.getOutputPath();
      OutputStream os = (output != null) ? new FileOutputStream(new File(output)) : System.out;
      log = new CsvLog(os, header, statistics);
    }

    // Construct parameter map.
    Map<Parameters, Object> params = new HashMap<Parameters, Object>();
    params.put(Parameters.n, clp.getPoints());
    params.put(Parameters.size, clp.getBoundingBox());
    if (factory instanceof VelocityVirmaniFactory) {
      params.put(Parameters.runs, clp.getRuns());
      params.put(Parameters.radius, clp.getRadius());
      params.put(Parameters.velocity, clp.getVelocity());
    }
    else {
      if (clp.hasRuns() || clp.hasRadius() || clp.hasVelocity()) {
        System.err.println("Use --runs, --velocity, and --radius for Virmani's velocity algorithm.");
        clp.help();
        return;
      }
    }

    AlgorithmRunner.run(number, threads, log, factory, params);
  }

  private static class CommandLineParser
  {
    private CommandLine cl;
    private Options opts;

    // default values:
    private final int defaultAlgorithm = 0;
    private final int defaultNumber = 1;
    private final int defaultPoints = 100;
    private final int defaultThreads = 4;
    private final int defaultRuns = 100;
    private final int defaultRadius = 400;
    private final int defaultVelocity = 20;
    private final int defaultBoundingBox = 1000;

    public void help() {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(
          "run.sh",
          "-\nUse with no arguments to show GUI.\n--database will override --output, if neither is given, csv output will be directed to console.-\n",
          opts,
          "\nExample:\n$ ./run.sh --number 1000 --points 500 --output polygon.csv",
          true);
    }

    @SuppressWarnings("static-access")
    public boolean parseCommandLine(String[] args) {

      Option help =
          OptionBuilder.withLongOpt("help").withArgName("Help").withDescription(
              "Displays this help message").hasArg(false).isRequired(false).create();

      Option usage =
          OptionBuilder.withLongOpt("usage").withArgName("usage").withDescription(
              "Displays this help message").hasArg(false).isRequired(false).create();

      StringBuilder sb = new StringBuilder();
      sb.append("The algorithm to execute, specified by ID (Default: " +
          defaultAlgorithm + "). Available algorithms:\n");
      for (int i = 0; i < factories.length; i++) {
        sb.append("[" + i + "] ");
        sb.append(factories[i].toString());
        if (i < factories.length - 1) sb.append("\n");
      }
      Option algorithm =
          OptionBuilder.withLongOpt("algorithm").withArgName("Algorithm").withDescription(
              sb.toString()).hasArg().isRequired(false).withType(Integer.class).create();

      Option points =
          OptionBuilder.withLongOpt("points").withArgName("Number of points").withDescription(
              "The number of points of the polygons to generate (Default: " +
                  defaultPoints + ")").hasArg().isRequired(false).withType(
              Integer.class).create();

      Option number =
          OptionBuilder.withLongOpt("number").withArgName("Number of polygons").withDescription(
              "The number of polygons to generate (Default: " + defaultNumber +
                  ")").hasArg().isRequired(false).withType(Integer.class).create();

      Option threads =
          OptionBuilder.withLongOpt("threads").withArgName("Number of threads").withDescription(
              "The number of threads to create (Default: " + defaultThreads +
                  ")").hasArg().isRequired(false).withType(Integer.class).create();

      Option output =
          OptionBuilder.withLongOpt("output").withArgName("Output path").withDescription(
              "When specified, the generated polygons (and statistics) will be saved in this File.").hasArg().isRequired(
              false).withType(String.class).create();
      
      Option database =
          OptionBuilder.withLongOpt("database").withArgName("Database path").withDescription(
              "When specified, the generated polygons (and statistics) will be saved in this Database.").hasArg().isRequired(
              false).withType(String.class).create();

      Option statistics =
          OptionBuilder.withLongOpt("no-statistics").withArgName(
              "No statistics").withDescription(
              "Do not include statistics into output.").isRequired(false).create();

      Option header =
          OptionBuilder.withLongOpt("no-header").withArgName("No header").withDescription(
              "Do not include a header in the output,").isRequired(false).create();

      Option runs =
          OptionBuilder.withLongOpt("runs").withArgName("number of runs").withDescription(
              "Number of iterations (Virmani's Velocity algorithm, default: " +
                  defaultRuns + ")").hasArg().withType(Integer.class).isRequired(
              false).create();

      Option radius =
          OptionBuilder.withLongOpt("radius").withArgName("circle radius").withDescription(
              "Radius of circle for initial regular polygon (Virmani's Velocity algorithm, default: " +
                  defaultRadius + ")").hasArg().withType(Integer.class).isRequired(
              false).create();

      Option velocity =
          OptionBuilder.withLongOpt("velocity").withArgName("velocity").withDescription(
              "Maximum velocity of moving points (Virmani's Velocity algorithm, default: " +
                  defaultVelocity + ")").hasArg().withType(Integer.class).isRequired(
              false).create();

      Option boundingbox =
          OptionBuilder.withLongOpt("boundingbox").withArgName("boundingbox").withDescription(
              "Length of the sides of the surrounding bounding square (default: " +
                  defaultBoundingBox + ")").hasArg().withType(Integer.class).isRequired(
              false).create();

      opts = new Options();
      opts.addOption(help);
      opts.addOption(usage);
      opts.addOption(algorithm);
      opts.addOption(points);
      opts.addOption(number);
      opts.addOption(threads);
      opts.addOption(output);
      opts.addOption(database);
      opts.addOption(statistics);
      opts.addOption(header);
      opts.addOption(runs);
      opts.addOption(radius);
      opts.addOption(velocity);
      opts.addOption(boundingbox);

      GnuParser parser = new GnuParser();
      try {
        cl = parser.parse(opts, args, true);
      }
      catch (ParseException e) {
        System.err.println("Could not parse command line: " + e.getMessage());
        return false;
      }

      if (cl.getArgs().length != 0) return false;

      return true;
    }

    public String getDatabase() {
      return cl.getOptionValue("database");
    }
    
    public String getOutputPath() {
      return cl.getOptionValue("output");
    }

    public boolean isHelp() {
      return cl.hasOption("help") || cl.hasOption("usage");
    }

    public int getAlgorithm() {
      return getIntValue("algorithm", defaultAlgorithm);
    }

    public Object getPoints() {
      return getIntValue("points", defaultPoints);
    }

    public int getThreads() {
      return getIntValue("threads", defaultThreads);
    }

    public int getNumber() {
      return getIntValue("number", defaultNumber);
    }

    public boolean hasRuns() {
      return cl.hasOption("runs");
    }

    public boolean hasRadius() {
      return cl.hasOption("radius");
    }

    public boolean hasVelocity() {
      return cl.hasOption("velocity");
    }

    public int getRuns() {
      return getIntValue("runs", defaultRuns);
    }

    public int getRadius() {
      return getIntValue("radius", defaultRadius);
    }

    public int getVelocity() {
      return getIntValue("velocity", defaultVelocity);
    }

    public int getBoundingBox() {
      return getIntValue("size", defaultBoundingBox);
    }

    public boolean getNoStatistics() {
      if (cl.hasOption("no-statistics")) return true;
      return false;
    }

    public boolean getNoHeader() {
      if (cl.hasOption("no-header")) return true;
      return false;
    }

    public int getParameterNumber() {
      return cl.getOptions().length;
    }

    private int getIntValue(String option, int defaultval) {
      if (cl.hasOption(option)) return Integer.parseInt(cl.getOptionValue(option));
      else return defaultval;
    }
  }

}
