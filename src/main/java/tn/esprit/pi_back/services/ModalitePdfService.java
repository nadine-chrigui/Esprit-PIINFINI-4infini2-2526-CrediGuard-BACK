package tn.esprit.pi_back.services;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.evaluation.LigneAmortissementDTO;
import tn.esprit.pi_back.entities.DemandeCredit;
import tn.esprit.pi_back.entities.Modalite;
import tn.esprit.pi_back.exceptions.ResourceNotFoundException;
import tn.esprit.pi_back.repositories.ModaliteRepository;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModalitePdfService {

    private static final Color BLUE = new Color(11, 58, 117);
    private static final Color LIGHT_BLUE = new Color(219, 234, 254);
    private static final Color RED = new Color(190, 18, 60);
    private static final Color LIGHT_RED = new Color(255, 241, 242);
    private static final Color TEXT = new Color(15, 23, 42);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color WHITE = Color.WHITE;

    private final ModaliteRepository modaliteRepository;
    private final ModaliteAmortissementService amortissementService;

    public byte[] generatePdf(Long demandeId) {
        Modalite modalite = modaliteRepository.findByDemandeCreditId(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Modalite not found"));

        List<LigneAmortissementDTO> lignes = amortissementService.getTableau(demandeId);
        DemandeCredit demande = modalite.getDemandeCredit();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4.rotate(), 28, 28, 28, 28);
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, demande);
            addSummary(document, modalite, demande);
            addRiskBox(document, modalite);
            addAmortizationTable(document, lignes);
            addFooter(document);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate amortization PDF", e);
        }
    }

    private void addHeader(Document document, DemandeCredit demande) throws Exception {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{70, 30});
        header.setSpacingAfter(18);

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.setPadding(10);
        left.setBackgroundColor(WHITE);

        Paragraph brand = new Paragraph("CrediGuard", font(24, Font.BOLD, BLUE));
        Paragraph title = new Paragraph("Tableau d'amortissement reel", font(18, Font.BOLD, TEXT));
        Paragraph ref = new Paragraph("Reference demande : " + safe(demande.getReference()), font(10, Font.NORMAL, MUTED));

        left.addElement(brand);
        left.addElement(title);
        left.addElement(ref);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setPadding(10);
        right.setBackgroundColor(BLUE);
        right.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph label = new Paragraph("DOCUMENT CREDIT", font(12, Font.BOLD, WHITE));
        Paragraph status = new Paragraph("BLEU - ROUGE - BLANC", font(9, Font.NORMAL, LIGHT_BLUE));
        label.setAlignment(Element.ALIGN_CENTER);
        status.setAlignment(Element.ALIGN_CENTER);

        right.addElement(label);
        right.addElement(status);

        header.addCell(left);
        header.addCell(right);

        document.add(header);
    }

    private void addSummary(Document document, Modalite modalite, DemandeCredit demande) throws Exception {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{25, 25, 25, 25});
        table.setSpacingAfter(16);

        addMetric(table, "Montant credit", money(demande.getMontantDemande()), BLUE);
        addMetric(table, "Duree", demande.getDureeMois() + " mois", BLUE);
        addMetric(table, "Taux annuel fixe", number(modalite.getTauxInteretAnnuel()) + "%", RED);
        addMetric(table, "Modalite choisie", safeEnum(modalite.getModaliteChoisie(), modalite.getModaliteRecommandee()), RED);

        addMetric(table, "Mensualite amortissable", money(modalite.getMensualiteAmortissable()), BLUE);
        addMetric(table, "Mensualite in fine", money(modalite.getMensualiteInFine()), BLUE);
        addMetric(table, "Capacite max", money(modalite.getCapaciteMensuelleMax()), RED);
        addMetric(table, "Grace", Boolean.TRUE.equals(modalite.getGraceActive()) ? modalite.getDureeGraceMois() + " mois" : "Non", RED);

        document.add(table);
    }

    private void addMetric(PdfPTable table, String label, String value, Color accent) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(10);
        cell.setBorderColor(BORDER);
        cell.setBackgroundColor(WHITE);

        Paragraph p1 = new Paragraph(label, font(9, Font.BOLD, MUTED));
        Paragraph p2 = new Paragraph(value, font(13, Font.BOLD, accent));

        cell.addElement(p1);
        cell.addElement(p2);
        table.addCell(cell);
    }

    private void addRiskBox(Document document, Modalite modalite) throws Exception {
        PdfPTable box = new PdfPTable(2);
        box.setWidthPercentage(100);
        box.setWidths(new float[]{70, 30});
        box.setSpacingAfter(16);

        PdfPCell left = new PdfPCell();
        left.setPadding(12);
        left.setBorderColor(LIGHT_BLUE);
        left.setBackgroundColor(new Color(248, 251, 255));

        left.addElement(new Paragraph("Decision et justification", font(13, Font.BOLD, BLUE)));
        left.addElement(new Paragraph(safe(modalite.getMotif()), font(10, Font.NORMAL, TEXT)));

        PdfPCell right = new PdfPCell();
        right.setPadding(12);
        right.setBorderColor(new Color(254, 205, 211));
        right.setBackgroundColor(LIGHT_RED);

        right.addElement(new Paragraph("Risque ML", font(12, Font.BOLD, RED)));
        right.addElement(new Paragraph("PD : " + percent(modalite.getProbabiliteDefaut()), font(10, Font.BOLD, TEXT)));
        right.addElement(new Paragraph("VaR 95 : " + percent(modalite.getVar95()), font(10, Font.BOLD, TEXT)));
        right.addElement(new Paragraph("Score : " + number(modalite.getScoreCredit()), font(10, Font.BOLD, TEXT)));

        box.addCell(left);
        box.addCell(right);
        document.add(box);
    }

    private void addAmortizationTable(Document document, List<LigneAmortissementDTO> lignes) throws Exception {
        Paragraph title = new Paragraph("Calendrier detaille des echeances", font(15, Font.BOLD, BLUE));
        title.setSpacingAfter(8);
        document.add(title);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{8, 14, 18, 15, 15, 15, 15});

        addHeaderCell(table, "Mois");
        addHeaderCell(table, "Date");
        addHeaderCell(table, "Phase");
        addHeaderCell(table, "Mensualite");
        addHeaderCell(table, "Interet");
        addHeaderCell(table, "Capital");
        addHeaderCell(table, "Restant du");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (LigneAmortissementDTO ligne : lignes) {
            boolean grace = "GRACE".equalsIgnoreCase(ligne.phase());

            addBodyCell(table, "#" + ligne.numeroEcheance(), false, grace);
            addBodyCell(table, ligne.dateEcheance() != null ? ligne.dateEcheance().format(formatter) : "-", false, grace);
            addBodyCell(table, ligne.phase(), true, grace);
            addBodyCell(table, money(ligne.mensualite()), false, grace);
            addBodyCell(table, money(ligne.interet()), false, grace);
            addBodyCell(table, money(ligne.capitalRembourse()), false, grace);
            addBodyCell(table, money(ligne.capitalRestantDu()), true, grace);
        }

        document.add(table);
    }

    private void addHeaderCell(PdfPTable table, String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font(9, Font.BOLD, WHITE)));
        cell.setBackgroundColor(BLUE);
        cell.setBorderColor(BLUE);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String value, boolean strong, boolean grace) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font(8, strong ? Font.BOLD : Font.NORMAL, strong ? BLUE : TEXT)));
        cell.setPadding(7);
        cell.setBorderColor(BORDER);
        cell.setBackgroundColor(grace ? LIGHT_RED : WHITE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addFooter(Document document) throws Exception {
        Paragraph footer = new Paragraph(
                "Ce document est genere automatiquement par CrediGuard. Les montants sont arrondis a deux decimales.",
                font(8, Font.NORMAL, MUTED)
        );
        footer.setSpacingBefore(14);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private Font font(int size, int style, Color color) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA, size, style);
        f.setColor(color);
        return f;
    }

    private String money(Double value) {
        if (value == null) return "0.00 TND";
        return String.format("%.2f TND", value);
    }

    private String number(Double value) {
        if (value == null) return "0.00";
        return String.format("%.2f", value);
    }

    private String percent(Double value) {
        if (value == null) return "0.00%";
        return String.format("%.2f%%", value * 100);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String safeEnum(Object chosen, Object fallback) {
        if (chosen != null) return chosen.toString();
        if (fallback != null) return fallback.toString();
        return "-";
    }
}
