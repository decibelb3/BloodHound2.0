package com.bloodhound2.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates blood pressure and cholesterol values against common guidelines and produces plain-language
 * messages. Does not perform diagnosis; UI layers only display the returned text.
 */
public final class HealthAlertService {

    public HealthAlertResult analyze(
            Integer systolic,
            Integer diastolic,
            Double totalCholesterol,
            Double hdl,
            Double ldl) {

        List<String> concerns = new ArrayList<>();
        List<String> goodNews = new ArrayList<>();

        analyzeBloodPressure(systolic, diastolic, concerns, goodNews);
        analyzeCholesterol(totalCholesterol, hdl, ldl, concerns, goodNews);

        boolean hasConcerns = !concerns.isEmpty();
        boolean anyMetric =
                systolic != null
                        || diastolic != null
                        || totalCholesterol != null
                        || hdl != null
                        || ldl != null;

        List<String> lines = new ArrayList<>();
        lines.addAll(concerns);
        lines.addAll(goodNews);

        if (lines.isEmpty()) {
            if (!anyMetric) {
                return new HealthAlertResult(
                        false,
                        List.of(
                                "Your measurement was saved. Add blood pressure or cholesterol numbers next time for personalized feedback."));
            }
            return new HealthAlertResult(
                    false,
                    List.of("Nice work—based on what you entered, your numbers look good. Keep tracking your health."));
        }

        return new HealthAlertResult(hasConcerns, lines);
    }

    private static void analyzeBloodPressure(
            Integer systolic,
            Integer diastolic,
            List<String> concerns,
            List<String> goodNews) {
        if (systolic == null && diastolic == null) {
            return;
        }
        if (systolic == null || diastolic == null) {
            concerns.add(
                    "Enter both the top (systolic) and bottom (diastolic) blood pressure numbers for a full reading.");
            return;
        }

        int sys = systolic;
        int dia = diastolic;

        if (sys >= 180 || dia >= 120) {
            concerns.add(
                    "Your blood pressure is very high. If you have chest pain, shortness of breath, trouble speaking, or other serious symptoms, get help right away. Otherwise, contact your doctor soon.");
            return;
        }
        if (sys >= 140 || dia >= 90) {
            concerns.add(
                    "Your blood pressure suggests stage 2 high blood pressure. Please make an appointment to talk with your health care provider.");
            return;
        }
        if ((sys >= 130 && sys <= 139) || (dia >= 80 && dia <= 89)) {
            concerns.add(
                    "Your blood pressure falls in the stage 1 high blood pressure range. A conversation with your doctor can help you plan next steps.");
            return;
        }
        if (sys >= 120 && sys <= 129 && dia < 80) {
            concerns.add(
                    "Your blood pressure is a bit higher than ideal (elevated). Healthy habits may help bring it down over time.");
            return;
        }
        if (sys < 90 || dia < 60) {
            concerns.add(
                    "Your blood pressure appears lower than typical (<90/<60). If you feel dizzy, faint, weak, or unwell, contact your doctor promptly.");
            return;
        }
        if (sys < 120 && dia < 80) {
            goodNews.add("Your blood pressure is in a healthy range.");
        }
    }

    private static void analyzeCholesterol(
            Double totalCholesterol,
            Double hdl,
            Double ldl,
            List<String> concerns,
            List<String> goodNews) {
        boolean any = totalCholesterol != null || hdl != null || ldl != null;
        if (!any) {
            return;
        }

        boolean cholesterolConcern = false;

        if (totalCholesterol != null && totalCholesterol > 200) {
            concerns.add(
                    "Your total cholesterol is above a common goal. Your doctor can help you understand what it means for you.");
            cholesterolConcern = true;
        }
        if (ldl != null && ldl > 130) {
            concerns.add(
                    "Your LDL cholesterol is higher than many guidelines suggest. Discuss follow-up with your health care provider.");
            cholesterolConcern = true;
        }
        if (hdl != null && hdl < 40) {
            concerns.add(
                    "Your HDL cholesterol is lower than recommended. Small lifestyle changes sometimes help raise it—ask your doctor for ideas.");
            cholesterolConcern = true;
        }

        if (!cholesterolConcern) {
            goodNews.add("Your cholesterol numbers you entered are within common goals.");
        }
    }
}
