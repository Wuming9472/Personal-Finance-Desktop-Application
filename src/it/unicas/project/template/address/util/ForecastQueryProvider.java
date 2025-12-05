package it.unicas.project.template.address.util;

/**
 * Shared SQL snippets for forecast calculations to keep dashboard and report in sync.
 */
public final class ForecastQueryProvider {

    /**
     * Query to aggregate income and expenses for the current month up to a given date.
     */
    public static final String MONTHLY_FORECAST_AGGREGATE =
            "SELECT " +
                    "SUM(CASE WHEN LOWER(type) IN ('entrata', 'income') THEN amount ELSE 0 END) as totaleEntrate, " +
                    "SUM(CASE WHEN LOWER(type) IN ('uscita', 'expense') THEN amount ELSE 0 END) as totaleUscite, " +
                    "COUNT(DISTINCT DATE(date)) as giorniConMovimenti " +
                    "FROM movements " +
                    "WHERE user_id = ? " +
                    "AND date >= ? " +
                    "AND date <= ?";

    private ForecastQueryProvider() {
        // Utility class
    }
}
