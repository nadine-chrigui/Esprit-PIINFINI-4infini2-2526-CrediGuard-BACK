package tn.esprit.pi_back.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.InsurancePolicy;
import tn.esprit.pi_back.repositories.InsurancePolicyRepository;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class InsurancePdfService {

    private final InsurancePolicyRepository policyRepository;

    public byte[] generatePolicyPdf(Long policyId) {
        InsurancePolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé avec l'ID: " + policyId));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(40, 50, 40, 50);

            // --- 1. BOXED TITLE (Matching user model) ---
            Table titleTable = new Table(1).useAllAvailableWidth();
            titleTable.addCell(new Cell().add(new Paragraph("CERTIFICAT D'ASSURANCE ET CONDITIONS PARTICULIÈRES")
                    .setBold().setTextAlignment(TextAlignment.CENTER).setFontSize(14))
                    .setPadding(10).setBorder(new com.itextpdf.layout.borders.SolidBorder(1)));
            document.add(titleTable);
            document.add(new Paragraph("\n"));

            // --- 2. SENDER & RECIPIENT BLOCKS ---
            Table addressTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
            
            // Sender (CrediGuard)
            addressTable.addCell(new Cell().add(new Paragraph("CrediGuard Assurances\nAvenue de la Liberté\n1002 Tunis, Tunisie")
                    .setFontSize(10).setMultipliedLeading(1.2f))
                    .setBorder(Border.NO_BORDER));
            
            // Recipient (Client)
            String clientName = policy.getClient() != null ? policy.getClient().getFullName() : "M./Mme Non spécifié";
            String clientEmail = policy.getClient() != null ? policy.getClient().getEmail() : "";
            addressTable.addCell(new Cell().add(new Paragraph("À l'attention de :\n" + clientName + "\n" + clientEmail)
                    .setFontSize(10).setMultipliedLeading(1.2f))
                    .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
            
            document.add(addressTable);
            document.add(new Paragraph("\n\n"));

            // --- 3. PLACE AND DATE ---
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale.FRENCH);
            document.add(new Paragraph("Tunis, le " + java.time.LocalDate.now().format(dateFormatter))
                    .setTextAlignment(TextAlignment.CENTER).setItalic().setFontSize(11));

            document.add(new Paragraph("\n"));

            // --- 4. FORMAL BODY TEXT ---
            document.add(new Paragraph("Objet : Attestation de couverture d'assurance").setBold().setUnderline().setFontSize(11));
            document.add(new Paragraph("\nMadame, Monsieur,\n\n").setFontSize(11));
            
            Paragraph body = new Paragraph();
            body.setFontSize(11).setMultipliedLeading(1.5f);
            body.add("Par la présente, nous vous confirmons que le contrat d'assurance n° ");
            body.add(new Text(policy.getPolicyNumber()).setBold());
            body.add(" a été valablement souscrit auprès de notre partenaire ");
            body.add(new Text(policy.getInsuranceCompany() != null ? policy.getInsuranceCompany().getName() : "Assureur Partenaire").setBold());
            body.add(".\n\nCe contrat, correspondant à l'offre '");
            body.add(new Text(policy.getInsuranceOffer() != null ? policy.getInsuranceOffer().getName() : "Standard").setBold());
            body.add("', est actif pour la période allant du ");
            body.add(new Text(policy.getStartDate() != null ? policy.getStartDate().format(dateFormatter) : "N/A").setBold());
            body.add(" au ");
            body.add(new Text(policy.getEndDate() != null ? policy.getEndDate().format(dateFormatter) : "N/A").setBold());
            body.add(".\n\nLe montant de la prime annuelle s'élève à ");
            body.add(new Text(policy.getPremiumAmount() + " TND").setBold());
            body.add(". Nous restons à votre entière disposition pour tout complément d'information et vous prions d'agréer, Madame, Monsieur, l'expression de nos salutations distinguées.");
            
            document.add(body);
            document.add(new Paragraph("\n\n"));

            // --- 5. VALIDATION & SIGNATURE ---
            Table footerTable = new Table(UnitValue.createPercentArray(new float[]{40, 60})).useAllAvailableWidth();

            // QR Code (Left) - Slightly larger
            String localIp = java.net.InetAddress.getLocalHost().getHostAddress();
            String verificationUrl = "http://" + localIp + ":8089/api/contrats/verify/" + policy.getPolicyNumber();
            byte[] qrCodeBytes = generateQRCode(verificationUrl, 110, 110);
            Image qrImg = new Image(ImageDataFactory.create(qrCodeBytes));
            footerTable.addCell(new Cell().add(new Paragraph("Vérification numérique :").setBold().setFontSize(8))
                    .add(qrImg).setBorder(Border.NO_BORDER));

            // Signature (Right) - Enhanced with certification text
            Cell sigCell = new Cell();
            sigCell.setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
            sigCell.add(new Paragraph("LA DIRECTION GÉNÉRALE").setBold().setFontSize(10).setMarginBottom(5));
            try {
                InputStream is = new ClassPathResource("static/images/signature.png").getInputStream();
                Image sigImg = new Image(ImageDataFactory.create(is.readAllBytes()));
                sigImg.setWidth(110);
                sigCell.add(sigImg.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT));
            } catch (Exception e) {}
            
            sigCell.add(new Paragraph("Signé numériquement le " + java.time.LocalDate.now().format(dateFormatter))
                    .setFontSize(8).setItalic());
            sigCell.add(new Paragraph("Document certifié conforme par CrediGuard Security")
                    .setFontSize(7).setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY));
            
            footerTable.addCell(sigCell);
            document.add(footerTable);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF formel", e);
        }

        return baos.toByteArray();
    }

    private Cell createLabelCell(String text) {
        return new Cell().add(new Paragraph(text).setBold().setFontSize(9))
                .setBorder(Border.NO_BORDER).setPadding(5);
    }

    private Cell createValueCell(String text) {
        return new Cell().add(new Paragraph(text).setFontSize(9))
                .setBorder(Border.NO_BORDER).setPadding(5);
    }

    private byte[] generateQRCode(String text, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }
}
