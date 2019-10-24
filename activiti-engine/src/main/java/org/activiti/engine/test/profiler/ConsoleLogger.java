package org.activiti.engine.test.profiler;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

 */
public class ConsoleLogger {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleLogger.class);
	protected ActivitiProfiler profiler;

    public ConsoleLogger(ActivitiProfiler profiler) {
        this.profiler = profiler;
    }

    public void log() {
        profiler.getProfileSessions().forEach(profileSession -> {

            System.out.println();
            logger.info("#############################################");
            logger.info("#############################################");
            logger.info(profileSession.getName());
            logger.info("#############################################");
            logger.info("#############################################");

            System.out.println();
            logger.info("Start time: " + profileSession.getStartTime());
            logger.info("End time: " + profileSession.getEndTime());
            logger.info(new StringBuilder().append("Total time: ").append(profileSession.getTotalTime()).append(" ms").toString());
            System.out.println();

            Map<String, CommandStats> allStats = profileSession.calculateSummaryStatistics();
            allStats.keySet().forEach(classFqn -> {
                CommandStats stats = allStats.get(classFqn);
                logger.info("Command class: " + classFqn);
                logger.info("Number of times invoked: " + stats.getCount());
                double commandPercentage = (double) stats.getTotalCommandTime / (double) profileSession.getTotalTime();
                logger.info((100.0 * Math.round(commandPercentage * 100.0) / 100.0) + "% of profile session was spent executing this command");

                System.out.println();
                logger.info(new StringBuilder().append("Average execution time: ").append(stats.getAverageExecutionTime()).append(" ms (Average database time: ").append(stats.getAverageDatabaseExecutionTime()).append(" ms (")
						.append(stats.getAverageDatabaseExecutionTimePercentage()).append("%) )").toString());

                System.out.println();
                logger.info("Database selects:");
                stats.getDbSelects().keySet().forEach(select -> logger.info(new StringBuilder().append(select).append(" : ").append(stats.getDbSelects().get(select)).toString()));

                System.out.println();
                logger.info("Database inserts:");
                stats.getDbInserts().keySet().forEach(insert -> logger.info(new StringBuilder().append(insert).append(" : ").append(stats.getDbInserts().get(insert)).toString()));

                System.out.println();
                logger.info("Database updates:");
                stats.getDbUpdates().keySet().forEach(update -> logger.info(new StringBuilder().append(update).append(" : ").append(stats.getDbSelects().get(update)).toString()));

                System.out.println();
                logger.info("Database delete:");
                stats.getDbDeletes().keySet().forEach(delete -> logger.info(new StringBuilder().append(delete).append(" : ").append(stats.getDbDeletes().get(delete)).toString()));


                System.out.println();
                System.out.println();
            });

        });
    }

}
