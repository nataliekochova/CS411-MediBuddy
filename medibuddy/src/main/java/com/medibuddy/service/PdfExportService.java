package com.medibuddy.service;

import com.medibuddy.model.MedicationSchedule;
import com.medibuddy.model.SavedMedication;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class PdfExportService {

    private static final float PAGE_MARGIN = 50f;
    private static final float PAGE_WIDTH = PDRectangle.LETTER.getWidth() - (PAGE_MARGIN * 2);
    private static final float BODY_FONT_SIZE = 11f;
    private static final float TITLE_FONT_SIZE = 20f;
    private static final float SECTION_FONT_SIZE = 14f;
    private static final float LINE_HEIGHT = 15f;

    private static final PDFont TITLE_FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDFont SECTION_FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDFont LABEL_FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDFont BODY_FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private static final DateTimeFormatter EXPORT_TIMESTAMP =
            DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a", Locale.US);

    public void exportUserMedicationReport(String username,
                                           List<SavedMedication> medications,
                                           Path outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PdfWriter writer = new PdfWriter(document);

            writer.writeHeading("MediBuddy Medication Report", TITLE_FONT, TITLE_FONT_SIZE, 24f);
            writer.writeKeyValue("Account", safeText(username));
            writer.writeKeyValue("Exported", EXPORT_TIMESTAMP.format(LocalDateTime.now()));
            writer.writeBlankLine(6f);

            if (medications.isEmpty()) {
                writer.writeSectionTitle("Medications", SECTION_FONT, SECTION_FONT_SIZE);
                writer.writeParagraph("No medications are currently saved for this account.");
            } else {
                writer.writeSectionTitle("Medications", SECTION_FONT, SECTION_FONT_SIZE);
                int index = 1;
                for (SavedMedication medication : medications) {
                    writer.writeMedication(index++, medication);
                }
            }

            writer.close();
            document.save(outputPath.toFile());
        }
    }

    private String safeText(String text) {
        if (text == null || text.isBlank()) {
            return "N/A";
        }
        return text.trim()
                .replace('\u2018', '\'')
                .replace('\u2019', '\'')
                .replace('\u201C', '"')
                .replace('\u201D', '"')
                .replace('\u2013', '-')
                .replace('\u2014', '-')
                .replace('\u2022', '*')
                .replace('\u00A0', ' ');
    }

    private final class PdfWriter {
        private final PDDocument document;
        private PDPage page;
        private PDPageContentStream stream;
        private float cursorY;

        private PdfWriter(PDDocument document) throws IOException {
            this.document = document;
            startNewPage();
        }

        private void writeMedication(int index, SavedMedication medication) throws IOException {
            writeSectionTitle(index + ". " + medication.getDisplayName(), SECTION_FONT, SECTION_FONT_SIZE);
            writeKeyValue("Dose / Form", medication.getDoseAndFormDisplay());
            writeKeyValue("Brand", medication.getBrandName());
            writeKeyValue("Generic", medication.getGenericName());
            writeKeyValue("Manufacturer", medication.getManufacturer());
            writeKeyValue("Start Date", medication.getStartDate() == null ? "N/A" : medication.getStartDate().toString());
            writeKeyValue("End Date", medication.getEndDate() == null ? "N/A" : medication.getEndDate().toString());
            writeKeyValue("Schedule", formatSchedules(medication.getSchedules()));
            writeKeyValue("Purpose", medication.getPurpose());
            writeKeyValue("Indications", medication.getIndications());
            writeKeyValue("Warnings", medication.getWarnings());
            writeKeyValue("Label Dosage Guidance", medication.getLabelDosage());
            writeBlankLine(8f);
        }

        private String formatSchedules(List<MedicationSchedule> schedules) {
            if (schedules == null || schedules.isEmpty()) {
                return "No schedules saved";
            }

            List<MedicationSchedule> sorted = new ArrayList<>(schedules);
            sorted.sort(Comparator
                    .comparingInt((MedicationSchedule schedule) -> dayOrder(schedule.getDay()))
                    .thenComparing(MedicationSchedule::getTime, String.CASE_INSENSITIVE_ORDER));

            List<String> parts = new ArrayList<>();
            for (MedicationSchedule schedule : sorted) {
                parts.add(safeText(schedule.getDay()) + " at " + safeText(schedule.getTime()));
            }
            return String.join(", ", parts);
        }

        private int dayOrder(String day) {
            return switch (safeText(day).toLowerCase(Locale.US)) {
                case "mon" -> 1;
                case "tue" -> 2;
                case "wed" -> 3;
                case "thu" -> 4;
                case "fri" -> 5;
                case "sat" -> 6;
                case "sun" -> 7;
                default -> 8;
            };
        }

        private void writeHeading(String text, PDFont font, float fontSize, float spacingAfter) throws IOException {
            writeTextLine(text, font, fontSize);
            cursorY -= spacingAfter;
        }

        private void writeSectionTitle(String text, PDFont font, float fontSize) throws IOException {
            ensureSpace((LINE_HEIGHT * 2) + 8f);
            writeTextLine(text, font, fontSize);
            cursorY -= 8f;
        }

        private void writeKeyValue(String label, String value) throws IOException {
            writeWrappedParagraph(label + ": " + safeText(value), LABEL_FONT, BODY_FONT_SIZE, BODY_FONT, BODY_FONT_SIZE);
        }

        private void writeParagraph(String text) throws IOException {
            writeWrappedParagraph(safeText(text), BODY_FONT, BODY_FONT_SIZE, BODY_FONT, BODY_FONT_SIZE);
        }

        private void writeWrappedParagraph(String text,
                                           PDFont firstLineFont,
                                           float firstLineFontSize,
                                           PDFont bodyFont,
                                           float bodyFontSize) throws IOException {
            List<String> lines = wrapText(text, bodyFont, bodyFontSize, PAGE_WIDTH);
            if (lines.isEmpty()) {
                lines = List.of("N/A");
            }

            ensureSpace(lines.size() * LINE_HEIGHT + 4f);
            for (int i = 0; i < lines.size(); i++) {
                PDFont font = i == 0 ? firstLineFont : bodyFont;
                float size = i == 0 ? firstLineFontSize : bodyFontSize;
                writeTextLine(lines.get(i), font, size);
            }
            cursorY -= 4f;
        }

        private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
            String normalized = text.replace("\r", "");
            List<String> lines = new ArrayList<>();

            for (String paragraph : normalized.split("\n")) {
                if (paragraph.isBlank()) {
                    lines.add("");
                    continue;
                }

                StringBuilder currentLine = new StringBuilder();
                for (String word : paragraph.trim().split("\\s+")) {
                    String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
                    if (textWidth(candidate, font, fontSize) <= maxWidth) {
                        currentLine.setLength(0);
                        currentLine.append(candidate);
                    } else {
                        if (!currentLine.isEmpty()) {
                            lines.add(currentLine.toString());
                        }
                        currentLine.setLength(0);
                        currentLine.append(word);
                    }
                }

                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                }
            }

            return lines;
        }

        private float textWidth(String text, PDFont font, float fontSize) throws IOException {
            return font.getStringWidth(text) / 1000f * fontSize;
        }

        private void writeTextLine(String text, PDFont font, float fontSize) throws IOException {
            ensureSpace(LINE_HEIGHT);
            stream.beginText();
            stream.setFont(font, fontSize);
            stream.newLineAtOffset(PAGE_MARGIN, cursorY);
            stream.showText(text);
            stream.endText();
            cursorY -= LINE_HEIGHT;
        }

        private void writeBlankLine(float amount) {
            cursorY -= amount;
        }

        private void ensureSpace(float neededHeight) throws IOException {
            if (cursorY - neededHeight <= PAGE_MARGIN) {
                startNewPage();
            }
        }

        private void startNewPage() throws IOException {
            if (stream != null) {
                stream.close();
            }

            page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            cursorY = page.getMediaBox().getHeight() - PAGE_MARGIN;
        }

        private void close() throws IOException {
            if (stream != null) {
                stream.close();
                stream = null;
            }
        }
    }
}
