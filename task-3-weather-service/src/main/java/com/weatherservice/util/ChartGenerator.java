package com.weatherservice.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.ByteArrayOutputStream;
import java.util.List;

// Генератор графиков
public class ChartGenerator {
    // Метод для генерации графика температуры
    public static byte[] generateTemperatureChart(List<String> times, List<Double> temps, String city) {
        try {
            if (times == null || temps == null || times.isEmpty() || temps.isEmpty())
                return generateErrorChart("Нет данных");

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int i = 0; i < Math.min(24, times.size()) && i < temps.size(); i++) {
                if (temps.get(i) != null)
                    dataset.addValue(temps.get(i), "Temperature", formatTime(times.get(i)));
            }

            JFreeChart chart = ChartFactory.createLineChart(
                    "24-часовой прогноз температуры для " + city,
                    "Время", "Температура (°C)", dataset);

            chart.getCategoryPlot()
                    .getDomainAxis()
                    .setCategoryLabelPositions(
                            org.jfree.chart.axis.CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4)
                    );

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(os, chart, 800, 600);
            return os.toByteArray();
        } catch (Exception e) {
            return generateErrorChart("Ошибка построения графика");
        }
    }

    // Метод генерации графика с ошибкой
    private static byte[] generateErrorChart(String msg) {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(0, "Ошибка", "Нет данных");
            JFreeChart chart = ChartFactory.createLineChart(msg, "Время", "Температура", dataset);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(os, chart, 800, 600);
            return os.toByteArray();
        } catch (Exception e) { return new byte[0]; }
    }

    private static String formatTime(String t) {
        return t.contains("T") ? t.split("T")[1].substring(0,5) : t;
    }
}
