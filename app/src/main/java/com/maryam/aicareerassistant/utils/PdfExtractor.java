package com.maryam.aicareerassistant.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.InputStream;

/**
 * Extracts plain text from a PDF file identified by a content:// Uri.
 * Uses PDFBox-Android — runs on a background thread (see ResumeViewModel).
 */
public class PdfExtractor {

    public interface ExtractionCallback {
        void onSuccess(String extractedText);
        void onFailure(String errorMessage);
    }

    /**
     * Must be called ONCE at app startup (already done in AICareerAssistantApp).
     */
    public static void init(Context context) {
        PDFBoxResourceLoader.init(context);
    }

    public static void extractText(ContentResolver resolver, Uri pdfUri, ExtractionCallback callback) {
        try (InputStream inputStream = resolver.openInputStream(pdfUri);
             PDDocument document = PDDocument.load(inputStream)) {

            if (document.isEncrypted()) {
                callback.onFailure("This PDF is password-protected. Please upload an unprotected resume.");
                return;
            }

            PDFTextStripper stripper = new PDFTextStripper();
            String rawText = stripper.getText(document);
            String cleanedText = cleanText(rawText);

            if (cleanedText.trim().isEmpty()) {
                callback.onFailure("Could not extract text from this PDF. It may be a scanned image. Please upload a text-based PDF.");
                return;
            }

            callback.onSuccess(cleanedText);

        } catch (Exception e) {
            callback.onFailure("Failed to read PDF: " + e.getMessage());
        }
    }

    /**
     * Removes excessive whitespace/newlines while keeping structure readable.
     */
    private static String cleanText(String raw) {
        if (raw == null) return "";
        return raw
                .replaceAll("\\r\\n|\\r", "\n")       // normalize line endings
                .replaceAll("[ \\t]+", " ")             // collapse multiple spaces/tabs
                .replaceAll("\\n{3,}", "\n\n")          // max 2 consecutive blank lines
                .trim();
    }
}